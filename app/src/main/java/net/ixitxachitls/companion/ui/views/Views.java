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

import android.text.Spannable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;

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

  @FunctionalInterface
  public interface Extractor<T extends View> {
    void extract(T view);
  }

  @SuppressWarnings("unchecked")
  public static <T extends View> void extractDataFromChildren(ViewGroup group,
                                                              Extractor<T> extractor) {
    for (int i = 0; i < group.getChildCount(); i++) {
      try {
        extractor.extract((T) group.getChildAt(i));
      } catch (ClassCastException e) {
        // Ignore other views.
      }
    }
  }

  private static boolean hasTag(View view, String tag) {
    return tag.equals(view.getTag());
  }

  public static void setOrHide(TextWrapper<TextView> view, String text, View... others) {
    view.text(text);
    view.visible(!text.isEmpty());
    for (View other : others) {
      other.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
    }
  }

  public static void setOrHide(TextWrapper<TextView> view, Spannable text) {
    view.text(text);
    view.visible(text.length() > 0);
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

  public static void updateVisibility(ViewGroup group, boolean debug, boolean dm) {
    for (int i = 0; i < group.getChildCount(); i++) {
      View view = group.getChildAt(i);
      if (view.getTag() != null) {
        if (view.getVisibility() == View.VISIBLE) {
          view.setVisibility((debug || !hasTag(view, "debug")) && (dm || !hasTag(view, "dm"))
              ? View.VISIBLE : View.GONE);
        }
      } else if (view instanceof ViewGroup) {
        updateVisibility((ViewGroup) view, debug, dm);
      }
    }
  }
}
