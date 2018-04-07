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

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import net.ixitxachitls.companion.CompanionApplication;

/**
 * Application based nsd accessor.
 */
public class ApplicationNsdAccessor implements NsdAccessor {

  private final NsdManager manager;

  public ApplicationNsdAccessor(CompanionApplication application) {
    this.manager = (NsdManager) application.getSystemService(Context.NSD_SERVICE);
  }

  @Override
  public void register(NsdServiceInfo serviceInfo, int protocolType, NsdManager
      .RegistrationListener listener) {
    manager.registerService(serviceInfo, protocolType, listener);
  }

  @Override
  public void unregister(NsdManager.RegistrationListener listener) {
    manager.unregisterService(listener);
  }

  @Override
  public void discover(String type, int protocolType, NsdManager.DiscoveryListener listener) {
    manager.discoverServices(type, protocolType, listener);
  }

  @Override
  public void undiscover(NsdManager.DiscoveryListener listener) {
    manager.stopServiceDiscovery(listener);
  }

  @Override
  public void resolve(NsdServiceInfo info, NsdManager.ResolveListener listener) {
    manager.resolveService(info, listener);
  }
}
