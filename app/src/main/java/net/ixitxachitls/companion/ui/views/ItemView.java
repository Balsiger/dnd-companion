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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Creature;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.ui.dialogs.EditItemDialog;
import net.ixitxachitls.companion.ui.dialogs.ItemDialog;
import net.ixitxachitls.companion.ui.views.wrappers.AbstractWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Texts;

import java.util.HashMap;
import java.util.Map;

/**
 * View for a single item.
 */
public class ItemView extends LinearLayout implements View.OnDragListener {

  private static final float MIN_DRAG_DISTANCE = 10;

  private final Campaign campaign;
  private final Item.Owner owner;
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

  public ItemView(Context context, Campaign campaign, Item.Owner owner, Item item,
                  boolean inContainer) {
    super(context);
    this.campaign = campaign;
    this.owner = owner;
    this.item = item;

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_item, this, false);
    title = Wrapper.<LinearLayout>wrap(view, R.id.title).onTouch(this::handleTouch);
    name = TextWrapper.wrap(view, R.id.name);
    Wrapper.<ImageView>wrap(view, R.id.unpack).visible(inContainer)
        .onClick(this::handleUnpack);
    value = TextWrapper.wrap(view, R.id.value);
    weight = TextWrapper.wrap(view, R.id.weight);
    TextWrapper<TextView> edit = TextWrapper.wrap(view, R.id.edit);
    if (owner.amDM() || campaign.amDM()) {
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
    Status.error("Item Status: " + event.getAction());
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
        //owner.dragEnded(event.getResult());
        showNoInsert();
        return true;

      case DragEvent.ACTION_DRAG_EXITED:
        showNoInsert();
        return true;

      case DragEvent.ACTION_DROP:
        if (showTopMargin) {
          owner.moveItemBefore(item, (Item) event.getLocalState());
        } else if (showBottomMargin) {
          owner.moveItemAfter(item, (Item) event.getLocalState());
        } else {
          if (item.isContainer()) {
            owner.moveItemInto(item, (Item) event.getLocalState());
          } else {
            owner.combine(item, (Item) event.getLocalState());
          }
        }
        return true;

      default:
        return false;
    }
  }

  public void showSummary() {
    ItemDialog.newInstance(item.getId(), owner.getId(), campaign.getId()).display();
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
    description.text(Texts.toSpanned(getContext(), item.getDescription(owner.amDM())));

    Map<Item, ItemView> views = collectItemViews();
    contents.get().removeAllViews();
    for (Item content : item.getContents()) {
      if (owner.isWearing(content)) {
        ItemView view = views.get(content);
        if (view == null) {
          view = new ItemView(getContext(), campaign, owner, content, true);
        } else {
          view.update();
        }

        contents.get().addView(view);
      }
    }
  }

  private String buildItemName() {
    String prefix = item.getCount() > 1 ? item.getCount() + "x " : "";
    String postfix = item.getMultiuse() > 1 ? " (" + item.getMultiuse() + " uses)" : "";
    if (item.getMultiple() > 1) {
      postfix += (postfix.isEmpty() ? "" : " ") + "(" + item.formatAmount() + ")";
    }

    if (owner.amDM()) {
      return prefix + item.getName() + postfix;
    } else {
      return prefix + item.getPlayerName() + postfix;
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
    EditItemDialog.newInstance(owner.getId(), item.getId()).onSaved((v) -> update()).display();
  }

  private boolean handleTouch(MotionEvent event) {
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        touchStartX = event.getX();
        touchStartY = event.getY();
        return true;

      case MotionEvent.ACTION_MOVE:
        if (owner.canEdit()
            && (Math.abs((int) (touchStartX - event.getX())) > MIN_DRAG_DISTANCE
            || Math.abs((int) (touchStartY - event.getY())) > MIN_DRAG_DISTANCE)) {
          startDragAndDrop(ClipData.newPlainText("name", item.getName()),
              new ItemDragShadowBuilder(this), item, 0);
          ((ViewGroup) getParent()).removeView(this);
        }
        return true;

      case MotionEvent.ACTION_UP:
        showSummary();
        return true;

      default:
        return false;
    }
  }

  private void handleUnpack() {
    if (owner instanceof Creature) {
      ((Creature) owner).moveItemLast(item);
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
