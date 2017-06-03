/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Player Companion.
 *
 * The Player Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Player Companion is distributed in the hope that it will be useful,
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
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitachitls.companion.R;

/**
 * A chip with rounded corners and a text.
 */
public class ChipView extends LinearLayout {

  protected TextView name;
  protected TextView subtitle;
  protected RoundImageView image;

  public ChipView(Context context, @DrawableRes int defaultImage,
                  @ColorRes int backgroundColor) {
    super(context);

    init(defaultImage, backgroundColor);
  }

  public ChipView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    TypedArray array = getContext().obtainStyledAttributes(attributes, R.styleable.IconView);
    @DrawableRes int defaultImage = array.getInt(R.styleable.ChipView_default_image, 0);
    @ColorRes int backgroundColor = array.getColor(R.styleable.ChipView_background_color, 0);

    init(defaultImage, backgroundColor);
  }

  private void init(@DrawableRes int defaultImage, @ColorRes int backgroundColor) {
    View view =
        LayoutInflater.from(getContext()).inflate(R.layout.view_chip, null, false);
    view.findViewById(R.id.back).setBackgroundTintList(
        getResources().getColorStateList(backgroundColor, null));

    name = (TextView) view.findViewById(R.id.name);
    subtitle = (TextView) view.findViewById(R.id.subtitle);
    image = (RoundImageView) view.findViewById(R.id.image);
    if (defaultImage > 0) {
      image.setImageDrawable(getResources().getDrawable(defaultImage, null));
    } else {
      image.setVisibility(INVISIBLE);
    }

    addView(view);
  }
}
