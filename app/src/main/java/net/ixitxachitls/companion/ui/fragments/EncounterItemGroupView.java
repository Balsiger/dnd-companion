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
import android.view.DragEvent;
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

  private Campaign campaign;
  private Encounter encounter;
  private String description;
  private List<Item> items;

  // UI elements.
  private TextWrapper<TextView> descriptionView;
  private LinearLayout itemsView;

  public EncounterItemGroupView(Context context) {
    super(context);

    init();
  }

  public EncounterItemGroupView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    init();
  }

  public void setup(Campaign campaign, Encounter encounter, String description, List<Item> items) {
    this.campaign = campaign;
    this.encounter = encounter;
    this.items = items;
    this.description = description;

    update();
  }

  private void init() {
    View view =
        LayoutInflater.from(getContext()).inflate(R.layout.view_encounter_item_group, null, false);

    DisplayMetrics displayMetrics = new DisplayMetrics();
    CompanionApplication.get().getCurrentActivity().getWindowManager().getDefaultDisplay()
        .getMetrics(displayMetrics);

    descriptionView = TextWrapper.wrap(view, R.id.description);
    descriptionView.get().setMaxWidth(displayMetrics.widthPixels - 100);
    itemsView = view.findViewById(R.id.items);

    view.setOnDragListener(this::onItemDrag);
    addView(view);
  }

  private boolean onItemDrag(View view, DragEvent event) {
    switch (event.getAction()) {
      case DragEvent.ACTION_DRAG_ENDED:
        update();
        return true;

      default:
        return true;
    }
  }

  private void update() {
    this.descriptionView.text(description);

    this.itemsView.removeAllViews();
    for (Item item : items) {
      ItemView itemView = new ItemView(getContext(), campaign, encounter, item, false);
      this.itemsView.addView(itemView);
    }
  }
}
