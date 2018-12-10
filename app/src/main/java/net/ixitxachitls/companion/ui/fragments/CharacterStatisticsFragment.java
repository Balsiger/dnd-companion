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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.Level;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.rules.XP;
import net.ixitxachitls.companion.ui.views.AbilityView;
import net.ixitxachitls.companion.ui.views.ConditionIconsView;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledTextView;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

/**
 * Fragment for a character's base statistics
 */
public class CharacterStatisticsFragment extends NestedCompanionFragment {

  protected Optional<Character> character = Optional.empty();

  // UI elements.
  protected ConditionIconsView conditions;
  protected AbilityView strength;
  protected AbilityView dexterity;
  protected AbilityView constitution;
  protected AbilityView intelligence;
  protected AbilityView wisdom;
  protected AbilityView charisma;
  protected LabelledEditTextView xp;
  protected Wrapper<ImageView> xpAdjust;
  protected TextWrapper<TextView> xpNext;
  protected LabelledTextView levels;
  protected LabelledEditTextView hp;
  protected Wrapper<ImageView> hpAdjust;
  protected LabelledTextView hpMax;
  protected LabelledEditTextView damageNonlethal;
  protected Wrapper<ImageView> hpNonlethalAdjust;
  protected TextWrapper<TextView> levelUp;

  public CharacterStatisticsFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
    super.onCreateView(inflater, container, state);

    ViewGroup view =
        (ViewGroup) inflater.inflate(R.layout.fragment_character_statistics, container, false);

    LinearLayout conditionsContainer = view.findViewById(R.id.conditions);
    conditions = new ConditionIconsView(view.getContext(), LinearLayout.HORIZONTAL,
        R.color.character, R.color.characterDark);
    conditionsContainer.addView(conditions);

    strength = view.findViewById(R.id.strength);
    dexterity = view.findViewById(R.id.dexterity);
    constitution = view.findViewById(R.id.constitution);
    intelligence = view.findViewById(R.id.intelligence);
    wisdom = view.findViewById(R.id.wisdom);
    charisma = view.findViewById(R.id.charisma);

    xp = view.findViewById(R.id.xp);
    xp.enabled(false).onBlur(this::redraw);
    xpAdjust = Wrapper.<ImageView>wrap(view, R.id.xp_adjust).gone();
    xpNext = TextWrapper.wrap(view, R.id.xp_next);
    levels = view.findViewById(R.id.levels);
    levelUp = TextWrapper.wrap(view, R.id.level_up);

    hp = view.findViewById(R.id.hp);
    hp.enabled(false);
    hpAdjust = Wrapper.<ImageView>wrap(view, R.id.hp_adjust).gone();
    hpMax = view.findViewById(R.id.hp_max);
    hpMax.enabled(false);
    damageNonlethal = view.findViewById(R.id.hp_nonlethal);
    damageNonlethal.enabled(false);
    hpNonlethalAdjust = Wrapper.<ImageView>wrap(view, R.id.nonlethal_adjust).gone();

    // TODO(merlin): This might be unnecessary?
    if (character.isPresent()) {
      update(character.get());
    }

    characters().observe(this, this::update);

    return view;
  }

  public void update(Character character) {
    this.character = Optional.of(character);

    if (strength == null) {
      return;
    }

    strength.value(Ability.STRENGTH, character.getStrength());
    dexterity.value(Ability.DEXTERITY, character.getDexterity());
    constitution.value(Ability.CONSTITUTION, character.getConstitution());
    intelligence.value(Ability.INTELLIGENCE, character.getIntelligence());
    wisdom.value(Ability.WISDOM, character.getWisdom());
    charisma.value(Ability.CHARISMA, character.getCharisma());

    xp.text(String.valueOf(character.getXp()));
    hp.text(String.valueOf(character.getHp()))
        .validate(new EditTextWrapper.RangeValidator(-20, character.getMaxHp()));
    hpMax.text(String.valueOf(character.getMaxHp()));
    damageNonlethal.text(String.valueOf(character.getNonlethalDamage()));
    conditions.update(character);

    redraw();
  }

  protected void redraw() {
    if (character.isPresent()) {
      xpNext.text("(next level " + XP.xpForLevel(character.get().getLevel() + 1) + ")");
      hp.text(String.valueOf(character.get().getHp()));
      levels.text(Level.summarized(character.get().getLevels()));
      levelUp.visible(character.get().getMaxLevel() > character.get().getLevel()
          || (character.get().getXp() == 0 && character.get().getLevel() == 0));
    }
  }

  private void update(Characters characters) {
    if (character.isPresent()) {
      update(character.get());
    }
  }
}
