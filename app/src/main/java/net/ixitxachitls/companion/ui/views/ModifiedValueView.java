/*
 * Copyright (c) 2017-2019 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Tabletop Companion.
 *
 * The Tabletop Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Tabletop Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.values.ModifiedValue;
import net.ixitxachitls.companion.ui.MessageDialog;

import androidx.annotation.StyleRes;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * A view showing a modified value.
 */
public class ModifiedValueView extends AppCompatTextView {

  private boolean showRange = false;

  public ModifiedValueView(Context context) {
    super(context);

    init();
  }

  public ModifiedValueView(Context context, AttributeSet attributes) {
    super(context, attributes);

    init();
  }

  public ModifiedValueView(Context context, AttributeSet attributes, int defStyleAttribute) {
    super(context, attributes, defStyleAttribute);

    init();
  }

  public ModifiedValueView ranged() {
    showRange = true;

    return this;
  }

  public ModifiedValueView set(ModifiedValue value) {
    setText(showRange ? value.totalRangeFormatted() : value.totalFormatted());
    setOnLongClickListener(v -> this.showDescription(value));

    return this;
  }

  public ModifiedValueView style(@StyleRes int style) {
    setTextAppearance(style);

    return this;
  }

  private void init() {
    setBackgroundColor(getResources().getColor(R.color.long_press, null));
    setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));
  }

  private boolean showDescription(ModifiedValue value) {
    MessageDialog.create(getContext())
        .title(value.getName())
        .message(value.describeModifiers())
        .show();

    return true;
  }
}
