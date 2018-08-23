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
import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.Item;
import net.ixitxachitls.companion.ui.views.wrappers.AbstractWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Texts;

/**
 * Created by balsiger on 8/14/18.
 */
public class ItemView extends LinearLayout implements View.OnDragListener {

  private final BaseCreature<?> creature;
  private final Item item;
  private final Wrapper<LinearLayout> title;
  private final TextWrapper<TextView> name;
  private final Wrapper<LinearLayout> details;

  private boolean expanded = false;
  private boolean showBottomMargin = false;
  private boolean showTopMargin = false;

  public ItemView(Context context, BaseCreature<?> creature, Item item) {
    super(context);
    this.creature = creature;
    this.item = item;

    View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_item, this, false);
    title = Wrapper.<LinearLayout>wrap(view, R.id.title).onTouch(this::handleTouch);
    name = TextWrapper.wrap(view, R.id.name).text(item.getName());
    TextWrapper.wrap(view, R.id.summary).text(item.summary());

    details = Wrapper.<LinearLayout>wrap(view, R.id.details).gone();
    TextWrapper.wrap(view, R.id.appearance).text(item.getAppearance());
    TextWrapper.wrap(view, R.id.description).text(
        Texts.toSpanned(getContext(), item.getDescription()));

    addView(view);
    setOnDragListener(this);
  }

  public void toggleDetails() {
    expanded = !expanded;
    details.visible(expanded);
  }

  @Override
  public boolean onDrag(View view, DragEvent event) {
    switch (event.getAction()) {
      case DragEvent.ACTION_DRAG_STARTED:
      case DragEvent.ACTION_DRAG_ENTERED:
        return true;

      case DragEvent.ACTION_DRAG_LOCATION:
        if (item.isContainer() && (event.getY() > 10 || event.getY() < getHeight() - 10)) {
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

      case DragEvent.ACTION_DRAG_EXITED:
        showNoInsert();
        return true;

      case DragEvent.ACTION_DROP:
        if (showTopMargin) {
          creature.moveItemBefore(item, (Item) event.getLocalState());
        } else if (showBottomMargin) {
          creature.moveItemAfter(item, (Item) event.getLocalState());
        }
        return true;

      default:
        return false;
    }
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

  private boolean handleTouch(MotionEvent event) {
    switch(event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        return true;

      case MotionEvent.ACTION_MOVE:
        startDragAndDrop(ClipData.newPlainText("name", item.getName()),
            new ItemDragShadowBuilder(name.get()), item, 0);
        ((ViewGroup) getParent()).removeView(this);
        return true;

      case MotionEvent.ACTION_UP:
        toggleDetails();
        return true;

      default:
        return false;
    }
  }


}
