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

import android.widget.Toast;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Creatures;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.data.dynamics.StoredEntries;
import net.ixitxachitls.companion.data.dynamics.StoredEntry;
import net.ixitxachitls.companion.data.dynamics.XpAward;
import net.ixitxachitls.companion.data.values.TimedCondition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The class responsible to process and handle messages from client or server.
 */
public abstract class MessageProcessor {

  private static final int MAX_RECEIVED_SIZE = 50;

  protected final CompanionApplication application;
  private final Deque<RecievedMessage> received = new ArrayDeque<>();
  protected final Set<String> inFlightMessages =
      Collections.newSetFromMap(new ConcurrentHashMap<>());

  public MessageProcessor(CompanionApplication application) {
    this.application = application;
  }

  protected void process(String senderId, String senderName, String receiverId, long messageId,
                         CompanionMessageData message) {

    if (message.requiresAck()) {
      String key = createKey(senderId, receiverId, messageId);
      if (inFlightMessages.contains(key)) {
        Status.log("ignoring message in flight");
        return;
      } else {
        inFlightMessages.add(key);
      }
    }

    switch (message.getType()) {
      case DEBUG:
        handleDebug(senderId, senderName, messageId, message.getDebug());
        break;

      case WELCOME:
        handleWelcome(senderId, senderName);
        break;

      case CAMPAIGN:
        handleCampaign(senderId, senderName, messageId, message.getCampaign());
        break;

      case CHARACTER:
        handleCharacter(senderId, message.getCharacter());
        break;

      case IMAGE:
        handleImage(senderId, message.getImage());
        break;

      case ACK:
        handleAck(senderId, message.getAck());
        break;

      case CAMPAIGN_DELETE:
        handleCampaignDeletion(senderId, messageId, message.getCampaignDelete());
        break;

      case CHARACTER_DELETE:
        handleCharacterDeletion(senderId, messageId, message.getCharacterDelete());
        break;

      case CONDITION:
        handleCondition(senderId, messageId, message.getConditionDeleteTargetId(),
            message.getCondition());

      case CONDITION_DELETE:
        handleConditionDelete(senderId, messageId, message.getConditionDeleteName(),
            message.getConditionDeleteSourceId(), message.getConditionDeleteTargetId());

      case XP_AWARD:
        handleXpAward(receiverId, senderId, messageId, message.getXpAward());
        break;

      default:
        handleInvalid(senderName);
        break;
    }


    Status.refreshServerConnection(Settings.get().getAppId());
    Status.refreshClientConnection(senderId);

    received.addFirst(new RecievedMessage(senderName, message));
    if (received.size() > MAX_RECEIVED_SIZE) {
      received.removeLast();
    }
  }

  private void handleCondition(String senderId, long messageId, String targetId,
                               TimedCondition condition) {
    Optional<? extends BaseCreature> creature;
    if (StoredEntry.hasType(targetId, Character.TYPE)) {
      creature = Characters.getCharacter(targetId).getValue();
    } else {
      creature = Creatures.getCreature(targetId).getValue();
    }

    if (creature.isPresent()) {
      if (creature.get().isLocal()) {
        creature.get().addAffectedCondition(condition);
      } else {
        CompanionMessenger.get().send(targetId, condition);
      }
    } else {
      Status.log("Cannot find creature for " + targetId + " to assign condition.");
    }

    CompanionMessenger.get().sendAckToServer(senderId, messageId);
  }

  protected String createKey(String senderId, String receiverId, long messageId) {
    return senderId + ":" + receiverId + ":" + messageId;
  }

  public List<String> receivedMessages() {
    List<String> messages = new ArrayList<>();

    for (RecievedMessage message : received) {
      messages.add("from " + message.senderName + ": " + message);
    }

    return messages;
  }

  protected void handleCampaign(String senderId, String senderName, long id, Campaign campaign) {
    throw new UnsupportedOperationException();
  }

  protected void handleCampaignDeletion(String senderId, long messageId, String campaignId) {
    throw new UnsupportedOperationException();
  }

  protected void handleCharacterDeletion(String senderId, long messageId, String characterId) {
    Characters.remove(characterId, false);
    CompanionMessenger.get().sendAckToServer(senderId, messageId);
  }

  protected void handleAck(String senderId, long messageId) {
    throw new UnsupportedOperationException();
  }

  protected void handleImage(String senderId, Image image) {
    throw new UnsupportedOperationException();
  }

  protected void handleXpAward(String receiverId, String senderId, long messageId, XpAward award) {
    throw new UnsupportedOperationException();
  }

  protected void handleConditionDelete(String senderId, long messageId, String conditionName,
                                       String sourceId, String targetId) {
    Optional<? extends BaseCreature> creature;
    if (StoredEntry.hasType(targetId, Character.TYPE)) {
      creature = Characters.getCharacter(targetId).getValue();
    } else {
      creature = Creatures.getCreature(targetId).getValue();
    }

    if (creature.isPresent()) {
      if (creature.get().isLocal()) {
        creature.get().removeAffectedCondition(conditionName, sourceId);
      } else {
        CompanionMessenger.get().sendDeletion(conditionName, sourceId, targetId);
      }
    } else {
      Status.log("Cannot find creature for " + targetId + " to assign condition.");
    }

    CompanionMessenger.get().sendAckToServer(senderId, messageId);
  }

  private void handleInvalid(String senderName) {
    Toast.makeText(application.getApplicationContext(),
        senderName + ": Unknown message ignored", Toast.LENGTH_LONG).show();
  }

  protected void handleDebug(String senderId, String senderName, long messageId, String debug) {
    if (!debug.isEmpty()) {
      Toast.makeText(application.getApplicationContext(),
          senderName + ": " + debug, Toast.LENGTH_LONG).show();
    }
  }

  protected void handleCharacter(String senderId, Character character) {
    if (!StoredEntries.isLocalId(character.getCharacterId())) {
      // Storing will also add the character if it's changed.
      character.store();
      Status.log("received character " + character.getName());
      status("received character " + character.getName());
    }
  }

  protected void handleWelcome(String remoteId, String remoteName) {
    for (Campaign campaign : Campaigns.getCampaignsByServer(remoteId)) {
      Characters.publish(campaign.getCampaignId());
    }

    CompanionMessenger.get().sendCurrent(remoteId);
  }

  public void status(String message) {
    Status.log(message);
  }

  public static class RecievedMessage {
    private final String senderName;
    private final CompanionMessageData message;

    public RecievedMessage(String senderName, CompanionMessageData message) {
      this.senderName = senderName;
      this.message = message;
    }

    @Override
    public String toString() {
      return "from " + senderName + ": " + message;
    }
  }
}
