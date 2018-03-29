/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.dialogs.ConnectionStatusDialog;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * View displaying a network connection and it's status.
 */
public class ConnectionView extends LinearLayout {

  private static final int OFFLINE_BEATS = 30;

  private String id;
  private String name;
  private int heartbeats = OFFLINE_BEATS;

  // UI elements.
  private IconView icon;
  private boolean server;

  public ConnectionView(Context context, String id, String name, boolean server) {
    super(context);

    init(id, name, server);
  }

  public String getName() {
    return name;
  }

  private void init(String id, String name, boolean server) {
    this.id = id;
    this.name = name;
    this.server = server;

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_connection, null, false);
    icon = view.findViewById(R.id.status);
    icon.setColorFilter(getResources().getColor(R.color.on, null));
    if (server) {
      icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_computer_black_24dp, null));
    }
    TextWrapper.wrap(view, R.id.name).text(name);

    view.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        showMessages();
      }
    });

    addView(view);
  }

  private void showMessages() {
    CompanionApplication app = (CompanionApplication) getContext().getApplicationContext();
    List<String> messages = new ArrayList<>();
    if (server) {
      //messages.addAll(CompanionServer.get().getSenderList(id));
      //if (app.getServerMessageProcessor().isPresent()) {
      //  messages.addAll(app.getServerMessageProcessor().get().receivedMessages());
      //} else {
        messages.add("No server message processor");
      //}
    } else {
      //messages.addAll(CompanionClients.get().getSenderList(id));
      //if (app.getClientMessageProcessor().isPresent()) {
      //  messages.addAll(app.getClientMessageProcessor().get().receivedMessages());
      //} else {
        messages.add("No client message processor");
      //}
    }

    ConnectionStatusDialog.newInstance(name, Strings.NEWLINE_JOINER.join(messages)).display();
  }

  public void heartbeat() {
    heartbeats--;

    if(heartbeats == 0) {
      out();
    }
  }

  public int getHeartbeats() {
    return heartbeats;
  }

  public void update() {
    icon.setColorFilter(getResources().getColor(R.color.on, null));
    icon.bleep();
    heartbeats = OFFLINE_BEATS;
  }

  public void out() {
    icon.setColorFilter(getResources().getColor(R.color.out, null));
  }
}
