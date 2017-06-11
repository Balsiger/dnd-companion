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
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.net.CompanionPublisher;
import net.ixitxachitls.companion.ui.Setup;

import java.util.HashMap;
import java.util.Map;

/**
 * View to display status messages.
 */
public class StatusView extends LinearLayout {

  private static final String TAG = "StatusView";

  boolean started = true;

  // UI elements.
  private IconView online;
  private TextView messages;
  private LinearLayout connections;
  private Map<String, ConnectionView> clientConnectionsByName = new HashMap<>();
  private Map<String, ConnectionView> serverConnectionsByName = new HashMap<>();
  private ScrollView messagesScroll;

  public StatusView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    init(attributes);
  }

  private void init(AttributeSet attributes) {
    TypedArray array = getContext().obtainStyledAttributes(attributes, R.styleable.StatusView);

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_status, null, false);
    view.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));
    online = (IconView) view.findViewById(R.id.online);
    online.setAction(this::restart);
    messagesScroll = (ScrollView) view.findViewById(R.id.messages_scroll);
    messages = Setup.textView(view, R.id.messages);
    messages.setMovementMethod(new ScrollingMovementMethod());
    connections = (LinearLayout) view.findViewById(R.id.connections);
    connections.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        toggleDebug();
      }
    });

    addView(view);
    update();
  }

  private void update() {
    messagesScroll.setVisibility(Settings.get().showStatus() ? VISIBLE : GONE);
  }

  private void toggleDebug() {
    Settings.get().setDebugStatus(!Settings.get().showStatus());
    update();
  }

  public void heartbeat() {
    online.bleep();

    for (ConnectionView connection : clientConnectionsByName.values()) {
      connection.heartbeat();
    }

    for (ConnectionView connection : serverConnectionsByName.values()) {
      connection.heartbeat();
    }

    update();
  }

  public void addMessage(String message) {
    if (!Settings.get().showStatus()) {
      return;
    }

    messages.setText(messages.getText() + message + "\n");
    messagesScroll.fullScroll(ScrollView.FOCUS_DOWN);
  }

  public void addClientConnection(String name) {
    if (clientConnectionsByName.containsKey(name)) {
      Log.d(TAG, "trying to add second connection for " + name);
      return;
    }

    ConnectionView connection = new ConnectionView(getContext(), name, false);
    clientConnectionsByName.put(name, connection);
    connections.addView(connection);
  }

  public void addServerConnection(String name) {
    if (serverConnectionsByName.containsKey(name)) {
      Log.d(TAG, "trying to add second connection for " + name);
      return;
    }

    ConnectionView connection = new ConnectionView(getContext(), name, true);
    serverConnectionsByName.put(name, connection);
    connections.addView(connection);
  }

  public void updateClientConnection(String name) {
    ConnectionView connection = clientConnectionsByName.get(name);
    if (connection == null) {
      Log.d(TAG, "no connection view for " + name);
      return;
    }

    connection.update();
  }

  public void updateServerConnection(String name) {
    ConnectionView connection = serverConnectionsByName.get(name);
    if (connection == null) {
      Log.d(TAG, "no connection view for " + name);
      return;
    }

    connection.update();
  }

  public void startServer() {
    String name = serverName();
    ConnectionView connection = serverConnectionsByName.get(name);
    if (connection == null) {
      connection = new ConnectionView(getContext(), name, true);
      serverConnectionsByName.put(name, connection);
      connections.addView(connection);
    }

    connection.on();
  }

  public void stopServer() {
    ConnectionView connection = serverConnectionsByName.get(serverName());
    if (connection == null) {
      Log.d(TAG, "no connection view for " + serverName());
      return;
    }

    connection.off();
  }

  private void restart() {
    if (started) {
      CompanionPublisher.get().stop();
    } else {
      CompanionPublisher.get().ensureServerStarted();
    }

    started = !started;
  }

  private String serverName() {
    String nickname = Settings.get().getNickname();
    if (nickname.endsWith("s")) {
      return nickname + "' server";
    }

    return nickname + "'s server";
  }
}
