/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

import android.os.Handler;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.data.dynamics.XpAward;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Class handling all sending, receiving and processing of network messages.
 */
public class CompanionMessenger implements Runnable {

  public static final int DELAY_MILLIS = 1_000;
  private static CompanionMessenger singleton;

  private final Handler handler;

  private final CompanionServer companionServer;
  private final CompanionClients companionClients;
  private final ClientMessageProcessor clientMessageProcessor;
  private final ServerMessageProcessor serverMessageProcessor;

  private CompanionMessenger(CompanionApplication application) {
    this.handler = new Handler();

    ScheduledMessages.init(application);
    companionServer = new CompanionServer(application);
    companionClients = new CompanionClients(application.getApplicationContext());
    clientMessageProcessor = new ClientMessageProcessor(application);
    serverMessageProcessor = new ServerMessageProcessor(application);
  }

  public static CompanionMessenger init(CompanionApplication application) {
    if (singleton != null) {
      throw new IllegalStateException("Messenger already initialized!");
    }

    singleton = new CompanionMessenger(application);
    return singleton;
  }

  public static CompanionMessenger get() {
    if (singleton == null) {
      throw new IllegalStateException("Messenger not yet initialized");
    }

    return singleton;
  }

  public void start() {
    companionClients.start();
    // Only start the server once we actually publish a campaign

    run();
  }

  public void stop() {
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
      throw new IllegalStateException("Cannot sent remote campaign!");
    }

    companionServer.schedule(clientIds(campaign), CompanionMessageData.from(campaign));
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
              companionServer.schedule(recipientId, CompanionMessageData.from(image.get()));
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
              companionServer.schedule(recipientId, CompanionMessageData.from(image.get()));
            }
          }
        }
      }
    }
  }

  public void send(Character character) {
    Status.log("sending character " + character + " to all");
    Optional<Campaign> campaign = Campaigns.getCampaign(character.getCampaignId()).getValue();
    if (campaign.isPresent() && campaign.get().isLocal()) {
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
              // Send the image to all known clients.
              companionServer.schedule(clientIds(campaign.get()), CompanionMessageData.from(image));
            } else {
              // Send the image to the server.
              companionClients.schedule(campaign.get().getServerId(),
                  CompanionMessageData.from(image));
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

  public void sendDeletion(Campaign campaign) {
    if (!campaign.isLocal()) {
      Status.toast("cannot delete remote campaign");
    }

    companionServer.schedule(clientIds(campaign), CompanionMessageData.fromDelete(campaign));
    Status.log("deleted campaign " + campaign);
  }

  public void sendDeletion(Character character) {
    Optional<Campaign> campaign = Campaigns.getCampaign(character.getCampaignId()).getValue();
    if (campaign.isPresent()) {
      if (campaign.get().isLocal()) {
        // Forward the campaign deletion to all 'other' clients.
        companionServer.schedule(clientIds(campaign.get(), character.getServerId()),
            CompanionMessageData.fromDelete(character));
      } else {
        companionClients.schedule(campaign.get().getServerId(),
            CompanionMessageData.fromDelete(character));
      }
      Status.log("deleted character " + character);
    } else {
      Status.toast("Cannot get campaign for character " + character + " for deletion");
    }
  }

  // Images are deleted with the associated entry, thus don't need their own deletion
  // handling.

  public void sendXpAward(Campaign campaign, Character character, int xp) {
    if (!campaign.isLocal()) {
      throw new IllegalStateException("Cannot award xp for a remote campaign");
    }

    Status.log("sending xp award of " + xp + " for " + character);
    companionServer.schedule(character.getServerId(), CompanionMessageData.from(
        new XpAward(character.getCharacterId(), campaign.getCampaignId(), xp)));
  }

  public void sendAckToClient(String recipientId, long messageId) {
    Status.log("sending ack for " + messageId + " to client " + recipientId);
    companionServer.schedule(recipientId, CompanionMessageData.fromAck(messageId));
  }

  public void sendAckToServer(String recipientId, long messageId) {
    Status.log("sending ack for " + messageId + " to server " + recipientId);
    companionClients.schedule(recipientId, CompanionMessageData.fromAck(messageId));
  }

  public void sendWelcome() {
    companionClients.schedule(CompanionMessageData.fromWelcome(Settings.get().getAppId(),
        Settings.get().getNickname()));
    companionServer.schedule(CompanionMessageData.fromWelcome(Settings.get().getAppId(),
        Settings.get().getNickname()));
  }

  public void ackServer(String recipientId, long messageId){
    companionServer.ack(recipientId, messageId);
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
        clientMessageProcessor.process(serverMessage.getSenderId(),
              serverMessage.getSenderName(), serverMessage.getMessageId(),
              serverMessage.getData());
      }

      // Handle message from clients.
      List<CompanionMessage> clientMessages = companionServer.receive();
      for (CompanionMessage clientMessage : clientMessages) {
        serverMessageProcessor.process(clientMessage.getRecieverId(),
            clientMessage.getMessageId(), clientMessage.getData());
      }
    } finally {
      handler.postDelayed(this, DELAY_MILLIS);
    }
  }
}
