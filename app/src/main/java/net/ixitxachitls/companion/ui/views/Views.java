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

package net.ixitxachitls.companion.ui.views;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Static utility methods for views.
 */
public class Views {

  @FunctionalInterface
  public interface Id<V, T> {
    T getId(V view);
  }

  @FunctionalInterface
  public interface Update<V extends View> {
    void update(V view);
  }

  @FunctionalInterface
  public interface UpdateValue<V extends View, T> {
    void update(V view, T value);
  }

  @FunctionalInterface
  public interface Factory<V extends View, T> {
    V create(T value);
  }

  public static <V extends View> void updateChildren(ViewGroup group, Update<V> update) {
    for (int i = 0; i < group.getChildCount(); i++) {
      update.update((V) group.getChildAt(i));
    }
  }

  public static <V extends View, T> void updateOrCreateChildren(List<T> values, Id<T, String> valueId,
                                                                ViewGroup group,
                                                                Id<V, String> viewId,
                                                                UpdateValue<V, T> update,
                                                                Factory<V, T> factory) {
  }

}
