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
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.data.dynamics.XpAward;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
      setupScheduler(message.getRecieverId());
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
      for (Optional<ScheduledMessage> message = scheduler.nextWaiting();
           message.isPresent();
           message = scheduler.nextWaiting()) {
        if (message.isPresent()) {
          Log.d(TAG, "sending " + message);
          ensureServer();
          // The messasge is already marked as sent or pending (depending on ack).
          // Thus, even if sending fails, we don't need to do anything. Pending message
          // will automatically be resent and sent messages can be safely ignored.
          server.get().send(message.get().getRecieverId(), message.get().getMessageId(),
              message.get().getData());
          application.updateClientConnection(getRecipientName(message.get().getRecieverId()));
        }
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
    CompanionMessageData data = CompanionMessageData.from(campaign);

    schedule(clientIds(campaign), data);

    Log.d(TAG, "published campaign " + campaign.getName());
    application.status("sent campaign " + campaign.getName());
  }

  public void republish(String recipientId) {
    for (Campaign campaign : Campaigns.getCampaigns()) {
      if (campaign.isLocal() && campaign.isPublished()) {
        schedule(recipientId, CompanionMessageData.from(campaign));

        for (Character character : campaign.getCharacters()) {
          update(character, recipientId);
        }
      }
    }
  }

  public void republishLocalCharacters(String recipientId) {
    for (Campaign campaign : Campaigns.getCampaigns()) {
      for (Character character : campaign.getCharacters()) {
        if (character.isLocal()) {
          update(character, recipientId);
        }
      }
    }
  }

  public void delete(Campaign campaign) {
    CompanionMessageData data = CompanionMessageData.fromDelete(campaign);

    schedule(clientIds(campaign), data);

    Log.d(TAG, "deleted campaign " + campaign.getName());
    application.status("sent campaign deletion " + campaign.getName());
  }

  public void delete(Character character) {
    Optional<Campaign> campaign = Campaigns.getCampaign(character.getCampaignId());
    if (campaign.isPresent()) {
      CompanionMessageData data = CompanionMessageData.fromDelete(character);
      schedule(clientIds(campaign.get()), data);
    }
  }

  public void update(Character updatedCharacter, String characterClientId) {
    CompanionMessageData data = CompanionMessageData.from(updatedCharacter);
    for (Character character
        : Characters.getCampaignCharacters(updatedCharacter.getCampaignId()).getValue()) {
      // Only update the character on clients that don't own it.
      if (!characterClientId.equals(character.getServerId())) {
        schedule(character.getServerId(), data);
      }
    }
  }

  public void update(String clientId, Image image) {
    CompanionMessageData data = CompanionMessageData.from(image);

    // Figure out which campaign this image belongs to.
    Optional<Character> imageCharacter = Characters.getCharacter(image.getId()).getValue();
    if (imageCharacter.isPresent()) {
      for (Character character :
          Characters.getCampaignCharacters(imageCharacter.get().getCampaignId()).getValue()) {
        // Only update the character on clients that don't own it.
        if (!clientId.equals(character.getServerId())) {
          schedule(character.getServerId(), data);
        }
      }
    }
  }

  public void awardXp(Campaign campaign, Character character, int xp) {
    CompanionMessageData data = CompanionMessageData.from(
        new XpAward(character.getCharacterId(), campaign.getCampaignId(), xp));
    schedule(character.getServerId(), data);
  }

  public void unpublish(Campaign campaign) {
    if (name.isPresent() && !Campaigns.hasAnyPublished() && isIdle()) {
      //stop();
    }
  }

  public void ack(String recipientId, long messageId) {
    setupScheduler(recipientId).ack(messageId);
  }

  private boolean isIdle() {
    for (MessageScheduler scheduler : schedulersByRecpient.values()) {
      if (!scheduler.isIdle()) {
        return false;
      }
    }

    return true;
  }

  private Set<String> clientIds(Campaign campaign) {
    Set<String> ids = new HashSet<>();
    ids.addAll(clientIds());

    for (Character character
        : Characters.getCampaignCharacters(campaign.getCampaignId()).getValue()) {
      ids.add(character.getServerId());
    }

    return ids;
  }

  private Collection<String> clientIds() {
    if (server.isPresent()) {
      return server.get().connectedIds();
    }

    return Collections.emptyList();
  }

  private void schedule(Iterable<String> recipients, CompanionMessageData data) {
    ensureServer();
    for (String recipient : recipients) {
      setupScheduler(recipient).schedule(data);
    }
  }

  private void schedule(String recipientId, CompanionMessageData data) {
    setupScheduler(recipientId).schedule(data);
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

  public void start() {
    if (Campaigns.hasAnyPublished()) {
      ensureServer();
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
      application.status("service registered");
    }

    @Override
    public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
      application.status("registering of service failed!");
    }

    @Override
    public void onServiceUnregistered(NsdServiceInfo arg0) {
      application.status("service unregistered.");
    }

    @Override
    public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
      application.status("unregistering service failed.");
    }
  }
}
