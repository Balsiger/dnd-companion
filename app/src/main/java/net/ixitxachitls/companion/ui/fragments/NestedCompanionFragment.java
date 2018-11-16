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
import android.view.View;
import android.view.ViewGroup;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.documents.Campaigns;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.CreatureConditions;
import net.ixitxachitls.companion.data.documents.Images;
import net.ixitxachitls.companion.data.documents.Messages;
import net.ixitxachitls.companion.data.documents.Monsters;
import net.ixitxachitls.companion.data.documents.User;

/**
 * A base framgent for all nested fragments.
 */
public class NestedCompanionFragment extends Fragment {

  private final CompanionContext context;

  // UI.
  protected ViewGroup view;

  public NestedCompanionFragment() {
    this.context = CompanionApplication.get().context();
  }

  public Campaigns campaigns() {
    return context.campaigns();
  }

  public CompanionContext context() {
    return context;
  }

  public Characters characters() {
    return context.characters();
  }

  public Monsters monsters() {
    return context.monsters();
  }

  public Images images() {
    return context.images();
  }

  public Messages messages() {
    return context.messages();
  }

  public CreatureConditions conditions() {
    return context.conditions();
  }

  public User me() {
    return context.me();
  }

  public void hide() {
    view.setVisibility(View.GONE);
  }

  public void show() {
    view.setVisibility(View.VISIBLE);
  }
}
