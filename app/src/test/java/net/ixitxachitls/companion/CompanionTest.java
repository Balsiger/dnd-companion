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

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.support.annotation.CallSuper;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.FakeCompanionContext;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.net.CompanionMessageData;
import net.ixitxachitls.companion.net.CompanionMessenger;
import net.ixitxachitls.companion.net.MessageProcessor;
import net.ixitxachitls.companion.net.MessageScheduler;
import net.ixitxachitls.companion.net.NetworkClient;
import net.ixitxachitls.companion.net.nsd.FakeNsdAccessor;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.storage.FakeAssetAccessor;
import net.ixitxachitls.companion.storage.FakeClient1DataBaseAccessor;
import net.ixitxachitls.companion.storage.FakeClient2DataBaseAccessor;
import net.ixitxachitls.companion.storage.FakeDataBaseAccessor;
import net.ixitxachitls.companion.storage.FakeServerDataBaseAccessor;
import net.ixitxachitls.companion.util.Misc;

import org.junit.Rule;
import org.robolectric.shadows.ShadowLooper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Base class for the companion tests.
 */
public abstract class CompanionTest {

  // Make .setValue calls synchronous and allow them to be called in tests.
  @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

  protected FakeNsdAccessor nsdAccessor;
  protected FakeAssetAccessor assetAccessor;

  protected FakeDataBaseAccessor serverDataBase;
  protected FakeCompanionContext serverContext;

  protected FakeDataBaseAccessor client1DataBase;
  protected FakeCompanionContext client1Context;

  protected FakeDataBaseAccessor client2DataBase;
  protected FakeCompanionContext client2Context;

  protected CompanionMessenger server;
  protected CompanionMessenger client1;
  protected CompanionMessenger client2;

  @CallSuper
  public void setUp() {
    Misc.IN_UNIT_TEST = true;

    nsdAccessor =  new FakeNsdAccessor();
    assetAccessor = new FakeAssetAccessor();
    serverDataBase = new FakeServerDataBaseAccessor();
    client1DataBase = new FakeClient1DataBaseAccessor();
    client2DataBase = new FakeClient2DataBaseAccessor();

    Entries.init(assetAccessor);
    serverContext = new FakeCompanionContext(serverDataBase, nsdAccessor, assetAccessor);
    client1Context = new FakeCompanionContext(client1DataBase, nsdAccessor, assetAccessor);
    client2Context = new FakeCompanionContext(client2DataBase, nsdAccessor, assetAccessor);

    server = serverContext.messenger();
    client1 = client1Context.messenger();
    client2 = client2Context.messenger();
  }

  protected Character character(CompanionContext context, String id) {
    return context.characters().getCharacter(id).getValue().get();
  }

  protected Campaign campaign(CompanionContext context, String id) {
    return context.campaigns().getCampaign(id).getValue().get();
  }

  protected List<String> campaignNames(CompanionContext context) {
    return context.campaigns().getAllCampaigns().stream()
        .map(Campaign::getName)
        .collect(Collectors.toList());
  }

  protected List<String> characterNames(CompanionContext context) {
    return context.characters().getAllCharacters().stream()
        .map(Character::getName)
        .collect(Collectors.toList());
  }

  protected void assertReceived(MessageProcessor processor, Message ... messages) {
    int i = 0;
    for (MessageProcessor.RecievedMessage received : processor.allReceivedMessages()) {
      messages[i++].assertEquals(i + "/" + received, received);
    }
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

  protected ScheduledMessage lastMessage(Collection<ScheduledMessage> messages) {
    return messages.stream().skip(messages.size() -1).findFirst().get();
  }

  protected void assertLastMessage(Collection<ScheduledMessage> messages, @Nullable String senderId,
                                   @Nullable String receiverId, long messageId,
                                   Entry.CompanionMessageProto.Payload.PayloadCase type,
                                   @Nullable String id) {
    assertFalse(messages.isEmpty());
    assertMessage(lastMessage(messages), senderId, receiverId, messageId, type, id);
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
      ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
    } while (pending(messengers));
  }

  protected void assertFinished(CompanionMessenger ... messengers) {
    List<String> ids = ids(messengers);
    List<String> serverIds = serverIds(messengers);
    for (CompanionMessenger messenger : messengers) {
      assertFinished(serverIds, ids, messenger);
    }
  }

  protected void assertFinished(List<String> serverIds, List<String> clientIds,
                                CompanionMessenger messenger) {
    assertFinished(serverIds, clientIds,
        messenger.getServer().getSchedulersByRecpientId().values());
  }

