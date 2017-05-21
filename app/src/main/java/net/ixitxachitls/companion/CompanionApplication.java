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
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.widget.Toast;

import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Images;
import net.ixitxachitls.companion.net.CompanionMessage;
import net.ixitxachitls.companion.net.CompanionPublisher;
import net.ixitxachitls.companion.net.CompanionSubscriber;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.ui.activities.CompanionActivity;
import net.ixitxachitls.companion.util.Ids;

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
  private @Nullable CompanionActivity currentActivity;
  private boolean serverStarted = false;

  @Override
  public void onCreate() {
    super.onCreate();

    Entries.init(this);
    Settings.init(this);
    Images.load(this);
    Campaigns.load(this);
    Characters.load(this);

    messageHandler = new Handler();
    messageChecker = new MessageChecker();

    companionPublisher = CompanionPublisher.init(getApplicationContext(), this);
    companionSubscriber = CompanionSubscriber.init(getApplicationContext());

    messageChecker.run();

    // Start discovering network services.
    companionSubscriber.start();

    registerActivityLifecycleCallbacks(this);

    Campaigns.get().publish();
    Characters.get().publish();
  }

  private class MessageChecker implements Runnable {

    public static final int DELAY_MILLIS = 1_000;

    @Override
    public void run() {
      if (currentActivity != null) {
        currentActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            currentActivity.heartbeat();
          }
        });
      }

      try {
        // Chek for messages from servers.
        List<CompanionMessage> clientMessages = companionSubscriber.receive();
        for (CompanionMessage serverMessage : clientMessages) {
          handleMessagesFromServer(serverMessage);
        }

        // Handle message from clients.
        List<CompanionMessage> serverMessages = companionPublisher.receive();
        for (CompanionMessage serverMessage : serverMessages) {
          handleMessagesFromClient(serverMessage);
        }

        if (clientMessages.isEmpty() && serverMessages.isEmpty()) {
          Log.d(TAG, "No new messages.");
        }
      } finally {
        messageHandler.postDelayed(messageChecker, DELAY_MILLIS);
      }
    }
  }

  private void handleMessagesFromServer(CompanionMessage message) {
    if (message.getProto().hasWelcome()) {
      currentActivity.addServerConnection(message.getName());

      // Republish all client content for the server's campaigns.
      for (Campaign campaign : Campaigns.get().getCampaigns(message.getId())) {
        Characters.get().publish(campaign.getCampaignId());
      }
    }

    if (message.getProto().hasCampaign()) {
      Campaign campaign = Campaign.fromRemoteProto(message.getProto().getCampaign());
      Campaigns.get().addOrUpdate(campaign);
      Log.d(TAG, "received campaign " + campaign.getName());
      status("received campaign " + campaign.getName());
      refresh();

      currentActivity.updateServerConnection(message.getName());
    }

    if (!message.getProto().getDebug().isEmpty()) {
      Toast.makeText(getApplicationContext(),
          message.getName() + ": " + message.getProto().getDebug(),
          Toast.LENGTH_LONG).show();
    }
  }

  private void handleMessagesFromClient(CompanionMessage message) {
    if (message.getProto().hasWelcome()) {
      CompanionPublisher.get().republish(Campaigns.get().getLocalCampaigns(),
          message.getId());
      currentActivity.addClientConnection(message.getName());
    }

    if (message.getProto().hasCharacter()) {
      Log.d(TAG, "received character " + message.getProto().getCharacter().getName()
          + " from " + message.getName());
      Character character = Character.fromRemoteProto(message.getProto().getCharacter());
      Characters.get().addOrUpdate(character);
      status("received character "
          + message.getProto().getCharacter().getName()
          + " from " + message.getName());
      currentActivity.updateClientConnection(message.getName());
      refresh();
    }

    if (message.getProto().hasImage()) {
      Log.d(TAG, "received image for " + message.getProto().getImage().getType()
          + " " + message.getProto().getImage().getId());
      Data.CompanionMessageProto.Image image = message.getProto().getImage();
      Images.get().save(image.getType(), Ids.makeRemote(image.getId()),
          Images.asBitmap(image.getImage()));
      refresh();
    }

    if (!message.getProto().getDebug().isEmpty()) {
      Toast.makeText(getApplicationContext(),
          message.getName() + ": " + message.getProto().getDebug(),
          Toast.LENGTH_LONG).show();
    }
  }

  private void status(String message) {
    if (currentActivity != null) {
      currentActivity.status(message);
    }
  }

  public void serverStopped() {
    if (currentActivity != null) {
      currentActivity.startServer();
    } else {
      serverStarted = false;
    }
  }

  public void serverStarted() {
    if (currentActivity != null) {
      currentActivity.stopServer();
    } else {
      serverStarted = true;
    }
  }

  private void refresh() {
    if (currentActivity != null) {
      currentActivity.refresh();
    }
  }

  // Activity lifecyle callbacks.
  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    currentActivity = (CompanionActivity) activity;
  }

  @Override
  public void onActivityStarted(Activity activity) {
  }

  @Override
  public void onActivityResumed(Activity activity) {
    // Don't mark the server as stopped if it has not been started. This prevents displaying a
    // server that might never be used by the user.
    if (serverStarted) {
      currentActivity.startServer();
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
