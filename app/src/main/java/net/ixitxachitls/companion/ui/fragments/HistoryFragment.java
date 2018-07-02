/*
 * Copyright (c) 2017-2018 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.fragments;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Histories;
import net.ixitxachitls.companion.data.dynamics.HistoryEntry;
import net.ixitxachitls.companion.ui.Prompt;
import net.ixitxachitls.companion.ui.views.wrappers.AbstractTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;

import java.util.List;

import javax.annotation.Nullable;

/**
 * A fragment for displaying and managing the history of campaigns or characters.
 */
public class HistoryFragment extends Fragment {

  private ViewGroup view;
  private ScrollView contentsScroll;
  private LinearLayout contents;
  private LinearLayout notifications;
  private Histories histories;
  private TextWrapper<TextView> all;

  private @Nullable String id;

  public HistoryFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    histories = CompanionApplication.get(getContext()).histories();

    view = (ViewGroup) inflater.inflate(R.layout.fragment_history, container, false);
    contentsScroll = view.findViewById(R.id.contents_scroll);
    contents = view.findViewById(R.id.contents);
    all = TextWrapper.wrap(view, R.id.all).onClick(this::toggleAll);
    notifications = view.findViewById(R.id.notifications);

    update((String) null);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

    update();
  }

  public void update() {
    update(histories.getEntries(id).getValue());
  }

  public void update(@Nullable String campaignId) {
    histories.getEntries(id).removeObservers(this);

    this.id = campaignId;
    LiveData<ImmutableList<HistoryEntry>> entries = histories.getEntries(id);
    entries.observe(this, this::update);
    update(entries.getValue());
  }

  private void update(List<HistoryEntry> entries) {
    notifications.removeAllViews();
    notifications.addView(all.get());
    for (HistoryEntry entry : entries) {
      notifications.addView(createNotification(entry));
    }
  }

  private TextView createLine(HistoryEntry entry) {
    TextWrapper<TextView> line = TextWrapper.wrap(new TextView(getContext()));
    line.text(entry.toString());
    return line.get();
  }

  private TextView createNotification(HistoryEntry entry) {
    TextWrapper<TextView> notification = TextWrapper.wrap(new TextView(getContext()));
    notification.text(entry.buildNotificationTitle());
    notification.get().setBackground(getContext().getDrawable(entry.getNotificationDrawable()));
    notification.align(AbstractTextWrapper.Align.CENTER);
    notification.onClick(() -> showEntry(entry));
    notification.bold();
    return notification.get();
  }

  private void showEntry(HistoryEntry entry) {
    Prompt.create(getContext()).title(entry.buildNotificationTitle())
        .message(entry.describe())
        .yes(() -> entry.markViewed())
        .noNo()
        .show();
  }

  private void toggleAll() {
    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) contentsScroll.getLayoutParams();
    params.height = params.height == 0 ? 200 : 0;
    contentsScroll.setLayoutParams(params);

    if (params.height > 0) {
      contents.removeAllViews();

      for (HistoryEntry entry : histories.getAll()) {
        contents.addView(createLine(entry));
      }
    }
  }
}
