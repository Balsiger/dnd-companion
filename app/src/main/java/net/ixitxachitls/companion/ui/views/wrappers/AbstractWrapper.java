/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Player Companion.
 *
 * The Player Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Player Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.views.wrappers;

import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.view.MotionEvent;
import android.view.View;

/**
 * Abstact base wrapper.
 */
class AbstractWrapper<V extends View, W extends AbstractWrapper<V, W>> {
  protected final V view;

  @SuppressWarnings("unchecked")
  protected AbstractWrapper(View parent, @IdRes int id) {
    this.view = (V) parent.findViewById(id);
  }

  public W backgroundColor(@ColorRes int color) {
    view.setBackgroundColor(view.getResources().getColor(color, null));

    return (W) this;
  }

  public W onClick(Wrapper.Action action) {
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        action.execute();
      }
    });

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

  public V get() {
    return view;
  }
}
