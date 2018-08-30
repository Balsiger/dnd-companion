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
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Creatures;
import net.ixitxachitls.companion.data.dynamics.Histories;
import net.ixitxachitls.companion.data.dynamics.Images;
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

    // The messenger needs other entries, thus cannot be created earlier.
    context.messenger().start(); // Stopping is done in MainActivity.exit();
    context.campaigns().publish();
    context.characters().publish();

    registerActivityLifecycleCallbacks(this);
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

  public Settings settings() {
    return context.settings();
  }

  public Campaigns campaigns() {
    return context.campaigns();
  }

  public Characters characters() {
    return context.characters();
  }

  public Creatures creatures() {
    return context.creatures();
  }

  public CompanionMessenger messenger() {
    return context.messenger();
  }

  public Images images(boolean local) {
    return context.images(local);
  }

  public Histories histories() {
    return context.histories();
  }

  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
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
