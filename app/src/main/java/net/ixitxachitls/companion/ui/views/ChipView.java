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
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * A small chip representing a character in the game.
 */
public class ChipView extends LinearLayout {

  private final String dataId;
  private final int chipColor;
  private final int backgroundColor;

  protected final Wrapper<RoundImageView> image;
  protected final TextWrapper<TextView> name;
  protected final LinearLayout icons;

  public ChipView(Context context, String dataId, String name, String subtitle,
                  @ColorRes int chipColor, @ColorRes int backgroundColor,
                  @DrawableRes int drawableRes) {
    super(context);

    this.dataId = dataId;
    this.chipColor = chipColor;
    this.backgroundColor = backgroundColor;

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_chip, this, false);

    this.name = TextWrapper.wrap(view, R.id.name);
    this.name.text(name).backgroundColor(chipColor).noWrap();

    this.icons = view.findViewById(R.id.icons);

    this.image = Wrapper.wrap(view, R.id.image);
    image.backgroundColor(backgroundColor);

    Drawable drawable = getResources().getDrawable(drawableRes, null);
    drawable.setTint(getResources().getColor(chipColor, null));
    image.get().setImageDrawable(drawable);

    addView(view);
  }

  public void addTo(ViewGroup group) {
    ViewGroup parent = (ViewGroup) getParent();
    if (parent != null) {
      parent.removeView(this);
    }

    group.addView(this);
  }

  public void setBackground(@ColorRes int color) {
    image.backgroundColor(color);
    name.backgroundColor(color);
  }

  @Deprecated
  protected void setSubtitle(String text) {
  }

  @Deprecated
  public void select(boolean select) {
    if (select) {
      select();
    } else {
      unselect();
    }
  }

  @Deprecated
  public void select() {
    name.backgroundColor(backgroundColor);
  }

  @Deprecated
  public void unselect() {
    name.backgroundColor(chipColor);
  }

  public String getDataId() {
    return dataId;
  }

  public void update() {}
}
