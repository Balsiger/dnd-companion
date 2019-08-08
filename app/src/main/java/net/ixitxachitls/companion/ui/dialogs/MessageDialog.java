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

package net.ixitxachitls.companion.ui.dialogs;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Message;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

/**
 * A view to allow to select one ore multiple characters as a target.
 */
public class MessageDialog extends Dialog {

  private static final String ARG_CAMPAIGN = "campaign";
  private static final String ARG_SENDER = "sender";

  private Optional<Campaign> campaign = Optional.empty();
  private Optional<String> sender = Optional.empty();

  private EditTextWrapper<EditText> message;
  private LinearLayout characters;
  private Map<CheckBox, String> checkBoxToCharacterId = new HashMap<>();

  private CheckBox createCheckBox(Character character) {
    CheckBox box = new CheckBox(getContext());
    box.setText(character.getName());
    checkBoxToCharacterId.put(box, character.getId());

    return box;
  }

  @Override
  protected void createContent(View view) {
    message = EditTextWrapper.<EditText>wrap(view, R.id.message);
    characters = view.findViewById(R.id.characters);
    Wrapper.<Button>wrap(view, R.id.send).onClick(this::send);

    campaign = campaigns().get(getArguments().getString(ARG_CAMPAIGN));
    sender = Optional.ofNullable(getArguments().getString(ARG_SENDER));
    if (sender.isPresent() && !User.isUser(sender.get())) {
      Wrapper.<TextView>wrap(view, R.id.dm_note).gone();
    }

    if (campaign.isPresent()) {
      for (Character character : characters().getCampaignCharacters(campaign.get().getId())) {
        if (sender.isPresent() && !character.getId().equals(sender.get())) {
          characters.addView(createCheckBox(character));
        }
      }
    }
  }

  private void send() {
    if (campaign.isPresent() && sender.isPresent()) {
      List<String> recipients = new ArrayList<>();
      for (int i = 0; i < characters.getChildCount(); i++) {
        if (characters.getChildAt(i) instanceof CheckBox) {
          CheckBox checkBox = (CheckBox) characters.getChildAt(i);
          if (checkBox.isChecked() && checkBoxToCharacterId.containsKey(checkBox)) {
            recipients.add(checkBoxToCharacterId.get(checkBox));
          }
        }
      }


      if (!User.isUser(sender.get())) {
        // Send to DM (campaign), if not sent by them.
        Message.createForText(context(), sender.get(), campaign.get().getId(), recipients,
            message.getText());
      }

      for (String recipient : recipients) {
        Message.createForText(context(), sender.get(), recipient, recipients, message.getText());
      }
    }

    save();
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String campaignId, String senderId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_CAMPAIGN, campaignId);
    arguments.putString(ARG_SENDER, senderId);
    return arguments;
  }

  public static MessageDialog newInstance(String campaignId, String senderId) {
    MessageDialog dialog = new MessageDialog();
    dialog.setArguments(arguments(R.layout.dialog_message,
        R.string.dialog_title_message, R.color.character, campaignId, senderId));
    return dialog;
  }
}
