/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

package net.ixitxachitls.companion.ui.dialogs;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.MiniatureFilter;
import net.ixitxachitls.companion.data.documents.MiniatureLocation;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.SortedSet;

/**
 * Dialog for editing miniature locations.
 */
public class MiniatureLocationsDialog extends Dialog {

  private LinearLayout locationsContainer;

  private void addLocation() {
    MiniatureLocationEditDialog.newInstance("").onSaved(this::addedLocation).display();
  }

  private void addedLocation(Object o) {
    update();
  }

  @Override
  protected void createContent(View view) {
    locationsContainer = view.findViewById(R.id.locations);
    Wrapper.wrap(view, R.id.add).onClick(this::addLocation);

    update();
  }

  private void update() {
    locationsContainer.removeAllViews();

    SortedSet<MiniatureLocation> locations = me().getSortedLocations();
    for (MiniatureLocation location : me().getSortedLocations()) {
      locationsContainer.addView(new MiniatureLocationCard(getContext(), locationsContainer,
          location).getCard());
    }
  }

  public static MiniatureLocationsDialog newInstance() {
    MiniatureLocationsDialog dialog = new MiniatureLocationsDialog();
    dialog.setArguments(arguments(R.layout.dialog_miniature_locations, "Locations",
        R.color.miniature));
    return dialog;
  }

  private class MiniatureLocationCard {

    private final MiniatureLocation location;

    // UI.
    private final LinearLayout filterContainer;
    private final TextWrapper<TextView> locationView;
    private final CardView card;

    public MiniatureLocationCard(Context context, ViewGroup container, MiniatureLocation location) {
      this.location = location;

      card = (CardView) LayoutInflater.from(context).inflate(R.layout.card_miniature_location,
          container, false);
      Wrapper.wrap(card).onClick(this::edit);
      Wrapper.wrap(card, R.id.delete)
          .description("Delete", "Delete the location.")
          .onClick(this::delete);

      locationView = TextWrapper.wrap(card, R.id.location);
      filterContainer = card.findViewById(R.id.filters);
      update();
    }

    public CardView getCard() {
      return card;
    }

    private void delete() {
      ConfirmationPrompt.create(getContext())
          .title("Delete Location").message("You really want to delete this location?")
          .yes(this::doDelete)
          .show();
    }

    private void doDelete() {
      me().deleteLocation(location.getName());
      MiniatureLocationsDialog.this.update();
    }

    private void edit() {
      MiniatureLocationEditDialog.newInstance(location.getName())
          .onSaved((o) -> MiniatureLocationsDialog.this.update()).display();
    }

    private void update() {
      locationView.text(location.getName());
      locationView.get().setBackgroundColor(location.getColor());
      if (location.getColor() == getContext().getColor(R.color.location_black)
          || location.getColor() == getContext().getColor(R.color.location_brown)) {
        locationView.textColor(R.color.white);
      } else {
        locationView.textColor(R.color.black);
      }
      filterContainer.removeAllViews();
      for (MiniatureFilter filter : location.getFilters()) {
        filterContainer.addView(
            TextWrapper.wrap(new TextView(getContext())).text(filter.getSummary()).get());
      }
    }
  }
}
