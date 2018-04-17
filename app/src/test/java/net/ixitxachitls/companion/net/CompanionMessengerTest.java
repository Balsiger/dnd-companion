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

import net.ixitxachitls.companion.CompanionTest;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.FakeCompanionContext;
import net.ixitxachitls.companion.data.dynamics.Images;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.net.nsd.FakeNsdAccessor;
import net.ixitxachitls.companion.proto.Entry;
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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(application = MultiDexApplication.class)
public class CompanionMessengerTest extends CompanionTest {

  // Make .setValue calls synchronous and allow them to be called in tests.
  @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

  protected final FakeDataBaseAccessor dataBaseAccessor = new FakeServerDataBaseAccessor();
  protected final FakeAssetAccessor assetAccessor = new FakeAssetAccessor();
  protected final FakeNsdAccessor nsdAccessor = new FakeNsdAccessor();
  protected FakeCompanionContext context;

  @Before
  public void setUp() {
    Entries.init(assetAccessor);

    context = new FakeCompanionContext(dataBaseAccessor, nsdAccessor);
    Images.load(context, assetAccessor);
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
    assertTrue(messenger.isOnline(context.campaigns().getCampaign("campaign-server-1")
        .getValue().get()));
    assertFalse(messenger.isOnline(context.campaigns().getCampaign("campaign-server-2")
        .getValue().get()));
    assertFalse(messenger.isOnline(context.campaigns().getCampaign("campaign-client1-3")
        .getValue().get()));
  }

