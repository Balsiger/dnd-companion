/*
 * Copyright (c) 2017-2018 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Tabletop Companion.
 *
 * The Tabletop Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Tabletop Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion;

import android.support.annotation.CallSuper;

import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.net.CompanionMessageData;
import net.ixitxachitls.companion.net.CompanionMessenger;
import net.ixitxachitls.companion.net.MessageScheduler;
import net.ixitxachitls.companion.net.NetworkClient;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.util.Misc;

import org.robolectric.shadows.ShadowLooper;

import java.util.Collection;

import javax.annotation.Nullable;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Base class for the companion tests.
 */
public abstract class CompanionTest {

  @CallSuper
  public void setUp() {
    Misc.IN_UNIT_TEST = true;
  }

  protected Character character(CompanionContext context, String id) {
    return context.characters().getCharacter(id).getValue().get();
  }

  protected void assertClientSent(CompanionMessenger messenger, String senderId,
                                  Entry.CompanionMessageProto.Payload.PayloadCase type,
                                  @Nullable String id, String ... recipientIds) {
    for (String recipientId : recipientIds) {
      assertMessage(messenger.getClients().getSchedulersByServerId().get(recipientId)
          .getSent(CompanionMessageData.getType(type)), senderId, recipientId, 0, type, id);
    }
  }

  protected void assertServerSent(CompanionMessenger messenger, String senderId,
                                  Entry.CompanionMessageProto.Payload.PayloadCase type,
                                  @Nullable String id,
                                  String ... recipientIds) {
    for (String recipientId : recipientIds) {
      assertMessage(messenger.getServer().getSchedulersByRecpientId().get(recipientId)
          .getSent(CompanionMessageData.getType(type)), senderId, recipientId, 0, type, id);
    }
  }

  protected void assertClientScheduled(CompanionMessenger messenger, String senderId,
                                       Entry.CompanionMessageProto.Payload.PayloadCase type,
                                       @Nullable String id, String ... recipientIds) {
    assertThat(messenger.getClients().getSchedulersByServerId().keySet(),
        containsInAnyOrder(recipientIds));
    for (String recipientId : recipientIds) {
      assertLastMessage(messenger.getClients().getSchedulersByServerId().get(recipientId)
          .getWaiting(), senderId, recipientId, 0, type, id);
    }
  }

  protected void assertServerScheduled(CompanionMessenger messenger, String senderId,
                                       Entry.CompanionMessageProto.Payload.PayloadCase type,
                                       @Nullable String id,
                                       String ... recipientIds) {
    assertThat(messenger.getServer().getSchedulersByRecpientId().keySet(),
        containsInAnyOrder(recipientIds));
    for (String recipientId : recipientIds) {
      assertLastMessage(messenger.getServer().getSchedulersByRecpientId().get(recipientId)
          .getWaiting(), senderId, recipientId, 0, type, id);
    }
  }

  protected void assertMessage(Collection<ScheduledMessage> messages, @Nullable String senderId,
                               @Nullable String receiverId, long messageId,
                               Entry.CompanionMessageProto.Payload.PayloadCase type,
                               @Nullable String id) {
    for (ScheduledMessage message : messages) {
      if (matches(message, senderId, receiverId, messageId, type, id)) {
        return;
      }
    }

    fail("Could not find message that matches in " + messages);
  }

  protected boolean matches(ScheduledMessage message, @Nullable String senderId,
                            @Nullable String receiverId, long messageId,
                            Entry.CompanionMessageProto.Payload.PayloadCase type,
                            @Nullable String id) {
    if (senderId != null) {
      if (!senderId.equals(message.getMessage().getSenderId())) {
        return false;
      }
    }
    if (receiverId != null) {
      if (!receiverId.equals(message.getMessage().getRecieverId())) {
        return false;
      }
    }
    if (messageId > 0) {
      if (messageId != message.getMessageId()) {
        return false;
      }
    }
    if (type != message.getData().toProto().getPayloadCase()) {
      return false;
    }
    if (id != null) {
      if (!id.equals(extractId(message, type))) {
        return false;
      }
    }

    return true;
  }

