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
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Message;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.MessageDialog;

import java.util.Optional;

/**
 * A view to show an icon for a outstanding message and allow to interact with it.
 */
public class MessageView extends AppCompatImageView {

  private final Character character;
  private final Message message;

  public MessageView(Context context, Character character, Message message) {
    super(context);

    this.character = character;
    this.message = message;

    Drawable drawable = getContext().getDrawable(R.drawable.ic_message_text_black_24dp);
    drawable.setTint(iconColor());
    setImageDrawable(drawable);

    setOnLongClickListener(this::onLongClick);
    setOnClickListener(this::onClick);
  }

  public @ColorInt int iconColor() {
    if (message.isXP()) {
      return getContext().getColor(R.color.characterDark);
    } if (message.isItem()) {
      return getContext().getColor(R.color.itemDark);
    } else {
      return getContext().getColor(R.color.characterDark);
    }
  }

  private boolean onLongClick(View view) {
    MessageDialog.create(getContext()).message(description()).title(title()).show();
    return true;
  }

  private void onClick(View view) {
    if (character.amPlayer()) {
      new ConfirmationPrompt(getContext())
          .title(title())
          .message(description())
          .yes(this::handle)
          .noNo()
          .show();
    }
  }

  private void handle() {
    if (message.isXP()) {
      character.addXp(message.getXP());
    } else if (message.isItem()) {
      character.add(message.getItem().get());
    }

    character.getContext().messages().deleteMessage(message.getId());
    character.store();
  }

  private String title() {
    if (message.isXP()) {
      return "XP Award";
    } else if (message.isItem()) {
      return "Received Item";
    } else {
      return "Unsupported Message";
    }
  }

  private String description() {
    if (message.isXP()) {
      return "Congratulations!\n"
          + "You DM has granted " + character.getName() + " " + message.getXP() + " XP!";
    } else if (message.isItem()) {
      Optional<Character> source =
          CompanionApplication.get().characters().get(message.getSourceId());
      return "You received a '" + message.getItem().get().getName()
          + (source.isPresent() ? "' from " + source.get().getName() : "' from the DM");
    } else {
      return "A generic message that is not currently supported";
    }
  }
}
