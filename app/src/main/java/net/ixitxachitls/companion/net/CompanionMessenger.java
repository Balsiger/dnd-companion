/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

import android.os.Handler;
import android.support.annotation.VisibleForTesting;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Creature;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.data.dynamics.Item;
import net.ixitxachitls.companion.data.dynamics.XpAward;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.net.nsd.NsdAccessor;
import net.ixitxachitls.companion.util.Ids;
import net.ixitxachitls.companion.util.Misc;

import java.util.List;

/**
 * Class handling all sending, receiving and processing of network messages.
 */
public class CompanionMessenger implements Runnable {

  private static final int DELAY_MILLIS = 1_000;

  private final CompanionContext companionContext;
  private final Handler handler;
  private final int delayMs;

  private final CompanionServer companionServer;
  private final CompanionClients companionClients;
  private final ClientMessageProcessor clientMessageProcessor;
  private final ServerMessageProcessor serverMessageProcessor;

  public CompanionMessenger(CompanionContext context, NsdAccessor nsdAccessor,
                            CompanionApplication application) {
    this(context, nsdAccessor, application, new Handler(), DELAY_MILLIS);
  }

  @VisibleForTesting
  public CompanionMessenger(CompanionContext context, NsdAccessor nsdAccessor,
                            CompanionApplication application, Handler handler, int delayMs) {
    this.companionContext = context;
    this.handler = handler;
    this.delayMs = delayMs;

    companionServer = new CompanionServer(context, nsdAccessor);
    companionClients = new CompanionClients(context, nsdAccessor);
    clientMessageProcessor = new ClientMessageProcessor(context, application, this);
    serverMessageProcessor = new ServerMessageProcessor(context, this);
  }

  public void start() {
    Status.log("starting messenger");
    companionClients.start();
    companionServer.startIfNecessary();

    run();
  }

  public void stop() {
    Status.log("stopping messenger");
    companionClients.stop();
    companionServer.stop();

    handler.removeCallbacks(this);
  }

  public CompanionContext getContext() {
    return companionContext;
  }

  /*
  public void send(Campaign campaign) {
    Status.log("sending campaign " + campaign + " to all");
    if (!campaign.isLocal()) {
      Status.error("Cannot sent remote campaign!");
    } else {
      //companionServer.schedule(clientIds(campaign), CompanionMessageData.from(campaign));
    }
  }
*/

  /**
   * Send the current state of all data to the given recipient.
   * @param recipientId The id of the recipient to send the data to.
   */
  public void sendCurrent(String recipientId) {
    // Send all local campaigns.
    for (Campaign campaign : companionContext.campaigns().getCampaigns()) {
      if (campaign.amDM()
          && !Misc.onEmulator() /*&& !getContext().settings().useRemoteCampaigns()*/) {
        //companionServer.schedule(recipientId, CompanionMessageData.from(campaign));

        // Send all characters and images of the campaign that are not under the recipients control.
        /*
        for (Character character : companionContext.characters().getCharacters()) {
          if (!character.getServerId().equals(recipientId)) {
            companionServer.schedule(recipientId, CompanionMessageData.from(character));
            Optional<Image> image = character.loadImage().getValue();
            if (image.isPresent()) {
              companionServer.schedule(recipientId,
                  CompanionMessageData.from(companionContext, image.get()));
            }
          }
        }
        */
      }
    }

    // Send all the data for campaigns controlled by the recipient.
    /*
    for (Campaign campaign : companionContext.fsCampaigns().getRemoteCampaigns()) {
      if (campaign.getServerId().equals(recipientId)) {
        for (Character character : campaign.getCharacters()) {
          if (character.isLocal()) {
            companionServer.schedule(recipientId, CompanionMessageData.from(character));
            Optional<Image> image = character.loadImage().getValue();
            if (image.isPresent()) {
              companionServer.schedule(recipientId,
                  CompanionMessageData.from(companionContext, image.get()));
            }
          }
        }
      }
    }
    */
  }

