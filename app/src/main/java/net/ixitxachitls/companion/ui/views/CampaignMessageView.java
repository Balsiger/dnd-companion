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

import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Message;
import net.ixitxachitls.companion.ui.dialogs.SellItemDialog;

/**
 * A view for a campaign targeted message.
 */
public class CampaignMessageView extends MessageView {

  private final Campaign campaign;

  public CampaignMessageView(Context context, Campaign campaign, Message message) {
    super(context, message);
    this.campaign = campaign;
  }

  @Override
  protected boolean canHandle() {
    return campaign.amDM();
  }

  @Override
  protected void handle() {
    if (canHandle()) {
      switch (message.getType()) {
        case itemSell:
          SellItemDialog.newInstance(message.getId()).display();
          // The message will be deleted from the dialog.
          return;
      }

      campaign.getContext().messages().deleteMessage(message.getId());
    }
  }

  @Override
  protected boolean showConfirmation() {
    switch (message.getType()) {
      case itemSell:
        return false;

      default:
        return super.showConfirmation();
    }
  }
}
