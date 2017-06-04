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
import android.graphics.Bitmap;

import com.google.common.base.Optional;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Images;

/**
 * Chip representation of a character.
 */
public class CharacterChipView extends ChipView {

  public CharacterChipView(Context context, Character character, int initiative, boolean ready) {
    this(context, character.getCharacterId(), character.getName(), initiative, ready,
        character.isLocal());
  }

  public CharacterChipView(Context context, String id, String name, int initiative, boolean ready,
                           boolean local) {
    super(context, R.drawable.ic_person_black_48dp, R.color.character);

    this.name.setText(name);
    Optional<Bitmap> bitmap = Images.get(local).load(Character.TYPE, id);
    if (bitmap.isPresent()) {
      image.setImageBitmap(bitmap.get());
    }

    if (ready) {
      subtitle.setText("init " + initiative);
    } else {
      this.name.setTextColor(getResources().getColor(R.color.cell, null));
    }
  }
}
