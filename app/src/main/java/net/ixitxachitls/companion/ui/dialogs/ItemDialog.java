/*
 * Copyright (c) 2017-2020 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
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
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;

import java.util.Optional;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

/**
 * A dialog to show the item summary.
 */
public class ItemDialog extends Dialog {

  private static final String ARG_ITEM_ID = "item_id";
  private static final String ARG_OWNER_ID = "owner_id";

  // The following are only set after onCreate.
  private Item.Owner owner;
  private Item item;
  private TextWrapper<TextView> name;

  @Override
  public void onCreate(Bundle savedInstance) {
    super.onCreate(savedInstance);

    if (getArguments() != null) {
      String ownerId = getArguments().getString(ARG_OWNER_ID);
      String itemId = getArguments().getString(ARG_ITEM_ID);

      Optional<? extends Item.Owner> owner = Item.findOwner(ownerId);
      if (owner.isPresent()) {
        this.owner = owner.get();
      } else {
        Status.error("Cannot find owner for item " + itemId);
        close();
        return;
      }

      Optional<Item> item = this.owner.getItem(itemId);
      if (item.isPresent()) {
        this.item = item.get();
      } else {
        Status.error("Cannot find item " + itemId);
        close();
        return;
      }
    }
  }

  @Override
  protected void createContent(View view) {
    name = TextWrapper.wrap(view, R.id.name);

    update();
  }

  private void update() {
    if (item == null || owner == null) {
      return;
    }

    if (owner.amDM()) {
      name.text(item.getName() + " (" + item.getPlayerName() + ")");
    } else {
      name.text(item.getPlayerName());
    }
  }

  protected static Bundle arguments(String itemId, String ownerId, @LayoutRes int layoutId,
                                    @StringRes int titleId, @ColorRes int colorId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ITEM_ID, itemId);
    arguments.putString(ARG_OWNER_ID, ownerId);
    return arguments;
  }

  public static ItemDialog newInstance(String itemId, String ownerId) {
    ItemDialog dialog = new ItemDialog();
    dialog.setArguments(arguments(itemId, ownerId, R.layout.dialog_item,
        R.string.dialog_item_title, R.color.item));
    return dialog;
  }
}
