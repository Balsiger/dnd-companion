/*
 * Copyright (c) 2017-2019 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.view.View;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Monster;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;

import java.util.Optional;

/**
 * A chip displaying a single monster.
 */
public class MonsterChipView extends CreatureChipView {

  private Monster monster;

  public MonsterChipView(Context context, Monster monster) {
    super(context, monster, R.color.monster, R.color.monsterLight,
        R.drawable.noun_monster_693507, 6);

    this.monster = monster;
    setOnClickListener(this::onClick);
  }

  private void onClick(View view) {
    CompanionFragments.get().showMonster(monster, Optional.of(view));
  }
}