  @Test
  public void sendLocalCampaign() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending local campaign schedules a new message.
    messenger.send(context.campaigns().getCampaign("campaign-server-1").getValue().get());
    assertTrue(messenger.getServer().started());
    assertFalse(messenger.getServer().getSchedulersByRecpientId().isEmpty());
    assertServerScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CAMPAIGN, "campaign-server-1",
        "client1", "client2", "client3");
  }

  @Test
  public void sendRemoteCampaign() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending a remote campaign fails (does not schedule anything).
    messenger.send(context.campaigns().getCampaign("campaign-client1-3").getValue().get());
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());
  }

  @Test
  public void sendCurrent() {
    Bitmap bitmap = BitmapFactory.decodeFile("../app/src/test/files/characters-local/"
        + "character-server-1.jpg");
    if (bitmap != null)
      System.out.println("FOUND");

    CompanionMessenger messenger = createMessenger();

    messenger.sendCurrent("client1");
    List<ScheduledMessage> waiting = new ArrayList<>(messenger.getServer()
        .getSchedulersByRecpientId().get("client1").getWaiting());
    assertEquals(8, waiting.size());
    assertMessage(waiting.get(0), "server", "client1", 24,
        Entry.CompanionMessageProto.Payload.PayloadCase.CAMPAIGN, "campaign-server-3");
    assertMessage(waiting.get(1), "server", "client1", 25,
        Entry.CompanionMessageProto.Payload.PayloadCase.CAMPAIGN, "campaign-server-1");
    assertMessage(waiting.get(2), "server", "client1", 26,
        Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER, "character-server-1");
    assertMessage(waiting.get(3), "server", "client1", 27,
        Entry.CompanionMessageProto.Payload.PayloadCase.IMAGE, "character-server-1");
    assertMessage(waiting.get(4), "server", "client1", 28,
        Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER, "character-client3-2");
    assertMessage(waiting.get(5), "server", "client1", 29,
        Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER, "character-client2-5");
    assertMessage(waiting.get(6), "server", "client1", 30,
        Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER, "character-server-2");
    assertMessage(waiting.get(7), "server", "client1", 31,
        Entry.CompanionMessageProto.Payload.PayloadCase.IMAGE, "character-server-2");
  }

  @Test
  public void sendLocalCharacter() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending local character schedules a new message.
    messenger.send(context.characters().getCharacter("character-server-1").getValue().get());
    assertTrue(messenger.getServer().started());
    assertFalse(messenger.getServer().getSchedulersByRecpientId().isEmpty());
    assertServerScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER, "character-server-1",
        "client1", "client2", "client3");
  }

  @Test
  public void sendRemoteLocalCharacter() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending a remote character of a local campaign sends it to all clients that don't own that
    // character.
    messenger.send(context.characters().getCharacter("character-client2-5").getValue().get());
    assertTrue(messenger.getServer().started());
    assertServerScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER,
        "character-client2-5", "client1", "client3");
  }

  @Test
  public void sendLocalRemoteCharacter() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending a local character of a remote campaign sends it to the owner of the campaign only.
    messenger.send(context.characters().getCharacter("character-server-2").getValue().get());
    assertFalse(messenger.getServer().started());
    assertClientScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER,
        "character-server-2", "client1");
  }

  @Test
  public void sendRemoteRemoteCharacter() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending a remote character of a remote campaign does nothing.
    messenger.send(context.characters().getCharacter("character-client1-4").getValue().get());
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
    assertServerScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.IMAGE,
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
    assertClientScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.IMAGE,
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
    assertClientScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.IMAGE,
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
    assertClientScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.IMAGE,
        "character-server-2", "client1");
  }

  @Test
  public void sendLocalCondition() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.send(context.creatures().getCreatureOrCharacter("character-server-1").get(),
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

    messenger.send(context.creatures().getCreatureOrCharacter("character-client1-4").get(),
        new TimedCondition(Conditions.FLAT_FOOTED, "server", 10));
    assertFalse(messenger.getServer().started());
    assertClientScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CONDITION,
        "character-client1-4", "client1");
  }

  @Test
  public void sendRemoteLocalCondition() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.send(context.creatures().getCreatureOrCharacter("character-client1-3").get(),
        new TimedCondition(Conditions.FLAT_FOOTED, "server", 10));
    assertTrue(messenger.getServer().started());
    assertServerScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CONDITION,
        "character-client1-3", "client1");
  }

  @Test
  public void sendCampaignDeletion() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.sendDeletion(context.campaigns().getCampaign("campaign-server-1").getValue().get());
    assertTrue(messenger.getServer().started());
    assertServerScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CAMPAIGN_DELETE,
        "campaign-server-1", "client1", "client2", "client3");
  }

  @Test
  public void sendLocalCharacterDeletion() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.sendDeletion(context.characters().getCharacter("character-server-1")
        .getValue().get());
    assertTrue(messenger.getServer().started());
    assertServerScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER_DELETE,
        "character-server-1", "client1", "client2", "client3");
  }

  @Test
  public void sendRemoteCharacterDeletion() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.sendDeletion(context.characters().getCharacter("character-server-2")
        .getValue().get());
    assertFalse(messenger.getServer().started());
    assertClientScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER_DELETE,
        "character-server-2", "client1");
  }

  @Test
  public void sendLocalConditionDeletion() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.sendDeletion(Conditions.FLAT_FOOTED.getName(), "server",
        context.creatures().getCreatureOrCharacter("character-client1-3").get());
    assertTrue(messenger.getServer().started());
    assertServerScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CONDITION_DELETE,
        "character-client1-3", "client1");
  }

  @Test
  public void sendRemoteConditionDeletion() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.sendDeletion(Conditions.FLAT_FOOTED.getName(), "server",
        context.creatures().getCreatureOrCharacter("character-client2-3").get());
    assertFalse(messenger.getServer().started());
    assertClientScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CONDITION_DELETE,
        "character-client2-3", "client1");
  }

  @Test
  public void sendXpAward() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.sendDeletion(Conditions.FLAT_FOOTED.getName(), "server",
        context.creatures().getCreatureOrCharacter("character-client2-3").get());
    assertFalse(messenger.getServer().started());
    assertClientScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CONDITION_DELETE,
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
        Entry.CompanionMessageProto.Payload.PayloadCase.ACK,
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
        Entry.CompanionMessageProto.Payload.PayloadCase.ACK,
        "42", "client1");
  }

  @Test
  public void sendWelcome() {
    CompanionMessenger messenger = createMessenger();

    // Initially, server is not started and schedulers are empty.
    assertFalse(messenger.getServer().started());
    assertTrue(messenger.getServer().getSchedulersByRecpientId().isEmpty());

    messenger.getClients().getClientsByServerId().put("client42", new NetworkClient(context));
    messenger.getServer().getSchedulersByRecpientId().put("client23",
        new MessageScheduler(context.settings(), "client23"));
    messenger.sendWelcome();
    assertTrue(messenger.getServer().started());
    assertClientScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.WELCOME,
        "Server", "client42");
    assertServerScheduled(messenger, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.WELCOME,
        "Server", "client23");
  }

  @Test
  public void ackServer() {
    CompanionMessenger messenger = createMessenger();

    MessageScheduler scheduler = new MessageScheduler(context.settings(), "client42");
    messenger.getServer().getSchedulersByRecpientId().put("client42", scheduler);
    scheduler.schedule(CompanionMessageData.fromDelete(
        context.campaigns().getCampaign("campaign-server-1").getValue().get()));

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

    MessageScheduler scheduler = new MessageScheduler(context.settings(), "client23");
    messenger.getClients().getSchedulersByServerId().put("client23", scheduler);
    scheduler.schedule(CompanionMessageData.fromDelete(
        context.campaigns().getCampaign("campaign-server-1").getValue().get()));

    assertEquals(1, scheduler.getWaiting().size());
    assertEquals(CompanionMessageData.Type.CAMPAIGN_DELETE,
        scheduler.nextWaiting().get().getMessage().getData().getType());
    assertEquals(1, scheduler.getPending().size());
    messenger.ackClient("client23", 24);
    assertEquals(0, scheduler.getPending().size());
    assertEquals(1, scheduler.getAcked().size());
  }

  private CompanionMessenger createMessenger() {
    return new CompanionMessenger(context, nsdAccessor, null, new Handler(), 50);
  }
}