/*
 * Copyright (c) 2017-2018 Peter Balsiger
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

import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;

/**
 * A view group whose children can be updated without recreation.
 */
public class UpdatableViewGroup<G extends ViewGroup, V extends View, I> {
  private final G view;
  private Map<I, V> childrenById = new HashMap<>();

  public UpdatableViewGroup(G view) {
    this.view = view;
  }

  @FunctionalInterface
  public interface Factory<V extends View, I> {
    @Nullable
    V create(I id);
  }
  @FunctionalInterface
  public interface Updater<V extends View, I> {
    void update(I id, V view);
  }

  @FunctionalInterface
  public interface SimpleUpdator<V extends View> {
    void update(V view);
  }

  public G getView() {
    return view;
  }

  public void ensureOnly(List<I> ids, Factory<V, I> factory) {
    Map<I, V> current = clearChildren();
    for (I id : ids) {
      V newView = current.get(id);
      if (newView == null) {
        newView = factory.create(id);
      }

      if (newView != null) {
        childrenById.put(id, newView);
        view.addView(newView);
      }
    }
  }

  public void simpleUpdate(SimpleUpdator<V> updater) {
    for (V view : childrenById.values()) {
      updater.update(view);
    }
  }

  public void update(List<I> ids, Updater<V, I> updater) {
    for (I id : ids) {
      updater.update(id, childrenById.get(id));
    }
  }

  private Map<I, V> clearChildren() {
    Map<I, V> current = childrenById;
    childrenById = new HashMap<>();
    view.removeAllViews();

    return current;
  }
}
