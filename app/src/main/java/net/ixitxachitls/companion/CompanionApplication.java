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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;

import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.documents.Campaigns;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.CreatureConditions;
import net.ixitxachitls.companion.data.documents.Images;
import net.ixitxachitls.companion.data.documents.Messages;
import net.ixitxachitls.companion.data.documents.Monsters;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.data.dynamics.Histories;
import net.ixitxachitls.companion.data.values.Encounters;
import net.ixitxachitls.companion.net.CompanionMessenger;
import net.ixitxachitls.companion.net.nsd.ApplicationNsdAccessor;
import net.ixitxachitls.companion.net.nsd.NsdAccessor;
import net.ixitxachitls.companion.storage.ApplicationAssetAccessor;
import net.ixitxachitls.companion.storage.ApplicationDataBaseAccessor;
import net.ixitxachitls.companion.storage.AssetAccessor;
import net.ixitxachitls.companion.storage.DataBaseAccessor;

/**
 * The main application for the companion.
 */
public class CompanionApplication extends MultiDexApplication
    implements Application.ActivityLifecycleCallbacks {

  private static CompanionApplication application;

  private final DataBaseAccessor dataBaseAccessor;
  private final AssetAccessor assetAccessor;

  private NsdAccessor nsdAccessor;
  private ApplicationCompanionContext context;
  private Activity currentActivity;

  public CompanionApplication() {
    this.dataBaseAccessor = new ApplicationDataBaseAccessor(this);
    this.assetAccessor = new ApplicationAssetAccessor(this);
  }

  @Override
  public void onCreate() {
    application = this;

    try {
      PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      Status.error("starting roleplay companion version " + packageInfo.versionName + " #"
          + packageInfo.versionCode);
    } catch (PackageManager.NameNotFoundException e) {
      Status.warning("starting roleplay companion with unknown version");
    }
    super.onCreate();

    nsdAccessor = new ApplicationNsdAccessor(this);

    Entries.init(this.getAssetAccessor());

    context = new ApplicationCompanionContext(this.getDataBaseAccessor(), this.getNsdAccessor(),
        this.getAssetAccessor(), this);
    registerActivityLifecycleCallbacks(this);
  }

  public void loggedIn() {
    // The messenger needs other entries, thus cannot be created earlier.
    context.messenger().start(); // Stopping is done in MainActivity.exit();
  }

  public static CompanionApplication get() {
    return application;
  }

  public static CompanionApplication get(Context context) {
    return (CompanionApplication) context.getApplicationContext();
  }

  public Activity getCurrentActivity() {
    return currentActivity;
  }

  // Companion Context accessors.
  public DataBaseAccessor getDataBaseAccessor() {
    return dataBaseAccessor;
  }

  public AssetAccessor getAssetAccessor() {
    return assetAccessor;
  }

  public NsdAccessor getNsdAccessor() {
    return nsdAccessor;
  }

  public CompanionContext context() {
    return context;
  }

  public User me() {
    return context.me();
  }

  public Campaigns campaigns() {
    return context.campaigns();
  }

  public Encounters encounters() {
    return context.encounters();
  }

  public Characters characters() {
    return context.characters();
  }

  public CreatureConditions conditions() {
    return context.conditions();
  }

  public Images images() {
    return context.images();
  }

  public Messages messages() {
    return context.messages();
  }

  public Monsters monsters() {
    return context.monsters();
  }

  public CompanionMessenger messenger() {
    return context.messenger();
  }

  public Histories histories() {
    return context.histories();
  }

  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    currentActivity = activity;
  }

  @Override
  public void onActivityStarted(Activity activity) {
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
}
