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

import com.google.common.base.Optional;

import net.ixitxachitls.companion.proto.Data;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Class supporting sending and receiving data. The transmitter starts two threads, and
 * sending and a receiving on the given socket.
 */
public class Transmitter {

  public static final String TAG = "Transmitter";

  private final String name;
  private final Sender sender;
  private final Thread senderThread;
  private final Receiver receiver;
  private final Thread receiverThread;

  public Transmitter(String name, InetAddress address, int port) throws IOException {
    this(name, new Socket(address, port));
  }

  public Transmitter(String name, Socket socket) {
    this.name = name;
    this.sender = new Sender(name, socket);
    this.receiver = new Receiver(name, socket);
    this.senderThread = new Thread(sender);
    this.receiverThread = new Thread(receiver);
  }

  public void start() {
    if (!senderThread.isAlive() && !senderThread.isInterrupted()) {
      senderThread.start();
    }

    if (!receiverThread.isAlive() && !receiverThread.isInterrupted()) {
      receiverThread.start();
    }
  }

  public void stop() {
    senderThread.interrupt();
    receiverThread.interrupt();
  }

  public void send(Data.CompanionMessageProto message) {
    Log.d(TAG, name + ": enqueing message");
    sender.add(message);
  }

  public Optional<Data.CompanionMessageProto> receive() {
    return Optional.fromNullable(receiver.receive());
  }
}
