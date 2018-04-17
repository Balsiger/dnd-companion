/*
 * Copyright (c) 2017-2018 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Tabletop Companion.
 *
 * The Tabletop Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Tabletop Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.net.nsd;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Fake accessor for nsd.
 */
public class FakeNsdAccessor implements NsdAccessor {

  private final Map<NsdManager.RegistrationListener, Registration> registrations = new HashMap<>();
  private final Map<NsdManager.DiscoveryListener, Discovery> discoveryListeners = new HashMap<>();

  public FakeNsdAccessor() {
  }

  @Override
  public void register(NsdServiceInfo info, int protocol,
                       NsdManager.RegistrationListener listener) {
    registrations.put(listener, new Registration(info, protocol));
    listener.onServiceRegistered(info);

    notifyDiscoveryListeners();
  }

  @Override
  public void unregister(NsdManager.RegistrationListener listener) {
    Registration registration = registrations.remove(listener);
    listener.onServiceUnregistered(registration.info);

    for (Map.Entry<NsdManager.DiscoveryListener, Discovery> discovery
        : discoveryListeners.entrySet()) {
      if (discovery.getValue().protocol == registration.protocol
          && discovery.getValue().type.equals(registration.info.getServiceType())) {
        discovery.getKey().onServiceLost(registration.info);
      }
    }
  }

  @Override
  public void discover(String type, int protocol, NsdManager.DiscoveryListener listener) {
    listener.onDiscoveryStarted(type);
    discoveryListeners.put(listener, new Discovery(type, protocol));

    notifyDiscoveryListeners();
  }

  @Override
  public void undiscover(NsdManager.DiscoveryListener listener) {
    listener.onDiscoveryStopped(discoveryListeners.get(listener).type);
    discoveryListeners.remove(listener);
  }

  @Override
  public void resolve(NsdServiceInfo info, NsdManager.ResolveListener listener) {
    listener.onServiceResolved(info);
  }

  private void notifyDiscoveryListeners() {
    for (Registration registration : registrations.values()) {
      for (Map.Entry<NsdManager.DiscoveryListener, Discovery> discovery
          : discoveryListeners.entrySet()) {
        if (discovery.getValue().type.equals(registration.info.getServiceType())
            && discovery.getValue().protocol == registration.protocol) {
          discovery.getKey().onServiceFound(registration.info);
        }
      }
    }
  }

  private class Registration {
    private final NsdServiceInfo info;
    private final int protocol;

    public Registration(NsdServiceInfo info, int protocol) {
      this.info = info;
      this.protocol = protocol;
    }
  }

  private class Discovery {
    private final String type;
    private final int protocol;

    public Discovery(String type, int protocol) {
      this.type = type;
      this.protocol = protocol;
    }
  }
}
