/*
 * Copyright (c) 2017-2018 Peter Balsiger
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
import android.graphics.drawable.Drawable;
import android.view.View;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Message;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.MessageDialog;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * A view to show an icon for a outstanding message and allow to interact with it.
 */
public abstract class MessageView extends AppCompatImageView {

  protected final Message message;

  public MessageView(Context context, Message message) {
    super(context);

    this.message = message;

    Drawable drawable = getContext().getDrawable(R.drawable.ic_message_text_black_24dp);
    drawable.setTint(iconColor());
    setImageDrawable(drawable);

    setOnLongClickListener(this::onLongClick);
    setOnClickListener(this::onClick);
  }

  public @ColorInt
  int iconColor() {
    switch (message.getType()) {
      case xp:
        return getContext().getColor(R.color.character);
      case itemAdd:
      case itemDelete:
      case itemSell:
        return getContext().getColor(R.color.itemDark);

      default:
        return getContext().getColor(R.color.black);
    }
  }

  protected boolean canHandle() {
    return false;
  }

  protected String description() {
    switch (message.getType()) {
      case itemDelete:
        return "The DM removed your '" + message.getItem().get().getName() + "'!";

      case itemSell: {
        return sourceName() + " sold a '" + message.getItem().get().getName() + "'.";
      }
      case text:
        return message.getText() + "\n\nSent to " + recipientNames() + ".";

      default:
        return "A generic message that is not currently supported";
    }
  }

  protected abstract void handle();

  protected String name(String id) {
    if (User.isUser(id)) {
      return "DM";
    }

    Optional<Character> character =
        CompanionApplication.get().characters().get(id);
    if (character.isPresent()) {
      return character.get().getName();
    }

    return id;
  }

  private void onClick(View view) {
    if (canHandle()) {
      if (showConfirmation()) {
        new ConfirmationPrompt(getContext())
            .title(title())
            .message(description())
            .yes(this::handle)
            .noNo()
            .show();
      } else {
        handle();
      }
    }
  }

  private boolean onLongClick(View view) {
    MessageDialog.create(getContext()).message(description()).title(title()).show();
    return true;
  }

  protected String recipientNames() {
    List<String> names = new ArrayList<>();
    for (String recipient : message.getRecipientIds()) {
      names.add(name(recipient));
    }

    return Strings.COMMA_JOINER.join(names);
  }

  protected boolean showConfirmation() {
    return true;
  }

  protected String sourceName() {
    return name(message.getSourceId());
  }

  protected String title() {
    switch(message.getType()) {
      case xp:
        return "XP Award";
      case itemAdd:
        return "Received Item";
      case itemDelete:
        return "Removed Item";
      case itemSell:
        return "Sold Item";
      case text:
        return "Message from " + sourceName();
      default:
        return "Unsupported Message";
    }
  }
}
