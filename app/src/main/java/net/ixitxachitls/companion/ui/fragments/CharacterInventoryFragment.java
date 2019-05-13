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

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Documents;
import net.ixitxachitls.companion.data.documents.Message;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.data.values.Money;
import net.ixitxachitls.companion.data.values.Weight;
import net.ixitxachitls.companion.rules.Items;
import net.ixitxachitls.companion.ui.dialogs.EditItemDialog;
import net.ixitxachitls.companion.ui.views.ImageDropTarget;
import net.ixitxachitls.companion.ui.views.ItemView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Child fragment for a characters inventory.
 */
public class CharacterInventoryFragment extends NestedCompanionFragment {

  private Optional<Character> character = Optional.empty();
  private boolean moveFirst = true;

  private TextWrapper<TextView> wealth;
  private TextWrapper<TextView> weight;
  private ViewGroup items;
  private ViewGroup view;
  private Wrapper<Button> addItem;
  private LinearLayout targetsCharacters;
  private Wrapper<ImageDropTarget> removeItem;
  private Wrapper<ImageDropTarget> sellItem;
  private EnumMap<Items.Slot, Wrapper<ImageView>> slotFigures = new EnumMap<>(Items.Slot.class);
  private EnumMap<Items.Slot, Wrapper<TextView>> slotTitles = new EnumMap<>(Items.Slot.class);
  private EnumMap<Items.Slot, LinearLayout> slotItems = new EnumMap<>(Items.Slot.class);
  private Optional<Items.Slot> lastOpenedSlot = Optional.empty();

  public CharacterInventoryFragment() {
    characters().observe(this, this::refresh);
    images().observe(this, this::refresh);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
    super.onCreateView(inflater, container, state);

    view = (ViewGroup) inflater.inflate(R.layout.fragment_character_inventory, container, false);

    wealth = TextWrapper.wrap(view, R.id.wealth);
    weight = TextWrapper.wrap(view, R.id.weight);
    items = view.findViewById(R.id.items);
    addItem = Wrapper.<Button>wrap(view, R.id.item_add)
        .onClick(this::addItem)
        .description("Add Item", "Add an item to the characters inventory.")
        .visible(character.isPresent() &&
            (character.get().amPlayer() || character.get().amDM()));
    targetsCharacters = view.findViewById(R.id.targets_characters);
    removeItem = Wrapper.<ImageDropTarget>wrap(view, R.id.item_remove)
        .description("Remove Item", "Drag an item over the trash to remove it");
    removeItem.get().setSupport(i -> i instanceof Item);
    removeItem.get().setDropExecutor(this::removeItem);
    removeItem.visible(character.isPresent()
        && (character.get().amPlayer() || character.get().amDM()));
    sellItem = Wrapper.<ImageDropTarget>wrap(view, R.id.item_sell)
        .description("Sell Item", "Sell the item, with DM approval.");
    sellItem.get().setSupport(i -> i instanceof Item);
    sellItem.get().setDropExecutor(this::sellItem);
    sellItem.visible(character.isPresent() && character.get().amPlayer());

    setupSlot(Items.Slot.head, R.id.figure_head, R.id.title_head, R.id.items_head);
    setupSlot(Items.Slot.eyes, R.id.figure_eyes, R.id.title_eyes, R.id.items_eyes);
    setupSlot(Items.Slot.neck, R.id.figure_neck, R.id.title_neck, R.id.items_neck);
    setupSlot(Items.Slot.shoulders, R.id.figure_shoulders, R.id.title_shoulders,
        R.id.items_shoulders);
    setupSlot(Items.Slot.torso, R.id.figure_torso, R.id.title_torso, R.id.items_torso);
    setupSlot(Items.Slot.body, R.id.figure_body, R.id.title_body, R.id.items_body);
    setupSlot(Items.Slot.wrists, R.id.figure_wrists, R.id.title_wrists, R.id.items_wrists);
    setupSlot(Items.Slot.hands, R.id.figure_hands, R.id.title_hands, R.id.items_hands);
    setupSlot(Items.Slot.fingers, R.id.figure_fingers, R.id.title_fingers, R.id.items_fingers);
    setupSlot(Items.Slot.feet, R.id.figure_feet, R.id.title_feet, R.id.items_feet);

    if (character.isPresent()) {
      update(character.get());
    }

    return view;
  }

  public void update(Character character) {
    this.character = Optional.of(character);

    characters().addPlayers(character.getCampaign());

    if (this.items != null && getContext() != null) {
      Money totalValue = Money.ZERO;
      Weight totalWeight = Weight.ZERO;

      Map<Item, ItemView> views = collectItemViews();
      items.removeAllViews();
      for (Item item : character.getItems()) {
        if (!character.isCarrying(item)) {
          ItemView view = views.get(item);
          if (view == null) {
            view = createLine(item);
          } else {
            view.update();
          }
          items.addView(view);
        }

        // TODO(merlin): Move this into character!
        totalValue = totalValue.add(item.getValue());
        totalWeight = totalWeight.add(item.getWeight());
      }

      refreshSlots();

      this.wealth.text(totalValue.simplify().toString());
      this.weight.text(totalWeight.toString());
      view.setOnDragListener((v, e) -> onItemDrag(v, e));
      addItem.visible(character.amPlayer() || character.amDM());

      refreshDropTargets();
    }
  }

