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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.dialogs.LevelsDialog;
import net.ixitxachitls.companion.ui.dialogs.NumberAdjustDialog;

/**
 * Local variant of the character statistics fragment.
 */
public class LocalCharacterStatisticsFragment extends CharacterStatisticsFragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
    View view = super.onCreateView(inflater, container, state);

    strength.setAction(this::editAbilities);
    dexterity.setAction(this::editAbilities);
    constitution.setAction(this::editAbilities);
    intelligence.setAction(this::editAbilities);
    wisdom.setAction(this::editAbilities);
    charisma.setAction(this::editAbilities);

    levels.onClick(this::editLevels);

    xp.onBlur(this::changeXp).enabled(true);
    xpAdjust.visible().onClick(this::adjustXp);
    hp.onBlur(this::changeHp).enabled(true);
    hpAdjust.visible().onClick(this::adjustHp);
    damageNonlethal.onBlur(this::changeNonlethalDamage).enabled(true);
    hpNonlethalAdjust.visible().onClick(this::adjustNonlethalDamage);

    return view;
  }

  private void adjustHp() {
    NumberAdjustDialog.newInstance(R.string.title_dialog_adjust_hp, R.color.character,
        "HP Adjustment", "Adjust the current HP value.")
        .setAdjustAction(this::doAdjustHp)
        .display();
  }

  private void adjustNonlethalDamage() {
    NumberAdjustDialog.newInstance(R.string.title_dialog_adjust_hp, R.color.character,
        "Nonlethal Damage Adjustment", "Adjust the current nonlethal damage.")
        .setAdjustAction(this::doAdjustNonlethalDamage)
        .display();
  }

  private void adjustXp() {
    NumberAdjustDialog.newInstance(R.string.title_dialog_adjust_xp, R.color.character,
        "XP Adjustment", "Adjust the current XP value.")
        .setAdjustAction(this::doAdjustXp)
        .display();
  }

  private void changeHp() {
    if (character.isPresent()) {
      try {
        character.get().setHp(Integer.parseInt(hp.getText()));
      } catch (NumberFormatException e) {
        character.get().setHp(1);
      }
      character.get().store();
    }

    redraw();
  }

  private void changeNonlethalDamage() {
    if (character.isPresent()) {
      try {
        character.get().setNonlethalDamage(Integer.parseInt(damageNonlethal.getText()));
      } catch (NumberFormatException e) {
        character.get().setNonlethalDamage(0);
      }
      character.get().store();
    }

    redraw();
  }

  private void changeXp() {
    if (character.isPresent() && !xp.getText().isEmpty()) {
      character.get().setXp(Integer.parseInt(xp.getText()));
      character.get().store();
    }
  }

  private void doAdjustHp(int value) {
    if (character.isPresent()) {
      character.get().addHp(value);
      character.get().store();
      redraw();
    }
  }

  private void doAdjustNonlethalDamage(int value) {
    if (character.isPresent()) {
      character.get().addNonlethalDamage(value);
      character.get().store();
      redraw();
    }
  }

  private void doAdjustXp(int value) {
    if (character.isPresent()) {
      character.get().addXp(value);
      character.get().store();
      redraw();
    }
  }

  private void editAbilities() {
    if (!character.isPresent()) {
      return;
    }

    AbilitiesDialog.newInstance(character.get().getId(),
        character.get().getCampaignId()).display();
  }

  private void editLevels() {
    if (character.isPresent()) {
      LevelsDialog.newInstance(character.get().getId()).display();
    }
  }
}
