/*
 * Copyright (c) 2017-2019 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
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

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Encounter;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.ui.views.ItemView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * A view to show a group of items in an encounter.
 */
public class EncounterItemGroupView extends LinearLayout {

  // UI elements.
  private TextWrapper<TextView> description;
  private LinearLayout items;

  public EncounterItemGroupView(Context context) {
    super(context);

    init();
  }

  public EncounterItemGroupView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    init();
  }

  private void init() {
    View view =
        LayoutInflater.from(getContext()).inflate(R.layout.view_encounter_item_group, null, false);

    DisplayMetrics displayMetrics = new DisplayMetrics();
    CompanionApplication.get().getCurrentActivity().getWindowManager().getDefaultDisplay()
        .getMetrics(displayMetrics);

    description = TextWrapper.wrap(view, R.id.description);
    description.get().setMaxWidth(displayMetrics.widthPixels - 100);
    items = view.findViewById(R.id.items);

    addView(view);
  }

  public void setup(Campaign campaign, Encounter encounter, String description, List<Item> items) {
    this.description.text(description);

    this.items.removeAllViews();
    for (Item item : items) {
      ItemView itemView = new ItemView(getContext(), campaign, encounter, item);
      this.items.addView(itemView);
    }
  }

}
