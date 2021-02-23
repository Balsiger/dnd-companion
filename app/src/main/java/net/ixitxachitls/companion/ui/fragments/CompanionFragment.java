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

import android.os.AsyncTask;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Campaigns;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.CreatureConditions;
import net.ixitxachitls.companion.data.documents.Images;
import net.ixitxachitls.companion.data.documents.Invites;
import net.ixitxachitls.companion.data.documents.Messages;
import net.ixitxachitls.companion.data.documents.Monsters;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.data.documents.Users;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.activities.MainActivity;
import net.ixitxachitls.companion.ui.views.ActionBarView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

import androidx.annotation.DrawableRes;
import androidx.fragment.app.Fragment;

/**
 * Base fragment for all our non-dialog fragments
 */
public abstract class CompanionFragment extends Fragment implements CompanionApplication.Updatable {

  public enum Type {
    settings, campaigns, campaign, character, miniatures, monster, localCharacter, products,
    loading,
  };

  private final Type type;

  CompanionFragment(Type type) {
    this.type = type;
    setRetainInstance(true);
  }

  public Type getType() {
    return type;
  }

  public abstract boolean goBack();

  @Override
  public void onResume() {
    ((MainActivity) getActivity()).setScreenName(getClass().getSimpleName());

    super.onResume();

    Status.log("resumed fragment " + getClass().getSimpleName());
    CompanionFragments.get().resumed(this);
  }

  public void toast(String message) {
    Status.toast(message);
  }

  @Override
  public void update() {
    Status.error("Updating not implemented for " + this);
  }

  protected ActionBarView.Action addAction(@DrawableRes int drawable, String title,
                                           String description) {
    return ((MainActivity) getActivity()).addAction(drawable, title, description);
  }

  protected ActionBarView.ActionGroup addActionGroup(@DrawableRes int drawable, String title,
                                                     String description) {
    return ((MainActivity) getActivity()).addActionGroup(drawable, title, description);
  }

  protected CompanionApplication application() {
    return CompanionApplication.get(getContext());
  }

  protected Campaigns campaigns() {
    return application().campaigns();
  }

  protected Characters characters() {
    return application().characters();
  }

  protected void clearActions() {
    ((MainActivity) getActivity()).clearActions();
  }

  protected CreatureConditions conditions() {
    return application().conditions();
  }

  protected Monsters creatures() {
    return application().monsters();
  }

  protected Images images() {
    return application().images();
  }

  protected Invites invites() {
    return application().invites();
  }

  protected User me() {
    return application().me();
  }

  protected Messages messages() {
    return application().messages();
  }

  protected Monsters monsters() {
    return application().monsters();
  }

  protected void show(Type fragment) {
    CompanionFragments.get().show(fragment, Optional.empty());
  }

  protected Users users() {
    return application().users();
  }
}
