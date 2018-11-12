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
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Monster;

/**
 * Title view for monsters in encounters
 */
public class EncounterMonsterTitleView extends EncounterTitleView<Monster> {
  public EncounterMonsterTitleView(Context context) {
    this(context, null);
  }

  public EncounterMonsterTitleView(Context context, @Nullable AttributeSet attributeSet) {
    super(context, attributeSet, R.color.monsterLight, R.color.monster, R.color.monsterLight,
        R.drawable.noun_monster_693507);
  }
}
