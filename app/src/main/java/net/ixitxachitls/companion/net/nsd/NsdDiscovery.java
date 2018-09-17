/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

package net.ixitxachitls.companion.net.nsd;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.annotation.Nullable;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.util.Misc;

import java.net.InetAddress;

/**
 * The component responsible for nsd service discovery.
 */
public class NsdDiscovery {

  private final User me;
  private final NsdAccessor nsdAccessor;
  private final NsdCallback nsdCallback;
  private @Nullable NsdManager.DiscoveryListener discoveryListener;
  private @Nullable NsdManager.ResolveListener resolveListener;

  public NsdDiscovery(User user, NsdAccessor nsdAccessor, NsdCallback nsdCallback) {
    this.me = user;
    this.nsdAccessor = nsdAccessor;
    this.nsdCallback = nsdCallback;
  }

  public void start() {
    Status.log("starting NSD client");
    discoveryListener = new DiscoveryListener();
    nsdAccessor.discover(NsdServer.TYPE, NsdManager.PROTOCOL_DNS_SD,
        discoveryListener);
  }

  public void stop() {
    Status.log("stopping NSD client");
    nsdAccessor.undiscover(discoveryListener);
    discoveryListener = null;
  }

  public interface NsdCallback {
    void nsdStarted(String name, InetAddress host, int port);
    void nsdStopped(String name);
  }

  private class DiscoveryListener implements NsdManager.DiscoveryListener {
    private boolean started = false;

    @Override
    public void onDiscoveryStarted(String regType) {
      Status.log("service discovery started");
      started = true;
    }

    @Override
    public void onServiceFound(NsdServiceInfo service) {
      Status.log("service discovered: " + service.getServiceName());
      if (!service.getServiceType().startsWith(NsdServer.TYPE)) {
        Status.log("unknown service type ignored: " + service.getServiceType());
      } else if(!Misc.onEmulator() && service.getServiceName().equals(me.getNickname())) {
        Status.log("own service ignored");
      } else  {
        Status.log("found service " + service.getServiceName());
        resolveListener = new ResolveListener();
        nsdAccessor.resolve(service, resolveListener);
      }
    }

    @Override
    public void onServiceLost(NsdServiceInfo service) {
      Status.log("service " + service.getServiceName() + " lost");
      Status.removeClientConnection(service.getServiceName());
      nsdCallback.nsdStopped(service.getServiceName());
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
      Status.log("discovery stopped for " + serviceType);
    }

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
      Status.log("start discovery failed with error " + errorCode);
      if (started) {
        nsdAccessor.undiscover(this);
      }
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
      Status.log("stop discovery failed with error " + errorCode);
      nsdAccessor.undiscover(this);
    }
  }

  private class ResolveListener implements NsdManager.ResolveListener {

    @Override
    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
      Status.log("service resolve failed with error " + errorCode);
    }

    @Override
    public void onServiceResolved(NsdServiceInfo serviceInfo) {
      Status.log("service " + serviceInfo.getServiceName() + " connected");

      if (!Misc.onEmulator() // Allow to find itself on emulator.
          && serviceInfo.getServiceName().endsWith(me.getNickname())) {
        Status.log("same nickname, ignored.");
        return;
      }

      nsdCallback.nsdStarted(serviceInfo.getServiceName(), serviceInfo.getHost(),
          serviceInfo.getPort());
    }
  }
}
