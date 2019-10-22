/*
 * Copyright (c) 2017-2018 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.fragments;

import android.util.Log;
import android.view.ViewGroup;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.documents.Adventures;
import net.ixitxachitls.companion.data.documents.Campaigns;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.CreatureConditions;
import net.ixitxachitls.companion.data.documents.Images;
import net.ixitxachitls.companion.data.documents.Messages;
import net.ixitxachitls.companion.data.documents.Monsters;
import net.ixitxachitls.companion.data.documents.User;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * A base fragment for all nested fragments.
 */
public class NestedCompanionFragment extends Fragment {

  private final CompanionContext context;

  // UI.
  protected ViewGroup view;

  public NestedCompanionFragment() {
    this.context = CompanionApplication.get().context();
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

  public void hide() {
    Log.d("Encounter", "hide: " + this);
    FragmentManager manager = getFragmentManager();
    manager.beginTransaction()
        .hide(this)
        .commit();
  }

  public Images images() {
    return context.images();
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

  public void show() {
    Log.d("Encounter", "show: " + this);
    FragmentManager manager = getFragmentManager();
    manager.beginTransaction()
        .show(this)
        .commit();
  }

  public void showAndHide(NestedCompanionFragment toHide) {
    Log.d("Encounter", "showAndHide: " + this + "/" + toHide);
    FragmentManager manager = getFragmentManager();
    manager.beginTransaction()
        .show(this)
        .hide(toHide)
        .commit();
  }
}
