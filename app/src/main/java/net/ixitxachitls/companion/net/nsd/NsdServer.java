/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

package net.ixitxachitls.companion.net.nsd;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.annotation.Nullable;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.net.CompanionMessage;
import net.ixitxachitls.companion.net.CompanionMessageData;
import net.ixitxachitls.companion.net.NetworkServer;

import java.util.Collection;
import java.util.List;

/**
 * The NSD component responsible for registering the companion nsd service and handling the server
 * communication.
 */
public class NsdServer {

  public static final String TYPE = "_companion._tcp";

  private final String name;
  private final NsdManager manager;
  private final NsdServiceInfo service;
  private final NetworkServer server;

  private boolean started = false;
  private @Nullable RegistrationListener registrationListener;

  public NsdServer(CompanionApplication application) {
    this.name = Settings.get().getNickname();
    this.manager = (NsdManager) application.getSystemService(Context.NSD_SERVICE);
    this.service = new NsdServiceInfo();
    this.server = new NetworkServer();

    this.service.setServiceName(this.name);
    this.service.setServiceType(TYPE);
  }

  public void start() {
    if (started) {
      return;
    }

    server.start();
    service.setHost(server.getAddress());
    service.setPort(server.getPort());
    registrationListener = new RegistrationListener();
    manager.registerService(service, NsdManager.PROTOCOL_DNS_SD, registrationListener);

    Status.log("nsd service registered");
    Status.addServerConnection(Settings.get().getAppId(), name);
    started = true;
  }

  public void stop() {
    if (!started) {
      return;
    }

    Status.log("unregistering nsd service " + name);
    Status.log("unregistering nsd service " + name);
    manager.unregisterService(registrationListener);
    registrationListener = null;
    server.stop();
    Status.removeServerConnection(name);

    started = false;
  }

  public void send(String recieverId, long messageId, CompanionMessageData data) {
    server.send(recieverId, messageId, data);
  }

  public List<CompanionMessage> receive() {
    return server.receive();
  }

  public String getNameForId(String id) {
    return server.getNameForId(id);
  }

  public boolean isStarted() {
    return started;
  }

  public Collection<String> connectedIds() {
    return server.connectedIds();
  }

  private class RegistrationListener implements  NsdManager.RegistrationListener {
    @Override
    public void onServiceRegistered(NsdServiceInfo info) {
      Status.log("nsd service " + name + " registered");
    }

    @Override
    public void onRegistrationFailed(NsdServiceInfo info, int errorCode) {
      Status.log("registering of nsd service " + name + " failed: " + errorCode);
    }

    @Override
    public void onServiceUnregistered(NsdServiceInfo info) {
      Status.log("nsd service " + name + " unregistered.");
    }

    @Override
    public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
      Status.log("unregistering nsd service " + name + " failed: " + errorCode);
    }
  }
}
