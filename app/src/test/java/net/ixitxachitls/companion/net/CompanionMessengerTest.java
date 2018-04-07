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

package net.ixitxachitls.companion.net;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.multidex.MultiDexApplication;

import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Creatures;
import net.ixitxachitls.companion.data.dynamics.Images;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.net.nsd.FakeNsdAccessor;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.rules.Conditions;
import net.ixitxachitls.companion.storage.FakeAssetAccessor;
import net.ixitxachitls.companion.storage.FakeDataBaseAccessor;
import net.ixitxachitls.companion.storage.FakeServerDataBaseAccessor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(application = MultiDexApplication.class)
public class CompanionMessengerTest {

  // Make .setValue calls synchronous and allow them to be called in tests.
  @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

  protected final FakeDataBaseAccessor dataBaseAccessor = new FakeServerDataBaseAccessor();
  protected final FakeAssetAccessor assetAccessor = new FakeAssetAccessor();
  protected FakeNsdAccessor nsdAccessor;

  @Before
  public void setUp() {
    Settings.init(dataBaseAccessor);
    nsdAccessor = new FakeNsdAccessor();
    Entries.init(assetAccessor);
    Campaigns.load(dataBaseAccessor);
    Characters.load(dataBaseAccessor);
    Images.load(assetAccessor);
  }

  @Test
  public void startStop() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.start();

