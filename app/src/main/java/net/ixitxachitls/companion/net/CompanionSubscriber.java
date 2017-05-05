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

import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.util.Misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Subscriber for campaign information.
 */
public class CompanionSubscriber {
  private static final String TAG = "Subscriber";
  private static CompanionSubscriber singleton;

  private final NsdManager manager;
  private @Nullable NsdManager.ResolveListener resolveListener;
  private Map<String, CompanionClient> clientById = new HashMap<>();
  private Map<String, String> nameById = new HashMap<>();

  private CompanionSubscriber(Context context) {
    this.manager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
  }

  public void publish(Character character) {
    String id = extractServerId(character.getCampaignId());
    CompanionClient client = clientById.get(id);
    if (client != null) {
      client.send(Data.CompanionMessageProto.newBuilder()
          .setCharacter(character.toProto())
          .build());
      Log.d(TAG, "character " + character.getName() + " published.");
    } else {
      Log.d(TAG, "cannot find client with id '" + id + "'");
    }
  }

  public static String extractServerId(String id) {
    return id.replaceAll("-\\d+-remote", "");
  }

  // TODO: Determine if singleton works and is necessary.
  public static CompanionSubscriber init(Context context) {
    singleton = new CompanionSubscriber(context);
    return singleton;
  }

  public static CompanionSubscriber get() {
    return singleton;
  }

  public boolean isServerActive(String serverId) {
    return clientById.containsKey(serverId);
  }

  public boolean isOnline() {
    return !clientById.isEmpty();
  }

  public void start() {
    Log.d(TAG, "Trying to find a companion");
    manager.discoverServices(CompanionPublisher.TYPE, NsdManager.PROTOCOL_DNS_SD,
        new CompanionDiscoveryListener());
  }

  public void stop() {
    for (CompanionClient client : clientById.values()) {
      client.stop();
    }

    clientById.clear();
  }

  private class CompanionDiscoveryListener implements NsdManager.DiscoveryListener {
    private boolean started = false;

    @Override
    public void onDiscoveryStarted(String regType) {
      Log.d(TAG, "Service discovery started");
      started = true;
    }

    @Override
    public void onServiceFound(NsdServiceInfo service) {
      Log.d(TAG, "Service discovery success: " + service);
      if (!service.getServiceType().startsWith(CompanionPublisher.TYPE)) {
        Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
      } else  {
        Log.d(TAG, "resolving service " + service);
        resolveListener = new CompanionResolveListener();
        manager.resolveService(service, resolveListener);
      }
    }

    @Override
    public void onServiceLost(NsdServiceInfo service) {
      Log.e(TAG, "service lost" + service);
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
      Log.i(TAG, "Discovery stopped: " + serviceType);
    }

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
      Log.e(TAG, "Discovery failed: Error code:" + errorCode);
      if (started) {
        manager.stopServiceDiscovery(this);
      }
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
      Log.e(TAG, "Discovery failed: Error code:" + errorCode);
      manager.stopServiceDiscovery(this);
    }
  }

  private class CompanionResolveListener implements NsdManager.ResolveListener {

    @Override
    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
      Log.e(TAG, "Resolve failed" + errorCode);
    }

    @Override
    public void onServiceResolved(NsdServiceInfo serviceInfo) {
      Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

      if (!Misc.onEmulator() // Allow to find itself on emulator.
          && serviceInfo.getServiceName().endsWith(Settings.get().getNickname())) {
        Log.d(TAG, "Same Nickname, ignored.");
        return;
      }

      // Start a client to communicate.
      CompanionClient client = new CompanionClient(serviceInfo.getHost(), serviceInfo.getPort());
      client.start();
      clientById.put("startup-" + clientById.keySet().size(), client);

      // Send a welcome message to the server.
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

    for (Map.Entry<String, CompanionClient> client : clientById.entrySet()) {
      Optional<Data.CompanionMessageProto> message = client.getValue().receive();
      if (message.isPresent()) {
        String id;
        String name;
        if (message.get().hasWelcome()) {
          id = message.get().getWelcome().getId();
          name = message.get().getWelcome().getName();
          clientById.remove(client.getKey());
          clientById.put(id, client.getValue());
          nameById.put(id, name);
        } else {
          id = client.getKey();
          name = nameById.getOrDefault(id, "(unknown)");
        }
        messages.add(new CompanionMessage(id, name, message.get()));
      }
    }

    return messages;
  }
}
