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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.dialogs.XPDialog;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;

/**
 * View for a single fixed xp reward value.
 */
public class XPFixedView extends TextView {

  private static final int MAX_COUNT = 9;

  private final XPDialog dialog;
  private final TextWrapper<XPFixedView> text;
  private final boolean isLast;

  private int count = 0;
  private int base = 0;

  public XPFixedView(Context context, XPDialog dialog, int base) {
    this(context, dialog, base, false);
  }

  public XPFixedView(Context context, XPDialog dialog, int base, boolean isLast) {
    super(context, null, 0, R.style.LargeText);
    this.dialog = dialog;

    this.base = base;
    this.isLast = isLast;

    text = TextWrapper.wrap(this)
        .ems(6)
        .backgroundColor(R.color.cell)
        .padding(TextWrapper.Padding.TOP_BOTTOM, 10)
        .padding(TextWrapper.Padding.LEFT_RIGHT, 5)
        .align(TextWrapper.Align.CENTER)
        .bold()
        .onClick(this::increase)
        .onLongClick(this::clear);

    refresh();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (!isLast) {
      text.margin(TextWrapper.Margin.RIGHT, 5);
    }

    text.weight(1);
  }

  public int getValue() {
    return base * count;
  }

  public void increase() {
    if (count < MAX_COUNT) {
      count++;
    }

    refresh();
  }

  public void clear() {
    count = 0;

    refresh();
  }

  private void refresh() {
    if (count == 0) {
      setText(String.valueOf(base));
      text.backgroundColor(R.color.cell);
    } else {
      setText(base + "x" + count);
      text.backgroundColor(R.color.colorAccent);
    }

    dialog.refresh();
  }
}
