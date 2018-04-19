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

import com.google.common.annotations.VisibleForTesting;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.proto.Entry;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A sender running on its own thread to send messages stored in a queue.
 */
public class Sender implements Runnable {
  private static final int CAPACITY = 100;

  private final String name;
  private Socket socket;
  private final BlockingQueue<Entry.CompanionMessageProto> queue =
      new ArrayBlockingQueue<>(CAPACITY);
  private final Callback callback;

  public Sender(Callback callback, String name, Socket socket) {
    this.callback = callback;
    Status.log("started " + name + " sender " + socket);
    this.socket = socket;
    this.name = name;
  }

  public void setSocket(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        send(queue.take());
      } catch (InterruptedException e) {
        Status.log("sender " + name + " interrupted");
      }
    }
  }

  public boolean add(Entry.CompanionMessageProto message) {
    return queue.add(message);
  }

  private void send(Entry.CompanionMessageProto message) {
    try {
      Status.log(name + " sent " + ScheduledMessage.info(message) + " to "
          + ScheduledMessage.info(message.getHeader().getReceiver()));
      message.writeDelimitedTo(socket.getOutputStream());
    } catch (IOException e) {
      queue.add(message);
      Status.exception("exception when writing message in " + name + " sender on " + socket + ": "
          + e, e);
      //callback.disconnected();
    }
  }

  public boolean hasPendingMessages() {
    return !queue.isEmpty();
  }

  @FunctionalInterface
  public interface Callback {
    void disconnected();
  }

  @VisibleForTesting
  public void closeSocket() throws IOException {
    socket.close();
  }
}
