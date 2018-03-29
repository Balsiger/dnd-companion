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

package net.ixitxachitls.companion.net.raw;

import android.support.annotation.Nullable;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.proto.Data;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * A receiver running on its own thread to receive messages and store them in a queue for later
 * processing.
 */
class Receiver implements Runnable {
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
        Data.CompanionMessageProto message =
            Data.CompanionMessageProto.parseDelimitedFrom(socket.getInputStream());
        if (message != null) {
          Status.log(name + " received " + ScheduledMessage.info(message) + " from "
              + ScheduledMessage.info(message.getHeader().getSender()));
          queue.put(message);
        }
      } catch (IOException | InterruptedException e) {
        Status.exception(name + " receiver error: ", e);
        break;
      }
    }
  }

  @Nullable
  public Data.CompanionMessageProto receive() {
    return queue.poll();
  }
}
