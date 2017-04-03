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

import net.ixitxachitls.companion.data.Campaign;
import net.ixitxachitls.companion.data.Settings;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class to publish data to the local network.
 */
public class CompanionPublisher {

  public static final String TYPE = "_companion._tcp";
  private static CompanionPublisher singleton;

  private final NsdManager manager;

  private @Nullable String name;
  private @Nullable NsdServiceInfo service;
  private @Nullable NsdManager.RegistrationListener registrationListener;
  private @Nullable CompanionServer server;
  private @Nullable List<Campaign> campaigns = new ArrayList<>();

  private CompanionPublisher(Context context) {
    this.manager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
  }

  public static CompanionPublisher init(Context context) {
    singleton = new CompanionPublisher(context);
    return singleton;
  }

  public static CompanionPublisher get() {
    return singleton;
  }

  public void publish(Campaign campaign) {
    name = Settings.get().getNickname();

    if (server == null) {
      server = new CompanionServer();
      if (server.start()) {
        register(name, server.getAddress(), server.getPort());
      }
    }

    Log.d("Publisher", "published campaign " + campaign.getName());
    campaigns.add(campaign);
  }

  public void unpublish(Campaign campaign) {
    campaigns.remove(campaign);
    if (campaigns.isEmpty() && !Strings.isNullOrEmpty(name)) {
      manager.unregisterService(registrationListener);
      registrationListener = null;
      server.stop();
      server = null;
    }
  }

  private void register(String name, InetAddress address, int port) {
    this.service  = new NsdServiceInfo();
    this.service.setServiceName(name);
    this.service.setServiceType(TYPE);
    this.service.setHost(address);
    this.service.setPort(port);

    Log.d("Publisher", "registering " + service);
    registrationListener = new CompanionRegistrationListener();
    manager.registerService(service, NsdManager.PROTOCOL_DNS_SD, registrationListener);
  }

  public void stop() {
    if (name != null) {
      Log.d("Publisher", "unregistering " + service);
      manager.unregisterService(registrationListener);
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
