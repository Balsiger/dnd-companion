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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.ui.Setup;
import net.ixitxachitls.companion.ui.fragments.BattleFragment;

/**
 * A chip with rounded corners and a text.
 */
public class InitiativeChip extends LinearLayout {

  private TextView name;
  private BattleFragment fragment;
  private Battle battle;

  public InitiativeChip(Context context, BattleFragment fragment, Battle battle, String name,
                        boolean monster, boolean ready, boolean active) {
    super(context);
    this.fragment = fragment;
    this.battle = battle;

    init(name, monster, ready, active);
  }

  private void init(String name, boolean monster, boolean ready, boolean active) {
    View view = LayoutInflater.from(getContext())
        .inflate(R.layout.view_chip, null, false);
    view.findViewById(R.id.back).setBackgroundTintList(
        getResources().getColorStateList(monster ? R.color.monster : R.color.character, null));

    this.name = (TextView) Setup.textView(view, R.id.name);
    this.name.setText(name);
    if (!ready) {
      this.name.setTextColor(getResources().getColor(R.color.cell, null));
    }

    Setup.button(view, R.id.done, this::done).setVisibility(active ? VISIBLE : GONE);
    Setup.button(view, R.id.delay, this::delay)
        .setVisibility(active && !battle.currentIsWaiting() && !battle.currentIsLast()
            ? VISIBLE : GONE);
    Setup.button(view, R.id.remove, this::remove).setVisibility(active && monster ? VISIBLE : GONE);

    addView(view);
  }

  private void done() {
    battle.combatantDone();
    fragment.refresh();
  }

  private void delay() {
    battle.combatantLater();
    fragment.refresh();
  }

  private void remove() {
    battle.removeCombatant();
    fragment.refresh();
  }
}
