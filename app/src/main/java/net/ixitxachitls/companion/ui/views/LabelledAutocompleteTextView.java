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
import android.content.res.TypedArray;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * Labelled view for an autocomplete text view.
 */
public class LabelledAutocompleteTextView extends AbstractLabelledView {

  // Ui elements.
  protected EditTextWrapper<? extends AutoCompleteTextView> text;

  public LabelledAutocompleteTextView(Context context) {
    super(context, R.layout.view_labelled_autocomplete);
  }

  public LabelledAutocompleteTextView(Context context, AttributeSet attributes) {
    super(context, attributes, R.layout.view_labelled_autocomplete);
  }

  protected LabelledAutocompleteTextView(Context context, AttributeSet attributes,
                                         @LayoutRes int layoutId) {
    super(context, attributes, layoutId);
  }

  public String getText() {
    return text.getText();
  }

  public void setAdapter(ArrayAdapter<String> adapter) {
    text.get().setAdapter(adapter);
  }

  public LabelledAutocompleteTextView enabled(boolean enabled) {
    text.enabled(enabled);

    return this;
  }

  public LabelledAutocompleteTextView onChange(Wrapper.Action action) {
    text.onChange(action);

    return this;
  }

  public LabelledAutocompleteTextView onEdit(Wrapper.Action action) {
    text.onEdit(action);

    return this;
  }

  public LabelledAutocompleteTextView onFocus(Wrapper.Action focusAction,
                                              Wrapper.Action focusLostAction) {
    text.get().setOnFocusChangeListener((view, hasFocus) -> {
      if (hasFocus) {
        focusAction.execute();
      } else {
        focusLostAction.execute();
      }
    });

    return this;
  }

  public LabelledAutocompleteTextView onFocus(Wrapper.Action action) {
    text.get().setOnFocusChangeListener((view, hasFocus) -> action.execute());

    return this;
  }

  public void showDropDown() {
    text.get().showDropDown();
  }

  public LabelledAutocompleteTextView text(String text) {
    this.text.text(text);

    return this;
  }

  public LabelledAutocompleteTextView threshold(int threshold) {
    text.get().setThreshold(threshold);

    return this;
  }

  @Override
  protected void setup(View view, TypedArray array, TypedArray baseArray) {
    super.setup(view, array, baseArray);

    text = EditTextWrapper.wrap(view, R.id.text);
    text.text(array.getString(R.styleable.LabelledTextView_defaultText));
    text.textColorValue(array.getColor(R.styleable.LabelledTextView_textColor,
        getContext().getResources().getColor(R.color.colorPrimary, null)));
    text.lineColorValue(array.getColor(R.styleable.LabelledView_lineColor,
        getContext().getResources().getColor(R.color.colorPrimary, null)));
  }
}
