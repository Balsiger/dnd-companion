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

package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.RoundImageView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Misc;
import net.ixitxachitls.companion.util.Strings;

import java.util.Arrays;
import java.util.Optional;

/**
 * Fragment for displaying settings values.
 */
public class SettingsFragment extends CompanionFragment {

  private Optional<User> me;

  // UI elements.
  private LabelledEditTextView name;
  private LabelledEditTextView email;
  private LabelledEditTextView nickname;
  private Wrapper<CheckBox> remoteCampaigns;
  private Wrapper<CheckBox> remoteCharacters;
  private LabelledEditTextView features;
  private Wrapper<Button> save;
  private RoundImageView image;

  public SettingsFragment() {
    super(Type.settings);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    me = application().context().me();
    ScrollView view = (ScrollView) inflater.inflate(R.layout.fragment_settings, container, false);

    name = view.findViewById(R.id.name);
    name.disabled();
    email = view.findViewById(R.id.email);
    email.disabled();
    nickname = view.findViewById(R.id.nickname);
    nickname.onEdit(this::editNickname).onChange(this::update);
    remoteCampaigns = Wrapper.wrap(view, R.id.remote_campaigns);
    //remoteCampaigns.get().setChecked(settings.useRemoteCampaigns());
    remoteCampaigns.visible(Misc.onEmulator());
    remoteCharacters = Wrapper.wrap(view, R.id.remote_characters);
    //remoteCharacters.get().setChecked(settings.useRemoteCharacters());
    remoteCharacters.visible(Misc.onEmulator());
    features = view.findViewById(R.id.features);
    features.onEdit(this::editFeatures);
    save = Wrapper.wrap(view, R.id.save);
    save.onClick(this::save);
    image = view.findViewById(R.id.image);
    if (me.isPresent()) {
      image.loadImageUrl(me.get().getPhotoUrl());
    }

    update();
    return view;
  }

  @Override
  public void onStart() {
    super.onStart();

    // We need to reset values here or they will be messed up by the saved state...?
    if (me.isPresent()) {
      name.text(me.get().getName());
      email.text(me.get().getEmail());
      nickname.text(me.get().getNickname());
      features.text(Strings.COMMA_JOINER.join(me.get().getFeatures()));
    }
  }

  private void save() {
    editNickname();
    //settings.useRemote(remoteCampaigns.get().isChecked(), remoteCharacters.get().isChecked());
    if (me.isPresent()) {
      me.get().setFeatures(Arrays.asList(features.getText().split("\\s*,\\s*")));
      me.get().store();

      // We have to send the welcome before showing another fragment, or the context will be null.
      CompanionApplication.get(getContext()).messenger().sendWelcome();
      CompanionFragments.get().show(Type.campaigns, Optional.empty());
    }
  }

  protected void editNickname() {
    if (me.isPresent()) {
      me.get().setNickname(nickname.getText());
    }
  }

  protected void editFeatures() {
    if (me.isPresent()) {
      me.get().setFeatures(Arrays.asList(features.getText().split("\\s*,\\s*")));
    }
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
