/*
 * Copyright (c) 2017-2018 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
    FragmentManager manager = getChildFragmentManager();
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
    FragmentManager manager = getChildFragmentManager();
    manager.beginTransaction()
        .show(this)
        .commit();
  }
}
