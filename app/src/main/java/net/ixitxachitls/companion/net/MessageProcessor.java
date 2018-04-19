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

import android.support.annotation.VisibleForTesting;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Image;
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

  protected final CompanionContext companionContext;
  protected final CompanionMessenger messenger;
  private final Deque<RecievedMessage> received = new ArrayDeque<>();
  protected final Set<String> inFlightMessages =
      Collections.newSetFromMap(new ConcurrentHashMap<>());

  public MessageProcessor(CompanionContext companionContext, CompanionMessenger messenger) {
    this.companionContext = companionContext;
    this.messenger = messenger;
  }

  protected void process(String senderId, String senderName, String receiverId, long messageId,
                         CompanionMessageData message) {

    if (message.requiresAck()) {
      if (messageId == 0) {
        Status.error("Got ack message with no message id: " + Status.nameFor(senderId) + " -> "
            + Status.nameFor(receiverId) + ": " + messageId);
      } else {
        String key = createKey(senderId, receiverId, messageId);
        if (inFlightMessages.contains(key)) {
          Status.log("ignoring message in flight: " + Status.nameFor(senderId) + " -> "
              + Status.nameFor(receiverId) + ": " + messageId);
          return;
        } else {
          inFlightMessages.add(key);
        }
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
        handleCondition(senderId, messageId, message.getConditionTargetId(),
            message.getCondition());
        break;

      case CONDITION_DELETE:
        handleConditionDelete(senderId, messageId, message.getConditionDeleteName(),
            message.getConditionDeleteSourceId(), message.getConditionDeleteTargetId());
        break;

      case XP_AWARD:
        handleXpAward(receiverId, senderId, messageId, message.getXpAward());
        break;

      default:
        handleInvalid(senderName);
        break;
    }

    Status.refreshServerConnection(companionContext.settings().getAppId());
    Status.refreshClientConnection(senderId);

    received.addFirst(new RecievedMessage(senderName, message, senderId, receiverId, messageId));
    if (received.size() > MAX_RECEIVED_SIZE) {
      received.removeLast();
    }
  }

  private void handleCondition(String senderId, long messageId, String targetId,
                               TimedCondition condition) {
    Optional<? extends BaseCreature> creature =
        companionContext.creatures().getCreatureOrCharacter(targetId);
    if (creature.isPresent()) {
      creature.get().addAffectedCondition(condition);
    } else {
      Status.error("Cannot find creature for '" + targetId + "' to assign condition.");
    }

    if (this instanceof ClientMessageProcessor) {
      messenger.sendAckToServer(senderId, messageId);
    } else {
      messenger.sendAckToClient(senderId, messageId);
    }
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
    Status.error("handling campaign not supported");
  }

  protected void handleCampaignDeletion(String senderId, long messageId, String campaignId) {
    Status.error("Handling campaign deletion not supported.");
  }

  protected void handleCharacterDeletion(String senderId, long messageId, String characterId) {
    companionContext.characters().remove(characterId, false);

    if (this instanceof ClientMessageProcessor) {
      messenger.sendAckToServer(senderId, messageId);
    } else {
      messenger.sendAckToClient(senderId, messageId);
    }
  }

  protected abstract void handleAck(String senderId, long messageId);

  protected void handleImage(String senderId, Image image) {
    Status.error("Handling image not supported.");
  }

  protected void handleXpAward(String receiverId, String senderId, long messageId, XpAward award) {
    Status.error("Handling xp award no supported");
  }

  protected void handleConditionDelete(String senderId, long messageId, String conditionName,
                                       String sourceId, String targetId) {
    Status.log("dismissing condition " + conditionName + " for " + Status.nameFor(targetId)
        + " from " + Status.nameFor(sourceId));
    Optional<? extends BaseCreature> creature =
        companionContext.creatures().getCreatureOrCharacter(targetId);
    if (creature.isPresent()) {
      creature.get().removeAffectedCondition(conditionName, sourceId);
    } else {
      Status.log("Cannot find creature for " + Status.nameFor(targetId) + " to assign condition.");
    }

    if (this instanceof ClientMessageProcessor) {
      messenger.sendAckToServer(senderId, messageId);
    } else {
      messenger.sendAckToClient(senderId, messageId);
    }
  }

  private void handleInvalid(String senderName) {
    Status.error(senderName + ": Unknown message ignored");
  }

  protected void handleDebug(String senderId, String senderName, long messageId, String debug) {
    if (!debug.isEmpty()) {
      Status.toast(senderName + ": " + debug);
    }
  }

  protected void handleCharacter(String senderId, Character character) {
    if (!character.isLocal()) {
      // Storing will also add the character if it's changed.
      character.store();
      Status.log("received character " + character.getName());
      status("received character " + character.getName());
    }
  }

  protected void handleWelcome(String remoteId, String remoteName) {
    Status.recordId(remoteId, remoteName);
    for (Campaign campaign : companionContext.campaigns().getCampaignsByServer(remoteId)) {
      companionContext.characters().publish(campaign.getCampaignId());
    }

    messenger.sendCurrent(remoteId);
  }

  public void status(String message) {
    Status.log(message);
  }

  public static class RecievedMessage {
    private final String senderName;
    private final CompanionMessageData message;
    private final String senderId;
    private final String receiverId;
    private final long messageId;

    public RecievedMessage(String senderName, CompanionMessageData message, String senderId,
                           String receiverId, long messageId) {
      this.senderName = senderName;
      this.message = message;
      this.senderId = senderId;
      this.receiverId = receiverId;
      this.messageId = messageId;
    }

    public CompanionMessageData getMessage() {
      return message;
    }

    public long getMessageId() {
      return messageId;
    }

    public String getReceiverId() {
      return receiverId;
    }

    public String getSenderId() {
      return senderId;
    }

    @Override
    public String toString() {
      return "from " + senderName + ": " + message;
    }
  }

  @VisibleForTesting
  public List<RecievedMessage> allReceivedMessages() {
    return new ArrayList<>(received);
  }
}
