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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.rules.XP;
import net.ixitxachitls.companion.ui.views.AbilityView;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

/**
 * Fragment for a character's base statistics
 */
public class CharacterStatisticsFragment extends Fragment {

  protected Optional<Character> character = Optional.empty();

  // UI elements.
  protected AbilityView strength;
  protected AbilityView dexterity;
  protected AbilityView constitution;
  protected AbilityView intelligence;
  protected AbilityView wisdom;
  protected AbilityView charisma;
  protected LabelledEditTextView xp;
  protected Wrapper<ImageView> xpAdd;
  protected Wrapper<ImageView> xpSubtract;
  protected TextWrapper<TextView> xpNext;
  protected LabelledEditTextView level;
  protected LabelledEditTextView hp;
  protected Wrapper<ImageView> hpAdd;
  protected Wrapper<ImageView> hpSubtract;
  protected LabelledEditTextView hpMax;
  protected LabelledEditTextView damageNonlethal;
  protected Wrapper<ImageView> hpNonlethalAdd;
  protected Wrapper<ImageView> hpNonlethalSubtract;

  public CharacterStatisticsFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
    super.onCreateView(inflater, container, state);

    ViewGroup view =
        (ViewGroup) inflater.inflate(R.layout.fragment_character_statistics, container, false);

    strength = view.findViewById(R.id.strength);
    dexterity = view.findViewById(R.id.dexterity);
    constitution = view.findViewById(R.id.constitution);
    intelligence = view.findViewById(R.id.intelligence);
    wisdom = view.findViewById(R.id.wisdom);
    charisma = view.findViewById(R.id.charisma);

    xp = view.findViewById(R.id.xp);
    xp.enabled(false);
    xpAdd = Wrapper.<ImageView>wrap(view, R.id.xp_add).gone();
    xpSubtract = Wrapper.<ImageView>wrap(view, R.id.xp_subtract).gone();
    xpNext = TextWrapper.wrap(view, R.id.xp_next);
    level = view.findViewById(R.id.level);
    level.enabled(false);

    hp = view.findViewById(R.id.hp);
    hp.enabled(false);
    hpAdd = Wrapper.<ImageView>wrap(view, R.id.hp_add).gone();
    hpSubtract = Wrapper.<ImageView>wrap(view, R.id.hp_subtract).gone();
    hpMax = view.findViewById(R.id.hp_max);
    hpMax.enabled(false);
    damageNonlethal = view.findViewById(R.id.hp_nonlethal);
    damageNonlethal.enabled(false);
    hpNonlethalAdd = Wrapper.<ImageView>wrap(view, R.id.hp_nonlethal_add).gone();
    hpNonlethalSubtract = Wrapper.<ImageView>wrap(view, R.id.hp_nonlethal_subtract).gone();

    update(character);
    return view;
  }

  public void update(Optional<Character> character) {
    this.character = character;

    if (!character.isPresent() || strength == null) {
      return;
    }

    strength.setValue(character.get().getStrength(),
        Ability.modifier(character.get().getStrength()));
    dexterity.setValue(character.get().getDexterity(),
        Ability.modifier(character.get().getDexterity()));
    constitution.setValue(character.get().getConstitution(),
        Ability.modifier(character.get().getConstitution()));
    intelligence.setValue(character.get().getIntelligence(),
        Ability.modifier(character.get().getIntelligence()));
    wisdom.setValue(character.get().getWisdom(),
        Ability.modifier(character.get().getWisdom()));
    charisma.setValue(character.get().getCharisma(),
        Ability.modifier(character.get().getCharisma()));

    xp.text(String.valueOf(character.get().getXp()));
    level.text(String.valueOf(character.get().getLevel()));
    hp.text(String.valueOf(character.get().getHp()));
    hpMax.text(String.valueOf(character.get().getMaxHp()));
    damageNonlethal.text(String.valueOf(character.get().getNonlethalDamage()));

    redraw();
  }

  protected void redraw() {
    if (character.isPresent()) {
      xpNext.text("(next level " + XP.xpForLevel(character.get().getLevel() + 1) + ")");
      hp.text(String.valueOf(character.get().getHp()));
    }
  }
}
