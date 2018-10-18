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

import android.support.multidex.MultiDexApplication;

import net.ixitxachitls.companion.CompanionTest;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.proto.Entry;

import org.junit.Before;
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

  @Before
  public void setUp() {
    super.setUp();
  }

  @Test
  public void startStop() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    server.start();

    assertTrue(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    server.stop();

    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());
  }

  @Test
  public void isOnline() {
    // Start the server, check that a campaign is online.
    server.start();
    /*
    assertTrue(server.isOnline(serverContext.campaigns().getCampaign("campaign-server-1")
        .getValue().get()));
    assertFalse(server.isOnline(serverContext.campaigns().getCampaign("campaign-server-2")
        .getValue().get()));
    assertFalse(server.isOnline(serverContext.campaigns().getCampaign("campaign-client1-3")
        .getValue().get()));
        */
  }

  @Test
  public void sendLocalCampaign() {
    /*
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending local campaign schedules a new message.
    server.send(serverContext.campaigns().getCampaign("campaign-server-1").getValue().get());
    assertTrue(server.getServer().started());
    assertFalse(server.getServer().getSchedulersByRecpientId().isEmpty());
    assertServerScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CAMPAIGN, "campaign-server-1",
        "client1", "client2", "client3");
        */
  }

  @Test
  public void sendRemoteCampaign() {
    /*
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending a remote campaign fails (does not schedule anything).
    server.send(serverContext.campaigns().getCampaign("campaign-client1-3").getValue().get());
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());
    */
  }

  @Test
  public void sendCurrent() {
    server.sendCurrent("client1");
    List<ScheduledMessage> waiting = new ArrayList<>(server.getServer()
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
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending local character schedules a new message.
    /*
    server.send(serverContext.characters().getCharacter("character-server-1").getValue().get());
    assertTrue(server.getServer().started());
    assertFalse(server.getServer().getSchedulersByRecpientId().isEmpty());
    assertServerScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER, "character-server-1",
        "client1", "client2", "client3");
        */
  }

  @Test
  public void sendRemoteLocalCharacter() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending a remote character of a local campaign sends it to all clients that don't own that
    // character.
    /*
    server.send(serverContext.characters().getCharacter("character-client2-5").getValue().get());
    assertTrue(server.getServer().started());
    assertServerScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER,
        "character-client2-5", "client1", "client3");
        */
  }

  @Test
  public void sendLocalRemoteCharacter() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending a local character of a remote campaign sends it to the owner of the campaign only.
    /*
    server.send(serverContext.characters().getCharacter("character-server-2").getValue().get());
    assertFalse(server.getServer().started());
    assertClientScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER,
        "character-server-2", "client1");
        */
  }

  @Test
  public void sendRemoteRemoteCharacter() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    // Sending a remote character of a remote campaign does nothing.
    /*
    server.send(serverContext.characters().getCharacter("character-client1-4").getValue().get());
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());
    */
  }

  @Test
  public void sendLocalLocalImage() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    server.send(serverContext.images(true).getImage("characters",
        "character-server-1").getValue().get());
    assertTrue(server.getServer().started());
    assertServerScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.IMAGE,
        "character-server-1", "client1", "client2", "client3");
  }

  @Test
  public void sendLocalRemoteImage() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    server.send(serverContext.images(true).getImage("characters",
        "character-server-2").getValue().get());
    assertFalse(server.getServer().started());
    assertClientScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.IMAGE,
        "character-server-2", "client1");
  }

  @Test
  public void sendRemoteLocalImage() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    server.send(serverContext.images(true).getImage("characters",
        "character-server-2").getValue().get());
    assertFalse(server.getServer().started());
    assertClientScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.IMAGE,
        "character-server-2", "client1");
  }

  @Test
  public void sendRemoteRemoteImage() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    server.send(serverContext.images(true).getImage("characters",
        "character-server-2").getValue().get());
    assertFalse(server.getServer().started());
    assertClientScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.IMAGE,
        "character-server-2", "client1");
  }

  @Test
  public void sendLocalCondition() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    /*
    server.send(serverContext.creatures().getCreatureOrCharacter("character-server-1").get(),
        new TimedCondition(Conditions.FLAT_FOOTED, "server", 10));
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());
    */
  }

  @Test
  public void sendRemoteRemoteCondition() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());
