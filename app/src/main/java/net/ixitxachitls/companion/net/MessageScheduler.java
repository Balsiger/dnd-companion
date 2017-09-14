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

import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.proto.Data;

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
  private final Multimap<Data.CompanionMessageProto.PayloadCase, ScheduledMessage> waiting =
      LinkedHashMultimap.create();
  private final Map<Long, ScheduledMessage> pending = new HashMap<>();
  private final Multimap<Data.CompanionMessageProto.PayloadCase, ScheduledMessage> sent =
      LinkedHashMultimap.create();
  private final Multimap<Data.CompanionMessageProto.PayloadCase, ScheduledMessage> acknowledged =
      LinkedHashMultimap.create();

  public MessageScheduler(String recipientId) {
    this.recipientId = recipientId;

    List<ScheduledMessage> toRemove = new ArrayList<>();
    for (ScheduledMessage message
        : ScheduledMessages.get().getMessagesByReceiver(recipientId)) {
      switch (message.getState()) {
        case WAITING:
          waiting.put(message.getType(), message);

        case PENDING:
          pending.put(message.getMessageId(), message);
          break;

        case SENT:
          toRemove.add(message);
          sent.put(message.getType(), message);
          break;

        case ACKED:
          toRemove.add(message);
          acknowledged.put(message.getType(), message);
          break;
      }
    }

    // Removed acked messages that are still stored (should normally not happen).
    for (ScheduledMessage message : toRemove) {
      ScheduledMessages.get().remove(message);
    }
  }

  public void schedule(Data.CompanionMessageProto proto) {
    // TODO: handle proto messages differently and split id, sender, receiver from
    // actual payload to not have to change the proto here.
    ScheduledMessage message =
        new ScheduledMessage(proto.toBuilder().setReceiver(recipientId).build());
    message.store();

    if (message.mayOverwrite()) {
      waiting.removeAll(proto.getPayloadCase());
    }

    waiting.put(proto.getPayloadCase(), message);
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
      if (pending.containsKey(message.getType())) {
        continue;
      }

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
      pending.remove(message);
      markAcked(message);
    }
  }

  public boolean isIdle() {
    return waiting.isEmpty() && pending.isEmpty();
  }

  private void markWaiting(ScheduledMessage message) {
    message.markWaiting();
    waiting.put(message.getType(), message);
  }

  private void markAcked(ScheduledMessage message) {
    message.markAck();
    ScheduledMessages.get().remove(message);
    acknowledged.put(message.getType(), message);
  }

  private void markSent(ScheduledMessage message) {
    message.markSent();
    ScheduledMessages.get().remove(message);
    sent.put(message.getType(), message);
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
      if (message.toProto().getMessage().getSender().equals(id)) {
        list.add(message.toSenderString());
      }
    }
  }
}
