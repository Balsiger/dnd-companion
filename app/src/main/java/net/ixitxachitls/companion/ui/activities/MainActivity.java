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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.ui.ConfirmationDialog;
import net.ixitxachitls.companion.ui.fragments.CompanionFragment;
import net.ixitxachitls.companion.ui.views.StatusView;

public class MainActivity extends CompanionActivity {

  private static final String TAG = "Main";

  // UI elements.
  private StatusView status;

  @Override
  protected void onCreate(@Nullable Bundle state) {
    super.onCreate(state);
    Log.d(TAG, "onCreate");

    CompanionFragments.init(getFragmentManager());

    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    setTitle(getString(R.string.app_name));

    View container = findViewById(R.id.activity_main);

    // Setup the status first, in case any fragment wants to set something.
    status = (StatusView) container.findViewById(R.id.status);

    CompanionFragments.get().show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      CompanionFragments.get().show(CompanionFragment.Type.settings);
      return true;
    }

    if (id == R.id.action_reset) {
      ConfirmationDialog.create(this)
          .title("Reset All Data")
          .message("Do you really want to delete all data? This step cannot be undone!")
          .yes(() -> DataBaseContentProvider.reset(getContentResolver()))
          .show();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void refresh() {
    CompanionFragments.get().refresh();
  }

  @Override
  public void status(String message) {
    status.addMessage(message);
  }

  @Override
  public void heartbeat() {
    status.heartbeat();
  }

  @Override
  public void addClientConnection(String name) {
    status.addClientConnection(name);
  }

  @Override
  public void updateClientConnection(String name) {
    status.updateClientConnection(name);
  }

  @Override
  public void addServerConnection(String name) {
    status.addServerConnection(name);
  }

  @Override
  public void updateServerConnection(String name) {
    status.updateServerConnection(name);
  }

  @Override
  public void startServer() {
    status.startServer();
  }

  @Override
  public void stopServer() {
    status.stopServer();
  }
}
