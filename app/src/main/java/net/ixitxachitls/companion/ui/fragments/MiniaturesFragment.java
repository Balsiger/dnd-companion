/*
 * Copyright (c) 2017-2019 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.FilteredTemplatesStore;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.MiniatureFilter;
import net.ixitxachitls.companion.data.templates.MiniatureTemplate;
import net.ixitxachitls.companion.ui.dialogs.MiniatureConfigurationDialog;
import net.ixitxachitls.companion.ui.dialogs.MiniatureFilterDialog;
import net.ixitxachitls.companion.ui.dialogs.MiniatureLocationsDialog;

import java.util.Optional;

import androidx.fragment.app.Fragment;

/**
 * A fragment to display the miniatures.
 */
public class MiniaturesFragment extends TemplatesFragment {

  private static final String LOADING_MINIATURES = "miniatures";

  public MiniaturesFragment() {
    super(Type.miniatures);
  }

  @Override
  protected void loadEntities() {
    super.loadEntities();
    me().readMiniatures(this::loadedEntities);
  }

  @Override
  protected void config() {
    MiniatureConfigurationDialog.newInstance().onSaved(o -> update()).display();
  }

  @Override
  protected void setupActions() {
    super.setupActions();

    addAction(R.drawable.ic_map_marker_black_24dp, "Locations",
        "Define or change the locations your miniatures are stored.")
        .onClick(this::editLocations);
  }

  protected void editLocations() {
    MiniatureLocationsDialog.newInstance().display();
  }

  @Override
  protected void filter() {
    MiniatureFilterDialog.newInstance(Templates.get().getMiniatureTemplates().getFilter(), true)
        .onSaved(this::filtered).display();
  }

  private void filtered(MiniatureFilter filter) {
    Templates.get().getMiniatureTemplates().filter(me(), filter);
    update();
    seek.setMax(Templates.get().getMiniatureTemplates().getFilteredNumber() - 1);
  }

  @Override
  protected void loadedEntities() {
    super.loadedEntities();
    update();
  }

  @Override
  protected String getTitle(int position) {
    Optional<MiniatureTemplate> miniature = Templates.get().getMiniatureTemplates().get(position);
    if (miniature.isPresent()) {
      return miniature.get().getName();
    } else {
      return "(not found)";
    }
  }

  @Override
  protected Fragment getTemplateFragment(int position) {
    Fragment fragment = new MiniatureFragment();
    Optional<MiniatureTemplate> template = Templates.get().getMiniatureTemplates().get(position);
    Bundle args = new Bundle();
    if (template.isPresent()) {
      args.putString(MiniatureFragment.ARG_NAME, template.get().getName());
    }
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  protected int getTemplatesCount() {
    return Templates.get().getMiniatureTemplates().getFilteredNumber();
  }

  @Override
  protected FilteredTemplatesStore<MiniatureTemplate, MiniatureFilter> getTemplates() {
    return Templates.get().getMiniatureTemplates();
  }
}