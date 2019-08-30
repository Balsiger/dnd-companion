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

package net.ixitxachitls.companion.ui.activities;

import android.transition.ChangeBounds;
import android.transition.Fade;
import android.view.View;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Monster;
import net.ixitxachitls.companion.ui.dialogs.Dialog;
import net.ixitxachitls.companion.ui.fragments.CampaignFragment;
import net.ixitxachitls.companion.ui.fragments.CampaignsFragment;
import net.ixitxachitls.companion.ui.fragments.CharacterFragment;
import net.ixitxachitls.companion.ui.fragments.CompanionFragment;
import net.ixitxachitls.companion.ui.fragments.LocalCharacterFragment;
import net.ixitxachitls.companion.ui.fragments.MiniaturesFragment;
import net.ixitxachitls.companion.ui.fragments.MonsterFragment;
import net.ixitxachitls.companion.ui.fragments.UserFragment;

import java.util.Optional;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * Manager for companion fragments.
 */
public class CompanionFragments {

  private static final String TRANSITION_NAME = "sharedMove";
  private static int EXIT_FADE_DURATION_MS = 100;
  private static int ENTER_FADE_DURATION_MS = 100;
  private static int MOVE_DURATION_MS = 200;

  private static CompanionFragments singleton;

  private final CompanionContext context;
  private FragmentManager fragmentManager;

  private Optional<CompanionFragment> currentFragment = Optional.empty();
  private Optional<CampaignFragment> campaignFragment = Optional.empty();
  private Optional<CampaignsFragment> campaignsFragment = Optional.empty();
  private Optional<UserFragment> settingsFragment = Optional.empty();
  private Optional<CharacterFragment> characterFragment = Optional.empty();
  private Optional<LocalCharacterFragment> localCharacterFragment = Optional.empty();
  private Optional<MiniaturesFragment> miniaturesFragment = Optional.empty();
  private Optional<MonsterFragment> monsterFragment = Optional.empty();

  private CompanionFragments(CompanionContext context, FragmentManager fragmentManager) {
    this.context = context;
    this.fragmentManager = fragmentManager;
  }

  public Optional<CompanionFragment> getCurrentFragment() {
    return currentFragment;
  }

  public FragmentManager getFragmentManager() {
    return fragmentManager;
  }

  public void display(Dialog dialog) {
    fragmentManager.beginTransaction().add(dialog, null).commit();
    fragmentManager.executePendingTransactions();
  }

  public boolean goBack() {
    if (!currentFragment.isPresent()) {
      return false;
    }

    return currentFragment.get().goBack();
  }

  public void resumed(CompanionFragment fragment) {
    currentFragment = Optional.of(fragment);
  }

  public void show() {
    if (currentFragment.isPresent()) {
      show(currentFragment.get(), Optional.empty());
    } else {
      // Show the default campaign to be able to come back to it.
      show(CompanionFragment.Type.campaigns, Optional.empty());
    }
  }

  public CompanionFragment show(CompanionFragment.Type fragment, Optional<View> sharedElement) {
    CompanionApplication.get().logEvent(fragment.toString(), fragment.name(), "view");
    switch (fragment) {
      case settings:
        if (!settingsFragment.isPresent()) {
          settingsFragment = Optional.of(new UserFragment());
        }
        return show(settingsFragment.get(), sharedElement);

      case character:
        if (!characterFragment.isPresent()) {
          characterFragment = Optional.of(new CharacterFragment());
        }
        return show(characterFragment.get(), sharedElement);

      case localCharacter:
        if (!localCharacterFragment.isPresent()) {
          localCharacterFragment = Optional.of(new LocalCharacterFragment());
        }
        return show(localCharacterFragment.get(), sharedElement);

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

      case miniatures:
        if (!miniaturesFragment.isPresent()) {
          miniaturesFragment = Optional.of(new MiniaturesFragment());
        }
        return show(miniaturesFragment.get(), sharedElement);

      case monster:
        if (!monsterFragment.isPresent()) {
          monsterFragment = Optional.of(new MonsterFragment());
        }
        return show(monsterFragment.get(), sharedElement);
    }
  }

  public void showCampaign(Campaign campaign, Optional<View> shared) {
    show(CompanionFragment.Type.campaign, shared);
    if (campaignFragment.isPresent()) {
      campaignFragment.get().showCampaign(campaign);
    }
  }

  public void showCharacter(Character character, Optional<View> shared) {
    if (character.amPlayer()) {
      show(CompanionFragment.Type.localCharacter, shared);
      if (localCharacterFragment.isPresent()) {
        localCharacterFragment.get().showCharacter(character);
      }
    } else {
      show(CompanionFragment.Type.character, shared);
      if (characterFragment.isPresent()) {
        characterFragment.get().showCharacter(character);
      }
    }
  }

  public void showMonster(Monster monster, Optional<View> shared) {
    show(CompanionFragment.Type.monster, shared);
    monsterFragment.get().showMonster(monster);
  }

  private Fade fade(int duration, int delay) {
    Fade fade = new Fade();
    fade.setStartDelay(delay);
    fade.setDuration(duration);
    return fade;
  }

  private CompanionFragment show(CompanionFragment fragment,
                                 Optional<View> sharedTransitionElement) {
    Status.log("showing fragment " + fragment.getClass().getSimpleName());
    if (fragment == currentFragment.orElse(null)) {
      return fragment;
    }

    // Shared element transition.
    FragmentTransaction transaction = fragmentManager.beginTransaction();
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

  public static CompanionFragments get() {
    return singleton;
  }

  public static void init(CompanionContext context, FragmentManager fragmentManager) {
    if (singleton == null) {
      singleton = new CompanionFragments(context, fragmentManager);
    } else {
      singleton.fragmentManager = fragmentManager;
    }
  }
}
