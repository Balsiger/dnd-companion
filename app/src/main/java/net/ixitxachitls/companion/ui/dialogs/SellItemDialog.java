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

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Message;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.data.values.Money;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledTextView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

/**
 * A dialog to sell an item.
 */
public class SellItemDialog extends Dialog {
  private static final String ARG_MESSAGE_ID = "message_id";

  private static final String ITEM_PP = "Platinum Piece";
  private static final String ITEM_GP = "Gold Piece";
  private static final String ITEM_SP = "Silver Piece";
  private static final String ITEM_CP = "Copper Piece";

  private Optional<Message> message = Optional.empty();

  private LabelledTextView name;
  private LabelledTextView seller;
  private LabelledEditTextView valuePP;
  private LabelledEditTextView valueGP;
  private LabelledEditTextView valueSP;
  private LabelledEditTextView valueCP;
  private Wrapper<Button> sendMoney;

  public SellItemDialog() {}

  @Override
  public void onCreate(Bundle savedInstance) {
    super.onCreate(savedInstance);

    if (getArguments() != null) {
      String messaageId = getArguments().getString(ARG_MESSAGE_ID);
      message = messages().get(messaageId);
    }
  }

  @Override
  protected void createContent(View view) {
    name = view.findViewById(R.id.name);
    name.text(name());
    seller = view.findViewById(R.id.seller);
    seller.text(seller());
    TextWrapper.wrap(view, R.id.description).text(description());
    valuePP = view.findViewById(R.id.value_pp);
    valueGP = view.findViewById(R.id.value_gp);
    valueSP = view.findViewById(R.id.value_sp);
    valueCP = view.findViewById(R.id.value_cp);
    sendMoney = Wrapper.<Button>wrap(view, R.id.sell);
    sendMoney.onClick(this::sendMoney);

    refreshValue();
  }

  private Item createCoins(String name, int number) {
    Item item = Item.create(context(), name);
    item.setMultiple(number);
    return item;
  }

  private String description() {
    if (message.isPresent() && message.get().getItem().isPresent()) {
      return message.get().getItem().get().getDMNotes();
    } else {
      return "Item not found.";
    }
  }

  private void maybeAddItemMessage(String itemName, String itemCount) {
    if (!itemCount.isEmpty()) {
      int count = Integer.parseInt(itemCount);
      if (count > 0) {
        Message.createForItemAdd(context(), me().getId(), message.get().getSourceId(),
            createCoins(itemName, count));
      }
    }
  }

  private String name() {
    if (message.isPresent() && message.get().getItem().isPresent()) {
      return message.get().getItem().get().getName();
    } else {
      return "Item not found.";
    }
  }

  private void refreshValue() {
    if (message.isPresent() && message.get().getItem().isPresent()) {
      Item item = message.get().getItem().get();
      Money value = item.getValue();
      if (!item.isMonetary()) {
        value = value.half();
      }

      valuePP.text(valueOrEmpty(value.getPlatinum()));
      valueGP.text(valueOrEmpty(value.getGold()));
      valueSP.text(valueOrEmpty(value.getSilver()));
      valueCP.text(valueOrEmpty(value.getCopper()));
    }
  }

  private String seller() {
    if (message.isPresent()) {
      Optional<Character> character = characters().get(message.get().getSourceId());
      if (character.isPresent()) {
        return character.get().getName();
      }
    }

    return "Unknown seller";
  }

  private void sendMoney() {
    if (message.isPresent()) {
      maybeAddItemMessage(ITEM_PP, valuePP.getText());
      maybeAddItemMessage(ITEM_GP, valueGP.getText());
      maybeAddItemMessage(ITEM_SP, valueSP.getText());
      maybeAddItemMessage(ITEM_CP, valueCP.getText());

      context().messages().deleteMessage(message.get().getId());
    }

    save();
  }

  private String valueOrEmpty(int value) {
    return value == 0 ? "" : String.valueOf(value);
  }

  protected static Bundle arguments(String messageId, @LayoutRes int layoutId,
                                    @StringRes int titleId, @ColorRes int colorId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_MESSAGE_ID, messageId);
    return arguments;
  }

  public static SellItemDialog newInstance(String messageId) {
    SellItemDialog dialog = new SellItemDialog();
    dialog.setArguments(arguments(messageId, R.layout.dialog_sell_item,
        R.string.sell_item, R.color.item));
    return dialog;
  }
}
