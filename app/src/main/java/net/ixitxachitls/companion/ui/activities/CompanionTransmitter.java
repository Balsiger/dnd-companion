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

package net.ixitxachitls.companion.ui.activities;

import android.support.annotation.Nullable;
import android.util.Log;

import net.ixitxachitls.companion.proto.Data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * Base class for campaign client and server to actually transmit data.
 */
public class CompanionTransmitter {

  private final Sender sender;
  private final Thread senderThread;
  private final Receiver receiver;
  private final Thread receiverThread;
  private String name;
  private final Socket socket;


  public CompanionTransmitter(String name, Socket socket) {
    this.name = name;
    this.socket = socket;
    this.sender = new Sender();
    this.receiver = new Receiver();
    this.senderThread = new Thread(sender);
    this.receiverThread = new Thread(receiver);
  }

  public void start() {
    senderThread.start();
    receiverThread.start();
  }

  public void stop() {
    senderThread.interrupt();
    receiverThread.interrupt();
  }

  private class Sender implements Runnable {
    private BlockingQueue<Data.CompanionMessageProto> queue;
    private int CAPACITY = 10;

    public Sender() {
      queue = new ArrayBlockingQueue<>(CAPACITY);
    }

    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          send(queue.take());
        } catch (InterruptedException e) {
        }
      }
    }

    public void send(Data.CompanionMessageProto message) {
      try {
        DataOutputStream out =
            new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        byte []data = message.toByteArray();
        out.writeInt(data.length);
        out.write(message.toByteArray());
        out.flush();
        Log.d("Transmitter", name + " sent message: " + message);
      } catch (UnknownHostException e) {
        Log.d("Transmitter", name + ": Unknown Host", e);
      } catch (IOException e) {
        Log.d("Transmitter", name + ": I/O Exception", e);
      }
    }
  }

  private class Receiver implements Runnable {
    TransferQueue<Data.CompanionMessageProto> queue = new LinkedTransferQueue<>();

    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          DataInputStream input =
              new DataInputStream(new BufferedInputStream(socket.getInputStream()));
          int length = input.readInt();
          byte[] data = new byte[length];
          input.read(data);
          Data.CompanionMessageProto message = Data.CompanionMessageProto.parseFrom(data);
          Log.d("Transmitter", name + " read from the stream: " + message.toString());
          queue.put(message);
        } catch (IOException | InterruptedException e) {
          Log.e("Transmitter", name + " receiver loop error: ", e);
          break;
        }
      }
    }
  }

  public void send(Data.CompanionMessageProto message) {
    Log.d("Transmitter", name + ": enqueing message");
    sender.queue.add(message);
  }

  public @Nullable Data.CompanionMessageProto receive() {
    Log.d("Transmitter", name + ": trying to receive message");
    return receiver.queue.poll();
  }
}
