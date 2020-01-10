/*
 * Copyright (c) 2017-2019 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
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
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * A viewpager to wrap a view as content.
 */
public class ViewViewPager extends ViewPager {

  public ViewViewPager(@NonNull Context context) {
    super(context);
  }

  public ViewViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    int height = 0;
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
      int h = child.getMeasuredHeight();
      if (h > height) {
        height = h;
      }
    }

    int tabHeight = getChildAt(0).getMeasuredHeight();
    if (height != 0) {
      heightMeasureSpec = MeasureSpec.makeMeasureSpec(height + tabHeight, MeasureSpec.EXACTLY);
    }

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  public static class StaticViewPagerAdapter extends PagerAdapter {

    private final List<Entry> entries;

    public StaticViewPagerAdapter(List<Entry> views) {
      this.entries = views;
    }

    @Override
    public int getCount() {
      return entries.size();
    }

    @Override
    public String getPageTitle(int position) {
      if (position < entries.size()) {
        return entries.get(position).title;
      }

      return "";
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
      if (position < entries.size()) {
        View view = entries.get(position).view;
        container.addView(view);
        return view;
      }

      return null;
    }

    @Override
    public void destroyItem (ViewGroup container, int position, Object object) {
      container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
      return view == object;
    }

    public static class Entry {
      private final String title;
      private final View view;

      public Entry(String title, View view) {
        this.title = title;
        this.view = view;
      }
    }
  }
}
