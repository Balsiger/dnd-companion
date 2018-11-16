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

import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.EditText;

import net.ixitxachitls.companion.R;

import java.util.Optional;

/**
 * Wrapper for edit texts.
 */
public final class EditTextWrapper<V extends EditText>
    extends AbstractTextWrapper<V, EditTextWrapper<V>> {

  @FunctionalInterface
  public interface Validator {
    public boolean validate(String value);
  }

  private Optional<Validator> validator = Optional.empty();
  private @ColorInt int lineColorValue;

  private EditTextWrapper(View parent, @IdRes int id) {
    super(parent, id);
  }

  private EditTextWrapper(V view) {
    super(view);
  }

  public void hideLine() {
    view.setBackgroundTintList(ColorStateList.valueOf(
        view.getContext().getResources().getColor(R.color.transparent, null)));
  }

  public static <V extends EditText> EditTextWrapper<V> wrap(View parent, @IdRes int id) {
    return new EditTextWrapper<>(parent, id);
  }

  public static <V extends EditText> EditTextWrapper<V> wrap(V view) {
    return new EditTextWrapper<V>(view);
  }

  public EditTextWrapper<V> label(@StringRes int label) {
    return label(view.getContext().getString(label));
  }

  public EditTextWrapper<V> label(String label) {
    view.setHint(label);

    return this;
  }

  public EditTextWrapper<V> lineColor(@ColorRes int color) {
    return lineColorValue(view.getResources().getColor(color, null));
  }

  public EditTextWrapper<V> lineColorValue(@ColorInt int color) {
    this.lineColorValue = color;
    view.setBackgroundTintList(ColorStateList.valueOf(color));

    return this;
  }

  public EditTextWrapper<V> validate(Validator validator) {
    this.validator = Optional.of(validator);
    return onChange(this::validate);
  }

  private void validate() {
    if (validator.isPresent()) {
      if (getText().isEmpty() || validator.get().validate(getText())) {
        clearError();
      } else {
        error();
      }
    }
  }

  public EditTextWrapper<V> error() {
    view.setBackgroundTintList(ColorStateList.valueOf(
        view.getResources().getColor(R.color.error, null)));
    return this;
  }

  public EditTextWrapper<V> clearError() {
    return lineColorValue(lineColorValue);
  }
}
