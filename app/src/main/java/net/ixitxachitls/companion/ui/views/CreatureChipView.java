/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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
import android.support.annotation.NonNull;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.Creature;

/**
 * Chip view for a creature (monster).
 */
public class CreatureChipView extends ChipView  implements Comparable<CreatureChipView> {

  @ColorRes private int backgroundColor;
  protected int sortKey;

  public CreatureChipView(Context context, Creature creature) {
    this(context, creature, R.color.monster, R.color.monsterDark);

    this.sortKey = sortKey(creature.getInitiative(), 0, 0);
  }

  public CreatureChipView(Context context, BaseCreature creature, @ColorRes int chipColor,
                          @ColorRes int backgroundColor) {
    super(context, creature.getCreatureId(), creature.getName(), "init " + creature.getInitiative(),
        chipColor, backgroundColor);

    this.backgroundColor = backgroundColor;
  }

  public void setBattleMode(boolean inBattle) {
    if (inBattle) {
      setBackground(R.color.battleDark);
    } else {
      setBackground(backgroundColor);
    }
  }

  protected static int sortKey(int initiative, int dexterity, int random) {
    return 1_00_00 * initiative + 1_00 * dexterity + random;
  }

  @Override
  public int compareTo(@NonNull CreatureChipView other) {
    if (this.getDataId().equals(other.getDataId())) {
      return 0;
    }

    return Integer.compare(other.sortKey, sortKey);
  }
}
