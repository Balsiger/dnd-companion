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
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.data.dynamics.StoredEntries;
import net.ixitxachitls.companion.data.dynamics.XpAward;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.net.nsd.NsdAccessor;
import net.ixitxachitls.companion.storage.DataBaseAccessor;
import net.ixitxachitls.companion.util.Ids;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Class handling all sending, receiving and processing of network messages.
 */
public class CompanionMessenger implements Runnable {

  private static final int DELAY_MILLIS = 1_000;
  private static CompanionMessenger singleton;

  private final DataBaseAccessor dataBaseAccessor;
  private final Handler handler;

  private final CompanionServer companionServer;
  private final CompanionClients companionClients;
  private final ClientMessageProcessor clientMessageProcessor;
  private final ServerMessageProcessor serverMessageProcessor;

  @VisibleForTesting
  public CompanionMessenger(DataBaseAccessor dataBaseAccessor,
                            NsdAccessor nsdAccessor,
                            Settings settings,
                            CompanionApplication application,
                            Handler handler) {
    this.dataBaseAccessor = dataBaseAccessor;
    this.handler = handler;

    ScheduledMessages.init(dataBaseAccessor);
    companionServer = new CompanionServer(nsdAccessor, settings);
    companionClients = new CompanionClients(nsdAccessor, settings.getDataBaseAccessor());
    clientMessageProcessor = new ClientMessageProcessor(application);
    serverMessageProcessor = new ServerMessageProcessor(application);
  }

  public static CompanionMessenger init(DataBaseAccessor dataBaseAccessor,
                                        NsdAccessor nsdAccessor,
                                        Settings settings,
                                        CompanionApplication application) {
    if (singleton != null) {
      Status.error("Messenger already initialized!");
    } else {
      singleton = new CompanionMessenger(dataBaseAccessor, nsdAccessor, settings, application,
          new Handler());
    }

    return singleton;
  }

