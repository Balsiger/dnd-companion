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

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.util.Log;
import android.view.View;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.ui.dialogs.Dialog;
import net.ixitxachitls.companion.ui.fragments.CampaignFragment;
import net.ixitxachitls.companion.ui.fragments.CampaignsFragment;
import net.ixitxachitls.companion.ui.fragments.CharacterFragment;
import net.ixitxachitls.companion.ui.fragments.CompanionFragment;
import net.ixitxachitls.companion.ui.fragments.SettingsFragment;

/**
 * Manager for companion fragments.
 */
public class CompanionFragments {
  private static final String TAG = "Fragments";

  private static final String TRANSITION_NAME = "sharedMove";
  private static int EXIT_FADE_DURATION_MS = 100;
  private static int ENTER_FADE_DURATION_MS = 100;
  private static int MOVE_DURATION_MS = 200;

  private static CompanionFragments singleton;

  private FragmentManager fragmentManager;
  private Optional<CompanionFragment> currentFragment = Optional.absent();
  private Optional<CampaignFragment> campaignFragment = Optional.absent();
  private Optional<CampaignsFragment> campaignsFragment = Optional.absent();
  private Optional<SettingsFragment> settingsFragment = Optional.absent();
  private Optional<CharacterFragment> characterFragment = Optional.absent();

  private CompanionFragments(FragmentManager fragmentManager) {
    this.fragmentManager = fragmentManager;
  }

  public void resumed(CompanionFragment fragment) {
    currentFragment = Optional.of(fragment);
  }

  public static void init(FragmentManager fragmentManager) {
    if (singleton == null) {
      singleton = new CompanionFragments(fragmentManager);
    } else {
      singleton.fragmentManager = fragmentManager;
      singleton.currentFragment = Optional.absent();
    }
  }

  public static CompanionFragments get() {
    return singleton;
  }

  public FragmentManager getFragmentManager() {
    return fragmentManager;
  }

  public void show() {
    if (currentFragment.isPresent()) {
      show(currentFragment.get(), Optional.absent());
    } else {
      // Show the default campaign to be able to come back to it.
      show(CompanionFragment.Type.campaigns, Optional.absent());

      // Show settings if not yet defined
      if (!Settings.get().isDefined()) {
        show(CompanionFragment.Type.settings, Optional.absent());
      }
    }
  }

  public CompanionFragment show(CompanionFragment.Type fragment, Optional<View> sharedElement) {
    switch(fragment) {
      case settings:
        if (!settingsFragment.isPresent()) {
          settingsFragment = Optional.of(new SettingsFragment());
        }
        return show(settingsFragment.get(), sharedElement);

      case character:
        if (!characterFragment.isPresent()) {
          characterFragment = Optional.of(new CharacterFragment());
        }
        return show(characterFragment.get(), sharedElement);

      default:
      case campaigns:
        if (!campaignsFragment.isPresent()) {
          campaignsFragment = Optional.of(new CampaignsFragment());
        }
        return show(campaignsFragment.get(), sharedElement);

      case campaign:
        if (!campaignFragment.isPresent()) {
          campaignFragment = Optional.of(new CampaignFragment());
        }

        return show(campaignFragment.get(), sharedElement);
    }
  }

  private CompanionFragment show(CompanionFragment fragment,
                                 Optional<View> sharedTransitionElement) {
    Log.d(TAG, "showing fragment " + fragment.getClass().getSimpleName());
    if (fragment == currentFragment.orNull()) {
      return fragment;
    }

    FragmentTransaction transaction = fragmentManager.beginTransaction();
    Log.d(TAG, "Adding to backstack " + fragment.getClass().getSimpleName());

    // Shared element transition.
    if (sharedTransitionElement.isPresent()) {
      currentFragment.get().setExitTransition(fade(ENTER_FADE_DURATION_MS, 0));
      currentFragment.get().setReenterTransition(fade(ENTER_FADE_DURATION_MS,
          EXIT_FADE_DURATION_MS + MOVE_DURATION_MS));

      fragment.setSharedElementEnterTransition(new ChangeBounds());
      sharedTransitionElement.get().setTransitionName(TRANSITION_NAME);
      fragment.setSharedElementReturnTransition(new ChangeBounds());

      fragment.setEnterTransition(fade(ENTER_FADE_DURATION_MS,
          EXIT_FADE_DURATION_MS + MOVE_DURATION_MS));
      fragment.setReturnTransition(fade(ENTER_FADE_DURATION_MS, 0));

      transaction.addSharedElement(sharedTransitionElement.get(), TRANSITION_NAME);
    }

    transaction.replace(R.id.content, fragment).commitAllowingStateLoss();
    fragmentManager.executePendingTransactions();
    return fragment;
  }

  public void show(String typeName) {
    if (Strings.isNullOrEmpty(typeName)) {
      show();
    } else {
      show(CompanionFragment.Type.valueOf(typeName), Optional.absent());
    }
  }

  public boolean goBack() {
    if (!currentFragment.isPresent()) {
      return false;
    }

    return currentFragment.get().goBack();
  }

  public void showCampaign(Campaign campaign, Optional<View> shared) {
    show(CompanionFragment.Type.campaign, shared);
    if (campaignFragment.isPresent()) {
      Campaigns.changeCurrent(campaign.getCampaignId());
      campaignFragment.get().showCampaign(campaign.getCampaignId());
    }
  }

  public void showCharacter(Character character, Optional<View> shared) {
    show(CompanionFragment.Type.character, shared);
    if (characterFragment.isPresent()) {
      characterFragment.get().showCharacter(character);
    }
  }

  private Fade fade(int duration, int delay) {
    Fade fade = new Fade();
    fade.setStartDelay(delay);
    fade.setDuration(duration);
    return fade;
  }

  public void display(Dialog dialog) {
    fragmentManager.beginTransaction().add(dialog, null).commit();
    fragmentManager.executePendingTransactions();
  }
}
