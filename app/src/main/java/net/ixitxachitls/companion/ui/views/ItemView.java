/*
 * Copyright (c) 2017-2018 Peter Balsiger
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

package net.ixitxachitls.companion.ui.views;

import android.content.ClipData;
import android.content.Context;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Creature;
import net.ixitxachitls.companion.data.documents.Encounter;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.ui.dialogs.EditItemDialog;
import net.ixitxachitls.companion.ui.views.wrappers.AbstractWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Texts;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * View for a single item.
 */
public class ItemView extends LinearLayout implements View.OnDragListener {

  private static final float MIN_DRAG_DISTANCE = 20;

  private final Campaign campaign;
  private final Optional<? extends Creature<?>> creature;
  private final Optional<Encounter> encounter;
  private final Item item;
  private final TextWrapper<TextView> name;
  private final TextWrapper<TextView> value;
  private final TextWrapper<TextView> weight;
  private final Wrapper<LinearLayout> title;
  private final Wrapper<LinearLayout> details;
  private final TextWrapper<TextView> appearance;
  private final TextWrapper<TextView> description;
  private final Wrapper<LinearLayout> contents;

  private boolean expanded = false;
  private boolean showBottomMargin = false;
  private boolean showTopMargin = false;
  private float touchStartX = 0;
  private float touchStartY = 0;

  public ItemView(Context context, Campaign campaign, Creature<? extends Creature> creature,
                  Item item) {
    this(context, campaign, Optional.of(creature), Optional.empty(), item);
  }

  public ItemView(Context context, Campaign campaign, Encounter encounter, Item item) {
    this(context, campaign, Optional.empty(), Optional.of(encounter), item);
  }

  private ItemView(Context context, Campaign campaign, Optional<? extends Creature<?>> creature,
                   Optional<Encounter> encounter, Item item) {
    super(context);
    this.campaign = campaign;
    this.creature = creature;
    this.encounter = encounter;
    this.item = item;

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_item, this, false);
    title = Wrapper.<LinearLayout>wrap(view, R.id.title).onTouch(this::handleTouch);
    name = TextWrapper.wrap(view, R.id.name);
    value = TextWrapper.wrap(view, R.id.value);
    weight = TextWrapper.wrap(view, R.id.weight);
    TextWrapper<TextView> edit = TextWrapper.wrap(view, R.id.edit);
    if ((creature.isPresent() && creature.get().amPlayer()) || campaign.amDM()) {
      edit.onClick(this::edit);
    } else {
      edit.gone();
    }

    details = Wrapper.<LinearLayout>wrap(view, R.id.details).gone();
    appearance = TextWrapper.wrap(view, R.id.appearance);
    description = TextWrapper.wrap(view, R.id.description);
    contents = Wrapper.<LinearLayout>wrap(view, R.id.contents);
    update();

    addView(view);
    setOnDragListener(this);
  }

  public Item getItem() {
    return item;
  }

  public boolean isExpanded() {
    return expanded;
  }

  @Override
  public boolean onDrag(View view, DragEvent event) {
    switch (event.getAction()) {
      case DragEvent.ACTION_DRAG_STARTED:
      case DragEvent.ACTION_DRAG_ENTERED:
        return true;

      case DragEvent.ACTION_DRAG_LOCATION:
        if ((item.isContainer() || item.similar((Item) event.getLocalState()))
            && (event.getY() > 10 || event.getY() < getHeight() - 10)) {
          showNoInsert();
          title.backgroundColor(R.color.itemLight);
        } else {
          if (getHeight() / 2 > event.getY()) {
            showTopInsert();
          } else {
            showBottomInsert();
          }
        }
        return true;

      case DragEvent.ACTION_DRAG_ENDED:
      case DragEvent.ACTION_DRAG_EXITED:
        showNoInsert();
        return true;

      case DragEvent.ACTION_DROP:
        if (creature.isPresent()) {
          if (showTopMargin) {
            creature.get().moveItemBefore(item, (Item) event.getLocalState());
          } else if (showBottomMargin) {
            creature.get().moveItemAfter(item, (Item) event.getLocalState());
          } else {
            if (item.isContainer()) {
              creature.get().moveItemInto(item, (Item) event.getLocalState());
            } else {
              creature.get().combine(item, (Item) event.getLocalState());
            }
          }
          return true;
        } else {
          return false;
        }

      default:
        return false;
    }
  }

  public void toggleDetails() {
    expanded = !expanded;
    details.visible(expanded);
  }

  public void update() {
    name.text(buildItemName());
    value.text(item.getValue().toString());
    weight.text(item.getWeight().toString());
    appearance.text(item.getAppearance());
    description.text(Texts.toSpanned(getContext(), item.getDescription()));

    Map<Item, ItemView> views = collectItemViews();
    contents.get().removeAllViews();
    for (Item content : item.getContents()) {
      if (!creature.isPresent() || !creature.get().isWearing(content)) {
        ItemView view = views.get(content);
        if (view == null) {
          view = new ItemView(getContext(), campaign, creature, encounter, content);
        } else {
          view.update();
        }

        contents.get().addView(view);
      }
    }
  }

  private String buildItemName() {
    String prefix = item.getMultiple() > 1 ? item.getMultiple() + "x " : "";
    String postfix = item.getMultiuse() > 1 ? " (" + item.getMultiuse() + " uses)" : "";

    if (creature.isPresent() && creature.get().amPlayer()) {
      return prefix + item.getPlayerName() + postfix;
    } else {
      return prefix + item.getName() + postfix;
    }
  }

  private Map<Item, ItemView> collectItemViews() {
    Map<Item, ItemView> views = new HashMap<>();
    for (int i = 0; i < contents.get().getChildCount(); i++) {
      ItemView view = (ItemView) contents.get().getChildAt(i);
      views.put(view.getItem(), view);
    }

    return views;
  }

  private void edit() {
    EditItemDialog.newInstance(encounter.isPresent() ? encounter.get().getId() : "",
        creature.isPresent() ? creature.get().getId() : "", item.getId())
        .onSaved((v) -> update())
        .display();
  }

  private boolean handleTouch(MotionEvent event) {
    switch(event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        touchStartX = event.getX();
        touchStartY = event.getY();
        return true;

      case MotionEvent.ACTION_MOVE:
        if ((!creature.isPresent() ||creature.get().canEdit())
            && (Math.abs((int) (touchStartX - event.getX())) > MIN_DRAG_DISTANCE
                || Math.abs((int) (touchStartY - event.getY())) > MIN_DRAG_DISTANCE)) {
          startDragAndDrop(ClipData.newPlainText("name", item.getName()),
              new ItemDragShadowBuilder(this), item, 0);
          ((ViewGroup) getParent()).removeView(this);
        }
        return true;

      case MotionEvent.ACTION_UP:
        toggleDetails();
        return true;

      default:
        return false;
    }
  }

  private void showBottomInsert() {
    if (showBottomMargin) {
      return;
    }

    showTopMargin = false;
    showBottomMargin = true;
    title.margin(AbstractWrapper.Margin.BOTTOM, 10);
    title.backgroundColor(R.color.item);
  }

  private void showNoInsert() {
    showBottomMargin = false;
    showTopMargin = false;
    title.margin(AbstractWrapper.Margin.TOP, 0);
    title.backgroundColor(R.color.item);
  }

  private void showTopInsert() {
    if (showTopMargin) {
      return;
    }

    showBottomMargin = false;
    showTopMargin = true;
    title.margin(AbstractWrapper.Margin.TOP, 10);
    title.backgroundColor(R.color.item);
  }


}
