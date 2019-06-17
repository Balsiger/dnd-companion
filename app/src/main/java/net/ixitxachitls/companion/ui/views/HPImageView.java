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
import android.view.View;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.values.ConditionData;
import net.ixitxachitls.companion.rules.Conditions;
import net.ixitxachitls.companion.ui.dialogs.NumberAdjustDialog;

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * A partial image view to show hit points.
 */
public class HPImageView extends PartialImageView {

  private enum State { alive, dying, stable, dead }

  private static final int SIZE_PX = 50;

  private final Drawable alive;
  private final Drawable aliveBackground;
  private final Drawable stable;
  private final Drawable stableBackground;
  private final Drawable dying;
  private final Drawable dyingBackground;
  private int hp;
  private int maxHp;
  private Optional<NumberAdjustDialog.Action> adjustAction = Optional.empty();

  public HPImageView(Context context) {
    this(context, null);
  }

  public HPImageView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    this.alive = context.getDrawable(R.drawable.baseline_favorite_black_24).mutate();
    this.alive.setTint(context.getColor(R.color.alive));
    this.aliveBackground = context.getDrawable(R.drawable.baseline_favorite_black_24).mutate();
    this.aliveBackground.setTint(context.getColor(R.color.characterDark));
    this.stable = context.getDrawable(R.drawable.ic_heart_pulse_black_24dp).mutate();
    this.stable.setTint(context.getColor(R.color.stable));
    this.stableBackground = context.getDrawable(R.drawable.ic_heart_pulse_black_24dp).mutate();
    this.stableBackground.setTint(context.getColor(R.color.characterDark));
    this.dying = context.getDrawable(R.drawable.ic_heart_pulse_black_24dp).mutate();
    this.dying.setTint(context.getColor(R.color.dying));
    this.dyingBackground = context.getDrawable(R.drawable.ic_heart_pulse_black_24dp).mutate();
    this.dyingBackground.setTint(context.getColor(R.color.characterDark));
    setAdjustViewBounds(true);
    setMaxHeight(SIZE_PX);
    setMaxWidth(SIZE_PX);

    setOnClickListener(this::clicked);

    update();
  }

  public HPImageView onAdjust(@Nullable NumberAdjustDialog.Action action) {
    this.adjustAction = Optional.ofNullable(action);
    return this;
  }

  public void setHp(int hp, int maxHp) {
    this.hp = hp;
    this.maxHp = maxHp;

    update();
  }

  protected boolean clicked(View view) {
    if (adjustAction.isPresent()) {
      NumberAdjustDialog.newInstance(R.string.title_dialog_adjust_hp, R.color.character,
          "HP Adjustment", "Adjust the current HP value.")
          .setAdjustAction(adjustAction.get())
          .display();

      return true;
    }

    return false;
  }

  @Override
  protected boolean longClicked(View view) {
    switch(state()) {
      case alive:
        showDescription("Alive, all is well", "No worries, you are still alive... for now...");
        break;

      case dying:
        showDescription(Conditions.DYING);
        break;

      case stable:
        showDescription("Stable", "You are dying, but stable. You currently don't get worse.");
        break;

      case dead:
        showDescription(Conditions.DEAD);
        break;
    }

    return true;
  }

  private void showDescription(ConditionData condition) {
    showDescription(condition.getName(),
        condition.getSummary() + "\n\n" + condition.getDescription());
  }

  private State state() {
    if (hp == 0) {
      return State.stable;
    } else if (hp <= -10) {
      return State.dead;
    } else if (hp < 0) {
      return State.dying;
    } else {
      return State.alive;
    }
  }

  private void update() {
    switch(state()) {
      case alive:
        setVisibility(VISIBLE);
        setImageDrawable(alive);
        setBackground(aliveBackground);
        if (maxHp > 0) {
          setPartial(hp * 100_00 / maxHp);
        } else {
          setPartial(100_00);
        }
        break;

      case dying:
        setVisibility(VISIBLE);
        setImageDrawable(dying);
        setBackground(dyingBackground);
        setPartial((10 + hp) * 10_00);
        break;

      case stable:
        setVisibility(VISIBLE);
        setImageDrawable(stable);
        setBackground(stableBackground);
        setPartial(100_00);
        break;

      case dead:
        setVisibility(GONE);
        break;
    }
  }
}
