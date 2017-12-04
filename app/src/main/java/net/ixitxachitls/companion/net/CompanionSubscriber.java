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

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.util.Ids;
import net.ixitxachitls.companion.util.Misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Subscriber for campaign information.
 */
public class CompanionSubscriber {
  private static final String TAG = "Subscriber";
  private static CompanionSubscriber singleton;

  private CompanionApplication application;
  private final NsdManager manager;
  private @Nullable NsdManager.ResolveListener resolveListener;
  private Map<String, CompanionClient> clientById = new ConcurrentHashMap<>();
  private Map<String, CompanionClient> clientByName = new ConcurrentHashMap<>();
  private final Map<String, MessageScheduler> schedulersByRecpient = new HashMap<>();

  private CompanionSubscriber(Context context, CompanionApplication application) {
    this.application = application;
    this.manager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
  }

  public void sendWaiting() {
    for (MessageScheduler scheduler : schedulersByRecpient.values()) {
      Optional<ScheduledMessage> message = scheduler.nextWaiting();
      if (message.isPresent()) {
        CompanionClient client = clientById.get(message.get().getRecieverId());
        // If the client is not currently available, the message will automatically be
        // retried in one minute.
        if (client != null) {
          Log.d(TAG, "sending " + message);
          client.send(message.get().getData());
          application.status("sent " + message);
          application.updateClientConnection(Settings.get().getNickname());
        }
      }
    }
  }

  public List<String> getSenderList(String id) {
    List<String> list = new ArrayList<>();

    for (MessageScheduler scheduler : schedulersByRecpient.values()) {
      list.addAll(scheduler.scheduledMessages(id));
    }

    return list;
  }

  public void publish(Character character) {
    String id = Ids.extractServerId(character.getCampaignId());
    setupScheduler(id).schedule(CompanionMessageData.from(character));
  }

  public void delete(Character character) {
    String id = Ids.extractServerId(character.getCampaignId());
    setupScheduler(id).schedule(CompanionMessageData.fromDelete(character));
  }

  public void publish(String campaignId, Image image) {
    CompanionClient client = clientById.get(Ids.extractServerId(campaignId));
    if (client != null) {
      client.send(CompanionMessageData.from(image));
      Log.d(TAG, "image for " + image.getType() + " " + image.getId() + " published.");
      application.status("sent image for " + image.getType() + " " + image.getId());
    } else {
      Log.d(TAG, "cannot find client with id '" + campaignId + "'");
    }
  }

  public void sendWelcome() {
    for (CompanionClient client : clientById.values()) {
      client.send(CompanionMessageData.fromWelcome(Settings.get().getAppId(),
          Settings.get().getNickname()));
    }
  }

  public static CompanionSubscriber init(Context context, CompanionApplication application) {
    singleton = new CompanionSubscriber(context, application);
    return singleton;
  }

  public static CompanionSubscriber get() {
    return singleton;
  }

  public void start() {
    Log.d(TAG, "Trying to find a companion");
    manager.discoverServices(CompanionPublisher.TYPE, NsdManager.PROTOCOL_DNS_SD,
        new CompanionDiscoveryListener());
  }

  public void stop() {
    for (CompanionClient client : clientByName.values()) {
      client.stop();
    }

    clientByName.clear();
    clientById.clear();
  }

  public boolean isOnline(String id) {
    return clientById.containsKey(id);
  }

  public boolean isOnline(Campaign campaign) {
    return isOnline(campaign.getServerId());
  }

  private class CompanionDiscoveryListener implements NsdManager.DiscoveryListener {
    private boolean started = false;

    @Override
    public void onDiscoveryStarted(String regType) {
      Log.d(TAG, "Service discovery started");
      application.status("service discovery started");
      started = true;
    }

    @Override
    public void onServiceFound(NsdServiceInfo service) {
      Log.d(TAG, "Service discovery success: " + service);
      if (!service.getServiceType().startsWith(CompanionPublisher.TYPE)) {
        Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
      } else  {
        Log.d(TAG, "resolving service " + service);
        application.status("found service " + service);
        resolveListener = new CompanionResolveListener();
        manager.resolveService(service, resolveListener);
      }
    }

    @Override
    public void onServiceLost(NsdServiceInfo service) {
      Log.e(TAG, "service lost" + service.getServiceName());
      application.status("service lost " + service.getServiceName());
      CompanionClient client = clientByName.remove(service.getServiceName());
      for (String id : clientById.keySet()) {
        if (clientById.get(id).equals(client)) {
          clientById.remove(id);
          break;
        }
      }

      application.refresh();
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
      Log.i(TAG, "Discovery stopped: " + serviceType);
      application.status("discovery stopped " + serviceType);
    }

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
      Log.e(TAG, "Discovery failed: Error code:" + errorCode);
      application.status("start discovery failed " + errorCode);
      if (started) {
        manager.stopServiceDiscovery(this);
      }
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
      Log.e(TAG, "Discovery failed: Error code:" + errorCode);
      application.status("stop discovery failed " + errorCode);
      manager.stopServiceDiscovery(this);
    }
  }

  private class CompanionResolveListener implements NsdManager.ResolveListener {

    @Override
    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
      Log.e(TAG, "Resolve failed" + errorCode);
      application.status("service resolve failed " + errorCode);
    }

    @Override
    public void onServiceResolved(NsdServiceInfo serviceInfo) {
      Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
      application.status("service resolved succeeded for " + serviceInfo);

      if (!Misc.onEmulator() // Allow to find itself on emulator.
          && serviceInfo.getServiceName().endsWith(Settings.get().getNickname())) {
        Log.d(TAG, "Same Nickname, ignored.");
        return;
      }

      // Start a client to communicate.
      application.status("service connected for " + serviceInfo);
      CompanionClient client = new CompanionClient(serviceInfo.getHost(), serviceInfo.getPort());
      client.start();
      clientByName.put(serviceInfo.getServiceName(), client);

      // Send a welcome message to the server.
      application.status("sending welcome message to server");
      client.send(CompanionMessageData.fromWelcome(Settings.get().getAppId(),
          Settings.get().getNickname()));
    }
  }

  public List<CompanionMessage> receive() {
    List<CompanionMessage> messages = new ArrayList<>();

    for (CompanionClient client : clientByName.values()) {
      for (Optional<CompanionMessage> message = client.receive();
           message.isPresent(); message = client.receive()) {

        if (message.isPresent()) {
          if (message.get().getData().getType() == CompanionMessageData.Type.WELCOME) {
            clientById.put(message.get().getData().getWelcomeId(), client);
          }
          messages.add(message.get());
        }
      }
    }

    return messages;
  }

  public void sendAck(String recipientId, long messageId) {
    setupScheduler(recipientId).schedule(CompanionMessageData.fromAck(messageId));
  }

  private String keyForClient(CompanionClient client) {
    for (Map.Entry<String, CompanionClient> entry : clientById.entrySet()) {
      if (entry.getValue() == client) {
        return entry.getKey();
      }
    }

    return "(unknown)";
  }

  private MessageScheduler setupScheduler(String serverId) {
    MessageScheduler scheduler = schedulersByRecpient.get(serverId);
    if (scheduler == null) {
      scheduler = new MessageScheduler(serverId);
      schedulersByRecpient.put(serverId, scheduler);
    }

    return scheduler;
  }
}
