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

import java.util.Optional;

/**
 * Listener for swipe events.
 */
public class OnSwipeTouchListener implements View.OnTouchListener {

  private final GestureDetector gestureDetector;
  private Optional<Action> onLeft = Optional.empty();
  private Optional<Action> onRight = Optional.empty();
  private Optional<Action> onTop = Optional.empty();
  private Optional<Action> onBottom = Optional.empty();
  public OnSwipeTouchListener (Context context) {
    gestureDetector = new GestureDetector(context, new GestureListener());
  }

  @FunctionalInterface
  public interface Action {
    void execute();
  }

  public OnSwipeTouchListener onBottom(Action action) {
    this.onBottom = Optional.of(action);
    return this;
  }

  public OnSwipeTouchListener onLeft(Action action) {
    this.onLeft = Optional.of(action);
    return this;
  }

  public OnSwipeTouchListener onRight(Action action) {
    this.onRight = Optional.of(action);
    return this;
  }

  public void onSwipeBottom() {
    if (onBottom.isPresent()) {
      onBottom.get().execute();
    }
  }

  public void onSwipeLeft() {
    if (onLeft.isPresent()) {
      onLeft.get().execute();
    }
  }

  public void onSwipeRight() {
    if (onRight.isPresent()) {
      onRight.get().execute();
    }
  }

  public void onSwipeTop() {
    if (onTop.isPresent()) {
      onTop.get().execute();
    }
  }

  public OnSwipeTouchListener onTop(Action action) {
    this.onTop = Optional.of(action);
    return this;
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    return gestureDetector.onTouchEvent(event);
  }

  private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

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
              onSwipeRight();
            } else {
              onSwipeLeft();
            }
            result = true;
          }
        }
        else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
          if (diffY > 0) {
            onSwipeBottom();
          } else {
            onSwipeTop();
          }
          result = true;
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }
      return result;
    }

    @Override
    public boolean onDown(MotionEvent e) {
      return true;
    }
  }
}
