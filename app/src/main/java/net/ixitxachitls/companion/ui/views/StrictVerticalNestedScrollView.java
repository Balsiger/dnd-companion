/*
 * Copyright (c) 2017-2019 Peter Balsiger
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
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import net.ixitxachitls.companion.Status;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

/**
 * A nested scroll view that does not intercept touch events that are mostly horizontal.
 * This improves interaction with view pagers.
 */
public class StrictVerticalNestedScrollView extends NestedScrollView {
  private GestureDetector gestureDetector;

  public StrictVerticalNestedScrollView(@NonNull Context context) {
    this(context, null, 0);
  }

  public StrictVerticalNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public StrictVerticalNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs,
                                        int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    gestureDetector = new GestureDetector(context, new YScrollDetector());
    setFadingEdgeLength(0);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    return gestureDetector.onTouchEvent(event) && super.onInterceptTouchEvent(event);
  }

  // Return false if we're scrolling in the x direction
  class YScrollDetector extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      Status.error("result: " + (Math.abs(distanceY) > Math.abs(distanceX)));
      return Math.abs(distanceY) > Math.abs(distanceX);
    }
  }
}
