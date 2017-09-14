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

package net.ixitxachitls.companion;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDexApplication;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Images;
import net.ixitxachitls.companion.net.ClientMessageProcessor;
import net.ixitxachitls.companion.net.CompanionMessage;
import net.ixitxachitls.companion.net.CompanionPublisher;
import net.ixitxachitls.companion.net.CompanionSubscriber;
import net.ixitxachitls.companion.net.ScheduledMessages;
import net.ixitxachitls.companion.net.ServerMessageProcessor;
import net.ixitxachitls.companion.ui.activities.CompanionActivity;

import java.util.List;

/**
 * The main application for the companion.
 */
public class CompanionApplication extends MultiDexApplication
  implements Application.ActivityLifecycleCallbacks {

  private static String TAG = "App";

  private static CompanionPublisher companionPublisher;
  private static CompanionSubscriber companionSubscriber;
  private static Handler messageHandler;
  private static MessageChecker messageChecker;
  private Optional<CompanionActivity> currentActivity = Optional.absent();
  private boolean serverStarted = false;
  private Optional<ClientMessageProcessor> clientMessageProcessor = Optional.absent();
  private Optional<ServerMessageProcessor> serverMessageProcessor = Optional.absent();

  @Override
  public void onCreate() {
    super.onCreate();

    Entries.init(this);
    Settings.init(this);
    ScheduledMessages.init(this);
    Images.load(this);
    Campaigns.load(this);
    Characters.load(this);

    messageHandler = new Handler();
    messageChecker = new MessageChecker();

    companionPublisher = CompanionPublisher.init(this);
    companionSubscriber = CompanionSubscriber.init(getApplicationContext(), this);

    messageChecker.run();

    // Start discovering network services.
    companionSubscriber.start();

    registerActivityLifecycleCallbacks(this);

    Campaigns.publish();
    Characters.publish();
  }

  private class MessageChecker implements Runnable {

    public static final int DELAY_MILLIS = 5_000;

    @Override
    public void run() {
      if (currentActivity.isPresent()) {
        currentActivity.get().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            currentActivity.get().heartbeat();
          }
        });
      }

      try {
        // Send waiting messages.
        CompanionPublisher.get().sendWaiting();
        CompanionSubscriber.get().sendWaiting();

        // Chek for messages from servers.
        if (clientMessageProcessor.isPresent()) {
          List<CompanionMessage> clientMessages = companionSubscriber.receive();
          for (CompanionMessage serverMessage : clientMessages) {
            clientMessageProcessor.get().process(serverMessage);
          }
        }

        // Handle message from clients.
        if (serverMessageProcessor.isPresent()) {
          List<CompanionMessage> serverMessages = companionPublisher.receive();
          for (CompanionMessage serverMessage : serverMessages) {
            serverMessageProcessor.get().process(serverMessage);
          }
        }
      } finally {
        messageHandler.postDelayed(messageChecker, DELAY_MILLIS);
      }
    }
  }

  public void status(String message) {
    if (currentActivity.isPresent()) {
      currentActivity.get().status(message);
    }
  }

  public void serverStopped() {
    if (currentActivity.isPresent()) {
      currentActivity.get().startServer();
    } else {
      serverStarted = false;
    }
  }

  public void serverStarted() {
    if (currentActivity.isPresent()) {
      currentActivity.get().stopServer();
    } else {
      serverStarted = true;
    }
  }

  public void refresh() {
    if (currentActivity.isPresent()) {
      currentActivity.get().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          currentActivity.get().refresh();
        }
      });
    }
  }

  public void updateClientConnection(String name) {
    if (currentActivity.isPresent()) {
      currentActivity.get().updateClientConnection(name);
    }
  }

  public Optional<ClientMessageProcessor> getClientMessageProcessor() {
    return clientMessageProcessor;
  }

  public Optional<ServerMessageProcessor> getServerMessageProcessor() {
    return serverMessageProcessor;
  }

  // Activity lifecyle callbacks.
  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    currentActivity = Optional.of((CompanionActivity) activity);
    clientMessageProcessor = Optional.of(new ClientMessageProcessor((CompanionActivity) activity));
    serverMessageProcessor = Optional.of(new ServerMessageProcessor((CompanionActivity) activity));
  }

  @Override
  public void onActivityStarted(Activity activity) {
  }

  @Override
  public void onActivityResumed(Activity activity) {
    // Don't mark the server as stopped if it has not been started. This prevents displaying a
    // server that might never be used by the user.
    if (serverStarted && currentActivity.isPresent()) {
      currentActivity.get().startServer();
    }
  }

  @Override
  public void onActivityPaused(Activity activity) {
  }

  @Override
  public void onActivityStopped(Activity activity) {
  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
  }

  @Override
  public void onActivityDestroyed(Activity activity) {
  }
}
