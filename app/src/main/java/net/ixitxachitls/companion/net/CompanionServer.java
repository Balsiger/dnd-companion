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

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.net.nsd.NsdServer;
import net.ixitxachitls.companion.util.Ids;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Class to publish data to the local network.
 */
public class CompanionServer {

  private final NsdServer nsdServer;
  private final Map<String, MessageScheduler> schedulersByRecpientId = new HashMap<>();

  public CompanionServer(CompanionApplication application) {
    this.nsdServer = new NsdServer(application);

    // Setup stored messages.
    for (ScheduledMessage message
        : ScheduledMessages.get().getMessagesBySender(Settings.get().getAppId())) {
      setupScheduler(message.getRecieverId());
    }
  }

  public void ack(String recipientId, long messageId) {
    MessageScheduler scheduler = schedulersByRecpientId.get(recipientId);
    if (scheduler != null) {
      scheduler.ack(messageId);
    } else {
      Status.log("Ignored ack for invalid recipient " + recipientId);
    }
  }

  public void revoke(String id) {
    schedulersByRecpientId.values().stream().forEach(s -> s.revoke(id));
  }

  public void sendWaiting() {
    boolean sent = false;
    for (MessageScheduler scheduler : schedulersByRecpientId.values()) {
      for (Optional<ScheduledMessage> message = scheduler.nextWaiting();
           message.isPresent();
           message = scheduler.nextWaiting()) {
        Status.log(message.get() + " to " + getRecipientName(message.get().getRecieverId()));
        // The messasge is already marked as sent or pending (depending on ack).
        // Thus, even if sending fails, we don't need to do anything. Pending message
        // will automatically be resent and sent messages can be safely ignored.
        nsdServer.send(message.get().getRecieverId(), message.get().getMessageId(),
            message.get().getData());
        Status.refreshClientConnection(message.get().getRecieverId());
        sent = true;
      }
    }

    if (started() && !sent && !Campaigns.hasAnyPublished()) {
      // Stop the server if there are no more message to be sent and there are no published
      // campaigns.
      stop();
    }
  }

  private String getRecipientName(String id) {
    if (id.equals(Settings.get().getAppId())) {
      return "(me)";
    }

    return nsdServer.getNameForId(id);
  }

  public boolean isOnline() {
    return nsdServer.isStarted();
  }

  public Set<String> connectedClientIds() {
    return schedulersByRecpientId.keySet();
  }

  public Set<String> clientIds(Campaign campaign) {
    Set<String> ids = new HashSet<>();
    ids.addAll(clientIds());

    for (String characterId
        : Characters.getCampaignCharacterIds(campaign.getCampaignId()).getValue()) {
      String id = Ids.extractServerId(characterId);
      // Don't send anything to oneself.
      if (!id.equals(Settings.get().getAppId())) {
        ids.add(id);
      }
    }

    return ids;
  }

  private Collection<String> clientIds() {
    return nsdServer.connectedIds();
  }

  public void schedule(CompanionMessageData message) {
    schedule(schedulersByRecpientId.keySet(), message);
  }

  public void schedule(Iterable<String> recipients, CompanionMessageData message) {
    startIfNecessary();
    for (String recipient : recipients) {
      setupScheduler(recipient).schedule(message);
    }
  }

  public void schedule(String recipientId, CompanionMessageData message) {
    setupScheduler(recipientId).schedule(message);
  }

  private MessageScheduler setupScheduler(String recipientId) {
    MessageScheduler scheduler = schedulersByRecpientId.get(recipientId);
    if (scheduler == null) {
      scheduler = new MessageScheduler(recipientId);
      schedulersByRecpientId.put(recipientId, scheduler);

      startIfNecessary();
    }

    return scheduler;
  }

  public void start() {
    Status.log("starting companion server");
    if (!nsdServer.isStarted()) {
      nsdServer.start();
    }
  }

  public void startIfNecessary() {
    if (!started() && Campaigns.hasAnyPublished()) {
      start();
    }
  }

  public boolean started() {
    return nsdServer.isStarted();
  }

  public void stop() {
    Status.log("stopping companion server");
    nsdServer.stop();
  }

  public List<CompanionMessage> receive() {
    return nsdServer.receive();
  }
}
