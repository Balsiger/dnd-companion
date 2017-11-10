/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Player Companion.
 *
 * The Player Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Player Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.net;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A scheduler for messages to a single recipient.
 */
public class MessageScheduler {

  private final String recipientId;
  private final Multimap<CompanionMessageData.Type, ScheduledMessage>
      waiting = LinkedHashMultimap.create();
  private final Map<Long, ScheduledMessage> pending = new HashMap<>();
  private final Multimap<CompanionMessageData.Type, ScheduledMessage>
      sent = LinkedHashMultimap.create();
  private final Multimap<CompanionMessageData.Type, ScheduledMessage>
      acknowledged = LinkedHashMultimap.create();

  public MessageScheduler(String recipientId) {
    this.recipientId = recipientId;

    List<ScheduledMessage> toRemove = new ArrayList<>();
    for (ScheduledMessage message
        : ScheduledMessages.get().getMessagesByReceiver(recipientId)) {
      switch (message.getState()) {
        case WAITING:
          waiting.put(message.getData().getType(), message);

        case PENDING:
          pending.put(message.getMessageId(), message);
          break;

        case SENT:
          toRemove.add(message);
          sent.put(message.getData().getType(), message);
          break;

        case ACKED:
          toRemove.add(message);
          acknowledged.put(message.getData().getType(), message);
          break;
      }
    }

    // Removed acked messages that are still stored (should normally not happen).
    for (ScheduledMessage message : toRemove) {
      ScheduledMessages.get().remove(message);
    }
  }

  public void schedule(CompanionMessageData data) {
    ScheduledMessage message = new ScheduledMessage(
        new CompanionMessage(Settings.get().getAppId(), Settings.get().getNickname(),
            recipientId, data.requiresAck() ? Settings.get().getNextMessageId() : 0,
            data));
    message.store();

    if (message.mayOverwrite()) {
      waiting.removeAll(data.getType());
    }

    waiting.put(data.getType(), message);
  }

  public Optional<ScheduledMessage> nextWaiting() {
    // Resend messages that are in pending state for too long.
    for (Iterator<ScheduledMessage> i = pending.values().iterator(); i.hasNext(); ) {
      ScheduledMessage message = i.next();
      if (message.isLate()) {
        markWaiting(message);
        i.remove();
      }
    }

    for (Iterator<ScheduledMessage> i = waiting.values().iterator(); i.hasNext(); ) {
      ScheduledMessage message = i.next();

      // Ignore messages that are already pending.
      if (pending.containsKey(message.getData().getType())) {
        continue;
      }

      // this does not seem to work as expected?
      i.remove();
      if (message.requiresAck()) {
        markPending(message);
      } else {
        markSent(message);
      }

      return Optional.of(message);
    }

    return Optional.absent();
  }

  public void ack(long messageId) {
    ScheduledMessage message = pending.get(messageId);
    if (message != null) {
      pending.remove(messageId);
      markAcked(message);
    }
  }

  public boolean isIdle() {
    return waiting.isEmpty() && pending.isEmpty();
  }

  private void markWaiting(ScheduledMessage message) {
    message.markWaiting();
    waiting.put(message.getData().getType(), message);
  }

  private void markAcked(ScheduledMessage message) {
    message.markAck();
    ScheduledMessages.get().remove(message);
    acknowledged.put(message.getData().getType(), message);
  }

  private void markSent(ScheduledMessage message) {
    message.markSent();
    ScheduledMessages.get().remove(message);
    sent.put(message.getData().getType(), message);
  }

  private void markPending(ScheduledMessage message) {
    message.markPending();
    pending.put(message.getMessageId(), message);
  }

  public List<String> scheduledMessages(String id) {
    List<String> list = new ArrayList<>();

    addMessages(list, waiting.values(), id);
    addMessages(list, pending.values(), id);
    addMessages(list, sent.values(), id);
    addMessages(list, acknowledged.values(), id);

    return list;
  }

  private void addMessages(List<String> list, Collection<ScheduledMessage> messages,
                           String id) {
    for (ScheduledMessage message : ImmutableList.copyOf(messages).reverse()) {
      if (message.getSenderId().equals(id)) {
        list.add(message.toSenderString());
      }
    }
  }
}
