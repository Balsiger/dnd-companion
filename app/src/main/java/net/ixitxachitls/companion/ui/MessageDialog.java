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

package net.ixitxachitls.companion.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.util.Texts;

import java.util.Optional;

/**
 * A dialog to show a simple message.
 */
public class MessageDialog {
  private final AlertDialog.Builder dialog;
  private Optional<View> view;

  public MessageDialog(Context context) {
    this.dialog = new AlertDialog.Builder(context, R.style.ThemeOverlay_AppCompat_Dialog);
    this.view =
        Optional.of(LayoutInflater.from(context).inflate(R.layout.view_scroll_text, null, false));
    this.dialog.setView(view.get());
  }

  public MessageDialog formatted(String message) {
    Spanned spanned = Texts.toSpanned(dialog.getContext(), message);
    dialog.setMessage(spanned);

    return this;
  }

  public MessageDialog layout(@LayoutRes int layout) {
    dialog.setView(layout);
    view = Optional.empty();
    return this;
  }

  public MessageDialog message(String message) {
    if (view.isPresent()) {
      TextView text = view.get().findViewById(R.id.text);
      if (text != null) {
        text.setText(message);
      } else {
        dialog.setMessage(message);
      }
    } else {
      dialog.setMessage(message);
    }
    return this;
  }

  public void show() {
    this.dialog.show();
  }

  public MessageDialog title(String title) {
    if (title != null) {
      dialog.setTitle(title);
    }

    return this;
  }

  public static MessageDialog create(Context context) {
    return new MessageDialog(context);
  }
}