  protected void assertLastMessage(Collection<ScheduledMessage> messages, @Nullable String senderId,
                                   @Nullable String receiverId, long messageId,
                                   Entry.CompanionMessageProto.Payload.PayloadCase type,
                                   @Nullable String id) {
    assertFalse(messages.isEmpty());
    assertMessage(messages.stream().skip(messages.size() - 1).findFirst().get(),
        senderId, receiverId, messageId, type, id);
  }

  protected void assertMessage(ScheduledMessage message, @Nullable String senderId,
                               @Nullable String receiverId, long messageId,
                               Entry.CompanionMessageProto.Payload.PayloadCase type,
                               @Nullable String id) {
    if (senderId != null) {
      assertEquals(senderId, message.getMessage().getSenderId());
    }
    if (receiverId != null) {
      assertEquals(receiverId, message.getMessage().getRecieverId());
    }
    if (messageId > 0) {
      assertEquals(messageId, message.getMessageId());
    }
    assertEquals(type, message.getData().toProto().getPayloadCase());
    if (id != null) {
      assertEquals(id, extractId(message, type));
    }
  }

  protected void processAllMessages(CompanionMessenger ... messengers) throws InterruptedException {
    Thread.sleep(100);
    do {
      System.out.println("processing all messages...");
      ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
    } while (pending(messengers));
  }

  protected void assertFinished(CompanionMessenger ... messengers) {
    for (CompanionMessenger messenger : messengers) {
      assertFinished(messenger);
    }
  }

  protected void assertFinished(CompanionMessenger messenger) {
    assertFinished(messenger.getServer().getSchedulersByRecpientId().values());
  }

  protected void assertFinished(Collection<MessageScheduler> schedulers) {
    for (MessageScheduler scheduler : schedulers) {
      assertFinished(scheduler);
    }
  }

  protected void assertFinished(MessageScheduler scheduler) {
    assertTrue(scheduler.getWaiting().isEmpty());
    assertTrue(scheduler.getPending().isEmpty());
  }

  private boolean pending(CompanionMessenger ... messengers) {
    for (CompanionMessenger messenger : messengers) {
      // Check that all pending messages are processed.
      if (messenger.getServer().getNsdServer().getServer().hasPendingMessage()) {
        return true;
      }

      for (NetworkClient client
          : messenger.getClients().getClientsByServerId().values()) {
        if (client.getTransmitter().hasPendingMessage()) {
          return true;
        }
      }

      for (MessageScheduler scheduler : messenger.getServer().getSchedulersByRecpientId().values()) {
        if (!scheduler.getWaiting().isEmpty()) {
          return true;
        }
      }

      for (MessageScheduler scheduler : messenger.getClients().getSchedulersByServerId().values()) {
        if (!scheduler.getWaiting().isEmpty()) {
          return true;
        }
      }
    }

    return false;
  }

  private String extractId(ScheduledMessage message,
                           Entry.CompanionMessageProto.Payload.PayloadCase type) {
    switch (type) {
      case CAMPAIGN:
        return message.getData().toProto().getCampaign().getId();

      case CHARACTER:
        return message.getData().toProto().getCharacter().getCreature().getId();

      case IMAGE:
        return message.getData().toProto().getImage().getId();

      case CONDITION:
        return message.getData().toProto().getCondition().getTargetId();

      case CAMPAIGN_DELETE:
        return message.getData().toProto().getCampaignDelete();

      case CHARACTER_DELETE:
        return message.getData().toProto().getCharacterDelete();

      case CONDITION_DELETE:
        return message.getData().toProto().getConditionDelete().getTargetId();

      case XP_AWARD:
        return message.getData().toProto().getXpAward().getCharacterId();

      case ACK:
        return String.valueOf(message.getData().toProto().getAck());

      case WELCOME:
        return String.valueOf(message.getData().toProto().getWelcome().getName());

      default:
        return "";
    }
  }
}
