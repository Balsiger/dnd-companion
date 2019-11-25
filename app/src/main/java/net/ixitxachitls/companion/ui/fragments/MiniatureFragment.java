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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.templates.MiniatureTemplate;
import net.ixitxachitls.companion.ui.views.EntityImageView;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledTextView;
import net.ixitxachitls.companion.util.Strings;

import java.util.Optional;

/**
 * A subpage or tab per miniature
 */
public class MiniatureFragment extends NestedCompanionFragment {
  public static final String ARG_NAME = "name";

  private static final String PATH = "miniatures/";

  protected Optional<MiniatureTemplate> miniature = Optional.empty();

  private EntityImageView image;
  private LabelledTextView set;
  private LabelledTextView race;
  private LabelledTextView type;
  private LabelledTextView classes;
  private LabelledEditTextView owned;
  private LabelledTextView location;

  public MiniatureFragment() {}

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {

    ViewGroup view =
        (ViewGroup) inflater.inflate(R.layout.fragment_miniatures_page, container, false);

    loadMiniature(getArguments().getString(ARG_NAME));

    image = view.findViewById(R.id.entity_image);
    image.setup(PATH, R.drawable.miniatures);
    set = view.findViewById(R.id.set);
    race = view.findViewById(R.id.race);
    type = view.findViewById(R.id.type);
    classes = view.findViewById(R.id.classes);
    location = view.findViewById(R.id.location);
    owned = view.findViewById(R.id.owned);

    refresh();
    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

    refresh();
  }

  @Override
  public void onPause() {
    super.onPause();

    if (miniature.isPresent()) {
      try {
        me().setMiniatureCount(miniature.get().getName(),
            owned.getText().isEmpty() ? 0 : Long.parseLong(owned.getText()));
      } catch (NumberFormatException e) {
        Status.error("Cannot parse number for owned miniatures: " + e);
      }
    }
  }

  private String formatSet() {
    if (miniature.isPresent()) {
      String formatted = miniature.get().getSet();
      if (miniature.get().getNumber() > 0) {
        formatted += " #" + miniature.get().getNumber() + miniature.get().getNumberAffix();
      }

      return formatted;
    }

    return "";
  }

  private String formatType() {
    if (miniature.isPresent()) {
      String type = miniature.get().getSize() + " " + miniature.get().getType();
      String subtypes = Strings.COMMA_JOINER.join(miniature.get().getSubtypes());
      if (!subtypes.isEmpty()) {
        type += " (" + subtypes + ")";
      }

      return type;
    }

    return "";
  }

  private void loadMiniature(String name) {
    miniature = Templates.get().getMiniatureTemplates().get(name);
  }

  private void refresh() {
    loadMiniature(getArguments().getString(ARG_NAME));

    if (miniature.isPresent()) {
      image.setName(miniature.get().getName(), miniature.get().getName());
      image.setNumber(Templates.get().getMiniatureTemplates().getNumber(miniature.get()),
          Templates.get().getMiniatureTemplates().getFilteredNumber());
      set.text(formatSet());
      race.text(miniature.get().getRace());
      type.text(formatType());
      classes.text(Strings.COMMA_JOINER.join(miniature.get().getClasses()));
      classes.setVisibility(miniature.get().getClasses().isEmpty() ? View.GONE : View.VISIBLE);
      long miniatureCount = me().getMiniatureCount(miniature.get().getName());
      owned.text(miniatureCount == 0 ? "" : String.valueOf(miniatureCount));
      location.text(me().locationFor(miniature.get()));
    } else {
      image.clearName();
      image.clearNumber();
      set.text("");
      race.text("");
      type.text("");
      classes.setVisibility(View.GONE);
      owned.text("");
      location.text("");
    }
  }
}
