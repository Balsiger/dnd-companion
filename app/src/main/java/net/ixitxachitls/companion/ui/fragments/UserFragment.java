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

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.MiniatureFilter;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.MiniatureFilterDialog;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.RoundImageView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Strings;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Fragment for displaying settings values.
 */
public class UserFragment extends CompanionFragment {

  private User me;

  // UI elements.
  private LabelledEditTextView nickname;
  private LabelledEditTextView features;
  private Wrapper<Button> save;
  private RoundImageView image;
  private LinearLayout locations;
  private LabelledEditTextView location;
  private ProgressBar progress;

  public UserFragment() {
    super(Type.settings);
  }

  @Override
  public boolean goBack() {
    CompanionFragments.get().show(Type.campaigns, Optional.empty());
    return true;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    me = application().context().me();
    ScrollView view = (ScrollView) inflater.inflate(R.layout.fragment_user, container, false);

    nickname = view.findViewById(R.id.nickname);
    nickname.onEdit(this::editNickname).onChange(this::update);
    features = view.findViewById(R.id.features);
    features.onEdit(this::editFeatures);
    save = Wrapper.<Button>wrap(view, R.id.save).onClick(this::save);
    image = view.findViewById(R.id.image);
    image.loadImageUrl(me.getPhotoUrl());
    locations = view.findViewById(R.id.locations);
    location = view.findViewById(R.id.location);
    Wrapper.<ImageView>wrap(view, R.id.add_location).onClick(this::addLocation);
    progress = view.findViewById(R.id.progress);

    progress.setIndeterminate(true);
    me().readMiniatures(this::miniaturesLoaded);
    update();
    return view;
  }

  @Override
  public void onStart() {
    super.onStart();

    // We need to reset values here or they will be messed up by the saved state...?
    nickname.text(me.getNickname());
    features.text(Strings.COMMA_JOINER.join(me.getFeatures()));
  }

  private void addLocation() {
    if (!location.isEmpty()) {
      MiniatureFilterDialog.newInstance(new MiniatureFilter("dialog")).onSaved(this::addLocation)
          .display();
    }
  }

  private void addLocation(MiniatureFilter filter) {
    me().addLocation(location.getText(), filter);
    update();
  }

  protected void editFeatures() {
    me.setFeatures(Arrays.asList(features.getText().split("\\s*,\\s*")));
  }

  protected void editNickname() {
    me.setNickname(nickname.getText());
  }

  private void miniaturesLoaded() {
    progress.setVisibility(View.GONE);
    update();
  }

  private void save() {
    editNickname();
    me.setFeatures(Arrays.asList(features.getText().split("\\s*,\\s*")));
    me.store();

    CompanionFragments.get().show(Type.campaigns, Optional.empty());
  }

  private void update() {
    if (nickname != null) {
      if (nickname.getText().length() > 0) {
        save.get().setVisibility(View.VISIBLE);
      } else {
        save.get().setVisibility(View.INVISIBLE);
      }
    }

    locations.removeAllViews();
    for (Map.Entry<String, MiniatureFilter> location : me().getLocations()) {
    //for (Map.Entry<String, MiniatureFilter> location : me().getPrioritizedLocations()) {
      locations.addView(new LocationLineView(getContext(), location.getKey(), location.getValue()));
    }
  }

  private class LocationLineView extends LinearLayout {
    private final String location;
    private final MiniatureFilter filter;
    private final TextWrapper<TextView> locationText;

    private LocationLineView(Context context, String location, MiniatureFilter filter) {
      super(context);

      this.location = location;
      this.filter = filter;

      View view =
          LayoutInflater.from(getContext()).inflate(R.layout.fragment_miniature_location_line,
              this, false);

      locationText = TextWrapper.wrap(view, R.id.location).onClick(this::edit);
      Wrapper.wrap(view, R.id.delete).onClick(this::delete);

      update();
      addView(view);
    }

    private void delete() {
      me().removeLocation(location, filter);
      UserFragment.this.update();
    }

    private void edit() {
      MiniatureFilterDialog.newInstance(filter).onSaved(this::update).display();
    }

    private String summary() {
      return location + ", " + filter.getSummary();
    }

    private void update() {
      locationText.text(summary());
    }

    private void update(MiniatureFilter filter) {
      me().addLocation(location, filter);
      update();
    }
  }
}
