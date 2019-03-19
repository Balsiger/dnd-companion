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
import android.widget.EditText;

import net.ixitxachitls.companion.ui.views.wrappers.AbstractWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * A editable text view with a label and description.
 */
public class LabelledEditTextView extends LabelledTextView<LabelledEditTextView, EditText> {

  // Ui elements.
  protected EditTextWrapper<EditText> text;

  public LabelledEditTextView(Context context, AttributeSet attributes) {
    super(context, attributes);
  }

  public boolean isEmpty() {
    return text.isEmpty();
  }

  public LabelledEditTextView label(String label) {
    this.label.text(label);

    return this;
  }

  public LabelledEditTextView onBlur(Wrapper.Action action) {
    text.onBlur(action);
    return this;
  }

  public LabelledEditTextView onChange(Wrapper.Action action) {
    text.onChange(action);

    return this;
  }

  public LabelledEditTextView onEdit(Wrapper.Action action) {
    text.onEdit(action);

    return this;
  }

  public LabelledEditTextView validate(EditTextWrapper.Validator validator) {
    text.validate(validator);
    return this;
  }

  @Override
  protected EditText createTextView() {
    text = EditTextWrapper.wrap(new EditText(getContext()));
    text.get().setBackground(null);
    text.padding(AbstractWrapper.Padding.BOTTOM, 0);
    text.padding(AbstractWrapper.Padding.TOP, 0);

    return text.get();
  }
}
