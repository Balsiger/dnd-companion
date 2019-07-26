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
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.MessageDialog;

/**
 * A simple image view with a tooltip on click.
 */
public class TooltipImageView extends AppCompatImageView {
  public TooltipImageView(Context context) {
    this(context, null, 0);
  }

  public TooltipImageView(Context context, AttributeSet attributes) {
    this(context, attributes, 0);
  }

  public TooltipImageView(Context context, AttributeSet attributes, int defStyleAttr) {
    super(context, attributes, defStyleAttr);

    TypedArray styles = context.obtainStyledAttributes(attributes, R.styleable.TooltipImageView);
    String title = styles.getString(R.styleable.TooltipImageView_tooltip_title);
    String message = styles.getString(R.styleable.TooltipImageView_tooltip_message);
    setOnClickListener(v -> MessageDialog.create(context).title(title).message(message).show());
    styles.recycle();
  }
}
