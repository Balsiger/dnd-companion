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
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * View to display status messages.
 */
public class StatusView extends LinearLayout {

  // Constants.
  private static final int REMOVE_BEATS = 120;

  // External data.
  private final CompanionApplication application;

  // Internal data.
  private boolean showDebug = false;

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


    application = CompanionApplication.get(context);

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_status, null, false);
    view.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));
    online = view.findViewById(R.id.online);
    online.setAction(this::restart);
    messagesScroll = view.findViewById(R.id.messages_scroll);
    messages = TextWrapper.wrap(view, R.id.messages)
        .onClick(this::toggleDebug)
        .onLongClick(this::clearDebug);
    messages.get().setMovementMethod(new ScrollingMovementMethod());
    connections = Wrapper.<LinearLayout>wrap(view, R.id.connections).onClick(this::toggleDebug);
    Wrapper.<HorizontalScrollView>wrap(view, R.id.connections_scroll)
        .onTouch(this::toggleDebug, MotionEvent.ACTION_UP);

    addView(view);
  }

  public void showException(String message, Exception e) {
    StringWriter writer = new StringWriter();
    PrintWriter printWriter = new PrintWriter(writer);
    e.printStackTrace(printWriter);
    new ConfirmationPrompt(getContext()).title(message).message(writer.toString()).noNo().show();
    printWriter.close();
  }

  private void toggleDebug() {
    showDebug = !showDebug;
    messagesScroll.setVisibility(showDebug ? VISIBLE : GONE);
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
      if (entry.getValue().getHeartbeats() < -REMOVE_BEATS) {
        i.remove();
        connections.get().removeView(entry.getValue());
      }
    }
  }

  public void addMessage(String message) {
    messages.append(Html.fromHtml("<div>" + message + "</div>", Html.FROM_HTML_MODE_COMPACT));
    messagesScroll.fullScroll(ScrollView.FOCUS_DOWN);
  }

  public void addWarningMessage(String message) {
    messages.append(Html.fromHtml("<div><b>" + message + "</b></div>",
        Html.FROM_HTML_MODE_COMPACT));
    messagesScroll.fullScroll(ScrollView.FOCUS_DOWN);
  }

  public void addErrorMessage(String message) {
    messages.append(Html.fromHtml("<div><b><font color=\"red\">" + message + "</font></b></div>",
        Html.FROM_HTML_MODE_COMPACT));
    messagesScroll.fullScroll(ScrollView.FOCUS_DOWN);
  }

  public void addClientConnection(String id, String name) {
    if (clientConnectionsById.containsKey(id)) {
      Status.log("trying to add second connection for " + name);
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
      Status.log("trying to add second connection for " + name);
      return;
    }

    ConnectionView connection = new ConnectionView(getContext(), id, name, true);
    serverConnectionsById.put(id, connection);
    connections.get().addView(connection);
  }

  public void updateClientConnection(String id) {
    ConnectionView connection = clientConnectionsById.get(id);
    if (connection == null) {
      Status.log("no connection view for " + Status.nameFor(id));
      return;
    }

    connection.update();
  }

  public void updateServerConnection(String id) {
    ConnectionView connection = serverConnectionsById.get(id);
    if (connection == null) {
      Status.log("no connection view for " + Status.nameFor(id));
      return;
    }

    connection.update();
  }

  private void restart() {
    if (started) {
      application.messenger().stop();
    } else {
      application.messenger().start();
    }

    started = !started;
  }

}
