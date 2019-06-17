/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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

package net.ixitxachitls.companion.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.drive.DriveStorage;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.MessageDialog;
import net.ixitxachitls.companion.ui.fragments.CompanionFragment;
import net.ixitxachitls.companion.ui.views.ActionBarView;
import net.ixitxachitls.companion.ui.views.StatusView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

  public static final int CODE_SIGN_IN = 1;
  public static final int CODE_DRVIE_AUTH = 2;

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-DD");

  private FirebaseAnalytics analytics;

  // UI elements.
  private StatusView status;
  private ActionBarView actions;
  private Optional<DriveStorage> drive = Optional.empty();

  public MainActivity() {
  }

  public ActionBarView.Action addAction(@DrawableRes int drawable, String title,
                                        String description) {
    return actions.addAction(drawable, title, description);
  }

  public ActionBarView.ActionGroup addActionGroup(@DrawableRes int drawable, String title,
                                                  String description) {
    return actions.addActionGroup(drawable, title, description);
  }

  public void clearActions() {
    actions.clearActions();
  }

  public void finishLoading(String text) {
    actions.finishLoading(text);
  }

  public void logEvent(String id, String name, String type) {
    Bundle bundle = new Bundle();
    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
    analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      case R.id.action_log:
        Status.toggleDebug();
        return true;

      case R.id.action_about:
        MessageDialog.create(this)
            .layout(R.layout.dialog_about)
            .show();
        return true;

      case R.id.action_sign_out:
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener(task -> finish());
        return true;

      case R.id.action_export:
        // Try to export again.
        export();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public void startLoading(String text) {
    actions.startLoading(text);
  }

  private void create() {
    setTheme(R.style.AppTheme_NoActionBar);

    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    setTitle(getString(R.string.app_name));

    View container = findViewById(R.id.activity_main);
    // Setup the status first, in case any fragment wants to log something.
    status = (StatusView) container.findViewById(R.id.status);
    Status.setView(status);

    actions = findViewById(R.id.actions);

    CompanionFragments.get().show();
  }

  private void exit() {
    finishAffinity();
    finishAndRemoveTask();
    android.os.Process.killProcess(android.os.Process.myPid());
  }

  private void export() {
    if (!drive.isPresent()) {
      drive = Optional.of(new DriveStorage(getApplicationContext()));
    }

    CompanionApplication application = CompanionApplication.get();
    application.me().readMiniatures(() -> {
      drive.get().save(
          getString(R.string.app_name) + " " + DATE_FORMAT.format(new Date()),
          ImmutableMap.<String, String>builder()
              .put("User - " + application.me().getNickname(),
                  formatData(application.me().write(new HashMap<>())))
              .put("User - Miniatures",
                  formatData(application.me().writeMiniatures()))
              .putAll(application.campaigns().getDMCampaigns().stream()
                  .collect(Collectors.toMap(c -> "Campaign - " + c.getName(),
                      c -> formatData(c.write()))))
              .putAll(application.characters().getPlayerCharacters(application.me().getId())
                  .stream()
                  .collect(Collectors.toMap(c -> "Character - " + c.getName(), c -> formatData(c.write()))))
              .build(),
          () -> Status.toast("All data has been successfully exported to Drive."),
          e -> {
            if (e instanceof UserRecoverableAuthIOException) {
              startActivityForResult(((UserRecoverableAuthIOException) e).getIntent(),
                  CODE_DRVIE_AUTH);
            }
          });
    });
  }

  private String formatData(Map<String, Object> data) {
    try {
      return new JSONObject(data).toString(2);
    } catch (JSONException e) {
      return data.toString();
    }
  }

  private void noExit() {
    // Nothing to do here, we just ignore the with to exit.
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch (requestCode) {
      case CODE_SIGN_IN:
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (resultCode == RESULT_OK) {
          FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
          CompanionApplication.get(this).context().users().login(
              user.getUid(), user.getPhotoUrl().toString());
          Status.log("Successfully logged in");
        } else if (response == null) {
          Status.error("Login required");
          MessageDialog.create(this)
              .title("Login Required")
              .message("You have to login using your Google account to have access to your "
                  + "characters and campaigns.")
              .show();
        } else {
          Status.error("Login failed: " + response.getError().getMessage());
          MessageDialog.create(this)
              .title("Login Failed")
              .message("Login failed with error: " + response.getError().getMessage())
              .show();
        }
        break;

      case CODE_DRVIE_AUTH:
        export();
        break;
    }
  }

  @Override
  public void onBackPressed() {
    if (!CompanionFragments.get().goBack()) {
      ConfirmationPrompt.create(this).title("Exit?")
          .message("Do you really want to exit the Roleplay Companion?")
          .no(this::noExit)
          .yes(this::exit)
          .show();
    }
  }

  @Override
  protected void onCreate(@Nullable Bundle state) {
    CompanionFragments.init(CompanionApplication.get(this).context(),
        getSupportFragmentManager());

    // Log the user in.
    List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());

    // Create and launch sign-in intent
    startActivityForResult(AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .build(), CODE_SIGN_IN);

    super.onCreate(state);

    analytics = FirebaseAnalytics.getInstance(this);
    create();
  }

  @Override
  public void onDestroy() {
    Status.clearView();
    Status.log("main activity destroyed");

    super.onDestroy();
  }

  private void refresh() {
    Optional<CompanionFragment> fragment = CompanionFragments.get().getCurrentFragment();
    if (fragment.isPresent()) {
      fragment.get().refresh();
    }
  }
}
