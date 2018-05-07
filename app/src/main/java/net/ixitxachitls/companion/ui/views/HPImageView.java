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
 * A partial image view to show hit points.
 */
public class HPImageView extends PartialImageView {

  private int hp;
  private int maxHp;

  private final Drawable alive;
  private final Drawable aliveBackground;
  private final Drawable stable;
  private final Drawable stableBackground;
  private final Drawable dying;
  private final Drawable dyingBackground;
  private final Drawable dead;

  public HPImageView(Context context) {
    this(context, null);
  }

  public HPImageView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    this.hp = hp;
    this.maxHp = maxHp;
    this.alive = context.getDrawable(R.drawable.ic_favorite_black_24dp).mutate();
    this.alive.setTint(context.getColor(R.color.alive));
    this.aliveBackground = context.getDrawable(R.drawable.ic_favorite_black_24dp).mutate();
    this.aliveBackground.setTint(context.getColor(R.color.dead));
    this.stable = context.getDrawable(R.drawable.heart_pulse).mutate();
    this.stable.setTint(context.getColor(R.color.stable));
    this.stableBackground = context.getDrawable(R.drawable.heart_pulse).mutate();
    this.stableBackground.setTint(context.getColor(R.color.dead));
    this.dying = context.getDrawable(R.drawable.heart_pulse).mutate();
    this.dying.setTint(context.getColor(R.color.dying));
    this.dyingBackground = context.getDrawable(R.drawable.heart_pulse).mutate();
    this.dyingBackground.setTint(context.getColor(R.color.dead));
    this.dead = context.getDrawable(R.drawable.skull).mutate();
    this.dead.setTint(context.getColor(R.color.dead));

    update();
  }

  public void setHp(int hp, int maxHp) {
    this.hp = hp;
    this.maxHp = maxHp;

    update();
  }

  private void update() {
    if (hp == 0) {
      setImageDrawable(stable);
      setBackground(stableBackground);
      setPartial(100_00);
    } else if (hp <= -10)  {
      setImageDrawable(dead);
      setBackground(null);
      setPartial(100_00);
    } else if (hp < 0) {
      setImageDrawable(dying);
      setBackground(dyingBackground);
      setPartial((10 + hp) * 10_00);
    } else {
      setImageDrawable(alive);
      setBackground(aliveBackground);
      if (maxHp > 0) {
        setPartial(hp * 100_00 / maxHp);
      } else {
        setPartial(100_00);
      }
    }
  }
}
