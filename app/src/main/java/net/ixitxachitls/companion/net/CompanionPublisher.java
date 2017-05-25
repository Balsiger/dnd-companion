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

import com.google.common.base.Strings;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.proto.Data;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

/**
 * Class to publish data to the local network.
 */
public class CompanionPublisher {

  public static final String TYPE = "_companion._tcp";
  public static final String TAG = "Publisher";
  private static CompanionPublisher singleton;

  private final NsdManager manager;
  private CompanionApplication application;

  private @Nullable String name;
  private @Nullable NsdServiceInfo service;
  private @Nullable NsdManager.RegistrationListener registrationListener;
  private @Nullable CompanionServer server;

  private CompanionPublisher(Context context, CompanionApplication application) {
    this.manager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    this.application = application;
  }

  public static CompanionPublisher init(Context context, CompanionApplication application) {
    singleton = new CompanionPublisher(context, application);
    return singleton;
  }

  public static CompanionPublisher get() {
    return singleton;
  }

  public boolean isOnline() {
    return server != null;
  }

  public void publish(Campaign campaign) {
    ensureServerStarted();
    server.sendAll(new CompanionMessage(Settings.get().getAppId(), Settings.get().getName(),
        Data.CompanionMessageProto.newBuilder()
            .setCampaign(campaign.toProto())
            .build()));

    Log.d(TAG, "published campaign " + campaign.getName());
    application.status("sent campaign " + campaign.getName());
  }

  private void ensureServerStarted() {
    if (server == null) {
      server = new CompanionServer();
      if (server.start()) {
        application.status("starting server");
        name = Settings.get().getNickname();
        register(name, server.getAddress(), server.getPort());
        application.serverStarted();
      }
    }
  }

  public void republish(List<Campaign> campaigns, String clientId) {
    ensureServerStarted();
    for (Campaign campaign : campaigns) {
      if (campaign.isDefault() || !campaign.isPublished()) {
        continue;
      }

      Log.d(TAG, "republished campaign " + campaign.getName());
      application.status("sent (republish) campaign " + campaign.getName());
      server.send(clientId, new CompanionMessage(Settings.get().getAppId(),
          Settings.get().getNickname(), Data.CompanionMessageProto.newBuilder()
          .setCampaign(campaign.toProto()).build()));
    }
  }

  public void unpublish(Campaign campaign) {
    if (!Strings.isNullOrEmpty(name) && !Campaigns.local().hasAnyPublished()) {
      stop();
    }
  }

  private void register(String name, InetAddress address, int port) {
    this.service  = new NsdServiceInfo();
    this.service.setServiceName(name);
    this.service.setServiceType(TYPE);
    this.service.setHost(address);
    this.service.setPort(port);

    Log.d(TAG, "registering " + service);
    application.status("registering service " + service);
    registrationListener = new CompanionRegistrationListener();
    manager.registerService(service, NsdManager.PROTOCOL_DNS_SD, registrationListener);
  }

  public void stop() {
    if (name != null) {
      Log.d(TAG, "unregistering " + service);
      application.status("unregistering service " + service);
      manager.unregisterService(registrationListener);
      registrationListener = null;
      server.stop();
      server = null;
      application.serverStopped();
    }
  }

  public List<CompanionMessage> receive() {
    if (server == null) {
      return Collections.emptyList();
    }

    return server.receive();
  }

  private class CompanionRegistrationListener implements  NsdManager.RegistrationListener {
    @Override
    public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
      name = NsdServiceInfo.getServiceName();
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
