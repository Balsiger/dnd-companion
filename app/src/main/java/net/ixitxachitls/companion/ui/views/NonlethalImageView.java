/*
 * Copyright (c) 2017-2018 Peter Balsiger
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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import net.ixitxachitls.companion.R;

import javax.annotation.Nullable;

/**
 * The icon to show nonlethal damage.
 */
public class NonlethalImageView extends PartialImageView {

  private int nonlethalDamage;
  private int hp;

  private final Drawable icon;

  public NonlethalImageView(Context context) {
    this(context, null);
  }

  public NonlethalImageView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    icon = context.getDrawable(R.drawable.icons8_punch_filled_50).mutate();
    icon.setTint(getContext().getColor(R.color.nonleathal));

    setImageDrawable(icon);
    setBackground(context.getDrawable(R.drawable.icons8_punch_filled_50).mutate());

    update();
  }

  public void setNonlethalDamage(int nonlethalDamage, int hp) {
    this.nonlethalDamage = nonlethalDamage;
    this.hp = hp;

    update();
  }

  private void update() {
    if (nonlethalDamage > 0 && nonlethalDamage < hp) {
      setVisibility(VISIBLE);
      setPartial(nonlethalDamage * 100_00 / hp);
    } else {
      setVisibility(GONE);
    }
  }
}
