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

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Campaign;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.net.CompanionMessage;
import net.ixitxachitls.companion.net.CompanionPublisher;
import net.ixitxachitls.companion.net.CompanionSubscriber;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.ui.ListProtoAdapter;
import net.ixitxachitls.companion.ui.Setup;
import net.ixitxachitls.companion.ui.fragments.CampaignListFragment;
import net.ixitxachitls.companion.ui.fragments.EditCampaignFragment;
import net.ixitxachitls.companion.ui.fragments.EditFragment;
import net.ixitxachitls.companion.ui.fragments.SettingsFragment;

import java.util.List;

public class MainActivity extends Activity implements EditFragment.AttachAction {

  private ListProtoAdapter<Data.CampaignProto> campaignsAdapter;
  private Settings settings;
  private CompanionPublisher companionPublisher;
  private CompanionSubscriber companionSubscriber;
  private Handler messageHandler;
  private MessageChecker messageChecker;
  private CampaignListFragment campaignListFragment;
  private SettingsFragment settingsFragment;
  private TextView status;
  private Fragment lastFragment;
  private Fragment currentFragment;

  public enum Fragments { campaigns, settings, };

  private void init() {
    Entries.init(this);
    settings = Settings.init(this);

    messageHandler = new Handler();
    messageChecker = new MessageChecker();

    companionPublisher = CompanionPublisher.init(getApplicationContext());
    companionSubscriber = CompanionSubscriber.init(getApplicationContext());

    messageChecker.run();

    // Start discovering network services.
    companionSubscriber.start();
  }

  public void setStatus(String status) {
    this.status.setText(status);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setup(savedInstanceState, R.layout.activity_main, R.string.app_name);
    View container = findViewById(R.id.activity_main);

    // Setup the status first, in case any fragment wants to set something.
    status = Setup.textView(container, R.id.status, null);

    init();

    show(Fragments.campaigns);
  }

  @Override
  protected void onDestroy() {
    companionPublisher.stop();
    companionSubscriber.stop();
    super.onDestroy();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_campaign_selection, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      show(Fragments.settings);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public void show(Fragments fragment) {
    lastFragment = currentFragment;

    switch(fragment) {
      case campaigns:
        if (campaignListFragment == null) {
          campaignListFragment = new CampaignListFragment();
        }
        show(campaignListFragment);
        break;

      case settings:
        if (settingsFragment == null) {
          settingsFragment = new SettingsFragment();
        }
        show(settingsFragment);
        break;
    }
  }

  private void show(Fragment fragment) {
    lastFragment = currentFragment;
    currentFragment = fragment;
    getFragmentManager()
        .beginTransaction()
        .replace(R.id.content, fragment)
        .commit();
  }

  public void showLast() {
    if (lastFragment != null) {
      show(lastFragment);
    } else {
      setStatus("Cannot show last fragment if there is none.");
    }
  }

  public void attached(EditFragment fragment) {
    if (fragment instanceof EditCampaignFragment) {
      ((EditCampaignFragment)fragment).setSaveListener(this::saveCampaign);
    }
  }

  public void saveCampaign(Campaign campaign) {
    campaign.store();
    //refresh();
  }

  private class MessageChecker implements Runnable {

    public static final int DELAY_MILLIS = 1_000;

    @Override
    public void run() {
      try {
        // Chek for messages from server.
        List<CompanionMessage> clientMessages = companionSubscriber.receive();
        for (CompanionMessage serverMessage : clientMessages) {
          handleClientMessage(serverMessage);
        }

        List<CompanionMessage> serverMessages = companionPublisher.receive();
        for (CompanionMessage serverMessage : serverMessages) {
          handleServerMessage(serverMessage);
        }

        if (clientMessages.isEmpty() && serverMessages.isEmpty()) {
          setStatus("No new messages.");
        }
      } finally {
        messageHandler.postDelayed(messageChecker, DELAY_MILLIS);
      }
    }
  }

  private void handleClientMessage(CompanionMessage message) {
    if (message.getProto().hasWelcome()) {
      Toast.makeText(getApplicationContext(), "Client " + message.getName() + " has connected!",
          Toast.LENGTH_LONG).show();
    }

    if (!message.getProto().getDebug().isEmpty()) {
      Toast.makeText(getApplicationContext(),
          message.getName() + ": " + message.getProto().getDebug(),
          Toast.LENGTH_LONG).show();
    }
  }

  private void handleServerMessage(CompanionMessage message) {
    if (message.getProto().hasWelcome()) {
      Toast.makeText(getApplicationContext(), "Server " + message.getName() + " has connected!",
          Toast.LENGTH_LONG).show();
    }

    if (!message.getProto().getDebug().isEmpty()) {
      Toast.makeText(getApplicationContext(),
          message.getName() + ": " + message.getProto().getDebug(),
          Toast.LENGTH_LONG).show();
    }
  }
}
