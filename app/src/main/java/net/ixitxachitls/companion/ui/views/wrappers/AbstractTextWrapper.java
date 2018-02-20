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

package net.ixitxachitls.companion.ui.views.wrappers;

import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Abstrat wrapper for text views.
 */
class AbstractTextWrapper<V extends TextView, W extends AbstractTextWrapper<V, W>>
    extends AbstractWrapper<V, W> {

  public enum Align { LEFT, RIGHT, CENTER };

  protected AbstractTextWrapper(View parent, @IdRes int id) {
    super(parent, id);
  }

  protected AbstractTextWrapper(V view) {
    super(view);
  }

  @SuppressWarnings("unchecked")
  public W noWrap() {
    view.setMaxLines(1);
    view.setEllipsize(TextUtils.TruncateAt.END);

    return (W) this;
  }

  public W text(@StringRes int text) {
    return text(view.getContext().getString(text));
  }

  public W ems(int ems) {
    view.setEms(ems);

    return (W) this;
  }

  public W align(Align align) {
    switch (align) {
      case LEFT:
        view.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        break;

      case RIGHT:
        view.setGravity(Gravity.RIGHT);
        break;

      case CENTER:
        view.setGravity(Gravity.CENTER);
        break;
    }

    return (W) this;
  }

  public W bold() {
    view.setTypeface(Typeface.DEFAULT_BOLD);

    return (W) this;
  }

  @SuppressWarnings("unchecked")
  public W text(String text) {
    view.setText(text);

    return (W) this;
  }

  @SuppressWarnings("unchecked")
  public W textColor(@ColorRes int color) {
    return textColorValue(view.getResources().getColor(color, null));
  }

  @SuppressWarnings("unchecked")
  public W textColorValue(@ColorInt int color) {
    view.setTextColor(color);

    return (W) this;
  }

  @SuppressWarnings("unchecked")
  public W textStyle(@StyleRes int style) {
    view.setTextAppearance(style);

    return (W) this;
  }

  @SuppressWarnings("unchecked")
  public W onEdit(Wrapper.Action action) {
    view.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        action.execute();
        return false;
      }
    });

    return (W) this;
  }

  @SuppressWarnings("unchecked")
  public W onChange(Wrapper.Action action) {
    view.addTextChangedListener(new TextChangeWatcher(action));

    return (W) this;
  }

  @SuppressWarnings("unchecked")
  public W enabled(boolean enabled) {
    view.setEnabled(enabled);

    return (W) this;
  }

  public String getText() {
    return view.getText().toString();
  }

  private static class TextChangeWatcher implements TextWatcher {

    private final Wrapper.Action action;
    private boolean changing = false;
    private boolean first = false;

    public TextChangeWatcher(Wrapper.Action action) {
      this.action = action;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      Log.d("before", "before");
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      // Prevent actions from changing text and triggering an endless loop.
      // Ignore the first trigger, as this comes from the first initilization, when
      // we don't have any valid data yet.
      if (changing || first) {
        first = false;
        return;
      }

      changing = true;
      action.execute();
      changing = false;
    }

    @Override
    public void afterTextChanged(Editable s) {
      Log.d("after", "after");
    }
  }
}
