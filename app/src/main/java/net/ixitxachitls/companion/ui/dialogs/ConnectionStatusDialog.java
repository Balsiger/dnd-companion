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

package net.ixitxachitls.companion.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * Dialog for showing the connection status.
 */
public class ConnectionStatusDialog extends Dialog {

  private static final String ARG_MESSAGES = "messages";

  private String messages;

  public static ConnectionStatusDialog newInstance(String title, String messages) {
    ConnectionStatusDialog dialog = new ConnectionStatusDialog();
    dialog.setArguments(arguments(R.layout.dialog_connection_status,
        title, R.color.cell, messages));
    return dialog;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, String title,
                                    @ColorRes int colorId, String messages) {
    Bundle arguments = Dialog.arguments(layoutId, title, colorId);
    arguments.putString(ARG_MESSAGES, messages);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    messages = getArguments().getString(ARG_MESSAGES);
  }

  @Override
  protected void createContent(View view) {
    Wrapper.<Button>wrap(view, R.id.save).onClick(this::save);
    TextWrapper<TextView> text = TextWrapper.wrap(view, R.id.messages);
    text.text(messages);
  }
}
