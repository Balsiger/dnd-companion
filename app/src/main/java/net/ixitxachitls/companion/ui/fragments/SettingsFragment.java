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

package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.net.CompanionSubscriber;
import net.ixitxachitls.companion.ui.Wrapper;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;

/**
 * Fragment for displaying settings values.
 */
public class SettingsFragment extends CompanionFragment {
  private Settings settings;

  // UI elements.
  private Wrapper<TextView> nickname;
  private Wrapper<Button> save;

  public SettingsFragment() {
    super(Type.settings);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    ConstraintLayout view = (ConstraintLayout)
        inflater.inflate(R.layout.fragment_settings, container, false);

    settings = Settings.get();

    nickname = Wrapper.wrap(view, R.id.nickname);
    nickname.text(settings.isDefined() ? settings.getNickname() : "")
        .label(R.string.settings_nickname_label)
        .onEdit(this::editNickname)
        .onChange(this::refresh);
    save = Wrapper.wrap(view, R.id.save);
    save.onClick(this::save);

    if (settings.isDefined()) {
      view.findViewById(R.id.initial).setVisibility(View.INVISIBLE);
    }

    refresh();
    return view;
  }

  private void save() {
    editNickname();
    settings.store();

    if (settings.isDefined()) {
      CompanionFragments.get().showLast();
      CompanionSubscriber.get().sendWelcome();
    }
  }

  protected void editNickname() {
    settings.setNickname(nickname.get().getText().toString());
  }

  @Override
  public void refresh() {
    super.refresh();

    if (nickname != null) {
      if (nickname.get().getText().length() > 0) {
        save.get().setVisibility(View.VISIBLE);
      } else {
        save.get().setVisibility(View.INVISIBLE);
      }
    }
  }
}
