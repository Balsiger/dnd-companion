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

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.net.nsd.NsdAccessor;
import net.ixitxachitls.companion.net.nsd.NsdDiscovery;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Subscriber for campaign information.
 */
public class CompanionClients implements NsdDiscovery.NsdCallback {
  private final CompanionContext companionContext;
  private final NsdDiscovery nsdDiscovery;
  private final Map<String, NetworkClient> clientsByServerId = new ConcurrentHashMap<>();
  private final Map<String, NetworkClient> clientsByServerName = new ConcurrentHashMap<>();
  private final Map<String, MessageScheduler> schedulersByServerId = new HashMap<>();

  private boolean started;

  public CompanionClients(CompanionContext companionContext, NsdAccessor nsdAccessor) {
    this.companionContext = companionContext;
    this.nsdDiscovery = new NsdDiscovery(companionContext.settings(), nsdAccessor, this);
  }

  // Network handling.
  public void start() {
    Status.log("starting companion clients");
    nsdDiscovery.start();
    started = true;
  }

  public void stop() {
    Status.log("stopping compaion clients");
    nsdDiscovery.stop();
    clientsByServerName.values().forEach(NetworkClient::stop);
    clientsByServerName.clear();
    clientsByServerId.clear();
    started = false;
  }

  public boolean isStarted() {
    return started;
  }

  public void sendWaiting() {
    for (MessageScheduler scheduler : schedulersByServerId.values()) {
      Optional<ScheduledMessage> message = scheduler.nextWaiting();
      if (message.isPresent()) {
        NetworkClient client = clientsByServerId.get(message.get().getRecieverId());

        // If the client is not currently available, the message will automatically be
        // retried in one minute.
        if (client != null) {
          client.send(message.get().getData());
          Status.refreshServerConnection(message.get().getRecieverId());
        }
      }
    }
  }

  public void revoke(String id) {
    schedulersByServerId.values().stream().forEach(s -> s.revoke(id));
  }

  @Override
  public void nsdStarted(String name, InetAddress host, int port) {
    // If there is an existing client, stop it first.
    NetworkClient client = clientsByServerName.get(name);
    if (client != null) {
      client.stop();
    }

    // Start a client to communicate.
    client = new NetworkClient(companionContext);
    if (!client.start(host, port)) {
      client.stop();
      client.start(host, port);
    }

    clientsByServerName.put(name, client);

    // Send a welcome message to the server.
    Status.log("sending welcome message to server");
    client.send(CompanionMessageData.fromWelcome(companionContext, companionContext.settings().getAppId(),
        companionContext.settings().getNickname()));
  }

  @Override
  public void nsdStopped(String name) {
    NetworkClient client = clientsByServerName.remove(name);
    if (client != null) {
      clientsByServerId.remove(client.getServerId());
      client.stop();
    }
  }

  public List<CompanionMessage> receive() {
    List<CompanionMessage> messages = new ArrayList<>();

    for (NetworkClient client : clientsByServerName.values()) {
      for (Optional<CompanionMessage> message = client.receive();
           message.isPresent(); message = client.receive()) {

        if (message.get().getData().getType() == CompanionMessageData.Type.WELCOME) {
          clientsByServerId.put(message.get().getData().getWelcomeId(), client);
        }
        messages.add(message.get());
      }
    }

    return messages;
  }

  // Scheduling.
  public void schedule(String serverId, CompanionMessageData message) {
    setupScheduler(serverId).schedule(message);
  }

  public void schedule(CompanionMessageData message) {
    for (String id : clientsByServerId.keySet()) {
      schedule(id, message);
    }
  }

  public boolean isServerOnline(String serverId) {
    return clientsByServerId.containsKey(serverId);
  }

  public void ack(String recipientId, long messageId) {
    setupScheduler(recipientId).ack(messageId);
  }

  private MessageScheduler setupScheduler(String serverId) {
    if (!schedulersByServerId.containsKey(serverId)) {
      schedulersByServerId.put(serverId,
          new MessageScheduler(companionContext.settings(), serverId));
    }

    return schedulersByServerId.get(serverId);
  }

  // Testing.
  @VisibleForTesting
  public  Map<String, MessageScheduler> getSchedulersByServerId() {
    return schedulersByServerId;
  }

  @VisibleForTesting
  public Map<String, NetworkClient> getClientsByServerId() {
    return clientsByServerId;
  }

  @VisibleForTesting
  public Map<String, NetworkClient> getClientsByServerName() {
    return clientsByServerName;
  }
}
