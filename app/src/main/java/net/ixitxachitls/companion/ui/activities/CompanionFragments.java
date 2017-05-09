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

package net.ixitxachitls.companion.ui.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.ui.fragments.BattleFragment;
import net.ixitxachitls.companion.ui.fragments.CampaignFragment;
import net.ixitxachitls.companion.ui.fragments.CampaignsFragment;
import net.ixitxachitls.companion.ui.fragments.CharacterFragment;
import net.ixitxachitls.companion.ui.fragments.CompanionFragment;
import net.ixitxachitls.companion.ui.fragments.SettingsFragment;

/**
 * Manager for companion fragments.
 */
public class CompanionFragments {
  private static final String TAG = "Fragment";
  private static CompanionFragments singleton;

  private final FragmentManager fragmentManager;
  private Optional<CompanionFragment> currentFragment = Optional.absent();
  private Optional<CampaignFragment> campaignFragment = Optional.absent();
  private Optional<CampaignsFragment> campaignsFragment = Optional.absent();
  private Optional<SettingsFragment> settingsFragment = Optional.absent();
  private Optional<CharacterFragment> characterFragment = Optional.absent();
  private Optional<BattleFragment> battleFragment = Optional.absent();

  private CompanionFragments(FragmentManager fragmentManager) {
    this.fragmentManager = fragmentManager;
  }

  public void resumed(CompanionFragment fragment) {
    currentFragment = Optional.of(fragment);
  }

  public static void init(FragmentManager fragmentManager) {
    if (singleton == null) {
      singleton = new CompanionFragments(fragmentManager);
    }
  }

  public static CompanionFragments get() {
    return singleton;
  }

  public void show() {
    if (currentFragment.isPresent()) {
      show(currentFragment.get());
    } else {
      // Show the default campaign to be able to come back to it.
      show(CompanionFragment.Type.campaigns);

      // Show settings if not yet defined
      if (!Settings.get().isDefined()) {
        show(CompanionFragment.Type.settings);
      }
    }
  }

  public CompanionFragment show(CompanionFragment.Type fragment) {
    switch(fragment) {
      case settings:
        if (!settingsFragment.isPresent()) {
          settingsFragment = Optional.of(new SettingsFragment());
        }
        return show(settingsFragment.get());

      case character:
        if (!characterFragment.isPresent()) {
          characterFragment = Optional.of(new CharacterFragment());
        }
        return show(characterFragment.get());

      default:
      case campaigns:
        if (!campaignsFragment.isPresent()) {
          campaignsFragment = Optional.of(new CampaignsFragment());
        }
        return show(campaignsFragment.get());

      case campaign:
        if (!campaignFragment.isPresent()) {
          campaignFragment = Optional.of(new CampaignFragment());
        }
        return show(campaignFragment.get());

      case battle:
        if (!battleFragment.isPresent()) {
          battleFragment = Optional.of(new BattleFragment());
        }
        return show(battleFragment.get());
    }
  }

  public CompanionFragment show(CompanionFragment fragment) {
    Log.d(TAG, "showing fragment " + fragment.getClass().getSimpleName());
    if (fragment == currentFragment.orNull()) {
      fragment.refresh();
      return fragment;
    }

    FragmentTransaction transaction = fragmentManager.beginTransaction();
    transaction.addToBackStack(fragment.getClass().getSimpleName());
    transaction.replace(R.id.content, fragment).commit();
    fragmentManager.executePendingTransactions();
    fragment.refresh();
    return fragment;
  }

  public void show(String typeName) {
    if (Strings.isNullOrEmpty(typeName)) {
      show();
    } else {
      show(CompanionFragment.Type.valueOf(typeName));
    }
  }

  public void showLast() {
    Log.d(TAG, "showing last fragment");
    fragmentManager.popBackStackImmediate();
    refresh();
  }

  public void showCampaign(Campaign campaign) {
    show(CompanionFragment.Type.campaign);
    if (campaignFragment.isPresent()) {
      campaignFragment.get().showCampaign(campaign);
    }
  }

  public void showCharacter(Character character) {
    show(CompanionFragment.Type.character);
    if (characterFragment.isPresent()) {
      characterFragment.get().showCharacter(character);
    }
  }

  public void showBattle(Campaign campaign) {
    show(CompanionFragment.Type.battle);
    if (battleFragment.isPresent()) {
      battleFragment.get().forCampaign(campaign);
    }
  }

  public void showBattle(Character character) {
    show(CompanionFragment.Type.battle);
    if (battleFragment.isPresent()) {
      battleFragment.get().forCharacter(character);
    }
  }

  public void refresh() {
    if (currentFragment.isPresent()) {
      Log.d(TAG, "refresh fragment " + currentFragment.get().getClass().getSimpleName());
      currentFragment.get().refresh();
    }
  }

}
