/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * along with the Roleplay Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.net;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Image;

/**
 * Processor on the server to process client messages.
 */
public class ServerMessageProcessor extends MessageProcessor {
  public ServerMessageProcessor(CompanionContext companionContext, CompanionMessenger messenger) {
    super(companionContext, messenger);
  }

  public void process(String senderId, String senderName, long messageId,
                      CompanionMessageData message) {
    process(senderId, senderName, context.settings().getAppId(), messageId, message);
  }

  @Override
  protected void handleAck(String senderId, long messageId) {
    messenger.ackServer(senderId, messageId);
  }

  @Override
  protected void handleCharacter(String senderId, Character character) {
    super.handleCharacter(senderId, character);

    // Send the character update to the other clients.
    messenger.send(character);
  }

  protected void handleImage(String senderId, Image image) {
    Status.log("received image for " + image.getType() + " " + image.getId());
    image.save(false);

    // Send the image update to the other clients.
    messenger.send(image);
  }

  @Override
  protected void handleWelcome(String remoteId, String remoteName) {
    status("received welcome from client " + remoteName);
    super.handleWelcome(remoteId, remoteName);
    Status.addClientConnection(remoteId, remoteName);

    // Publish all local campaigns to that client.
    context.campaigns().publish();

    // Publish all local characters to that client.
    context.characters().publish();
  }
}
