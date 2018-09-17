/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
 *
 * The Roleplay Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Roleplay Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Roleplay Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.net;

import android.support.annotation.VisibleForTesting;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A scheduler for messages to a single recipient.
 */
public class MessageScheduler {

  private final CompanionContext context;
  private final String recipientId;
  private final Multimap<CompanionMessageData.Type, ScheduledMessage>
      waiting = LinkedHashMultimap.create();
  private final Map<Long, ScheduledMessage> pendingByMessageId = new HashMap<>();
  private final Multimap<CompanionMessageData.Type, ScheduledMessage>
      sentByType = LinkedHashMultimap.create();
  private final Multimap<CompanionMessageData.Type, ScheduledMessage>
      acknowledgedByType = LinkedHashMultimap.create();

  public MessageScheduler(CompanionContext context, String recipientId) {
    this.context = context;
    this.recipientId = recipientId;

    List<ScheduledMessage> toRemove = new ArrayList<>();
    for (ScheduledMessage message : context.messages().getMessagesByReceiver(recipientId)) {

      if (message.isOutdated()) {
        toRemove.add(message);
      } else {
        switch (message.getState()) {
          case WAITING:
            waiting.put(message.getData().getType(), message);

          case PENDING:
            pendingByMessageId.put(message.getMessageId(), message);
            break;

          case SENT:
            toRemove.add(message);
            sentByType.put(message.getData().getType(), message);
            break;

          case ACKED:
            toRemove.add(message);
            acknowledgedByType.put(message.getData().getType(), message);
            break;
        }
      }
    }

    // Removed acked messages that are still stored (should normally not happen).
    for (ScheduledMessage message : toRemove) {
      context.messages().remove(message);
    }
  }

  public String getRecipientId() {
    return recipientId;
  }

  public boolean hasWaiting(List<String> serverIds, List<String> clientIds) {
    for (ScheduledMessage message : waiting.values()) {
      if (serverIds.contains(message.getSenderId())
          && clientIds.contains(message.getRecieverId())) {
        return true;
      }
    }

    return false;
  }

  public void schedule(CompanionMessageData data) {
    ScheduledMessage message = new ScheduledMessage(data.campaigns(),
        new CompanionMessage(context.me().get().getId(), context.me().get().getNickname(),
            recipientId, 0 /*context.settings().getNextMessageId()*/, data));
    Status.log("scheduling to " + Status.nameFor(recipientId) + ": " + message);
    message.store();

    if (message.mayOverwrite()) {
      for (Iterator<ScheduledMessage> i = waiting.get(data.getType()).iterator(); i.hasNext(); ) {
        CompanionMessageData oldMessage = i.next().getData();
        if (overwrites(oldMessage, message.getData())) {
          i.remove();
        }
      }
    }

    waiting.put(data.getType(), message);
  }

  private boolean overwrites(CompanionMessageData oldMessage, CompanionMessageData newMessage) {
    if (!oldMessage.mayOverwrite() || !newMessage.mayOverwrite()
        || oldMessage.getType() != newMessage.getType()) {
      return false;
    }

    switch(oldMessage.getType()) {
      case CAMPAIGN_DELETE:
        return oldMessage.getCampaignDelete().equals(newMessage.getCampaignDelete());

      case CHARACTER:
        return oldMessage.getCharacter().getCharacterId().equals(
            newMessage.getCharacter().getCharacterId());

      case CAMPAIGN:
        return oldMessage.getCampaign().getCampaignId().equals(
            newMessage.getCampaign().getCampaignId());

      case IMAGE:
        return oldMessage.getImage().getId().equals(newMessage.getImage().getId())
            && oldMessage.getImage().getType().equals(newMessage.getImage().getType());

      default:
        return false;
    }
  }

  public Optional<ScheduledMessage> nextWaiting() {
    // Resend messages that are in pending state for too long.
    for (Iterator<ScheduledMessage> i = pendingByMessageId.values().iterator(); i.hasNext(); ) {
      ScheduledMessage message = i.next();
      if (message.isLate()) {
        markWaiting(message);
        i.remove();
      }
    }

    for (Iterator<ScheduledMessage> i = waiting.values().iterator(); i.hasNext(); ) {
      ScheduledMessage message = i.next();

      i.remove();
      if (message.requiresAck()) {
        markPending(message);
      } else {
        markSent(message);
      }

      return Optional.of(message);
    }

    return Optional.empty();
  }

  public void ack(long messageId) {
    ScheduledMessage message = pendingByMessageId.get(messageId);
    if (message != null) {
      pendingByMessageId.remove(messageId);
      markAcked(message);
    } else {
      Status.log("Ignored ack for unknown message id " + messageId + " for "
          + Status.nameFor(recipientId));
    }
  }

  public void revoke(String id) {
    revoke(waiting.values(), id);
    revoke(pendingByMessageId.values(), id);
  }

  private void revoke(Iterable<ScheduledMessage> messages, String id) {
    for (Iterator<ScheduledMessage> i = messages.iterator(); i.hasNext(); ) {
      if (isRevoked(i.next(), id)) {
        i.remove();
      }
    }
  }

  private boolean isRevoked(ScheduledMessage message, String id) {
    switch(message.getData().getType()) {
      case UNKNOWN:
      case DEBUG:
      case WELCOME:
      case ACK:
      case CAMPAIGN_DELETE:
      case CHARACTER_DELETE:
      case XP_AWARD:
      default:
        return false;

      case CAMPAIGN:
        return message.getData().getCampaign().getCampaignId().equals(id);

      case CHARACTER:
        return message.getData().getCharacter().getCharacterId().equals(id);

      case IMAGE:
        return message.getData().getImage().getId().equals(id);
    }
  }

  private void markWaiting(ScheduledMessage message) {
    message.markWaiting();
    waiting.put(message.getData().getType(), message);
  }

  private void markAcked(ScheduledMessage message) {
    message.markAck();
    context.messages().remove(message);
    acknowledgedByType.put(message.getData().getType(), message);
  }

  private void markSent(ScheduledMessage message) {
    message.markSent();
    context.messages().remove(message);
    sentByType.put(message.getData().getType(), message);
  }

  private void markPending(ScheduledMessage message) {
    message.markPending();
    pendingByMessageId.put(message.getMessageId(), message);
  }

  // Testing.
  @VisibleForTesting
  public Collection<ScheduledMessage> getWaiting() {
    return waiting.values();
  }

  @VisibleForTesting
  public Collection<ScheduledMessage> getSent(CompanionMessageData.Type type) {
    return sentByType.get(type);
  }

  @VisibleForTesting
  public Collection<ScheduledMessage> getPending() {
    return pendingByMessageId.values();
  }

  @VisibleForTesting
  public Collection<ScheduledMessage> getAcked() {
    return acknowledgedByType.values();
  }
}
