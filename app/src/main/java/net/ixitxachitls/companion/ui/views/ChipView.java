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
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitachitls.companion.R;

/**
 * A small chip
 */
public class ChipView extends LinearLayout {

  private static final int PADDING_SELECT = 10;

  private final LinearLayout container;
  private final LinearLayout back;
  protected final TextView name;
  protected final TextView subtitle;
  protected final RoundImageView image;
  protected final ViewStub details;

  public ChipView(Context context, @DrawableRes int defaultImage,
                  String name, String subtitle, @ColorRes int backgroundColor) {
    super(context);

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_chip, null, false);

    this.container = (LinearLayout) view.findViewById(R.id.container);
    this.back = (LinearLayout) view.findViewById(R.id.back);
    back.setBackgroundTintList(
        getResources().getColorStateList(backgroundColor, null));
    this.name = (TextView) view.findViewById(R.id.name);
    this.name.setText(name);
    this.subtitle = (TextView) view.findViewById(R.id.subtitle);
    if (subtitle.isEmpty()) {
      this.subtitle.setVisibility(GONE);
    } else {
      this.subtitle.setText(subtitle);
    }
    this.image = (RoundImageView) view.findViewById(R.id.image);
    this.details = (ViewStub) view.findViewById(R.id.details);

    if (defaultImage > 0) {
      image.setImageDrawable(getResources().getDrawable(defaultImage, null));
    } else {
      image.setVisibility(INVISIBLE);
    }

    addView(view);
  }

  public void disabled() {
    name.setTextColor(getResources().getColor(R.color.disabled, null));
    subtitle.setTextColor(getResources().getColor(R.color.disabled, null));
  }

  public void select() {
    back.setElevation(2 * PADDING_SELECT);
    container.setPadding(container.getPaddingLeft() - PADDING_SELECT,
        container.getPaddingTop() - PADDING_SELECT,
        container.getPaddingRight(),
        container.getPaddingBottom());
  }

  public void on() {
    setBackground(R.color.on);
  }

  public void off() {
    setBackground(R.color.off);
  }

  public void setBackground(@ColorRes int color) {
    setBackgroundColor(getResources().getColor(color, null));
  }
}
