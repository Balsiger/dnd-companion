/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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
import android.support.annotation.ColorRes;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.Creature;
import net.ixitxachitls.companion.data.enums.BattleStatus;

/**
 * Chip view for a creature (monster).
 */
public class CreatureChipView extends ChipView {

  protected final HPImageView hp;
  protected final NonlethalImageView nonlethal;

  public CreatureChipView(Context context, Creature creature) {
    this(context, creature, R.color.monsterDark, R.color.monsterLight);
  }

  protected CreatureChipView(Context context, BaseCreature creature, @ColorRes int chipColor,
                             @ColorRes int higlightColor) {
    super(context, creature.getCreatureId(), creature.getName(), "init " + creature.getInitiative(),
        chipColor, higlightColor);

    hp = new HPImageView(getContext());
    icons.addView(hp);
    nonlethal = new NonlethalImageView(getContext());
    icons.addView(nonlethal);

    update(creature);
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

  public void update(BaseCreature creature) {
    hp.setHp(creature.getHp(), creature.getMaxHp());
    nonlethal.setNonlethalDamage(creature.getNonlethalDamage(), creature.getHp());
  }
}
