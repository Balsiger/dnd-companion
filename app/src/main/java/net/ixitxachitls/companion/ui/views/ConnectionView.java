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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.ui.Setup;

/**
 * View displaying a network connection and it's status.
 */
public class ConnectionView extends LinearLayout {

  private static final int OFFLINE_BEATS = 100;

  private int heartbeats = OFFLINE_BEATS;

  // UI elements.
  private IconView icon;

  public ConnectionView(Context context, String name, boolean server) {
    super(context);

    init(name, server);
  }

  private void init(String name, boolean server) {
    View view = LayoutInflater.from(getContext())
        .inflate(R.layout.view_connection, null, false);
    icon = (IconView) view.findViewById(R.id.status);
    if (server) {
      icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_computer_black_24dp, null));
    }
    Setup.textView(view, R.id.name, name);

    addView(view);
  }

  public void heartbeat() {
    heartbeats--;

    if(heartbeats <= 0) {
      out();
    }
  }

  public void update() {
    icon.bleep();
    heartbeats = OFFLINE_BEATS;
  }

  public void out() {
    icon.setColorFilter(getResources().getColor(R.color.out, null));
  }

  public void on() {
    icon.setColorFilter(getResources().getColor(R.color.on, null));
    heartbeats = Integer.MAX_VALUE;
  }

  public void off() {
    icon.setColorFilter(getResources().getColor(R.color.off, null));
    heartbeats = Integer.MAX_VALUE;
  }
}
