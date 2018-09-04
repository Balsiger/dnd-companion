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

package net.ixitxachitls.companion.ui.views.wrappers;

import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

/**
 * Abstrat wrapper for text views.
 */
public class AbstractTextWrapper<V extends TextView, W extends AbstractTextWrapper<V, W>>
    extends AbstractWrapper<V, W> {

  public enum Align { LEFT, RIGHT, CENTER }

  protected AbstractTextWrapper(View parent, @IdRes int id) {
    super(parent, id);
  }

  protected AbstractTextWrapper(V view) {
    super(view);
  }

  public boolean isEmpty() {
    return view.getText().length() == 0;
  }

  @SuppressWarnings("unchecked")
  public W noWrap() {
    view.setMaxLines(1);
    view.setEllipsize(TextUtils.TruncateAt.END);

    return (W) this;
  }

  @SuppressWarnings("unchecked")
  public W text(@StringRes int text) {
    return text(view.getContext().getString(text));
  }

  @SuppressWarnings("unchecked")
  public W ems(int ems) {
    view.setEms(ems);

    return (W) this;
  }

  @SuppressWarnings("unchecked")
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

  @SuppressWarnings("unchecked")
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
  public W text(Spanned text) {
    view.setText(text);

    return (W) this;
  }

  @SuppressWarnings("unchecked")
  public W append(String text) {
    view.append(text);

    return (W) this;
  }

  @SuppressWarnings("unchecked")
  public W append(Spanned text) {
    view.append(text);

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
    view.setOnEditorActionListener((v, actionId, event) -> {
      action.execute();
      return false;
    });

    return (W) this;
  }

  @SuppressWarnings("unchecked")
  public W onChange(Wrapper.Action action) {
    view.addTextChangedListener(new TextChangeWatcher(action));

    return (W) this;
  }

  public String getText() {
    return view.getText().toString();
  }

  public CharSequence getCharSequence() {
    return view.getText();
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
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      // Prevent actions from changing text and triggering an endless loop.
      // Ignore the first trigger, as this comes from the first initialization, when
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
    }
  }
}
