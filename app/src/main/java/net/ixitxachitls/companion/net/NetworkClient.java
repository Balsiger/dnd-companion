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
import net.ixitxachitls.companion.data.Data;
import net.ixitxachitls.companion.net.raw.Transmitter;
import net.ixitxachitls.companion.proto.Entry;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;

/**
 * Client able to talk to a Companion server.
 */
public class NetworkClient {

  private final Data data;
  private Transmitter transmitter;
  private String serverId = "";
  private String serverName = "(not yet known)";

  public NetworkClient(Data data) {
    this.data = data;
    Status.log("creating network client");
  }

  public String getServerId() {
    return serverId;
  }

  public boolean start(InetAddress address, int port) {
    Status.log("starting client to " + address + ":" + port);
    try {
      this.transmitter = new Transmitter("client", address, port);
    } catch (IOException e) {
      Status.log("cannot create transmitter for " + address + ":" + port);
      return false;
    }

    transmitter.start();
    return true;
  }

  public void stop() {
    Status.log("stopping client");
    if (transmitter != null) {
      transmitter.stop();
    }
  }

  public void send(CompanionMessageData message) {
    if (transmitter != null) {
      Entry.CompanionMessageProto proto = Entry.CompanionMessageProto.newBuilder()
          .setHeader(Entry.CompanionMessageProto.Header.newBuilder()
              .setSender(Entry.CompanionMessageProto.Header.Id.newBuilder()
                  .setId(data.settings().getAppId())
                  .setName(data.settings().getNickname())
                  .build())
              .setReceiver(Entry.CompanionMessageProto.Header.Id.newBuilder()
                  .setId(serverId)
                  .setName(serverName)
                  .build())
              .build())
          .setData(message.toProto())
          .build();

      transmitter.send(proto);
    } else {
      Status.log("No transmitter, cannot send");
    }
  }

  public Optional<CompanionMessage> receive() {
    if (transmitter == null) {
      return Optional.empty();
    }

    Optional<Entry.CompanionMessageProto> message = transmitter.receive();
    if (!message.isPresent()) {
      return Optional.empty();
    }

    // Handle welcome message to obtain server id and name.
    if (message.get().getData().getPayloadCase()
        == Entry.CompanionMessageProto.Payload.PayloadCase.WELCOME) {
      serverId = message.get().getData().getWelcome().getId();
      serverName = message.get().getData().getWelcome().getName();
      Status.log("setting server id to " + serverId + "/" + serverName);
    }

    return Optional.of(CompanionMessage.fromProto(data, message.get()));
  }
}
