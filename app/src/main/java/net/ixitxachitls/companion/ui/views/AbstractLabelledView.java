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
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.MessageDialog;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;

/**
 * Base labelled view.
 */
abstract class AbstractLabelledView extends LinearLayout {

  protected static final int ATTR_INPUT_TYPE = 0;

  // UI elements.
  protected TextWrapper<TextView> label;

  AbstractLabelledView(Context context, @LayoutRes int layout) {
    super(context);

    setup(null, layout);
  }

  AbstractLabelledView(Context context, AttributeSet attributes, @LayoutRes int layout) {
    super(context, attributes);

    setup(attributes, layout);
  }

  private void setup(@Nullable AttributeSet attributes, @LayoutRes int layout) {
    TypedArray array =
        getContext().obtainStyledAttributes(attributes, R.styleable.LabelledEditTextView);
    TypedArray baseArray =
        getContext().obtainStyledAttributes(attributes, new int [] { android.R.attr.inputType, });

    View view = LayoutInflater.from(getContext()).inflate(layout, null, false);
    view.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));
    addView(view);

    setup(view, array, baseArray);

    array.recycle();
  }

  @CallSuper
  protected void setup(View view, TypedArray array, TypedArray baseArray) {
    label = TextWrapper.wrap(view, R.id.label);
    label.text(array.getString(R.styleable.LabelledEditTextView_labelText));
    label.textColorValue(array.getColor(R.styleable.LabelledEditTextView_labelColor,
        getContext().getResources().getColor(R.color.colorPrimary, null)));

    String name = array.getString(R.styleable.LabelledEditTextView_labelText);
    String description = array.getString(R.styleable.LabelledEditTextView_descriptionText);
    if (description != null && !description.isEmpty()) {
      label.onLongClick(() -> showDescription(name, description));
    }
  }

  private void showDescription(String name, String description) {
    new MessageDialog(getContext()).title(name).message(description).show();
  }

  public void gone() {
    setVisibility(GONE);
  }
}