  protected void assertFinished(List<String> serverIds, List<String> clientIds,
                                Collection<MessageScheduler> schedulers) {
    for (MessageScheduler scheduler : schedulers) {
      if (clientIds.contains(scheduler.getRecipientId())) {
        assertFinished(serverIds, clientIds, scheduler);
      }
    }
  }

  protected void assertFinished(List<String> serverIds, List<String> clientIds,
                                MessageScheduler scheduler) {
    assertFalse(scheduler.hasWaiting(serverIds, clientIds));
  }

  List<String> ids(CompanionMessenger ... messengers) {
    return Arrays.asList(messengers).stream()
        .map(m -> m.getContext().settings().getAppId())
        .collect(Collectors.toList());
  }

  List<String> serverIds(CompanionMessenger ... messengers) {
    return Arrays.asList(messengers).stream()
        .filter(m -> m.getServer().started())
        .map(m -> m.getContext().settings().getAppId())
        .collect(Collectors.toList());
  }

  List<String> withId(List<String> ids, String id) {
    List<String> union = new ArrayList<>(ids);
    union.add(id);
    return union;
  }

  private boolean pending(CompanionMessenger ... messengers) {
    List<String> ids = ids(messengers);
    List<String> serverIds = serverIds(messengers);
    for (CompanionMessenger messenger : messengers) {
      // Check that all pending messages are processed.
      if (messenger.getServer().getNsdServer().getServer().hasPendingMessage(ids)) {
        return true;
      }

      for (Map.Entry<String, NetworkClient> client
          : messenger.getClients().getClientsByServerId().entrySet()) {
        if (ids.contains(client.getKey())
            && client.getValue().getTransmitter().hasPendingMessage()) {
          return true;
        }
      }

      for (String id : messenger.getServer().getSchedulersByRecpientId().keySet()) {
        if (ids.contains(id)) {
          MessageScheduler scheduler = messenger.getServer().getSchedulersByRecpientId().get(id);
          if (scheduler.hasWaiting(serverIds, ids)) {
            return true;
          }
        }
      }

      for (String id : messenger.getClients().getSchedulersByServerId().keySet()) {
        if (serverIds.contains(id)) {
          MessageScheduler scheduler = messenger.getClients().getSchedulersByServerId().get(id);
          if (scheduler.hasWaiting(ImmutableList.of(id), serverIds)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  private static String extractId(ScheduledMessage message,
                                  Entry.CompanionMessageProto.Payload.PayloadCase type) {
    return CompanionTest.extractId(message.getData(), type);
  }

  private static String extractId(CompanionMessageData data,
                                  Entry.CompanionMessageProto.Payload.PayloadCase type) {
    switch (type) {
      case CAMPAIGN:
        return data.toProto().getCampaign().getId();

      case CHARACTER:
        return data.toProto().getCharacter().getCreature().getId();

      case IMAGE:
        return data.toProto().getImage().getId();

      case CONDITION:
        return data.toProto().getCondition().getTargetId();

      case CAMPAIGN_DELETE:
        return data.toProto().getCampaignDelete();

      case CHARACTER_DELETE:
        return data.toProto().getCharacterDelete();

      case CONDITION_DELETE:
        return data.toProto().getConditionDelete().getTargetId();

      case XP_AWARD:
        return data.toProto().getXpAward().getCharacterId();

      case ACK:
        return String.valueOf(data.toProto().getAck());

      case WELCOME:
        return String.valueOf(data.toProto().getWelcome().getName());

      default:
        return "";
    }
  }

  public static class Message {
    private final String id;
    private final Entry.CompanionMessageProto.Payload.PayloadCase type;
    private final String sender;
    private final String recipient;

    public Message(String id, Entry.CompanionMessageProto.Payload.PayloadCase type, String sender,
                   String recipient) {
      this.id = id;
      this.type = type;
      this.sender = sender;
      this.recipient = recipient;
    }

    public void assertEquals(String text, MessageProcessor.RecievedMessage message) {
      org.junit.Assert.assertEquals(text, type, message.getMessage().toProto().getPayloadCase());
      org.junit.Assert.assertTrue(text,
          CompanionTest.extractId(message.getMessage(), type).matches(id));
      org.junit.Assert.assertTrue(text, message.getSenderId().matches(sender));
      org.junit.Assert.assertTrue(text, message.getReceiverId().matches(recipient));
    }
  }
}
