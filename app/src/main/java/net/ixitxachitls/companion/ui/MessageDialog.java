/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

package net.ixitxachitls.companion.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.LayoutRes;

import net.ixitxachitls.companion.R;

/**
 * A dialog to show a simple message.
 */
public class MessageDialog {
  private final AlertDialog.Builder dialog;

  public MessageDialog(Context context) {
    this.dialog = new AlertDialog.Builder(context, R.style.ThemeOverlay_AppCompat_Dialog);
  }

  public static MessageDialog create(Context context) {
    return new MessageDialog(context);
  }

  public MessageDialog title(String title) {
    if (title != null) {
      dialog.setTitle(title);
    }

    return this;
  }

  public MessageDialog message(String message) {
    dialog.setMessage(message);
    return this;
  }

  public MessageDialog layout(@LayoutRes int layout) {
    dialog.setView(layout);
    return this;
  }

  public void show() {
    this.dialog.show();
  }
}
