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
import android.support.multidex.MultiDexApplication;

import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.Adventures;
import net.ixitxachitls.companion.data.documents.Campaigns;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.CreatureConditions;
import net.ixitxachitls.companion.data.documents.Images;
import net.ixitxachitls.companion.data.documents.Invites;
import net.ixitxachitls.companion.data.documents.Messages;
import net.ixitxachitls.companion.data.documents.Monsters;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.data.documents.Users;
import net.ixitxachitls.companion.data.values.Encounters;
import net.ixitxachitls.companion.storage.ApplicationAssetAccessor;
import net.ixitxachitls.companion.storage.AssetAccessor;
import net.ixitxachitls.companion.ui.activities.MainActivity;

/**
 * The main application for the companion.
 */
public class CompanionApplication extends MultiDexApplication
    implements Application.ActivityLifecycleCallbacks {

  private static final String PROGRESS_LOADING = "entities";

  private static CompanionApplication application;

  private final AssetAccessor assetAccessor;

  private ApplicationCompanionContext context;
  private Activity currentActivity;

  public CompanionApplication() {
    this.assetAccessor = new ApplicationAssetAccessor(this);
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
  public void onActivityStarted(Activity activity) {
    if (currentActivity instanceof MainActivity) {
      MainActivity main = (MainActivity) currentActivity;
      main.startLoading(PROGRESS_LOADING);
      AsyncTask.execute(() -> {
        Templates.init(CompanionApplication.this.getAssetAccessor());
        main.runOnUiThread(() -> main.finishLoading(PROGRESS_LOADING));
      });
    }

    currentActivity = activity;
  }

  @Override
  public void onActivityResumed(Activity activity) {
    currentActivity = activity;
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

  @Override
  public void onCreate() {
    application = this;
    super.onCreate();

    context = new ApplicationCompanionContext(this);
    registerActivityLifecycleCallbacks(this);
  }

  public Users users() {
    return context.users();
  }

  public static CompanionApplication get() {
    return application;
  }

  public static CompanionApplication get(Context context) {
    return (CompanionApplication) context.getApplicationContext();
  }
}
