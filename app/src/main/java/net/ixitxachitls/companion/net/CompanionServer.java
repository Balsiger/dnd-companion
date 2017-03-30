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

import android.support.annotation.Nullable;
import android.util.Log;

import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.ui.activities.CompanionTransmitter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Server endpoint for network communication.
 */
public class CompanionServer implements Runnable {

  private @Nullable ServerSocket socket;
  private Thread thread;
  private Map<String, CompanionTransmitter> transmittersById = new HashMap<>();
  private Map<String, String> namesById = new HashMap<>();

  public CompanionServer() {
    this.thread = new Thread(this);
  }

  public boolean start() {
    try {
      socket = new ServerSocket(0);
      thread.start();
    } catch (IOException e) {
      Log.e(TAG, "Error creating ServerSocket: ", e);
      e.printStackTrace();
      return false;
    }

    return true;
  }

  public void stop() {
    thread.interrupt();
    try {
      socket.close();
    } catch (IOException ioe) {
      Log.e("Server", "Error when closing server socket.");
    }
  }

  public int getPort() {
    return socket.getLocalPort();
  }

  public InetAddress getAddress() {
    return socket.getInetAddress();
  }

  @Override
  public void run() {
      Log.d("Server", "ServerSocket Created, awaiting connection on "
          + socket.getInetAddress() + ":" + socket.getLocalPort());

      while (!Thread.currentThread().isInterrupted()) {
        try {
          Socket transmissionSocket = socket.accept();
          CompanionTransmitter transmitter = new CompanionTransmitter(transmissionSocket);
          // Temporarily store the transmitter without id until we get the welcome message.
          transmittersById.put("startup-" + transmittersById.keySet().size(), transmitter);
          transmitter.start();

          // Send a welcome message to the client.
          transmitter.send(Data.CompanionMessageProto.newBuilder()
              .setWelcome(Data.CompanionMessageProto.Welcome.newBuilder()
                  .setId(Settings.get().getAppId())
                  .setName(Settings.get().getNickname())
                  .build())
              .build());
          Log.d("Server", "Connected.");
        } catch (IOException e) {
          Log.d("Server", "io exception when waiting for connections: " + e);
          e.printStackTrace();
        }
      }

    for (CompanionTransmitter transmitter : transmittersById.values()) {
      transmitter.stop();
    }

    transmittersById.clear();
  }

  public List<CompanionMessage> receive() {
    List<CompanionMessage> messages = new ArrayList<>();

    for (Map.Entry<String, CompanionTransmitter> transmitter : transmittersById.entrySet()) {
      Data.CompanionMessageProto message = transmitter.getValue().receive();
      if (message != null) {
        String id;
        String name;
        if (message.hasWelcome()) {
          id = message.getWelcome().getId();
          name = message.getWelcome().getName();

          transmittersById.remove(transmitter.getKey());
          transmittersById.put(id, transmitter.getValue());
          namesById.put(id, name);
        } else {
          id = transmitter.getKey();
          name = namesById.getOrDefault(id, "(unknown)");
        }
        messages.add(new CompanionMessage(id, name, message));
      }
    }

    return messages;
  }
}
