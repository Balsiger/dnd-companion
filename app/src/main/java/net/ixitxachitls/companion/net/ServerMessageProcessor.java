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

import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.ui.activities.CompanionActivity;

/**
 * Processor on the server to process client messages.
 */
public class ServerMessageProcessor extends MessageProcessor {
  private static final String TAG = "SrvMsgProc";

  public ServerMessageProcessor(CompanionActivity mainActivity) {
    super(mainActivity);
  }

  public void process(String receiverId, long messageId, CompanionMessageData message) {
    process(Settings.get().getAppId(), Settings.get().getNickname(), receiverId, messageId,
        message);
  }

  @Override
  protected void handleAck(String recipientId, long messageId) {
    CompanionPublisher.get().ack(recipientId, messageId);
  }

  @Override
  protected void handleCharacter(String senderId, Character character) {
    super.handleCharacter(senderId, character);

    // Send the character update to the other clients.
    CompanionPublisher.get().update(character, senderId);
  }

  protected void handleImage(String senderId, Image image) {
    Log.d(TAG, "received image for " + image.getType() + " " + image.getId());
    image.save(false);

    // Send the image update to the other clients.
    CompanionPublisher.get().update(senderId, image);
    refresh();
  }

  @Override
  protected void handleWelcome(String remoteId, String remoteName) {
    status("received welcome from client " + remoteName);
    super.handleWelcome(remoteId, remoteName);
    mainActivity.addClientConnection(remoteId, remoteName);

    // Publish all local campaigns to that client.
    Campaigns.publish();
  }
}