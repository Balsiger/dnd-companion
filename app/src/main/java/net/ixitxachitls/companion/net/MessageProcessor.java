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
import android.widget.Toast;

import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Images;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.data.dynamics.StoredEntries;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.ui.activities.CompanionActivity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * The class responsible to process and handle messages from client or server.
 */
public abstract class MessageProcessor {

  private static final String TAG = "MsgProc";
  private static final int MAX_RECEIVED_SIZE = 50;

  protected final CompanionActivity mainActivity;
  private final Deque<CompanionMessage> received = new ArrayDeque<>();


  public MessageProcessor(CompanionActivity mainActivity) {
    this.mainActivity = mainActivity;
  }

  public void process(CompanionMessage message) {
    switch (message.getProto().getPayloadCase()) {
      case DEBUG:
        handleDebug(message.getSenderId(), message.getSenderName(), message.getProto().getId(),
            message.getProto().getDebug());
        break;

      case WELCOME:
        handleWelcome(message.getSenderId(), message.getSenderName());
        break;

      case CAMPAIGN:
        handleCampaign(message.getSenderId(), message.getSenderName(), message.getProto().getId(),
            message.getProto().getCampaign());
        break;

      case CHARACTER:
        handleCharacter(message);
        break;

      case IMAGE:
        handleImage(message.getSenderId(), message.getProto().getImage());
        break;

      case ACK:
        handleAck(message.getSenderName(), message.getProto().getAck());
        break;

      case CAMPAIGN_DELETE:
        handleCampaignDeletion(message.getSenderId(), message.getProto().getId(),
            message.getProto().getCampaignDelete());
        break;

      case XP_AWARD:
        handleXpAward(message.getSenderId(), message.getProto().getId(),
            message.getProto().getXpAward().getCampaignId(),
            message.getProto().getXpAward().getCharacterId(),
            message.getProto().getXpAward().getXpAward());
        break;

      default:
      case PAYLOAD_NOT_SET:
        handleInvalid(message.getSenderId(), message.getSenderName(), message.getProto().getId());
        break;
    }

    mainActivity.updateClientConnection(Settings.get().getNickname());
    mainActivity.updateServerConnection(message.getSenderName());

    received.addFirst(message);
    if (received.size() > MAX_RECEIVED_SIZE) {
      received.removeLast();
    }
  }

  protected void handleXpAward(String senderId, long messageId, String campaignId,
                               String characterId, int xp) {
    throw new UnsupportedOperationException();
  }

  public List<String> receivedMessages() {
    List<String> messages = new ArrayList<>();

    for (CompanionMessage message : received) {
      messages.add("from " + message.getSenderName() + ": " +
          ScheduledMessage.toString(message.getProto()));
    }

    return messages;
  }

  protected void handleCampaign(String senderId, String senderName, long id,
                              Data.CampaignProto campaignProto) {
    throw new UnsupportedOperationException();
  }

  protected void handleCampaignDeletion(String senderId, long messageId, String campaignId) {
    throw new UnsupportedOperationException();
  }

  protected void handleAck(String recipientId, long messageId) {
    throw new UnsupportedOperationException();
  }

  private void handleImage(String senderId, Data.CompanionMessageProto.Image imageProto) {
    Log.d(TAG, "received image for " + imageProto.getType() + " " + imageProto.getId());
    Images.remote().save(imageProto.getType(), imageProto.getId(),
        Images.asBitmap(imageProto.getImage()));

    // Send the image update to the other clients.
    CompanionPublisher.get().update(imageProto, senderId);
    refresh();
  }

  private void handleInvalid(String senderId, String senderName, long messageId) {
    Toast.makeText(mainActivity.getApplicationContext(),
        senderName + ": Unknown message ignored", Toast.LENGTH_LONG).show();
    ack(senderId, messageId);
  }

  protected void handleDebug(String senderId, String senderName, long messageId, String debug) {
    if (!debug.isEmpty()) {
      Toast.makeText(mainActivity.getApplicationContext(),
          senderName + ": " + debug, Toast.LENGTH_LONG).show();
      ack(senderId, messageId);
    }
  }

  protected void handleCharacter(CompanionMessage message) {
    if (!StoredEntries.isLocalId(message.getProto().getCharacter().getId())) {
      Character character = Character.fromProto(
          Characters.getRemoteIdFor(message.getProto().getCharacter().getId()), false,
          message.getProto().getCharacter());
      // Storing will also add the character if it's changed.
      character.store();
      Log.d(TAG, "received character " + character.getName());
      status("received character " + character.getName());

      refresh();
    }
  }

  protected void handleWelcome(String remoteId, String remoteName) {

    // TODO: ensure this is handled by the general message flows on reconnection.
    // Republish all client content for the server's campaigns.
    //for (Campaign campaign : Campaigns.getCampaigns(remoteId)) {
    //  Characters.publish(campaign.getCampaignId());
    //}
    //CompanionPublisher.get().republish(Campaigns.getLocalCampaigns(),
    //    message.getSenderId());

  }

  protected void ack(String remoteId, long messageId) {
    CompanionPublisher.get().ack(remoteId, messageId);
  }

  public void status(String message) {
    mainActivity.status(message);
  }

  public void refresh() {
    mainActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mainActivity.refresh();
      }
    });
  }
}
