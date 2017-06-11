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

package net.ixitxachitls.companion.ui.fragments;

import android.app.Fragment;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.activities.MainActivity;

/**
 * Base fragment for all our non-dialog fragments
 */
public abstract class CompanionFragment extends Fragment {
  private static final String TAG = "Fragment";

  public enum Type { settings, campaigns, campaign, character, battle, };

  private final Type type;

  CompanionFragment(Type type) {
    this.type = type;
    setRetainInstance(true);
  }

  public Type getType() {
    return type;
  }

  public void toast(String message) {
    ((MainActivity) getActivity()).toast(message);
  }

  protected void show(Type fragment) {
    CompanionFragments.get().show(fragment, Optional.absent());
  }

  @CallSuper
  public void refresh() {
    Log.d(TAG, "refreshing " + getClass().getSimpleName());
  }

  @Override
  public void onResume() {
    super.onResume();

    Log.d(TAG, "resumed fragment " + getClass().getSimpleName());
    CompanionFragments.get().resumed(this);
    refresh();
  }
}
