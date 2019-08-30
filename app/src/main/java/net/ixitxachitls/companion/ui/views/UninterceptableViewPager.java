/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * A view pager that should not be intercepatable (this does not seem to work though).
 */
public class UninterceptableViewPager extends ViewPager {

  public UninterceptableViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    boolean ret = super.onInterceptTouchEvent(ev);
    if (ret) {
      getParent().requestDisallowInterceptTouchEvent(true);
    }
    return ret;
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    boolean ret = super.onTouchEvent(ev);
    if (ret) {
      getParent().requestDisallowInterceptTouchEvent(true);
    }
    return ret;
  }
}