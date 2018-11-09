/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * along with the Roleplay Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.views.wrappers;

import android.content.res.ColorStateList;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import net.ixitxachitls.companion.ui.Alert;
import net.ixitxachitls.companion.ui.MessageDialog;

/**
 * Abstact base wrapper.
 */
public class AbstractWrapper<V extends View, W extends AbstractWrapper<V, W>> {
  protected final V view;

  // State values that need to be set on attach.
  private int gravity;

  public enum Margin { LEFT, RIGHT, TOP, BOTTOM };
  public enum Padding { LEFT, RIGHT, TOP, BOTTOM, ALL, LEFT_RIGHT, TOP_BOTTOM };

  @SuppressWarnings("unchecked")
  protected AbstractWrapper(View parent, @IdRes int id) {
    this(parent.findViewById(id));
  }

  protected AbstractWrapper(V view) {
    this.view = view;
  }

  public W backgroundColor(@ColorRes int color) {
    view.setBackgroundColor(view.getResources().getColor(color, null));

    return (W) this;
  }

  private int dpToPx(int dp) {
    DisplayMetrics metrics = view.getContext().getResources().getDisplayMetrics();
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
  }

  public W padding(Padding position, int paddingDp) {
    int paddingPx = dpToPx(paddingDp);
    int left = view.getPaddingLeft();
    int top = view.getPaddingTop();
    int right = view.getPaddingRight();
    int bottom = view.getPaddingBottom();

    switch (position) {
      case LEFT:
        left = paddingPx;
        break;

      case RIGHT:
        right = paddingPx;
        break;

      case TOP:
        top = paddingPx;
        break;

      case BOTTOM:
        bottom = paddingPx;
        break;

      case ALL:
        left = paddingPx;
        right = paddingPx;
        top = paddingPx;
        bottom = paddingPx;
        break;

      case LEFT_RIGHT:
        left = paddingPx;
        right = paddingPx;
        break;

      case TOP_BOTTOM:
        top = paddingPx;
        bottom = paddingPx;
        break;
    }

    view.setPadding(left, top, right, bottom);

    return (W) this;
  }

  // NOTE: This method _MUST_ be called after adding the view to the parent.
  @SuppressWarnings("unchecked")
  public W margin(Margin position, int marginDp) {
    int marginPx = dpToPx(marginDp);
    int left = 0;
    int top = 0;
    int right = 0;
    int bottom = 0;

    switch (position) {
      case LEFT:
        left = marginPx;
        break;

      case RIGHT:
        right = marginPx;
        break;

      case TOP:
        top = marginPx;
        break;

      case BOTTOM:
        bottom = marginPx;
        break;
    }

    ViewGroup.LayoutParams params = view.getLayoutParams();
    if (params instanceof ViewGroup.MarginLayoutParams) {
      ((ViewGroup.MarginLayoutParams) params).setMargins(left, top, right, bottom);
    } else {
      Alert.show(view.getContext(), "Rendering error",
          "Layout params " + params.getClass() + " not supported. Margin not set.");
    }

    view.setLayoutParams(params);

    return (W) this;
  }

  // NOTE: This method _MUST_ be called after adding the view to the parent.
  public W weight(float weight) {
    ViewGroup.LayoutParams params = view.getLayoutParams();
    if (params instanceof LinearLayout.LayoutParams) {
      ((LinearLayout.LayoutParams) params).weight = weight;
    } else {
      Alert.show(view.getContext(), "Rendering error",
          "Layout params " + params.getClass() + " not supported. Weight not set.");
    }


    return (W) this;
  }

  public W onClick(Wrapper.Action action) {
    view.setOnClickListener(v -> action.execute());

    return (W) this;
  }

  public W removeClick() {
    view.setOnClickListener(null);

    return (W) this;
  }

  public W onTouch(Wrapper.TouchAction action) {
    view.setOnTouchListener((v, event) -> action.execute(event));

    return (W) this;
  }

  public W onBlur(Wrapper.Action action) {
    view.setOnFocusChangeListener((view, hasFocus) -> {
      if (!hasFocus) {
        action.execute();
      }
    });

    return (W) this;
  }

  public W description(String name, String description) {
    onLongClick(()
        -> MessageDialog.create(get().getContext()).message(description).title(name).show());

    return (W) this;
  }

  public W description(String name, @LayoutRes int layout) {
    onLongClick(()
        -> MessageDialog.create(get().getContext()).layout(layout).title(name).show());

    return (W) this;
  }

  public W onLongClick(Wrapper.Action action) {
    view.setOnLongClickListener(v -> { action.execute(); return true; });

    return (W) this;
  }

  public W onTouch(Wrapper.Action action, int onAction) {
    view.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == onAction) {
          action.execute();
          return true;
        }
        return false;
      }
    });

    return (W) this;
  }

  public W onFocusLost(Wrapper.Action action) {
    view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
          action.execute();
        }
      }
    });

    return (W) this;
  }

  public W elevate(int elevation) {
    view.setElevation(2 * elevation);
    view.setPadding(view.getPaddingLeft() - elevation,
        view.getPaddingTop() - elevation,
        view.getPaddingRight(),
        view.getPaddingBottom());

    return (W) this;
  }

  public W visible(boolean visible) {
    view.setVisibility(visible ? View.VISIBLE : View.GONE);

    return (W) this;
  }

  public W visible() {
    view.setVisibility(View.VISIBLE);

    return (W) this;
  }

  public W invisible() {
    view.setVisibility(View.INVISIBLE);

    return (W) this;
  }

  public W gone() {
    view.setVisibility(View.GONE);

    return (W) this;
  }

  public W enabled(boolean enabled) {
    view.setEnabled(enabled);

    return (W) this;
  }

  public W enabled() {
    return enabled(true);
  }

  public W disabled() {
    return enabled(false);
  }

  public W tint(@ColorRes int color) {
    if (view instanceof ImageView) {
      ((ImageView) view).setImageTintList(ColorStateList.valueOf(
          view.getContext().getColor(color)));
    }

    return (W) this;
  }

  public V get() {
    return view;
  }

  public W width(int width) {
    view.getLayoutParams().width = width;

    return (W) this;
  }

  public W height(int height) {
    view.getLayoutParams().height = height;

    return (W) this;
  }
}
