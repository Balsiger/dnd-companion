/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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
 * along with the Tabletop Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.support.annotation.ColorRes;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.Creature;
import net.ixitxachitls.companion.data.enums.BattleStatus;

/**
 * Chip view for a creature (monster).
 */
public class CreatureChipView extends ChipView {

  public CreatureChipView(Context context, Creature creature) {
    this(context, creature, R.color.monsterDark, R.color.monsterLight);
  }

  protected CreatureChipView(Context context, BaseCreature creature, @ColorRes int chipColor,
                          @ColorRes int higlightColor) {
    super(context, creature.getCreatureId(), creature.getName(), "init " + creature.getInitiative(),
        chipColor, higlightColor);
  }

  public String getCreatureId() {
    return getDataId();
  }

  public void setBattleMode(BattleStatus inBattle) {
    if (inBattle == BattleStatus.ENDED) {
      setBackground(R.color.cell);
      setSubtitle("");
    } else {
      setBackground(R.color.battleDark);
      if (inBattle != BattleStatus.STARTING) {
        setSubtitle("");
      }
    }
  }
}
