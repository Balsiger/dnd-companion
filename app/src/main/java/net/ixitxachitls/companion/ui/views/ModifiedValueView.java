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
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.values.ModifiedValue;
import net.ixitxachitls.companion.ui.MessageDialog;

/**
 * A view showing a modified value.
 */
public class ModifiedValueView extends AppCompatTextView {

  public ModifiedValueView(Context context) {
    super(context);

    init();
  }

  public ModifiedValueView(Context context, AttributeSet attrs) {
    super(context, attrs);

    init();
  }

  public ModifiedValueView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    init();
  }

  public void set(ModifiedValue value) {
    setText(value.totalFormatted());
    setOnLongClickListener((v) -> this.showDescription(value));
  }

  private void init() {
    setBackground(getContext().getDrawable(R.drawable.dashed_rectangle));
  }

  private boolean showDescription(ModifiedValue value) {
    MessageDialog.create(getContext())
        .title(value.getName())
        .message(value.describeModifiers())
        .show();

    return true;
  }
}