    assertTrue(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.stop();

    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());
  }

  @Test
  public void isOnline() {
    CompanionMessenger messenger = createMessenger();

    // Start the server, check that a campaign is online.
    messenger.start();
    assertTrue(messenger.isOnline(Campaigns.getCampaign("campaign-server-1").getValue().get()));
    assertFalse(messenger.isOnline(Campaigns.getCampaign("campaign-server-2").getValue().get()));
    assertFalse(messenger.isOnline(Campaigns.getCampaign("campaign-client1-3").getValue().get()));
  }

  @Test
  public void sendLocalCampaign() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending local campaign schedules a new message.
    messenger.send(Campaigns.getCampaign("campaign-server-1").getValue().get());
    assertTrue(messenger.getServer().started());
    assertFalse(messenger.getServer().getSchedulersByRecpientId().isEmpty());
    assertServerScheduled(messenger, "server", Data.CompanionMessageProto.Payload.PayloadCase.CAMPAIGN,
        "campaign-server-1", "client1", "client2", "client3");
  }

  @Test
  public void sendRemoteCampaign() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending a remote campaign fails (does not schedule anything).
    messenger.send(Campaigns.getCampaign("campaign-client1-3").getValue().get());
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());
  }

  @Test
  public void sendCurrent() {
    Bitmap bitmap = BitmapFactory.decodeFile("../app/src/test/files/characters-local/character-server-1.jpg");
    if (bitmap != null)
      System.out.println("FOUND");

    CompanionMessenger messenger = createMessenger();

    messenger.sendCurrent("client1");
    List<ScheduledMessage> waiting = new ArrayList<>(messenger.getServer()
        .getSchedulersByRecpientId().get("client1").getWaiting());
    assertEquals(8, waiting.size());
    assertMessage(waiting.get(0), "server", "client1", 24,
        Data.CompanionMessageProto.Payload.PayloadCase.CAMPAIGN, "campaign-server-3");
    assertMessage(waiting.get(1), "server", "client1", 25,
        Data.CompanionMessageProto.Payload.PayloadCase.CAMPAIGN, "campaign-server-1");
    assertMessage(waiting.get(2), "server", "client1", 26,
        Data.CompanionMessageProto.Payload.PayloadCase.CHARACTER, "character-server-1");
    assertMessage(waiting.get(3), "server", "client1", 27,
        Data.CompanionMessageProto.Payload.PayloadCase.IMAGE, "character-server-1");
    assertMessage(waiting.get(4), "server", "client1", 28,
        Data.CompanionMessageProto.Payload.PayloadCase.CHARACTER, "character-client3-2");
    assertMessage(waiting.get(5), "server", "client1", 29,
        Data.CompanionMessageProto.Payload.PayloadCase.CHARACTER, "character-client2-5");
    assertMessage(waiting.get(6), "server", "client1", 30,
        Data.CompanionMessageProto.Payload.PayloadCase.CHARACTER, "character-server-2");
    assertMessage(waiting.get(7), "server", "client1", 31,
        Data.CompanionMessageProto.Payload.PayloadCase.IMAGE, "character-server-2");
  }

  @Test
  public void sendLocalCharacter() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending local character schedules a new message.
    messenger.send(Characters.getCharacter("character-server-1").getValue().get());
    assertTrue(messenger.getServer().started());
    assertFalse(messenger.getServer().getSchedulersByRecpientId().isEmpty());
    assertServerScheduled(messenger, "server", Data.CompanionMessageProto.Payload.PayloadCase.CHARACTER,
        "character-server-1", "client1", "client2", "client3");
  }

  @Test
  public void sendRemoteLocalCharacter() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending a remote character of a local campaign sends it to all clients that don't own that
    // character.
    messenger.send(Characters.getCharacter("character-client2-5").getValue().get());
    assertTrue(messenger.getServer().started());
    assertServerScheduled(messenger, "server", Data.CompanionMessageProto.Payload.PayloadCase.CHARACTER,
        "character-client2-5", "client1", "client3");
  }

  @Test
  public void sendLocalRemoteCharacter() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending a local character of a remote campaign sends it to the owner of the campaign only.
    messenger.send(Characters.getCharacter("character-server-2").getValue().get());
    assertFalse(messenger.getServer().started());
    assertClientScheduled(messenger, "server", Data.CompanionMessageProto.Payload.PayloadCase.CHARACTER,
        "character-server-2", "client1");
  }

  @Test
  public void sendRemoteRemoteCharacter() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending a remote character of a remote campaign does nothing.
    messenger.send(Characters.getCharacter("character-client1-4").getValue().get());
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());
  }

  @Test
  public void sendLocalLocalImage() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.send(Images.local().getImage("characters", "character-server-1").getValue().get());
    assertTrue(messenger.getServer().started());
    assertServerScheduled(messenger, "server", Data.CompanionMessageProto.Payload.PayloadCase.IMAGE,
        "character-server-1", "client1", "client2", "client3");
  }

  @Test
  public void sendLocalRemoteImage() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.send(Images.local().getImage("characters", "character-server-2").getValue().get());
    assertFalse(messenger.getServer().started());
    assertClientScheduled(messenger, "server", Data.CompanionMessageProto.Payload.PayloadCase.IMAGE,
        "character-server-2", "client1");
  }

  @Test
  public void sendRemoteLocalImage() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.send(Images.local().getImage("characters", "character-server-2").getValue().get());
    assertFalse(messenger.getServer().started());
    assertClientScheduled(messenger, "server", Data.CompanionMessageProto.Payload.PayloadCase.IMAGE,
        "character-server-2", "client1");
  }

  @Test
  public void sendRemoteRemoteImage() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.send(Images.local().getImage("characters", "character-server-2").getValue().get());
    assertFalse(messenger.getServer().started());
    assertClientScheduled(messenger, "server", Data.CompanionMessageProto.Payload.PayloadCase.IMAGE,
        "character-server-2", "client1");
  }

  @Test
  public void sendLocalCondition() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.send(Creatures.getCreatureOrCharacter("character-server-1").get(),
        new TimedCondition(Conditions.FLAT_FOOTED, "server", 10));
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());
  }

  @Test
  public void sendRemoteRemoteCondition() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.send(Creatures.getCreatureOrCharacter("character-client1-4").get(),
        new TimedCondition(Conditions.FLAT_FOOTED, "server", 10));
    assertFalse(messenger.getServer().started());
    assertClientScheduled(messenger, "server",
        Data.CompanionMessageProto.Payload.PayloadCase.CONDITION,
        "character-client1-4", "client1");
  }

  @Test
  public void sendRemoteLocalCondition() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.send(Creatures.getCreatureOrCharacter("character-client1-3").get(),
        new TimedCondition(Conditions.FLAT_FOOTED, "server", 10));
    assertTrue(messenger.getServer().started());
    assertServerScheduled(messenger, "server",
        Data.CompanionMessageProto.Payload.PayloadCase.CONDITION,
        "character-client1-3", "client1");
  }

  @Test
  public void sendCampaignDeletion() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.sendDeletion(Campaigns.getCampaign("campaign-server-1").getValue().get());
    assertTrue(messenger.getServer().started());
    assertServerScheduled(messenger, "server",
        Data.CompanionMessageProto.Payload.PayloadCase.CAMPAIGN_DELETE,
        "campaign-server-1", "client1", "client2", "client3");
  }

  @Test
  public void sendLocalCharacterDeletion() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.sendDeletion(Characters.getCharacter("character-server-1").getValue().get());
    assertTrue(messenger.getServer().started());
    assertServerScheduled(messenger, "server",
        Data.CompanionMessageProto.Payload.PayloadCase.CHARACTER_DELETE,
        "character-server-1", "client1", "client2", "client3");
  }

  @Test
  public void sendRemoteCharacterDeletion() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.sendDeletion(Characters.getCharacter("character-server-2").getValue().get());
    assertFalse(messenger.getServer().started());
    assertClientScheduled(messenger, "server",
        Data.CompanionMessageProto.Payload.PayloadCase.CHARACTER_DELETE,
        "character-server-2", "client1");
  }

  @Test
  public void sendLocalConditionDeletion() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.sendDeletion(Conditions.FLAT_FOOTED.getName(), "server",
        Creatures.getCreatureOrCharacter("character-client1-3").get());
    assertTrue(messenger.getServer().started());
    assertServerScheduled(messenger, "server",
        Data.CompanionMessageProto.Payload.PayloadCase.CONDITION_DELETE,
        "character-client1-3", "client1");
  }

  @Test
  public void sendRemoteConditionDeletion() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.sendDeletion(Conditions.FLAT_FOOTED.getName(), "server",
        Creatures.getCreatureOrCharacter("character-client2-3").get());
    assertFalse(messenger.getServer().started());
    assertClientScheduled(messenger, "server",
        Data.CompanionMessageProto.Payload.PayloadCase.CONDITION_DELETE,
        "character-client2-3", "client1");
  }

  @Test
  public void sendXpAward() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.sendDeletion(Conditions.FLAT_FOOTED.getName(), "server",
        Creatures.getCreatureOrCharacter("character-client2-3").get());
    assertFalse(messenger.getServer().started());
    assertClientScheduled(messenger, "server",
        Data.CompanionMessageProto.Payload.PayloadCase.CONDITION_DELETE,
        "character-client2-3", "client1");
  }

  @Test
  public void sendAckToClient() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.sendAckToClient("client1", 42);
    assertTrue(messenger.getServer().started());
    assertServerScheduled(messenger, "server",
        Data.CompanionMessageProto.Payload.PayloadCase.ACK,
        "42", "client1");
  }

  @Test
  public void sendAckToServer() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.sendAckToServer("client1", 42);
    assertFalse(messenger.getServer().started());
    assertClientScheduled(messenger, "server",
        Data.CompanionMessageProto.Payload.PayloadCase.ACK,
        "42", "client1");
  }

  @Test
  public void sendWelcome() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.getClients().getClientsByServerId().put("client42",
        new NetworkClient(dataBaseAccessor));
    messenger.getServer().getSchedulersByRecpientId().put("client23",
        new MessageScheduler("client23", dataBaseAccessor));
    messenger.sendWelcome();
    assertTrue(messenger.getServer().started());
    assertClientScheduled(messenger, "server",
        Data.CompanionMessageProto.Payload.PayloadCase.WELCOME,
        "Server", "client42");
    assertServerScheduled(messenger, "server",
        Data.CompanionMessageProto.Payload.PayloadCase.WELCOME,
        "Server", "client23");
  }

  @Test
  public void ackServer() {
    CompanionMessenger messenger = createMessenger();

    MessageScheduler scheduler = new MessageScheduler("client42", dataBaseAccessor);
    messenger.getServer().getSchedulersByRecpientId().put("client42", scheduler);
    scheduler.schedule(CompanionMessageData.fromDelete(
        Campaigns.getCampaign("campaign-server-1").getValue().get()));

    assertEquals(1, scheduler.getWaiting().size());
    assertEquals(CompanionMessageData.Type.CAMPAIGN_DELETE,
        scheduler.nextWaiting().get().getMessage().getData().getType());
    assertEquals(1, scheduler.getPending().size());
    messenger.ackServer("client42", 24);
    assertEquals(0, scheduler.getPending().size());
    assertEquals(1, scheduler.getAcked().size());
  }

  @Test
  public void ackClient() {
    CompanionMessenger messenger = createMessenger();

    MessageScheduler scheduler = new MessageScheduler("client23", dataBaseAccessor);
    messenger.getClients().getSchedulersByServerId().put("client23", scheduler);
    scheduler.schedule(CompanionMessageData.fromDelete(
        Campaigns.getCampaign("campaign-server-1").getValue().get()));

    assertEquals(1, scheduler.getWaiting().size());
    assertEquals(CompanionMessageData.Type.CAMPAIGN_DELETE,
        scheduler.nextWaiting().get().getMessage().getData().getType());
    assertEquals(1, scheduler.getPending().size());
    messenger.ackClient("client23", 24);
    assertEquals(0, scheduler.getPending().size());
    assertEquals(1, scheduler.getAcked().size());
  }

  private CompanionMessenger createMessenger() {
    return new CompanionMessenger(dataBaseAccessor, nsdAccessor, Settings.get(), null,
        new Handler());
  }

  protected void assertClientScheduled(CompanionMessenger messenger, String senderId,
                                       Data.CompanionMessageProto.Payload.PayloadCase type,
                                       @Nullable String id,
                                       String ... recipientIds) {
    assertThat(messenger.getClients().getSchedulersByServerId().keySet(),
        containsInAnyOrder(recipientIds));
    for (String recipientId : recipientIds) {
      assertLastMessage(messenger.getClients().getSchedulersByServerId().get(recipientId)
          .getWaiting(), senderId, recipientId, 0, type, id);
    }
  }

  protected void assertServerScheduled(CompanionMessenger messenger, String senderId,
                                       Data.CompanionMessageProto.Payload.PayloadCase type,
                                       @Nullable String id,
                                       String ... recipientIds) {
    assertThat(messenger.getServer().getSchedulersByRecpientId().keySet(),
        containsInAnyOrder(recipientIds));
    for (String recipientId : recipientIds) {
      assertLastMessage(messenger.getServer().getSchedulersByRecpientId().get(recipientId)
          .getWaiting(), senderId, recipientId, 0, type, id);
    }
  }

  protected void assertLastMessage(Collection<ScheduledMessage> messages, @Nullable String senderId,
                                   @Nullable String receiverId, long messageId,
                                   Data.CompanionMessageProto.Payload.PayloadCase type,
                                   @Nullable String id) {
    assertMessage(messages.stream().skip(messages.size() - 1).findFirst().get(),
        senderId, receiverId, messageId, type, id);
  }

  protected void assertMessage(ScheduledMessage message, @Nullable String senderId,
                               @Nullable String receiverId, long messageId,
                               Data.CompanionMessageProto.Payload.PayloadCase type,
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
      switch (type) {
        case CAMPAIGN:
          assertEquals(id, message.getData().toProto().getCampaign().getId());
          break;

        case CHARACTER:
          assertEquals(id, message.getData().toProto().getCharacter().getCreature().getId());
          break;

        case IMAGE:
          assertEquals(id, message.getData().toProto().getImage().getId());
          break;

        case CONDITION:
          assertEquals(id, message.getData().toProto().getCondition().getTargetId());
          break;

        case CAMPAIGN_DELETE:
          assertEquals(id, message.getData().toProto().getCampaignDelete());
          break;

        case CHARACTER_DELETE:
          assertEquals(id, message.getData().toProto().getCharacterDelete());
          break;

        case CONDITION_DELETE:
          assertEquals(id, message.getData().toProto().getConditionDelete().getTargetId());
          break;

        case XP_AWARD:
          assertEquals(id, message.getData().toProto().getXpAward().getCharacterId());
          break;

        case ACK:
          assertEquals(id, String.valueOf(message.getData().toProto().getAck()));
          break;

        case WELCOME:
          assertEquals(id, String.valueOf(message.getData().toProto().getWelcome().getName()));
          break;

        default:
          fail();
      }
    }
  }
}