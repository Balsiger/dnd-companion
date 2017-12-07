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

import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.data.dynamics.XpAward;
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

  public void process(String senderId, String senderName, long messageId,
                      CompanionMessageData message) {
    process(senderId, senderName, Settings.get().getAppId(), messageId, message);
  }

  @Override
  protected void handleImage(String senderId, Image image) {
    Log.d(TAG, "received image for " + image.getType() + " " + image.getId());
    image.save(false);
    refresh();
  }

  @Override
  protected void handleCampaign(String senderId, String senderName, long id, Campaign campaign) {
    // Storing will also add the campaign if it's changed.
    campaign.store();
    Log.d(TAG, "received campaign " + campaign.getName());
    status("received campaign " + campaign.getName());
    refresh();
  }

  @Override
  protected void handleCampaignDeletion(String senderId, long messageId, String campaignId) {
    Campaigns.get(false).removeCampaign(campaignId);
    CompanionSubscriber.get().sendAck(senderId, messageId);
    refresh();
  }

  @Override
  protected void handleWelcome(String remoteId, String remoteName) {
    status("received welcome from server " + remoteName);
    super.handleWelcome(remoteId, remoteName);
    mainActivity.addServerConnection(remoteId, remoteName);
  }

  @Override
  protected void handleXpAward(String receiverId, String senderId, long messageId, XpAward award) {
    Optional<Character> character = Characters.getCharacter(award.getCharacterId()).getValue();
    if (character.isPresent()) {
      new ConfirmationDialog(mainActivity)
          .title("XP Award")
          .message("Congratulation!\n"
              + "Your DM has granted " + character.get().getName() + " " + award.getXp() + " XP!")
          .yes(() -> addXpAward(senderId, messageId, character.get().getCharacterId(),
              award.getXp()))
          .no(() -> inFlightMessages.remove(createKey(receiverId, senderId, messageId)))
          .show();
    }
  }

  private void addXpAward(String senderId, long messageId, String characterId, int xp) {
    Optional<Character> character = Characters.getCharacter(characterId).getValue();
    if (character.isPresent()) {
      character.get().addXp(xp);
      CompanionSubscriber.get().sendAck(senderId, messageId);
      refresh();
      character = Characters.getCharacter(characterId).getValue();
    }
  }
}
