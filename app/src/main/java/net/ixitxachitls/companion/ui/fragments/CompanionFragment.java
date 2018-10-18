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

package net.ixitxachitls.companion.ui.fragments;

import android.support.v4.app.Fragment;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Campaigns;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.Images;
import net.ixitxachitls.companion.data.documents.Monsters;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;

import java.util.Optional;

/**
 * Base fragment for all our non-dialog fragments
 */
public abstract class CompanionFragment extends Fragment {

  public enum Type {settings, campaigns, campaign, character, localCharacter};

  private final Type type;

  CompanionFragment(Type type) {
    this.type = type;
    setRetainInstance(true);
  }

  public Type getType() {
    return type;
  }

  public void toast(String message) {
    Status.toast(message);
  }

  protected void show(Type fragment) {
    CompanionFragments.get().show(fragment, Optional.empty());
  }

  public abstract boolean goBack();

  protected CompanionApplication application() {
    return CompanionApplication.get(getContext());
  }

  protected User me() {
    return application().me();
  }

  protected Campaigns campaigns() {
    return application().campaigns();
  }

  protected Characters characters() {
    return application().characters();
  }

  protected Images images() {
    return application().images();
  }

  protected Monsters creatures() {
    return application().creatures();
  }

  @Override
  public void onResume() {
    super.onResume();

    Status.log("resumed fragment " + getClass().getSimpleName());
    CompanionFragments.get().resumed(this);
  }
}
