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
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.ui.Wrapper;

/**
 * A small chip
 */
public class ChipView extends LinearLayout {

  private static final int PADDING_SELECT = 10;

  protected final Wrapper<RelativeLayout> container;
  protected final Wrapper<TextView> name;
  protected final Wrapper<TextView> subtitle;
  protected final RoundImageView image;

  public ChipView(Context context, @DrawableRes int defaultImage,
                  String name, String subtitle, @ColorRes int chipColor,
                  @ColorRes int backgroundColor) {
    super(context);
    Log.d("CHIP", "creating chip");

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_chip, null, false);

    this.container = Wrapper.wrap(view, R.id.container);
    container.backgroundColor(backgroundColor);
    this.name = Wrapper.wrap(view, R.id.name);
    this.name.text(name).backgroundColor(chipColor);

    this.subtitle = Wrapper.wrap(view, R.id.subtitle);
    this.subtitle.text(subtitle).backgroundColor(chipColor);
    if (subtitle.isEmpty()) {
      this.subtitle.gone();
    } else {
      this.subtitle.visible();
    }

    this.image = (RoundImageView) view.findViewById(R.id.image);

    if (defaultImage > 0) {
      Drawable drawable = getResources().getDrawable(defaultImage, null);
      drawable.setTint(getResources().getColor(chipColor, null));
      image.setImageDrawable(drawable);
    }

    addView(view);
  }

  public void disabled() {
    name.textColor(R.color.disabled);
    subtitle.textColor(R.color.disabled);
  }

  public void select() {
    container.elevate(PADDING_SELECT);
  }
}
