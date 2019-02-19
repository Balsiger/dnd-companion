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
import android.util.AttributeSet;
import android.view.View;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * A text view with a label and description.
 */
public class LabelledTextView extends LabelledEditTextView {

  public LabelledTextView(Context context) {
    super(context);
  }

  public LabelledTextView(Context context, AttributeSet attributes) {
    super(context, attributes);
  }

  @Override
  public LabelledTextView enabled(boolean enabled) {
    text.enabled(enabled);

    return this;
  }

  @Override
  public LabelledTextView text(String text) {
    this.text.text(text);

    return this;
  }

  @Override
  protected void setup(View view, TypedArray array, TypedArray baseArray) {
    super.setup(view, array, baseArray);

    // Prevent the editAction text to actually be editable.
    text.get().setKeyListener(null);
    text.get().setFocusableInTouchMode(false);
    if (array.getColor(R.styleable.LabelledEditTextView_lineColor, 0) == 0) {
      text.hideLine();
    }
  }

  public LabelledTextView onClick(Wrapper.Action action) {
    text.onClick(action);

    return this;
  }
}
