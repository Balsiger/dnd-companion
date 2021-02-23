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

package net.ixitxachitls.companion;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;

import com.google.common.base.Stopwatch;
import com.tenmiles.helpstack.HSHelpStack;
import com.tenmiles.helpstack.gears.HSEmailGear;

import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.Adventures;
import net.ixitxachitls.companion.data.documents.Campaigns;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.CreatureConditions;
import net.ixitxachitls.companion.data.documents.Encounters;
import net.ixitxachitls.companion.data.documents.Images;
import net.ixitxachitls.companion.data.documents.Invites;
import net.ixitxachitls.companion.data.documents.Messages;
import net.ixitxachitls.companion.data.documents.Monsters;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.data.documents.Users;
import net.ixitxachitls.companion.storage.ApplicationAssetAccessor;
import net.ixitxachitls.companion.storage.AssetAccessor;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.activities.MainActivity;
import net.ixitxachitls.companion.ui.fragments.CompanionFragment;

import java.time.Duration;
import java.util.Optional;

/**
 * The main application for the companion.
 */
public class CompanionApplication extends Application
    implements Application.ActivityLifecycleCallbacks {

  private static final String PROGRESS_LOADING = "entities";

  private static CompanionApplication application;

  private final AssetAccessor assetAccessor;

  private ApplicationCompanionContext context;
  private Activity currentActivity;
  private boolean updating = false;
  private Stopwatch stopwatch = Stopwatch.createUnstarted();

  public CompanionApplication() {
    this.assetAccessor = new ApplicationAssetAccessor(this);
  }

  public interface Updatable {
    public void update();
  }

  public AssetAccessor getAssetAccessor() {
    return assetAccessor;
  }

  public Activity getCurrentActivity() {
    return currentActivity;
  }

  public Adventures adventures() {
    return context.adventures();
  }

  public Campaigns campaigns() {
    return context.campaigns();
  }

  public Characters characters() {
    return context.characters();
  }

  public CreatureConditions conditions() {
    return context.conditions();
  }

  public CompanionContext context() {
    return context;
  }

  public Duration elapsed() {
    return stopwatch.elapsed();
  }

  public Encounters encounters() {
    return context.encounters();
  }

  public Images images() {
    return context.images();
  }

  public Invites invites() {
    return context.invites();
  }

  public void logEvent(String id, String name, String type) {
    if (currentActivity instanceof MainActivity) {
      ((MainActivity) currentActivity).logEvent(id, name, type);
    }
  }

  public User me() {
    return context.me();
  }

  public Messages messages() {
    return context.messages();
  }

  public Monsters monsters() {
    return context.monsters();
  }

  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    currentActivity = activity;
  }

  @Override
  public void onActivityDestroyed(Activity activity) {
  }

  @Override
  public void onActivityPaused(Activity activity) {
  }

  @Override
  public void onActivityResumed(Activity activity) {
    currentActivity = activity;
  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
  }

  @Override
  public void onActivityStarted(Activity activity) {
    if (currentActivity instanceof MainActivity) {
      MainActivity main = (MainActivity) currentActivity;

      AsyncTask.execute(() -> {
        Templates.init(CompanionApplication.this.getAssetAccessor(), main);
        Templates.get().executeAfterLoading(() -> {
          users().executeAfterLoggingIn(() -> {
            Status.log("Loading done, showing campaigns");
            CompanionFragments.get().show(CompanionFragment.Type.campaigns, Optional.empty());
          });
        });
      });
    }

    currentActivity = activity;
  }

  @Override
  public void onActivityStopped(Activity activity) {
  }

  @Override
  public void onCreate() {
    application = this;
    super.onCreate();

    context = new ApplicationCompanionContext(this);
    registerActivityLifecycleCallbacks(this);

    HSHelpStack helpStack = HSHelpStack.getInstance(this);
    HSEmailGear emailGear = new HSEmailGear("companion@ixitxachitls.net",
        R.xml.faq);
    helpStack.setGear(emailGear);
  }

  public void startWatch() {
    if (stopwatch.isRunning()) {
      stopwatch.reset();
    }

    stopwatch.start();
  }

  public void update(String source) {
    runOnUiThread(() -> {
      if (updating) {
        Status.error("Cannot update during an update! (" + source + ")");
      } else {
        updating = true;
        Status.update("Starting update: " + source);

        if (currentActivity instanceof MainActivity) {
          ((MainActivity) currentActivity).update();
          Status.update("Update done: " + source);
        } else {
          Status.error("Don't know how to update " + currentActivity + " (" + source + ")");
        }

        updating = false;
      }
    });
  }

  public Users users() {
    return context.users();
  }

  private void runOnUiThread(Runnable action) {
    if (currentActivity instanceof MainActivity) {
      MainActivity main = (MainActivity) currentActivity;
      main.runOnUiThread(action);
    }
  }

  public static CompanionApplication get() {
    return application;
  }

  public static CompanionApplication get(Context context) {
    return (CompanionApplication) context.getApplicationContext();
  }
}
