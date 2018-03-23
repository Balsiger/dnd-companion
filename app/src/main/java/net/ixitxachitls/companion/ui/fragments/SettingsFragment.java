/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * along with the Tabletop Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.net.CompanionMessenger;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Misc;
import net.ixitxachitls.companion.util.Strings;

import java.util.Arrays;
import java.util.Optional;

/**
 * Fragment for displaying settings values.
 */
public class SettingsFragment extends CompanionFragment {
  private Settings settings;

  // UI elements.
  private LabelledEditTextView nickname;
  private Wrapper<CheckBox> remoteCampaigns;
  private Wrapper<CheckBox> remoteCharacters;
  private LabelledEditTextView features;
  private Wrapper<Button> save;

  public SettingsFragment() {
    super(Type.settings);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    ConstraintLayout view = (ConstraintLayout)
        inflater.inflate(R.layout.fragment_settings, container, false);

    settings = Settings.get();

    nickname = view.findViewById(R.id.nickname);
    nickname.onEdit(this::editNickname).onChange(this::update);
    remoteCampaigns = Wrapper.wrap(view, R.id.remote_campaigns);
    remoteCampaigns.get().setChecked(settings.useRemoteCampaigns());
    remoteCampaigns.visible(Misc.onEmulator());
    remoteCharacters = Wrapper.wrap(view, R.id.remote_characters);
    remoteCharacters.get().setChecked(settings.useRemoteCharacters());
    remoteCharacters.visible(Misc.onEmulator());
    features = view.findViewById(R.id.features);
    features.onEdit(this::editFeatures);
    save = Wrapper.wrap(view, R.id.save);
    save.onClick(this::save);

    if (settings.isDefined()) {
      view.findViewById(R.id.initial).setVisibility(View.INVISIBLE);
    }

    update();
    return view;
  }

  @Override
  public void onStart() {
    super.onStart();

    // We need to reset values here or they will be messed up by the saved state...?
    nickname.text(settings.isDefined() ? settings.getNickname() : "");
    features.text(Strings.COMMA_JOINER.join(settings.getFeatures()));
  }

  private void save() {
    editNickname();
    settings.useRemote(remoteCampaigns.get().isChecked(), remoteCharacters.get().isChecked());
    settings.setFeatures(Arrays.asList(features.getText().split("\\s*,\\s*")));
    settings.store();

    if (settings.isDefined()) {
      CompanionFragments.get().show(Type.campaigns, Optional.empty());
      CompanionMessenger.get().sendWelcome();
    }
  }

  protected void editNickname() {
    settings.setNickname(nickname.getText());
  }

  protected void editFeatures() {
    settings.setFeatures(Arrays.asList(features.getText().split("\\s*,\\s*")));
  }

  @Override
  public boolean goBack() {
    CompanionFragments.get().show(Type.campaigns, Optional.empty());
    return true;
  }

  private void update() {
    if (nickname != null) {
      if (nickname.getText().length() > 0) {
        save.get().setVisibility(View.VISIBLE);
      } else {
        save.get().setVisibility(View.INVISIBLE);
      }
    }
  }
}
