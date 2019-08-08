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

package net.ixitxachitls.companion.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

/**
 * ArrayAdapter that allows complex layouts for its elemets.
 */
public class ListAdapter<T> extends ArrayAdapter<T> {
  private final LayoutInflater inflator;
  private final int layout;
  private final List<T> items;
  private final ViewBinder<T> binder;

  public ListAdapter(Context context, @LayoutRes int layout, List<T> items,
                     ViewBinder<T> binder) {
    super(context, layout, items);

    this.inflator = LayoutInflater.from(context);
    this.layout = layout;
    this.items = items;
    this.binder = binder;
  }

  public interface ViewBinder<T> {
    public void bind(View view, T item, int position);
  }

  @Override
  public View getView(int position, @Nullable View convertView, ViewGroup parent) {
    View view = convertView;
    if (view == null) {
      view = inflator.inflate(layout, parent, false);
    }
    binder.bind(view, items.get(position), position);
    return view;
  }
}
