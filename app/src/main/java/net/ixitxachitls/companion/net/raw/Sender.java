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

import android.util.Log;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.proto.Data;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A sender running on its own thread to send messages stored in a queue.
 */
class Sender implements Runnable {
  public static final String TAG = "Sender";
  private static final int CAPACITY = 100;

  private final String name;
  private final Socket socket;
  private final BlockingQueue<Data.CompanionMessageProto> queue =
      new ArrayBlockingQueue<>(CAPACITY);

  public Sender(String name, Socket socket) {
    this.socket = socket;
    this.name = name;
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        send(queue.take());
      } catch (InterruptedException e) {
        log("interrupted");
      }
    }
  }

  public boolean add(Data.CompanionMessageProto message) {
    return queue.add(message);
  }

  private void send(Data.CompanionMessageProto message) {
    try {
      DataOutputStream out =
          new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
      byte[] data = message.toByteArray();
      out.writeInt(data.length);
      out.write(message.toByteArray());
      out.flush();
      log("sent message: " + message);
      Status.log(name + " sent " + ScheduledMessage.info(message) + " to "
          + ScheduledMessage.info(message.getHeader().getReceiver()));
    } catch (UnknownHostException e) {
      log("Unknown Host", e);
    } catch (IOException e) {
      log("I/O Exception", e);
    }
  }

  private void log(String message) {
    Log.d(TAG, name + ": " + message);
  }

  private void log(String message, Exception e) {
    Log.d(TAG, name + ": " + message, e);
  }
}