/*
    server.send(serverContext.creatures().getCreatureOrCharacter("character-client1-4").get(),
        new TimedCondition(Conditions.FLAT_FOOTED, "server", 10));
    assertFalse(server.getServer().started());
    assertClientScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CONDITION,
        "character-client1-4", "client1");
        */
  }

  @Test
  public void sendRemoteLocalCondition() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    /*
    server.send(serverContext.creatures().getCreatureOrCharacter("character-client1-3").get(),
        new TimedCondition(Conditions.FLAT_FOOTED, "server", 10));
    assertTrue(server.getServer().started());
    assertServerScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CONDITION,
        "character-client1-3", "client1");
        */
  }

  @Test
  public void sendCampaignDeletion() {
    /*
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    server.sendDeletion(serverContext.campaigns().getCampaign("campaign-server-1").getValue().get());
    assertTrue(server.getServer().started());
    assertServerScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CAMPAIGN_DELETE,
        "campaign-server-1", "client1", "client2", "client3");
        */
  }

  @Test
  public void sendLocalCharacterDeletion() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    /*
    server.sendDeletion(serverContext.characters().getCharacter("character-server-1")
        .getValue().get());
    assertTrue(server.getServer().started());
    assertServerScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER_DELETE,
        "character-server-1", "client1", "client2", "client3");
        */
  }

  @Test
  public void sendRemoteCharacterDeletion() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    /*
    server.sendDeletion(serverContext.characters().getCharacter("character-server-2")
        .getValue().get());
    assertFalse(server.getServer().started());
    assertClientScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CHARACTER_DELETE,
        "character-server-2", "client1");
        */
  }

  @Test
  public void sendLocalConditionDeletion() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    /*
    server.sendDeletion(Conditions.FLAT_FOOTED.getName(), "server",
        serverContext.creatures().getCreatureOrCharacter("character-client1-3").get());
    assertTrue(server.getServer().started());
    assertServerScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CONDITION_DELETE,
        "character-client1-3", "client1");
        */
  }

  @Test
  public void sendRemoteConditionDeletion() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    /*
    server.sendDeletion(Conditions.FLAT_FOOTED.getName(), "server",
        serverContext.creatures().getCreatureOrCharacter("character-client2-3").get());
    assertFalse(server.getServer().started());
    assertClientScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CONDITION_DELETE,
        "character-client2-3", "client1");
        */
  }

  @Test
  public void sendXpAward() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    /*
    server.sendDeletion(Conditions.FLAT_FOOTED.getName(), "server",
        serverContext.creatures().getCreatureOrCharacter("character-client2-3").get());
    assertFalse(server.getServer().started());
    assertClientScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.CONDITION_DELETE,
        "character-client2-3", "client1");
        */
  }

  @Test
  public void sendAckToClient() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    server.sendAckToClient("client1", 42);
    assertTrue(server.getServer().started());
    assertServerScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.ACK,
        "42", "client1");
  }

  @Test
  public void sendAckToServer() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    server.sendAckToServer("client1", 42);
    assertFalse(server.getServer().started());
    assertClientScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.ACK,
        "42", "client1");
  }

  @Test
  public void sendWelcome() {
    // Initially, server is not started and schedulers are empty.
    assertFalse(server.getServer().started());
    assertTrue(server.getServer().getSchedulersByRecpientId().isEmpty());

    server.getClients().getClientsByServerId().put("client42",
        new NetworkClient(serverContext));
    server.getServer().getSchedulersByRecpientId().put("client23",
        new MessageScheduler(serverContext, "client23"));
    server.sendWelcome();
    assertTrue(server.getServer().started());
    assertClientScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.WELCOME,
        "Server", "client42");
    assertServerScheduled(server, "server",
        Entry.CompanionMessageProto.Payload.PayloadCase.WELCOME,
        "Server", "client23");
  }

  @Test
  public void ackServer() {
    /*
    MessageScheduler scheduler = new MessageScheduler(serverContext, "client42");
    server.getServer().getSchedulersByRecpientId().put("client42", scheduler);
    scheduler.schedule(CompanionMessageData.fromDelete(
        serverContext.campaigns().getCampaign("campaign-server-1").getValue().get()));

    assertEquals(1, scheduler.getWaiting().size());
    assertEquals(CompanionMessageData.Type.CAMPAIGN_DELETE,
        scheduler.nextWaiting().get().getMessage().getData().getType());
    assertEquals(1, scheduler.getPending().size());
    server.ackServer("client42", 24);
    assertEquals(0, scheduler.getPending().size());
    assertEquals(1, scheduler.getAcked().size());
    */
  }

  @Test
  public void ackClient() {
    /*
    MessageScheduler scheduler = new MessageScheduler(serverContext, "client23");
    server.getClients().getSchedulersByServerId().put("client23", scheduler);
    scheduler.schedule(CompanionMessageData.fromDelete(
        serverContext.campaigns().getCampaign("campaign-server-1").getValue().get()));

    assertEquals(1, scheduler.getWaiting().size());
    assertEquals(CompanionMessageData.Type.CAMPAIGN_DELETE,
        scheduler.nextWaiting().get().getMessage().getData().getType());
    assertEquals(1, scheduler.getPending().size());
    server.ackClient("client23", 24);
    assertEquals(0, scheduler.getPending().size());
    assertEquals(1, scheduler.getAcked().size());
    */
  }
}