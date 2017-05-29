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
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.util.Ids;
import net.ixitxachitls.companion.util.Misc;

import java.util.ArrayList;
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
  private Map<NsdServiceInfo, CompanionClient> clientByService = new ConcurrentHashMap<>();
  private Map<String, String> nameById = new ConcurrentHashMap<>();

  private CompanionSubscriber(Context context, CompanionApplication application) {
    this.application = application;
    this.manager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
  }

  public void publish(Character character) {
    String id = Ids.extractServerId(character.getCampaignId());
    CompanionClient client = clientById.get(id);
    if (client != null) {
      client.send(Data.CompanionMessageProto.newBuilder()
          .setCharacter(character.toProto())
          .build());
      Log.d(TAG, "character " + character.getName() + " published.");
      application.status("sent character " + character.getName());
    } else {
      Log.d(TAG, "cannot find client with id '" + id + "'");
    }
  }

  public void publishImage(Data.CompanionMessageProto.Image image) {
    CompanionClient client = clientById.get(Ids.extractServerId(image.getCampaignId()));
    if (client != null) {
      client.send(Data.CompanionMessageProto.newBuilder()
          .setImage(image)
          .build());
      Log.d(TAG, "image for " + image.getType() + " " + image.getId() + " published.");
      application.status("sent image for " + image.getType() + " " + image.getId());
    } else {
      Log.d(TAG, "cannot find client with id '" + image.getCampaignId() + "'");
    }
  }

  // TODO: Determine if singleton works and is necessary.
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
    for (CompanionClient client : clientByService.values()) {
      client.stop();
    }

    clientByService.clear();
    clientById.clear();
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
      Log.e(TAG, "service lost" + service);
      application.status("service lost " + service);
      CompanionClient client = clientByService.remove(service);
      for (String id : clientById.keySet()) {
        if (clientById.get(id) == client) {
          clientById.remove(id);
          break;
        }
      }
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
      clientByService.put(serviceInfo, client);

      // Send a welcome message to the server.
      application.status("sending welcome message to server");
      client.send(Data.CompanionMessageProto.newBuilder()
          .setWelcome(Data.CompanionMessageProto.Welcome.newBuilder()
              .setId(Settings.get().getAppId())
              .setName(Settings.get().getNickname())
              .build())
          .build());
    }
  }

  public List<CompanionMessage> receive() {
    List<CompanionMessage> messages = new ArrayList<>();

    for (CompanionClient client : clientByService.values()) {
      for (Optional<Data.CompanionMessageProto> message = client.receive();
           message.isPresent(); message = client.receive()) {
        if (message.isPresent()) {
          String id;
          String name;
          if (message.get().hasWelcome()) {
            id = message.get().getWelcome().getId();
            name = message.get().getWelcome().getName();
            clientById.put(id, client);
            nameById.put(id, name);
          } else {
            id = keyForClient(client);
            name = nameById.getOrDefault(id, "(unknown)");
          }
          messages.add(new CompanionMessage(id, name, message.get()));
        }
      }
    }

    return messages;
  }

  private String keyForClient(CompanionClient client) {
    for (Map.Entry<String, CompanionClient> entry : clientById.entrySet()) {
      if (entry.getValue() == client) {
        return entry.getKey();
      }
    }

    return "(unknown)";
  }
}
