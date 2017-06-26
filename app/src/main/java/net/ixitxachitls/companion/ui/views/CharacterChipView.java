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
import android.graphics.drawable.BitmapDrawable;

import com.google.common.base.Optional;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Character;

/**
 * A chip displaying character information.
 */
public class CharacterChipView extends ChipView {

  public CharacterChipView(Context context, Character character) {
    super(context, character.getCharacterId(), character.getName(), "", R.color.character,
        R.color.characterDark);

    Optional<Bitmap> bitmap = character.loadImage();
    if (bitmap.isPresent()) {
      BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap.get());
      image.setImageDrawable(drawable);
    }

    if (character.isLocal()) {
      name.backgroundColor(R.color.characterLight);
      subtitle.backgroundColor(R.color.characterLight);
    }
  }
}
