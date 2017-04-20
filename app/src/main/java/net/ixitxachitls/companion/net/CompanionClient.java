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

import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.ui.activities.CompanionTransmitter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Client able to talk to a Campaign server.
 */
public class CompanionClient {
  private CompanionTransmitter transmitter;
  private final InetAddress address;
  private final int port;

  public CompanionClient(InetAddress address, int port) {
    Log.d("Client", "creating client to " + address + ":" + port);
    this.address = address;
    this.port = port;
  }

  public void send(Data.CompanionMessageProto message) {
    if (transmitter != null) {
      transmitter.send(message);
    }
  }

  public void start() {
    Log.d("Client", "starting client");
    try {
      transmitter = new CompanionTransmitter("client", new Socket(address, port));
      transmitter.start();
      transmitter.send(Data.CompanionMessageProto.newBuilder()
          .setDebug("This is the client sending information")
          .build());

    } catch (IOException e) {
      Log.e("Client", "cannot open client socket: " + e);
      e.printStackTrace();
    }
  }

  public void stop() {

  }

  public Optional<Data.CompanionMessageProto> receive() {
    if (transmitter == null) {
      return Optional.absent();
    }

    return transmitter.receive();
  }
}