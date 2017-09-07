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
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.proto.Data;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to publish data to the local network.
 */
public class CompanionPublisher {

  public static final String TAG = "Publisher";
  public static final String TYPE = "_companion._tcp";

  private static CompanionPublisher singleton;

  private final CompanionApplication application;
  private Optional<CompanionServer> server = Optional.absent();
  private Optional<String> name = Optional.absent();
  private final NsdManager manager;
  private @Nullable NsdServiceInfo service = null;
  private @Nullable CompanionRegistrationListener registrationListener;
  private final Map<String, MessageScheduler> schedulersByRecpient = new HashMap<>();

  private CompanionPublisher(CompanionApplication application) {
    this.application = application;
    this.manager = (NsdManager) application.getSystemService(Context.NSD_SERVICE);

    // Setup stored messages.
    for (ScheduledMessage message
        : ScheduledMessages.get().getMessagesBySender(Settings.get().getAppId())) {
      setupScheduler(message.getRecipient());
    }
  }

  public static CompanionPublisher init(CompanionApplication application) {
    singleton = new CompanionPublisher(application);
    return singleton;
  }

  public static CompanionPublisher get() {
    return singleton;
  }

  public void sendWaiting() {
    for (MessageScheduler scheduler : schedulersByRecpient.values()) {
      Optional<ScheduledMessage> message = scheduler.nextWaiting();
      if (message.isPresent()) {
        Log.d(TAG, "sending " + message);
        ensureServer();
        server.get().send(message.get().getRecipient(), message.get().toMessage());
        application.updateClientConnection(getRecipientName(message.get().getRecipient()));
      }
    }
  }

  public String getRecipientName(String id) {
    if (server.isPresent()) {
      return server.get().getNameForId(id);
    }

    return id;
  }

  public boolean isOnline() {
    return server.isPresent();
  }

  public List<String> getSenderList(String id) {
    List<String> list = new ArrayList<>();

    for (MessageScheduler scheduler : schedulersByRecpient.values()) {
      list.addAll(scheduler.scheduledMessages(id));
    }

    return list;
  }

  public void publish(Campaign campaign) {
    Data.CompanionMessageProto proto = Data.CompanionMessageProto.newBuilder()
        .setCampaign(campaign.toProto())
        .setSender(Settings.get().getAppId())
        .build();

    for (Character character : Characters.getAllCharacters(campaign.getCampaignId())) {
      schedule(character.getServerId(), proto);
    }

    Log.d(TAG, "published campaign " + campaign.getName());
    application.status("sent campaign " + campaign.getName());
  }

  public void update(Character updatedCharacter, String characterClientId) {
    Data.CompanionMessageProto proto = Data.CompanionMessageProto.newBuilder()
        .setCharacter(updatedCharacter.toProto())
        .setSender(Settings.get().getAppId())
        .build();

    for (Character character
        : Characters.getAllCharacters(updatedCharacter.getCampaignId())) {
      // Only update the character on clients that don't own it.
      if (!characterClientId.equals(character.getServerId())) {
        schedule(character.getServerId(), proto);
      }
    }
  }

  public void update(Data.CompanionMessageProto.Image image, String characterClientId) {
    Data.CompanionMessageProto proto = Data.CompanionMessageProto.newBuilder()
        .setImage(image)
        .setSender(Settings.get().getAppId())
        .build();

    for (Character character
        : Characters.getAllCharacters(image.getCampaignId())) {
      // Only update the character on clients that don't own it.
      if (!characterClientId.equals(character.getServerId())) {
        schedule(character.getServerId(), proto);
      }
    }
  }

  public void unpublish(Campaign campaign) {
    /*
    if (!Strings.isNullOrEmpty(name) && !Campaigns.hasAnyPublished()) {
      stop();
    }

    // also send a deletion request to clients
    */
  }

  private void schedule(String recipientId, Data.CompanionMessageProto proto) {
    setupScheduler(recipientId).schedule(proto);
  }

  private MessageScheduler setupScheduler(String recipientId) {
    MessageScheduler scheduler = schedulersByRecpient.get(recipientId);
    if (scheduler == null) {
      scheduler = new MessageScheduler(recipientId);
      schedulersByRecpient.put(recipientId, scheduler);
    }

    return scheduler;
  }

  public void ensureServer() {
    if (!server.isPresent()) {
      server = Optional.of(new CompanionServer(application));
    }

    if (!name.isPresent() && server.get().start()) {
      application.status("starting server");
      name = Optional.of(Settings.get().getNickname());
      register(name.get(), server.get().getAddress(), server.get().getPort());
      application.serverStarted();
    }
  }

  public void stop() {
    if (name.isPresent()) {
      Log.d(TAG, "unregistering " + service.getServiceName());
      application.status("unregistering service " + service.getServiceName());
      manager.unregisterService(registrationListener);
      registrationListener = null;
      server.get().stop();
      server = Optional.absent();
      name = Optional.absent();
      application.serverStopped();
    }
  }

  public List<CompanionMessage> receive() {
    if (server.isPresent()) {
      return server.get().receive();
    }

    return Collections.emptyList();
  }

  public void ack(String remoteId, long messageId) {
    /*
    server.send(remoteId, new CompanionMessage(Data.CompanionMessageProto.newBuilder()
        .setAck(messageId)
        .build()));
        */
  }

  private void register(String name, InetAddress address, int port) {
    this.service  = new NsdServiceInfo();
    this.service.setServiceName(name);
    this.service.setServiceType(TYPE);
    this.service.setHost(address);
    this.service.setPort(port);

    Log.d(TAG, "registering " + service.getServiceName());
    application.status("registering service " + service.getServiceName());
    registrationListener = new CompanionRegistrationListener();
    manager.registerService(service, NsdManager.PROTOCOL_DNS_SD, registrationListener);
  }

  private class CompanionRegistrationListener implements  NsdManager.RegistrationListener {
    @Override
    public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
      name = Optional.of(NsdServiceInfo.getServiceName());
    }

    @Override
    public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
    }

    @Override
    public void onServiceUnregistered(NsdServiceInfo arg0) {
    }

    @Override
    public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
    }
  }
}
