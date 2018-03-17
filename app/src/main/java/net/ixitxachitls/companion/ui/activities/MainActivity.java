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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.drive.DriveStorage;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Images;
import net.ixitxachitls.companion.net.CompanionMessenger;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.ui.ConfirmationDialog;
import net.ixitxachitls.companion.ui.fragments.CompanionFragment;
import net.ixitxachitls.companion.ui.views.StatusView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainActivity extends AppCompatActivity {

  public static final int RESOLVE_DRIVE_CONNECTION_CODE = 1;
  public static final int DRIVE_IMPORT_OPEN_CODE = 2;

  private DriveStorage driveStorage;

  // UI elements.
  private StatusView status;
  private Menu menu;

  @Override
  protected void onCreate(@Nullable Bundle state) {
    super.onCreate(state);
    Status.log("onCreate");

    CompanionFragments.init(getSupportFragmentManager());
    driveStorage = new DriveStorage(this);

    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    setTitle(getString(R.string.app_name));

    View container = findViewById(R.id.activity_main);

    // Setup the status first, in case any fragment wants to log something.
    status = (StatusView) container.findViewById(R.id.status);
    Status.setView(status);
    CompanionFragments.get().show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    this.menu = menu;

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      CompanionFragments.get().show(CompanionFragment.Type.settings, Optional.empty());
      return true;
    }

    if (id == R.id.action_export) {
      List<DriveStorage.File> files = new ArrayList<>();
      // Export local campaigns.
      for (Campaign campaign : Campaigns.getLocalCampaigns()) {
        files.add(new DriveStorage.TextFile(campaign.getName() + ".campaign.txt",
            campaign.getCampaignId(), "text/plain", "# This file will not be re-imported."
            + campaign.toProto().toString()));
        files.add(new DriveStorage.BinaryFile(campaign.getName() + ".campaign",
            campaign.getCampaignId(), "application/x-protobuf", campaign.toProto().toByteArray()));
      }
      // Export local characters.
      for (Character character : Characters.getLocalCharacters()) {
        files.add(new DriveStorage.TextFile(character.getName() + ".character.txt",
            character.getCharacterId(), "text/plain", "# This file will not be re-imported."
            + character.toProto().toString()));
        files.add(new DriveStorage.BinaryFile(character.getName() + ".character",
            character.getCharacterId(), "application/x-protobuf", character.toProto().toByteArray()));
        Optional<InputStream> image =
            Images.local().read(Character.TABLE, character.getCharacterId());
        if (image.isPresent()) {
          files.add(new DriveStorage.StreamFile(character.getName() + ".character.jpg",
              character.getCharacterId(), "image/jpeg", image.get()));
        }
      }

      driveStorage.start(new DriveStorage.Export(files));
    }

    if (id == R.id.action_import) {
      driveStorage.start(new DriveStorage.SelectImportFolder());
    }

    if (id == R.id.action_reset) {
      ConfirmationDialog.create(this)
          .title("Reset All Data")
          .message("Do you really want to delete all data? This step cannot be undone!")
          .yes(() -> DataBaseContentProvider.reset(getApplicationContext(), getContentResolver()))
          .show();
      return true;
    }

    if (id == R.id.action_clear_messages) {
      ConfirmationDialog.create(this)
          .title("Clear Messages")
          .message("Do you really want to clear all pending messages? This step cannot be undone!")
          .yes(() -> DataBaseContentProvider.clearMessages(
              getApplicationContext(), getContentResolver()))
          .show();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onDestroy() {
    Status.clearView();
    Status.log("main activity destroyed");

    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    if (!CompanionFragments.get().goBack()) {
      ConfirmationDialog.create(this).title("Exit?")
          .message("Do you really want to exit the Tabletop Companion?")
          .no(this::noExit)
          .yes(this::exit)
          .show();
    }
  }

  private void noExit() {
    // Nothing to do here, we just ignore the with to exit.
  }

  private void exit() {
    CompanionMessenger.get().stop();
    finishAffinity();
    finishAndRemoveTask();
    android.os.Process.killProcess(android.os.Process.myPid());
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    switch (requestCode) {
      case RESOLVE_DRIVE_CONNECTION_CODE:
        if (resultCode == RESULT_OK) {
          driveStorage.connect();
        }
        break;

      case DRIVE_IMPORT_OPEN_CODE:
        if (resultCode == RESULT_OK) {
          driveStorage.start(new DriveStorage.Import(((DriveId) data.getParcelableExtra(
              OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID)).asDriveFolder()));
        }

      default:
        super.onActivityResult(requestCode, resultCode, data);
    }
  }
}
