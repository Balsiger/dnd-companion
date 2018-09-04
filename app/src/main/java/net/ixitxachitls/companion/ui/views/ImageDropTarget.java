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

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;

import net.ixitxachitls.companion.R;

import java.util.Optional;

/**
 * An image view that also serves as a drop target for certain other views.
 */
public class ImageDropTarget<S> extends android.support.v7.widget.AppCompatImageView
    implements View.OnDragListener {

  @FunctionalInterface
  public interface Support {
    boolean supports(Object state);
  }

  @FunctionalInterface
  public interface Executor {
    boolean execute(Object state);
  }

  private Optional<Support> support = Optional.empty();
  private Optional<Executor> dropExecutor = Optional.empty();

  public ImageDropTarget(Context context) {
    super(context);
    setOnDragListener(this);
  }

  public ImageDropTarget(Context context, AttributeSet attrs) {
    super(context, attrs);
    setOnDragListener(this);
  }

  public ImageDropTarget(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setOnDragListener(this);
  }

  public void setSupport(Support support) {
    this.support = Optional.of(support);
  }

  public void setDropExecutor(Executor executor) {
    this.dropExecutor = Optional.of(executor);
  }

  @Override
  public boolean onDrag(View view, DragEvent event) {
    switch (event.getAction()) {
      case DragEvent.ACTION_DRAG_STARTED:
        if (true) return true;
        if (support.isPresent()) {
          return support.get().supports(event.getLocalState());
        } else {
          return false;
        }

      case DragEvent.ACTION_DRAG_ENTERED:
        setTint(R.color.battle);
        return true;

      case DragEvent.ACTION_DRAG_EXITED:
        setTint(R.color.item);
        return true;

      case DragEvent.ACTION_DROP:
        setTint(R.color.item);
        if (dropExecutor.isPresent()) {
          return dropExecutor.get().execute(event.getLocalState());
        }
        return false;

      default:
        return false;
    }
  }

  private void setTint(@ColorRes int color) {
    setImageTintList(ColorStateList.valueOf(getResources().getColor(color, null)));
  }
}
