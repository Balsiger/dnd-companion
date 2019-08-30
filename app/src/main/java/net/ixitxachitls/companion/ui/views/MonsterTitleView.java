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
import android.util.AttributeSet;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Monster;

import androidx.annotation.Nullable;

/**
 * A view for displaying the title for a monster.
 */
public class MonsterTitleView extends CreatureTitleView<Monster> {
  public MonsterTitleView(Context context) {
    this(context, null);
  }

  public MonsterTitleView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes, R.color.monsterLight, R.color.monster,
        R.drawable.noun_monster_693507);
  }

  @Override
  protected String getImagePath() {
    if (creature.isPresent()) {
      return creature.get().getImagePath();
    }

    return "";
  }

  @Override
  public void update(Monster monster) {
    super.update(monster);

    update(CompanionApplication.get().images());
  }

  @Override
  protected String formatSubtitle() {
    if (creature.isPresent()) {
      return creature.get().getSize() + " " + creature.get().getType() + ", " +
          creature.get().getAlignmentStatus() + " " + creature.get().getAlignment();
    }

    return "";
  }
}
