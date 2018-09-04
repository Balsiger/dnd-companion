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

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.ui.dialogs.NumberPrompt;

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

    xp.onBlur(this::changeXp).enabled(true);
    xpAdd.visible().onClick(this::addXp);
    xpSubtract.visible().onClick(this::subtractXp);
    level.onBlur(this::changeLevel).enabled(true);
    hp.onBlur(this::changeHp).enabled(true);
    hpAdd.visible().onClick(this::addHp);
    hpSubtract.visible().onClick(this::subtractHp);
    hpMax.onBlur(this::changeMaxHp).enabled(true);
    damageNonlethal.onBlur(this::changeNonlethalDamage).enabled(true);
    hpNonlethalAdd.visible().onClick(this::addNonlethalDamage);
    hpNonlethalSubtract.visible().onClick(this::subtractNonlethalDamage);

    return view;
  }

  private void editAbilities() {
    if (!character.isPresent()) {
      return;
    }

    AbilitiesDialog.newInstance(character.get().getCharacterId(),
        character.get().getCampaignId()).display();
  }

  private void changeXp() {
    if (character.isPresent() && !xp.getText().isEmpty()) {
      character.get().asLocal().setXp(Integer.parseInt(xp.getText()));
    }
  }

  private void changeLevel() {
    Status.error("level change");
    if (character.isPresent() && !level.getText().isEmpty()) {
      try {
        character.get().asLocal().setLevel(Integer.parseInt(level.getText()));
      } catch (NumberFormatException e) {
        character.get().asLocal().setLevel(1);
      }
    }

    redraw();
  }

  private void changeHp() {
    if (character.isPresent()) {
      try {
        character.get().setHp(Integer.parseInt(hp.getText()));
      } catch (NumberFormatException e) {
        character.get().asLocal().setHp(1);
      }
    }

    redraw();
  }

  private void changeMaxHp() {
    if (character.isPresent()) {
      try {
        character.get().setMaxHp(Integer.parseInt(hpMax.getText()));
      } catch (NumberFormatException e) {
        character.get().asLocal().setMaxHp(1);
      }
    }

    redraw();
  }

  private void changeNonlethalDamage() {
    if (character.isPresent()) {
      try {
        character.get().setNonlethalDamage(Integer.parseInt(damageNonlethal.getText()));
      } catch (NumberFormatException e) {
        character.get().asLocal().setNonlethalDamage(0);
      }
    }

    redraw();
  }

  private void addHp() {
    NumberPrompt prompt = new NumberPrompt(getContext())
        .title("Add hit points")
        .message("The number of hit points to add to your current value.");
    prompt.yes(() -> {
      if (character.isPresent()) {
        character.get().addHp(prompt.getNumber());
        redraw();
      }
    });
    prompt.show();
  }

  private void subtractHp() {
    NumberPrompt prompt = new NumberPrompt(getContext())
        .title("Subtract hit points")
        .message("The number of hit points to subtract from your current value.");
    prompt.yes(() -> {
      if (character.isPresent()) {
        character.get().addHp(-prompt.getNumber());
        redraw();
      }
    });
    prompt.show();
  }

  private void addNonlethalDamage() {
    NumberPrompt prompt = new NumberPrompt(getContext())
        .title("Add Non Lethal Damaga")
        .message("The number of hit points to add to your current value of non lethal damage.");
    prompt.yes(() -> {
      if (character.isPresent()) {
        character.get().addNonlethalDamage(prompt.getNumber());
        redraw();
      }
    });
    prompt.show();
  }

  private void subtractNonlethalDamage() {
    NumberPrompt prompt = new NumberPrompt(getContext())
        .title("Subtract Non Lethal Damage")
        .message("The number of hit points to subtract from your current value of non lethal "
            + "damage.");
    prompt.yes(() -> {
      if (character.isPresent()) {
        character.get().addNonlethalDamage(-prompt.getNumber());
        redraw();
      }
    });
    prompt.show();
  }

  private void addXp() {
    NumberPrompt prompt = new NumberPrompt(getContext())
        .title("Add XP")
        .message("The number of XPs to add to your current value.");
    prompt.yes(() -> {
      if (character.isPresent()) {
        character.get().addXp(prompt.getNumber());
        redraw();
      }
    });
    prompt.show();
  }

  private void subtractXp() {
    NumberPrompt prompt = new NumberPrompt(getContext())
        .title("Subtract XP")
        .message("The number of Xps to subtract from your current value.");
    prompt.yes(() -> {
      if (character.isPresent()) {
        character.get().addXp(-prompt.getNumber());
        redraw();
      }
    });
    prompt.show();
  }
}
