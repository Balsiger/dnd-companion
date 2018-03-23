/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Tabletop Companion.
 *
 * The Tabletop Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Tabletop Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Tabletop Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;

import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Creatures;
import net.ixitxachitls.companion.data.dynamics.Images;
import net.ixitxachitls.companion.net.CompanionMessenger;

/**
 * The main application for the companion.
 */
public class CompanionApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

  private CompanionMessenger messenger;
  private Activity currentActivity;

  @Override
  public void onCreate() {
    try {
      PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      Status.error("starting rpg companion version " + packageInfo.versionName + " #" + packageInfo.versionCode);
    } catch (PackageManager.NameNotFoundException e) {
      Status.warning("starting rpg companion with unknown version");
    }
    super.onCreate();

    Entries.init(this);
    Settings.init(this);

    Campaigns.load(this);
    Images.load(this);
    Characters.load(this);
    Creatures.load(this);

    messenger = CompanionMessenger.init(this);
    messenger.start(); // Stopping is done in MainActivity.exit();
    Campaigns.publish();
    Characters.publish();

    registerActivityLifecycleCallbacks(this);
  }

  public Activity getCurrentActivity() {
    return currentActivity;
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