  @Deprecated
  public static CompanionMessenger get() {
    if (singleton == null) {
      Status.error("Messenger not yet initialized");
    }

    return singleton;
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

  public boolean isOnline(Campaign campaign) {
    if (campaign.isLocal()) {
      return campaign.isPublished() && companionServer.isOnline();
    } else {
      return companionClients.isServerOnline(campaign.getServerId());
    }
  }

  public void send(Campaign campaign) {
    Status.log("sending campaign " + campaign + " to all");
    if (!campaign.isLocal()) {
      Status.error("Cannot sent remote campaign!");
    } else {
      companionServer.schedule(clientIds(campaign), CompanionMessageData.from(campaign));
    }
  }

  /**
   * Send the current state of all data to the given recipient.
   * @param recipientId The id of the recipient to send the data to.
   */
  public void sendCurrent(String recipientId) {
    // Send all local campaigns.
    for (Campaign campaign : Campaigns.getLocalCampaigns()) {
      if (campaign.isPublished()) {
        companionServer.schedule(recipientId, CompanionMessageData.from(campaign));

        // Send all characters and images of the campaign that are not under the recipients control.
        for (Character character : campaign.getCharacters()) {
          if (!character.getServerId().equals(recipientId)) {
            companionServer.schedule(recipientId, CompanionMessageData.from(character));
            Optional<Image> image = character.loadImage().getValue();
            if (image.isPresent()) {
              companionServer.schedule(recipientId,
                  CompanionMessageData.from(image.get(), dataBaseAccessor));
            }
          }
        }
      }
    }

    // Send all the data for campaigns controlled by the recipient.
    for (Campaign campaign : Campaigns.getRemoteCampaigns()) {
      if (campaign.getServerId().equals(recipientId)) {
        for (Character character : campaign.getCharacters()) {
          if (character.isLocal()) {
            companionServer.schedule(recipientId, CompanionMessageData.from(character));
            Optional<Image> image = character.loadImage().getValue();
            if (image.isPresent()) {
              companionServer.schedule(recipientId,
                  CompanionMessageData.from(image.get(), dataBaseAccessor));
            }
          }
        }
      }
    }
  }

  public void send(Character character) {
    Status.log("sending character " + character + " to all");
    Optional<Campaign> campaign = Campaigns.getCampaign(character.getCampaignId()).getValue();
    if (campaign.isPresent()) {
      if (campaign.get().isLocal()) {
        // If the character is local, we have to send it to all clients for update.
        // If the character is remote, we got an update from a client and likewise need to update all
        // clients.
        if (campaign.get().isPublished()) {
          // Send the character to all connected clients and all owners of characters in the same
          // campaign.
          companionServer.schedule(clientIds(campaign.get(), character.getServerId()),
              CompanionMessageData.from(character));
        }
      } else {
        if (character.isLocal()) {
          // Send the character to the server for redestribution.
          companionClients.schedule(campaign.get().getServerId(),
              CompanionMessageData.from(character));
        } else {
          Status.log("Cannot publish a remote character for a remote campaign.");
        }
      }
    } else {
      Status.log("Campaign not found, character " + character + " not sent");
    }
  }

  public void send(Image image) {
    Status.log("sending image " + image + " to all");
    switch (image.getType()) {
      case Character.TABLE:
        Optional<Character> character = Characters.getCharacter(image.getId()).getValue();
        if (character.isPresent()) {
          Optional<Campaign> campaign =
              Campaigns.getCampaign(character.get().getCampaignId()).getValue();
          if (campaign.isPresent()) {
            if (campaign.get().isLocal() || !character.get().isLocal()) {
              // Send the image to all known clients, except the owner.
              companionServer.schedule(
                  clientIds(campaign.get(), Ids.extractServerId(image.getId())),
                  CompanionMessageData.from(image, dataBaseAccessor));
            } else {
              // Send the image to the server (unless we are the server as well).
              if (!campaign.get().getServerId().equals(Settings.get().getAppId())) {
                companionClients.schedule(campaign.get().getServerId(),
                    CompanionMessageData.from(image, dataBaseAccessor));
              }
            }
          } else {
            Status.log("cannot find campaign for character image " + image + " of " + character);
          }
        } else {
          Status.log("cannot find character for image " + image);
        }
        break;

      default:
        Status.toast("Unsupported image type cannot be sent");
    }
  }

  public void send(BaseCreature creature, TimedCondition condition) {
    // Local creatures should already be handled locally.
    if (!creature.isLocal()) {
      Status.log("sending timed condition " + condition + " to " + creature);
      Optional<Campaign> campaign = creature.getCampaign();
      if (campaign.isPresent()) {
        if (campaign.get().isLocal()) {
          companionServer.schedule(Ids.extractServerId(creature.getCreatureId()),
              CompanionMessageData.from(creature.getCreatureId(), condition, dataBaseAccessor));
        } else {
          companionClients.schedule(campaign.get().getServerId(),
              CompanionMessageData.from(creature.getCreatureId(), condition, dataBaseAccessor));
        }
      } else {
        Status.error("Cannot find campaign for " + creature + ", cannot send condition");
      }
    }
  }

  public void sendDeletion(Campaign campaign) {
    if (!campaign.isLocal()) {
      Status.toast("cannot delete remote campaign");
    }

    companionServer.schedule(clientIds(campaign), CompanionMessageData.fromDelete(campaign));
    revoke(campaign.getCampaignId());
    Status.log("deleted campaign " + campaign);
  }

  public void sendDeletion(Character character) {
    Optional<Campaign> campaign = Campaigns.getCampaign(character.getCampaignId()).getValue();
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

  public void sendDeletion(String conditionName, String sourceId, BaseCreature creature) {
    if (!creature.isLocal()) {
      Status.log("handling condition deletion for " + conditionName + " from "
          + StoredEntries.nameFor(sourceId) + " affecting " + creature.getName());
      Optional<Campaign> campaign = Campaigns.getCampaign(creature.getCampaignId()).getValue();
      if (campaign.isPresent()) {
        if (campaign.get().isLocal()) {
          companionServer.schedule(Ids.extractServerId(creature.getCreatureId()),
              CompanionMessageData.fromDelete(conditionName, sourceId, creature.getCreatureId(),
                  dataBaseAccessor));
        } else {
          companionClients.schedule(campaign.get().getServerId(),
              CompanionMessageData.fromDelete(conditionName, sourceId, creature.getCreatureId(),
                  dataBaseAccessor));
        }
      } else {
        Status.log("Cannot send condition deletion for "
            + StoredEntries.nameFor(creature.getCreatureId()) + " for unknown campaign "
            + creature.getCampaignId());
      }
    }
  }

  // Images are deleted with the associated entry, thus don't need their own deletion
  // handling.

  private void revoke(String id) {
    companionServer.revoke(id);
    companionClients.revoke(id);
  }

  public void sendXpAward(String campaignId, String characterId, int xp) {
    companionServer.schedule(Ids.extractServerId(characterId),
        CompanionMessageData.from(new XpAward(characterId, campaignId, xp), dataBaseAccessor));
  }

  public void sendAckToClient(String recipientId, long messageId) {
    Status.log("sending ack for " + messageId + " to client " + recipientId);
    companionServer.schedule(recipientId,
        CompanionMessageData.fromAck(messageId, dataBaseAccessor));
  }

  public void sendAckToServer(String recipientId, long messageId) {
    Status.log("sending ack for " + messageId + " to server " + recipientId);
    companionClients.schedule(recipientId,
        CompanionMessageData.fromAck(messageId, dataBaseAccessor));
  }

  public void sendWelcome() {
    companionClients.schedule(CompanionMessageData.fromWelcome(Settings.get().getAppId(),
        Settings.get().getNickname(), dataBaseAccessor));
    companionServer.schedule(CompanionMessageData.fromWelcome(Settings.get().getAppId(),
        Settings.get().getNickname(), dataBaseAccessor));
  }

  public void ackServer(String recipientId, long messageId) {
    companionServer.ack(recipientId, messageId);
  }

  public void ackClient(String recipientId, long messageId) {
    companionClients.ack(recipientId, messageId);
  }

  private Set<String> clientIds(Campaign campaign, String ... ignoreIds) {
    Set<String> ids = companionServer.clientIds(campaign);
    ids.addAll(companionServer.connectedClientIds());

    // Ignore the given id and ourselves.
    ids.remove(Settings.get().getAppId());
    ids.removeAll(Arrays.asList(ignoreIds));

    return ids;
  }

  @Override
  public void run() {
    Status.heartBeat();

    try {
      // Send waiting messages.
      companionServer.sendWaiting();
      companionClients.sendWaiting();

      // Chek for messages from servers.
      List<CompanionMessage> serverMessages = companionClients.receive();
      for (CompanionMessage serverMessage : serverMessages) {
        clientMessageProcessor.process(serverMessage.getSenderId(), serverMessage.getSenderName(),
            serverMessage.getMessageId(), serverMessage.getData());
      }

      // Handle message from clients.
      List<CompanionMessage> clientMessages = companionServer.receive();
      for (CompanionMessage clientMessage : clientMessages) {
        serverMessageProcessor.process(clientMessage.getSenderId(), clientMessage.getSenderName(),
            clientMessage.getMessageId(), clientMessage.getData());
      }
    } finally {
      handler.postDelayed(this, DELAY_MILLIS);
    }
  }


  // Testing.
  @VisibleForTesting
  public CompanionServer getServer() {
    return companionServer;
  }

  public CompanionClients getClients() {
    return companionClients;
  }
}
