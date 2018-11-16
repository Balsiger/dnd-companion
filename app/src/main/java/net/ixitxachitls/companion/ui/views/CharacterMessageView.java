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
import android.support.annotation.CallSuper;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Message;
import net.ixitxachitls.companion.data.values.Item;

import java.util.Optional;

/**
 * A view for a character targeted message.
 */
public class CharacterMessageView extends MessageView {
  private final Character character;

  public CharacterMessageView(Context context, Character character, Message message) {
    super(context, message);

    this.character = character;
  }

  @Override
  protected boolean canHandle() {
    return character.amPlayer();
  }

  @Override
  @CallSuper
  protected void handle() {
    if (canHandle()) {
      switch (message.getType()) {
        case xp:
          character.addXp(message.getXP());
          break;

        case itemAdd:
          character.add(message.getItem().get());
          break;

        case itemDelete:
          character.removeItem(message.getItem().get());
          break;
      }

      character.getContext().messages().deleteMessage(message.getId());
      character.store();
    }
  }

  @Override
  protected String description() {
    switch (message.getType()) {
      case xp:
        return "Congratulations!\n"
            + "You DM has granted " + character.getName() + " " + message.getXP() + " XP!";
      case itemAdd: {
        Item item = message.getItem().get();
        Optional<Character> source =
            CompanionApplication.get().characters().get(message.getSourceId());
        String from = (source.isPresent() ? source.get().getName() : "the DM");
        String message = character.hasItem(item.getId())
            ? "Your '" + item.getName() + "' has been updated by"
            : "You received a '" + item.getName() + "' from";

        return message + " " + from + ".";
      }

      default:
        return super.description();
    }
  }
}