  private void addItem() {
    if (character.isPresent()) {
      EditItemDialog.newInstance(character.get().getId()).display();
    } else {
      Status.error("No character available!");
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

  private boolean dragSlot(Items.Slot slot, DragEvent event) {
    switch (event.getAction()) {
      case DragEvent.ACTION_DRAG_STARTED:
        lastOpenedSlot = Optional.empty();
        return true;

      case DragEvent.ACTION_DRAG_ENTERED:
        if (slotItems.get(slot).getVisibility() == View.GONE) {
          lastOpenedSlot = Optional.of(slot);
          toggleSlot(slot);
        }
        return true;

      case DragEvent.ACTION_DRAG_EXITED:
        if (lastOpenedSlot.isPresent()) {
          toggleSlot(lastOpenedSlot.get());
          lastOpenedSlot = Optional.empty();
        }
        return true;

      case DragEvent.ACTION_DRAG_LOCATION:
        // We need to handle this because the default handling in the text view will throw an
        // exception.
        return true;

      case DragEvent.ACTION_DROP:
        if (character.isPresent()) {
          character.get().carry(slot, (Item) event.getLocalState());

          return true;
        } else {
          return false;
        }

      default:
        return false;
    }
  }

  private boolean moveItem(Object state, Character target) {
    if (character.isPresent() && state instanceof Item) {
      character.get().removeItem((Item) state);
      Message.createForItemAdd(context(), character.get().getId(), target.getId(), (Item) state);
      return true;
    }

    return false;
  }

  private boolean onItemDrag(View view, DragEvent event) {
    switch (event.getAction()) {
      case DragEvent.ACTION_DRAG_STARTED:
        return true;

      case DragEvent.ACTION_DRAG_LOCATION:
        int[] locations = new int[2];
        moveFirst = event.getY() < items.getTop();
        Status.log(event.getY() + " : " + items.getTop());
        return true;

      case DragEvent.ACTION_DRAG_ENDED:
        if (event.getResult()) {
          // The drop was handled by some view.
          return true;
        }

        // Fall through to handle like drop!

      case DragEvent.ACTION_DROP:
        if (character.isPresent() && character.get().amPlayer()) {
          Item item = (Item) event.getLocalState();
           if (moveFirst) {
            //character.get().moveItemFirst(item);
          } else {
            //character.get().moveItemLast(item);
          }
        }
        return true;

      default:
        return false;
    }
  }

  private void refresh(Documents.Update update) {
    refreshDropTargets();
  }

  private void refreshDropTargets() {
    if (character.isPresent()) {
      targetsCharacters.removeAllViews();
      for (Character other : characters().getCampaignCharacters(character.get().getCampaignId())) {
        if (!other.equals(character)) {
          Drawable image;
          if (images().get(other.getId(), 1).isPresent()) {
            image = new BitmapDrawable(getResources(), images().get(other.getId(), 1).get());
          } else {
            image = getResources().getDrawable(R.drawable.ic_person_black_48dp, null);
          }
          ImageDropTarget target = new ImageDropTarget(getContext(), image, other.getName(), true);
          target.setSupport(i -> i instanceof Item);
          target.setDropExecutor((i) -> moveItem(i, other));
          targetsCharacters.addView(target);
        }
      }
    }
  }

  private void refreshSlots() {
    for (Items.Slot slot : Items.Slot.values()) {
      slotItems.get(slot).removeAllViews();
      slotTitles.get(slot).backgroundColor(R.color.characterDark);
      if (character.isPresent()) {
        for (String id : character.get().carrying(slot)) {
          Optional<Item> item = character.get().getItem(id);
          if (item.isPresent()) {
            slotItems.get(slot).addView(createLine(item.get()));
            slotTitles.get(slot).backgroundColor(R.color.character);
          } else {
            Status.error("Cannot find item " + id + " for slot " + slot);
          }
        }
      }
    }
  }

  private boolean removeItem(Object state) {
    if (character.isPresent() && state instanceof Item) {
      if (character.get().amPlayer()) {
        character.get().removeItem((Item) state);
        return true;
      } else if (character.get().amDM()) {
        Message.createForItemDelete(
            CompanionApplication.get().context(), character.get().getId(), (Item) state);
        return true;
      }
    }

    return false;
  }

  private boolean sellItem(Object state) {
    if (character.isPresent() && state instanceof Item && character.get().amPlayer()
        && character.get().getCampaign().isPresent()) {
      character.get().removeItem((Item) state);
      Message.createForItemSell(
          CompanionApplication.get().context(), character.get().getId(),
          character.get().getCampaign().get().getId(), (Item) state);
      Status.toast("Item has been sold.");
    }

    return false;
  }

  private void setupSlot(Items.Slot slot, @IdRes int figureId, @IdRes int titleId,
                         @IdRes int itemsId) {
    slotFigures.put(slot, Wrapper.<ImageView>wrap(view, figureId).gone());
    slotTitles.put(slot, Wrapper.<TextView>wrap(view, titleId).onClick(() -> toggleSlot(slot)));
    slotItems.put(slot, view.findViewById(itemsId));
    view.findViewById(itemsId).setVisibility(View.GONE);

    slotTitles.get(slot).onDrag((e) -> dragSlot(slot, e));
  }

  private void toggleSlot(Items.Slot slot) {
    if (slotItems.get(slot).getVisibility() == View.GONE) {
      slotTitles.get(slot).backgroundColor(R.color.slot_selected);
      slotItems.get(slot).setVisibility(View.VISIBLE);
      slotFigures.get(slot).visible();
    } else {
      slotTitles.get(slot).backgroundColor(
          slotItems.get(slot).getChildCount() > 0 ? R.color.character : R.color.characterDark);
      slotItems.get(slot).setVisibility(View.GONE);
      slotFigures.get(slot).gone();
    }
  }
}
