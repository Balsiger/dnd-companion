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

package net.ixitxachitls.companion.ui;

import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A wrapper for view for easier building and setup.
 */
public class Wrapper<V extends View> {
  private final V view;

  public static <V extends View> Wrapper<V> wrap(View parent, @IdRes int id) {
    return new Wrapper<>(parent, id);
  }

  @SuppressWarnings("unchecked")
  private Wrapper(View parent, @IdRes int id) {
    this.view = (V) parent.findViewById(id);
  }

  public Wrapper<V> text(@StringRes int text) {
    return text(view.getContext().getString(text));
  }

  public Wrapper<V> text(String text) {
    if (TextView.class.isAssignableFrom(view.getClass())) {
      ((TextView) view).setText(text);
    } else {
      error("Cannot set text");
    }

    return this;
  }

  public Wrapper<V> label(@StringRes int label) {
    return label(view.getContext().getString(label));
  }

  public Wrapper<V> label(String label) {
    if (view instanceof EditText) {
      ((EditText) view).setHint(label);
    } else {
      error("Cannot set label");
    }

    return this;
  }

  public Wrapper<V> textColor(@ColorRes int color) {
    if (view instanceof TextView) {
      ((TextView) view).setTextColor(view.getResources().getColor(color, null));
    } else {
      error("Cannot set text color");
    }

    return this;
  }

  public Wrapper<V> backgroundColor(@ColorRes int color) {
    view.setBackgroundColor(view.getResources().getColor(color, null));

    return this;
  }

  private void error(String message){
    throw new IllegalStateException(message + ": " + view.getClass().getSimpleName());
  }

  public Wrapper<V> onClick(Action action) {
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        action.execute();
      }
    });
    return this;
  }

  public Wrapper<V> onEdit(Action action) {
    if (view instanceof TextView) {
      ((TextView) view).setOnEditorActionListener(new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
          action.execute();
          return false;
        }
      });
    } else {
      error("Cannot set onEdit");
    }

    return this;
  }

  public Wrapper<V> onChange(Action action) {
    if (view instanceof TextView) {
      ((TextView) view).addTextChangedListener(new TextChangeWatcher(action));
    } else {
      error("Cannot set onChange");
    }

    return this;
  }

  public Wrapper<V> elevate(int elevation) {
    view.setElevation(2 * elevation);
    view.setPadding(view.getPaddingLeft() - elevation,
        view.getPaddingTop() - elevation,
        view.getPaddingRight(),
        view.getPaddingBottom());

    return this;
  }

  public Wrapper<V> visible() {
    view.setVisibility(View.VISIBLE);

    return this;
  }

  public Wrapper<V> invisible() {
    view.setVisibility(View.INVISIBLE);

    return this;
  }

  public Wrapper<V> gone() {
    view.setVisibility(View.GONE);

    return this;
  }

  public V get() {
    return view;
  }

  @FunctionalInterface
  public interface Action {
    void execute();
  }

  private static class TextChangeWatcher implements TextWatcher {

    private final Action action;

    public TextChangeWatcher(Action action) {
      this.action = action;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      action.execute();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
  }
}
