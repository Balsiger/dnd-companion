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
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.util.Misc;

/**
 * A chip displaying character information.
 */
public class CharacterChipView extends CreatureChipView {

  private static final String TAG = "CharChipView";

  private Character character;

  public CharacterChipView(Context context, Character character) {
    super(context, character, character.isLocal() ? R.color.character : R.color.characterDark,
        R.color.characterLight);

    this.character = character;
    setOnClickListener(this::onClick);
  }

  public void update(Character character) {
    this.character = character;

    Log.d(TAG, "updating character " + character);
    this.character = character;
    if (Misc.onEmulator()) {
      name.text((character.isLocal() ? "L" : "R") + "/" + character.getName());
    } else {
      name.text(character.getName());
    }
    if (character.getInitiative() == Character.NO_INITIATIVE) {
      setSubtitle("");
    } else {
      setSubtitle(String.valueOf(character.getInitiative()));
    }
  }

  public void update(Image characterImage) {
    BitmapDrawable drawable = new BitmapDrawable(getResources(),
        characterImage.getBitmap());
      image.setImageDrawable(drawable);
  }

  private void onClick(View view) {
    CompanionFragments.get().showCharacter(character, Optional.of(view));
  }
}
