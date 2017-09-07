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

package net.ixitxachitls.companion.ui.activities;

import android.support.v7.app.AppCompatActivity;

/**
 * Base class for all activities in the companion.
 */
public abstract class CompanionActivity extends AppCompatActivity {
  public abstract void status(String message);
  public abstract void refresh();
  public abstract void heartbeat();
  public abstract void addClientConnection(String id, String name);
  public abstract void updateClientConnection(String name);
  public abstract void addServerConnection(String id, String name);
  public abstract void updateServerConnection(String name);
  public abstract void startServer();
  public abstract void stopServer();
}
