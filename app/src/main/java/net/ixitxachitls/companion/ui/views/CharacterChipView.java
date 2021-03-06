/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
import android.view.View;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Message;
import net.ixitxachitls.companion.data.documents.Messages;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A chip displaying character information.
 */
public class CharacterChipView extends CreatureChipView {

  private Character character;

  public CharacterChipView(Context context, Character character, int perLine) {
    super(context, character, character.amPlayer() ? R.color.character : R.color.characterDark,
        R.color.characterLight, R.drawable.noun_viking_30736, perLine);

    this.character = character;
    setOnClickListener(this::onClick);
  }

  public Character getCharacter() {
    return character;
  }

  public void update(Character character) {
    this.character = character;
    name.text(character.getName());
    super.update(character);
  }

  public void update() {
    update(character);
  }

  private void onClick(View view) {
    CompanionFragments.get().showCharacter(character, Optional.of(view));
  }

  public void update(Messages messages) {
    if (character.amPlayer() || character.amDM()) {
      clearMessages();
      for (Message message : messages.getMessages(character.getId())) {
        icons.addView(new CharacterMessageView(getContext(), character, message), 0);
      }
    }
  }

  private void clearMessages() {
    List<View> toRemove = new ArrayList<>();
    for (int i = 0; i < icons.getChildCount(); i++) {
      View view = icons.getChildAt(i);
      if (view instanceof MessageView) {
        toRemove.add(view);
      }
    }

    for (View view : toRemove) {
      icons.removeView(view);
    }
  }
}
