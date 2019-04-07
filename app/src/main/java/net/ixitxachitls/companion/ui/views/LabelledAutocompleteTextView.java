/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import net.ixitxachitls.companion.ui.views.wrappers.AbstractWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * Labelled view for an autocomplete text view.
 */
public class LabelledAutocompleteTextView
    <T extends LabelledAutocompleteTextView, V extends AutoCompleteTextView>
    extends LabelledTextView<T, V> {

  // Ui elements.
  protected EditTextWrapper<V> text;

  public LabelledAutocompleteTextView(Context context, AttributeSet attributes) {
    super(context, attributes);
  }

  @Override
  public String getText() {
    return text.getText();
  }

  @Override
  protected V createTextView() {
    text = EditTextWrapper.<V>wrap((V) new AutoCompleteTextView(getContext()));
    text.get().setBackground(null);
    text.padding(AbstractWrapper.Padding.BOTTOM, 0);
    text.padding(AbstractWrapper.Padding.TOP, 0);

    return (V) text.get();
  }

  public void setAdapter(ArrayAdapter<String> adapter) {
    text.get().setAdapter(adapter);
  }

  public T onBlur(Wrapper.Action action) {
    text.onBlur(action);

    return (T) this;
  }

  public T onChange(Wrapper.Action action) {
    text.onChange(action);

    return (T) this;
  }

  public T onEdit(Wrapper.Action action) {
    text.onEdit(action);

    return (T) this;
  }

  public T onFocus(Wrapper.Action focusAction,
                                              Wrapper.Action focusLostAction) {
    text.get().setOnFocusChangeListener((view, hasFocus) -> {
      if (hasFocus) {
        focusAction.execute();
      } else {
        focusLostAction.execute();
      }
    });

    return (T) this;
  }

  public T onFocus(Wrapper.Action action) {
    text.get().setOnFocusChangeListener((view, hasFocus) -> action.execute());

    return (T) this;
  }

  public void showDropDown() {
    text.get().showDropDown();
  }

  public T threshold(int threshold) {
    text.get().setThreshold(threshold);

    return (T) this;
  }

  public T validate(EditTextWrapper.Validator validator) {
    text.validate(validator);

    return (T) this;
  }
}
