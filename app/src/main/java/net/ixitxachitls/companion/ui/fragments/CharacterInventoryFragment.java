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
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Item;
import net.ixitxachitls.companion.data.values.Money;
import net.ixitxachitls.companion.data.values.Weight;
import net.ixitxachitls.companion.ui.dialogs.EditItemDialog;
import net.ixitxachitls.companion.ui.views.ItemView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Child fragment for a characters inventory.
 */
public class CharacterInventoryFragment extends Fragment {

  private Optional<Character> character = Optional.empty();
  private boolean moveFirst = true;

  private TextWrapper<TextView> wealth;
  private TextWrapper<TextView> weight;
  private ViewGroup items;
  private ViewGroup view;

  public CharacterInventoryFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
    super.onCreateView(inflater, container, state);

    view = (ViewGroup) inflater.inflate(R.layout.fragment_character_inventory, container, false);

    wealth = TextWrapper.wrap(view, R.id.wealth);
    weight = TextWrapper.wrap(view, R.id.weight);
    items = view.findViewById(R.id.items);
    Wrapper.wrap(view, R.id.item_add)
        .onClick(this::addItem)
        .description("Add Item", "Add an item to the characters inventory.");

    if (character.isPresent()) {
      update(character.get());
    }

    return view;
  }

  public void update(Character character) {
    this.character = Optional.of(character);

    if (this.items != null && getContext() != null) {
      Money totalValue = Money.ZERO;
      Weight totalWeight = Weight.ZERO;

      Map<Item, ItemView> views = collectItemViews();
      items.removeAllViews();
      for (Item item : character.getItems()) {
        ItemView view = views.get(item);
        if (view == null) {
          view = createLine(item);
        } else {
          view.update();
        }
        items.addView(view);
        totalValue = totalValue.add(item.getValue());
        totalWeight = totalWeight.add(item.getWeight());
      }
      this.wealth.text(totalValue.toString());
      this.weight.text(totalWeight.toString());
      view.setOnDragListener((v, e) -> onItemDrag(v, e));
    }
  }

  private Map<Item, ItemView> collectItemViews() {
    Map<Item, ItemView> views = new HashMap<>();
    for (int i = 0; i < items.getChildCount(); i++) {
      ItemView view = (ItemView) items.getChildAt(i);
      views.put(view.getItem(), view);
    }

    return views;
  }

  private ItemView createLine(Item item) {
    return new ItemView(getContext(), character.get(), item);
  }

  private void addItem() {
    if (character.isPresent()) {
      EditItemDialog.newInstance(character.get().getCharacterId()).display();
    } else {
      Status.error("No character available!");
    }
  }

  private boolean onItemDrag(View view, DragEvent event) {
    switch (event.getAction()) {
      case DragEvent.ACTION_DRAG_STARTED:
        return true;

      case DragEvent.ACTION_DRAG_LOCATION:
        int []locations = new int[2];
        items.getLocationInWindow(locations);
        moveFirst = event.getY() < locations[1];
        return true;

      case DragEvent.ACTION_DRAG_ENDED:
        if (event.getResult()) {
          // The drop was handled by some view.
          return true;
        }

        // Fall through to handle like drop!

      case DragEvent.ACTION_DROP:
        if (character.isPresent()) {
          Item item = (Item) event.getLocalState();
          if (moveFirst) {
            character.get().moveItemFirst(item);
          } else {
            character.get().moveItemLast(item);
          }
        }
        return true;

      default:
        return false;
    }
  }
}
