/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * along with the Tabletop Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;

/**
 * Icon showing the network status of an entry.
 */
public class NetworkIcon extends IconView {

  public NetworkIcon(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);
  }

  public void setStatus(boolean isLocal, boolean isOn) {
    Status.log("settings static " + (isLocal ? "local" : "remote") + "/" + (isOn ? "on" : "off"));
    if (isLocal && isOn) {
      setImageResource(R.drawable.ic_signal_wifi_4_bar_black_24dp);
    } else if (isLocal && !isOn) {
      setImageResource(R.drawable.ic_signal_wifi_off_black_24dp);
    } else if (isOn) {
      setImageResource(R.drawable.ic_cloud_black_24dp);
    } else {
      setImageResource(R.drawable.ic_cloud_off_black_24dp);
    }
  }
}
