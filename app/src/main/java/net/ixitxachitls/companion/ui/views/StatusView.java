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
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.net.CompanionMessenger;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * View to display status messages.
 */
public class StatusView extends LinearLayout {

  // Constants.
  private static final String TAG = "StatusView";
  private static final int REMOVE_BEATS = 120;

  // External data.
  private final Settings settings;

  // UI elements.
  private final IconView online;
  private final TextWrapper<TextView> messages;
  private final ScrollView messagesScroll;
  private final Wrapper<LinearLayout> connections;
  private final Map<String, ConnectionView> clientConnectionsById = new HashMap<>();
  private final Map<String, ConnectionView> serverConnectionsById = new HashMap<>();

  // Values.
  private boolean started = true;

  public StatusView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    settings = Settings.get();

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_status, null, false);
    view.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));
    online = (IconView) view.findViewById(R.id.online);
    online.setAction(this::restart);
    messagesScroll = view.findViewById(R.id.messages_scroll);
    messages = TextWrapper.wrap(view, R.id.messages)
        .onClick(this::toggleDebug)
        .onLongClick(this::clearDebug);
    messages.get().setMovementMethod(new ScrollingMovementMethod());
    connections = Wrapper.<LinearLayout>wrap(view, R.id.connections).onClick(this::toggleDebug);
    Wrapper.<HorizontalScrollView>wrap(view, R.id.connections_scroll)
        .onTouch(this::toggleDebug, MotionEvent.ACTION_UP);

    settings.shouldShowStatus().observe(ViewUtils.getActivity(context), data -> {
        messagesScroll.setVisibility(data ? VISIBLE : GONE); });

    addView(view);
  }

  private void toggleDebug() {
    settings.setDebugStatus(!settings.shouldShowStatus().getValue());
  }

  private void clearDebug() {
    messages.text("");
  }

  public void heartBeatWithRemove() {
    online.bleep();

    clientConnectionsById.values().forEach(ConnectionView::heartbeat);
    heartBeatWithRemove(serverConnectionsById.entrySet());
  }

  private void heartBeatWithRemove(Iterable<Map.Entry<String, ConnectionView>> views) {
    for (Iterator<Map.Entry<String, ConnectionView>> i = views.iterator(); i.hasNext(); ) {
      Map.Entry<String, ConnectionView> entry = i.next();
      entry.getValue().heartbeat();
      if (!entry.getKey().equals(Settings.get().getAppId())
          && entry.getValue().getHeartbeats() < -REMOVE_BEATS) {
        i.remove();
        connections.get().removeView(entry.getValue());
      }
    }
  }

  public void addMessage(String message) {
    messages.text(messages.getText() + message + "\n");
    messagesScroll.fullScroll(ScrollView.FOCUS_DOWN);
  }

  public void addClientConnection(String id, String name) {
    if (clientConnectionsById.containsKey(id)) {
      Log.d(TAG, "trying to add second connection for " + name);
      return;
    }

    ConnectionView connection = new ConnectionView(getContext(), id, name, false);
    clientConnectionsById.put(id, connection);
    connections.get().addView(connection);
  }

  public void removeClientConnection(String id) {
    ConnectionView connection = clientConnectionsById.remove(id);
    if (connection != null) {
      ((ViewGroup) connection.getParent()).removeView(connection);
    }
  }

  public void removeServerConnection(String id) {
    ConnectionView connection = serverConnectionsById.remove(id);
    if (connection != null) {
      ((ViewGroup) connection.getParent()).removeView(connection);
    }
  }

  // TODO(merlin): remove id?
  public void addServerConnection(String id, String name) {
    if (serverConnectionsById.containsKey(id)) {
      Log.d(TAG, "trying to add second connection for " + name);
      return;
    }

    ConnectionView connection = new ConnectionView(getContext(), id, name, true);
    serverConnectionsById.put(id, connection);
    connections.get().addView(connection);
  }

  public void updateClientConnection(String id) {
    ConnectionView connection = clientConnectionsById.get(id);
    if (connection == null) {
      Log.d(TAG, "no connection view for " + id);
      return;
    }

    connection.update();
  }

  public void updateServerConnection(String id) {
    ConnectionView connection = serverConnectionsById.get(id);
    if (connection == null) {
      Log.d(TAG, "no connection view for " + id);
      return;
    }

    connection.update();
  }

  private void restart() {
    if (started) {
      CompanionMessenger.get().stop();
    } else {
      CompanionMessenger.get().start();
    }

    started = !started;
  }

}
