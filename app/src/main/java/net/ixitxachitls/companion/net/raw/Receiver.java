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

package net.ixitxachitls.companion.net.raw;

import android.support.annotation.Nullable;
import android.util.Log;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.proto.Data;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * A receiver running on its own thread to receive messages and store them in a queue for later
 * processing.
 */
class Receiver implements Runnable {

  private static final String TAG = "Receiver";

  private final String name;
  private final Socket socket;
  private final TransferQueue<Data.CompanionMessageProto> queue = new LinkedTransferQueue<>();

  public Receiver(String name, Socket socket) {
    this.name = name;
    this.socket = socket;
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        DataInputStream input =
            new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        int length = input.readInt();
        if (length < 0 || length > 1_000_000) {
          throw new IllegalArgumentException("Cannot read message with size of " + length
              + " from stream!");
        }
        byte[] data = new byte[length];
        int read = input.read(data);
        if (read != length) {
          Log.e(TAG, name + ": length of content does not match, expected " + length + " but got "
              + read);
        }
        Data.CompanionMessageProto message = Data.CompanionMessageProto.parseFrom(data);
        Log.d("Transmitter", name + " read from the stream: " + message.toString());
        Status.log(name + " received " + ScheduledMessage.info(message) + " from "
            + ScheduledMessage.info(message.getHeader().getSender()));
        queue.put(message);
      } catch (IOException | InterruptedException e) {
        Log.e("Transmitter", name + " receiver error: ", e);
        break;
      }
    }
  }

  @Nullable
  public Data.CompanionMessageProto receive() {
    return queue.poll();
  }
}
