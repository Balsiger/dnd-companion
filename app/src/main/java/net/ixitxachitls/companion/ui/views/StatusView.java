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
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;

import java.io.PrintWriter;
import java.io.StringWriter;

import androidx.annotation.Nullable;

/**
 * View to display status messages.
 */
public class StatusView extends LinearLayout {

  // UI elements.
  private final TextWrapper<TextView> messages;
  private final ScrollView messagesScroll;
  // Internal data.
  private boolean showDebug = false;

  public StatusView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_status, null, false);
    view.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));
    messagesScroll = view.findViewById(R.id.messages_scroll);
    messagesScroll.setVisibility(GONE);
    messages = TextWrapper.wrap(view, R.id.messages)
        .onClick(this::toggleDebug)
        .onLongClick(this::clearDebug);
    messages.get().setMovementMethod(new ScrollingMovementMethod());

    addView(view);
  }

  public boolean isShowing() {
    return showDebug;
  }

  public void addErrorMessage(String message) {
    messages.append(Html.fromHtml("<div><b><font color=\"red\">" + message + "</font></b></div>",
        Html.FROM_HTML_MODE_COMPACT));
    messagesScroll.fullScroll(ScrollView.FOCUS_DOWN);
  }

  public void addException(String message, Exception e) {
    StringWriter writer = new StringWriter();
    PrintWriter printWriter = new PrintWriter(writer);
    e.printStackTrace(printWriter);

    addMessage(message + "\n" + writer.toString());
  }

  public void addMessage(String message) {
    messages.append(Html.fromHtml("<div>" + message + "</div>", Html.FROM_HTML_MODE_COMPACT));
    messagesScroll.fullScroll(ScrollView.FOCUS_DOWN);
  }

  public void addUpdateMessage(String message) {
    messages.append(Html.fromHtml("<div><b><font color=\"blue\">" + message + "</font></b></div>",
        Html.FROM_HTML_MODE_COMPACT));
    messagesScroll.fullScroll(ScrollView.FOCUS_DOWN);
  }

  public void addWarningMessage(String message) {
    messages.append(Html.fromHtml("<div><b>" + message + "</b></div>",
        Html.FROM_HTML_MODE_COMPACT));
    messagesScroll.fullScroll(ScrollView.FOCUS_DOWN);
  }

  public void showException(String message, Exception e) {
    StringWriter writer = new StringWriter();
    PrintWriter printWriter = new PrintWriter(writer);
    e.printStackTrace(printWriter);
    new ConfirmationPrompt(getContext()).title(message).message(writer.toString()).noNo().show();
    printWriter.close();
  }

  public void toggleDebug() {
    showDebug = !showDebug;
    messagesScroll.setVisibility(showDebug ? VISIBLE : GONE);
  }

  private void clearDebug() {
    messages.text("");
  }
}