  /*
  public void send(Character character) {
    Status.log("sending character " + character + " to all");
    Optional<Campaign> campaign =
        companionContext.campaigns().get(character.getCampaignId());
    if (campaign.isPresent()) {
      if (campaign.get().amDM()) {
        // If the character is local, we have to send it to all clients for update.
        // If the character is remote, we got an update from a client and likewise need to update all
        // clients.
        // Send the character to all connected clients and all owners of characters in the same
        // campaign.
        companionServer.schedule(clientIds(campaign.get(), character.getServerId()),
            CompanionMessageData.from(character));
      } else {
        if (character.isLocal()) {
          // Send the character to the server for redestribution.
          //companionClients.schedule(campaign.get().getServerId(),
          //    CompanionMessageData.from(character));
        } else {
          Status.log("Cannot publish a remote character for a remote campaign.");
        }
      }
    } else {
      Status.log("Campaign not found, character " + character + " not sent");
    }
  }
  */

  public void send(Image image) {
    Status.log("sending image " + image + " to all");
    switch (image.getType()) {
      /*
      case Character.TABLE:
        Optional<Character> character = companionContext.characters().getCharacter(image.getId()).getValue();
        if (character.isPresent()) {
          Optional<Campaign> campaign =
              companionContext.campaigns().getCampaign(character.get().getCampaignId());
          if (campaign.isPresent()) {
            if (campaign.get().amDM() || !character.get().isLocal()) {
              // Send the image to all known clients, except the owner.
              companionServer.schedule(
                  clientIds(campaign.get(), Ids.extractServerId(image.getId())),
                  CompanionMessageData.from(companionContext, image));
            } else {
              // Send the image to the server (unless we are the server as well).
              if (!campaign.get().getServerId().equals(companionContext.me().getId())) {
                companionClients.schedule(campaign.get().getServerId(),
                    CompanionMessageData.from(companionContext, image));
              }
            }
          } else {
            Status.log("cannot find campaign for character image " + image + " of " + character);
          }
        } else {
          Status.log("cannot find character for image " + image);
        }
        break;
    */

      default:
        Status.toast("Unsupported image type cannot be sent");
    }
  }

  public void send(Creature creature, TimedCondition condition) {
    // Local creatures should already be handled locally.
    //if (!creature.isLocal()) {
      Status.log("sending timed condition " + condition + " to " + creature);
      /*
      Optional<Campaign> campaign = creature.getCampaign();
      if (campaign.isPresent()) {
        if (campaign.get().isLocal()) {
          companionServer.schedule(Ids.extractServerId(creature.getCreatureId()),
              CompanionMessageData.from(companionContext, creature.getCreatureId(), condition));
        } else {
          companionClients.schedule(campaign.get().getServerId(),
              CompanionMessageData.from(companionContext, creature.getCreatureId(), condition));
        }
      } else {
        Status.error("Cannot find campaign for " + creature + ", cannot send condition");
      }
        */
    //}
  }

  public void addItem(Creature<?> creature, Item item) {
    /*
    Optional<Campaign> campaign = creature.getCampaign();
    if (!creature.isLocal() && campaign.isPresent() && campaign.get().amDM()) {
      Status.log("Sending newly created item " + item + " to " + creature);
      companionServer.schedule(Ids.extractServerId(creature.getCreatureId()),
          CompanionMessageData.from(companionContext, item, creature.getCreatureId()));
    } else {
      Status.error("Sending item " + item + " to " + creature + " no allowed");
    }
    */
  }

    /*
  public void sendDeletion(Character character) {
    Optional<Campaign> campaign =
        companionContext.campaigns().getCampaign(character.getCampaignId()).getValue();
    if (campaign.isPresent()) {
      if (campaign.get().isLocal()) {
        // Forward the character deletion to all 'other' clients.
        companionServer.schedule(clientIds(campaign.get(), character.getServerId()),
            CompanionMessageData.fromDelete(character));
      } else if (character.isLocal()) {
        companionClients.schedule(campaign.get().getServerId(),
            CompanionMessageData.fromDelete(character));
      }
      Status.log("deleted character " + character);
    } else {
      Status.toast("Cannot get campaign for character " + character + " for deletion");
    }
  }
    */

