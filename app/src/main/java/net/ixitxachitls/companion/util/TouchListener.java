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

package net.ixitxachitls.companion.util;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

/**
 * Listener for swipe events.
 */
public class TouchListener implements View.OnTouchListener {

  private final GestureDetector gestureDetector;
  private Optional<Wrapper.Action> onSwipeLeft = Optional.empty();
  private Optional<Wrapper.Action> onSwipeRight = Optional.empty();
  private Optional<Wrapper.Action> onSwipeTop = Optional.empty();
  private Optional<Wrapper.Action> onSwipeBottom = Optional.empty();
  private Optional<Wrapper.Action> onDoubleTap = Optional.empty();

  public TouchListener(Context context) {
    gestureDetector = new GestureDetector(context, new GestureListener());
  }

  public TouchListener onDoubleTap(Wrapper.Action action) {
    this.onDoubleTap = Optional.of(action);
    return this;
  }

  public TouchListener onSwipeBottom(Wrapper.Action action) {
    this.onSwipeBottom = Optional.of(action);
    return this;
  }

  public TouchListener onSwipeLeft(Wrapper.Action action) {
    this.onSwipeLeft = Optional.of(action);
    return this;
  }

  public TouchListener onSwipeRight(Wrapper.Action action) {
    this.onSwipeRight = Optional.of(action);
    return this;
  }

  public TouchListener onSwipeTop(Wrapper.Action action) {
    this.onSwipeTop = Optional.of(action);
    return this;
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    return gestureDetector.onTouchEvent(event);
  }

  private void doOnDoubleTap() {
    if (onDoubleTap.isPresent()) {
      onDoubleTap.get().execute();
    }
  }

  private void doOnSwipeBottom() {
    if (onSwipeBottom.isPresent()) {
      onSwipeBottom.get().execute();
    }
  }

  private void doOnSwipeLeft() {
    if (onSwipeLeft.isPresent()) {
      onSwipeLeft.get().execute();
    }
  }

  private void doOnSwipeRight() {
    if (onSwipeRight.isPresent()) {
      onSwipeRight.get().execute();
    }
  }

  private void doOnSwipeTop() {
    if (onSwipeTop.isPresent()) {
      onSwipeTop.get().execute();
    }
  }

  private class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      boolean result = false;
      try {
        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();
        if (Math.abs(diffX) > Math.abs(diffY)) {
          if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (diffX > 0) {
              doOnSwipeRight();
            } else {
              doOnSwipeLeft();
            }
            result = true;
          }
        }
        else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
          if (diffY > 0) {
            doOnSwipeBottom();
          } else {
            doOnSwipeTop();
          }
          result = true;
        }
      } catch (Exception e) {
        Status.exception("Error in touch listener: ", e);
      }
      return result;
    }

    @Override
    public boolean onDown(MotionEvent event) {
      return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
      doOnDoubleTap();
      return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
      return true;
    }
  }
}
