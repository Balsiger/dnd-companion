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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

/**
 * An image view that also serves as a drop target for certain other views.
 */
public class ImageDropTarget extends LinearLayout implements View.OnDragListener {

  // State.
  private final boolean round;
  private ColorStateList tint;

  // UI elements.
  private Wrapper<ImageView> image;
  private TextWrapper<TextView> text;

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
    this(context, null);
  }

  public ImageDropTarget(Context context, AttributeSet attributes) {
    super(context, attributes);
    this.round = false;

    TypedArray array =
        getContext().obtainStyledAttributes(attributes, R.styleable.ImageDropTarget);
    init(array.getDrawable(R.styleable.ImageDropTarget_image),
        array.getString(R.styleable.ImageDropTarget_text));
  }

  public ImageDropTarget(Context context, Drawable icon, String text, boolean round) {
    super(context);
    this.round = round;

    init(icon, text);
  }

  private void init(Drawable icon, String text) {
    View view = LayoutInflater.from(getContext()).inflate(
        round ? R.layout.view_image_drop_target_round : R.layout.view_image_drop_target, this,
        false);
    this.image = Wrapper.<ImageView>wrap(view, R.id.icon);
    this.image.get().setImageDrawable(icon);
    this.text = TextWrapper.wrap(view, R.id.text).text(text).noWrap();
    addView(view);

    if (image.get().getBackgroundTintList() == null) {
      tint = ColorStateList.valueOf(getResources().getColor(R.color.black, null));
    } else {
      tint = image.get().getBackgroundTintList();
    }
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
        image.get().setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.drag_over,
            null)));
        text.textColor(R.color.drag_over);
        return true;

      case DragEvent.ACTION_DRAG_EXITED:
        image.get().setImageTintList(tint);
        text.textColorValue(tint.getDefaultColor());
        return true;

      case DragEvent.ACTION_DROP:
        image.get().setImageTintList(tint);
        text.textColorValue(tint.getDefaultColor());
        if (dropExecutor.isPresent()) {
          return dropExecutor.get().execute(event.getLocalState());
        }
        return false;

      default:
        return false;
    }
  }
}