  public void sendDeletion(String conditionName, String sourceId, Creature creature) {
    /*
    if (!creature.isLocal()) {
      Status.log("handling condition deletion for " + conditionName + " from "
          + companionContext.creatures().nameFor(sourceId) + " affecting " + creature.getName());
      Optional<Campaign> campaign =
          companionContext.campaigns().getCampaign(creature.getCampaignId()).getValue();
      if (campaign.isPresent()) {
        if (campaign.get().isLocal()) {
          companionServer.schedule(Ids.extractServerId(creature.getCreatureId()),
              CompanionMessageData.fromDelete(companionContext, conditionName, sourceId,
                  creature.getCreatureId()));
        } else {
          companionClients.schedule(campaign.get().getServerId(),
              CompanionMessageData.fromDelete(companionContext, conditionName, sourceId,
                  creature.getCreatureId()));
        }
      } else {
        Status.log("Cannot send condition deletion for "
            + companionContext.creatures().nameFor(creature.getCreatureId()) + " for unknown campaign "
            + creature.getCampaignId());
      }
    }
    */
  }

  // Images are deleted with the associated entry, thus don't need their own deletion
  // handling.

  private void revoke(String id) {
    companionServer.revoke(id);
    companionClients.revoke(id);
  }

  public void sendXpAward(String campaignId, String characterId, int xp) {
    companionServer.schedule(Ids.extractServerId(characterId),
        CompanionMessageData.from(companionContext, new XpAward(characterId, campaignId, xp)));
  }

  public void sendAckToClient(String recipientId, long messageId) {
    Status.log("sending ack for " + messageId + " to client " + recipientId);
    companionServer.schedule(recipientId,
        CompanionMessageData.fromAck(companionContext, messageId));
  }

  public void sendAckToServer(String recipientId, long messageId) {
    Status.log("sending ack for " + messageId + " to server " + recipientId);
    companionClients.schedule(recipientId,
        CompanionMessageData.fromAck(companionContext, messageId));
  }

  public void sendWelcome() {
    companionClients.schedule(CompanionMessageData.fromWelcome(companionContext, companionContext.me().getId(),
        companionContext.me().getNickname()));
    companionServer.schedule(CompanionMessageData.fromWelcome(companionContext,
        companionContext.me().getId(), companionContext.me().getNickname()));
  }

  public void ackServer(String recipientId, long messageId) {
    companionServer.ack(recipientId, messageId);
  }

  public void ackClient(String recipientId, long messageId) {
    companionClients.ack(recipientId, messageId);
  }

  /*
  private Set<String> clientIds(Campaign campaign, String ... ignoreIds) {
    Set<String> ids = companionServer.clientIds(campaign);
    ids.addAll(companionServer.connectedClientIds());

    // Ignore the given id and ourselves.
    if (!Misc.onEmulator()) {
      ids.remove(companionContext.me().getId());
    }
    ids.removeAll(Arrays.asList(ignoreIds));

    return ids;
  }
  */

  @Override
  public void run() {
    try {
      // Send waiting messages.
      companionServer.sendWaiting();
      companionClients.sendWaiting();

      // Check for messages from servers.
      List<CompanionMessage> serverMessages = companionClients.receive();
      if (!serverMessages.isEmpty()) {
        Status.log(companionContext.me().getNickname() + " received " + serverMessages.size()
            + " server messages for processing");
      }
      for (CompanionMessage serverMessage : serverMessages) {
        clientMessageProcessor.process(serverMessage.getSenderId(), serverMessage.getSenderName(),
            serverMessage.getMessageId(), serverMessage.getData());
      }

      // Handle message from clients.
      List<CompanionMessage> clientMessages = companionServer.receive();
      if (!clientMessages.isEmpty()) {
        Status.log(companionContext.me().getNickname() + " received " + clientMessages.size()
            + " client messages for processing");
      }
      for (CompanionMessage clientMessage : clientMessages) {
        serverMessageProcessor.process(clientMessage.getSenderId(), clientMessage.getSenderName(),
            clientMessage.getMessageId(), clientMessage.getData());
      }
    } finally {
      handler.postDelayed(this, delayMs);
    }
  }


  // Testing.
  @VisibleForTesting
  public CompanionServer getServer() {
    return companionServer;
  }

  @VisibleForTesting
  public CompanionClients getClients() {
    return companionClients;
  }

  @VisibleForTesting
  public ServerMessageProcessor getServerMessageProcessor() {
    return serverMessageProcessor;
  }

  @VisibleForTesting
  public ClientMessageProcessor getClientMessageProcessor() {
    return clientMessageProcessor;
  }

  @VisibleForTesting
  public Handler getHandler() {
    return handler;
  }
}
