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

package net.ixitxachitls.companion.net;

import android.util.Log;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.ui.ConfirmationDialog;
import net.ixitxachitls.companion.ui.activities.CompanionActivity;

/**
 * Processor specifically for client side message handling.
 */
public class ClientMessageProcessor extends MessageProcessor {

  private static final String TAG = "ClientMsgProc";

  public ClientMessageProcessor(CompanionActivity activity) {
    super(activity);
  }

  @Override
  protected void handleCampaign(String senderId, String senderName, long id,
                              Data.CampaignProto campaignProto) {
    Campaign campaign = Campaign.fromProto(Campaigns.getRemoteIdFor(campaignProto.getId()),
        false, campaignProto);

    // Storing will also add the campaign if it's changed.
    campaign.store();
    Log.d(TAG, "received campaign " + campaign.getName());
    status("received campaign " + campaign.getName());
    refresh();
  }

  @Override
  protected void handleCampaignDeletion(String senderId, long messageId, String campaignId) {
    Campaigns.get(false).remove(campaignId);
    CompanionSubscriber.get().sendAck(senderId, messageId);
    refresh();
  }

  @Override
  protected void handleWelcome(String remoteId, String remoteName) {
    status("received welcome from server " + remoteName);
    super.handleWelcome(remoteId, remoteName);
    mainActivity.addServerConnection(remoteId, remoteName);

    // send a welcome message back?
  }

  @Override
  protected void handleXpAward(String senderId, long messageId, String campaignId,
                               String characterId, int xp) {
    Optional<Character> character = Characters.getCharacter(characterId, campaignId);
    if (character.isPresent()) {
      new ConfirmationDialog(mainActivity)
          .title("XP Award")
          .message("Congratulation!\n"
              + "Your DM has granted " + character.get().getName() + " " + xp + " XP!")
          .yes(() -> addXpAward(senderId, messageId, character.get(), xp))
          .show();
    }
  }

  private void addXpAward(String senderId, long messageId, Character character, int xp) {
    character.addXp(xp);
    CompanionSubscriber.get().sendAck(senderId, messageId);
    refresh();
  }
}
