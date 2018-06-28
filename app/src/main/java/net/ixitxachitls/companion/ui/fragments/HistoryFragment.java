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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Histories;
import net.ixitxachitls.companion.data.dynamics.HistoryEntry;
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

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

    update(histories.getEntries().getValue());
  }

  public void update() {
    if (id == null) {
      update(histories.getEntries().getValue());
    } else {
      update(histories.getEntries(id).getValue());
    }
  }

  public void update(@Nullable String campaignId) {
    this.id = campaignId;
    update();
  }

  private void update(List<HistoryEntry> entries) {
    contents.removeAllViews();

    for (HistoryEntry entry : entries) {
      contents.addView(createLine(entry));
    }
  }

  private TextView createLine(HistoryEntry entry) {
    TextWrapper<TextView> line = TextWrapper.wrap(new TextView(getContext()));
    line.text(entry.toString());
    return line.get();
  }

  private void toggleAll() {
    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) contentsScroll.getLayoutParams();
    params.height = params.height == 0 ? 200 : 0;
    contentsScroll.setLayoutParams(params);
  }
}
