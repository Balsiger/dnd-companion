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

import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Creature;
import net.ixitxachitls.companion.data.documents.Monster;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.ui.views.DiceView;
import net.ixitxachitls.companion.ui.views.EncounterCharacterTitleView;
import net.ixitxachitls.companion.ui.views.EncounterMonsterTitleView;
import net.ixitxachitls.companion.ui.views.EncounterTitleView;
import net.ixitxachitls.companion.ui.views.UpdatableViewGroup;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;

import java.util.Optional;

/**
 * A fragment to show on ongoing encounter (battle).
 */
public class EncounterFragment extends NestedCompanionFragment {

  private Optional<Battle> encounter = Optional.empty();

  // UI.
  private DiceView initiative;
  private UpdatableViewGroup<LinearLayout, EncounterTitleView<? extends Creature<?>>, String>
      creatures;
  private TextWrapper<TextView> turn;
  private Transition transition = new AutoTransition();

  public EncounterFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    view = (ViewGroup) inflater.inflate(R.layout.fragment_encounter, container, false);
    view.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));

    initiative = view.findViewById(R.id.initiative);
    initiative.setDice(20);

    creatures = new UpdatableViewGroup(view.findViewById(R.id.monsters));

    turn = TextWrapper.wrap(view, R.id.turn);

    return view;
  }

  public void show(Campaign campaign) {
    this.encounter = Optional.of(campaign.getBattle());

    update();
  }

  public void update() {
    if (isHidden()) {
      return;
    }

    if (encounter.isPresent()) {
      // Campaigns.
      if (encounter.get().isStarting()) {
        turn.text("Waiting for initiative...");
      } else if (encounter.get().isSurprised()) {
        turn.text("Surprise round");
      } else {
        turn.text("Turn " + encounter.get().getTurn());
      }

      // Characters.
      encounter.get().update(characters());
      Optional<Character> character =
          encounter.get().firstPlayerCharacterNeedingInitiative();
      if (character.isPresent()) {
        initiative.setLabel("Initiative for " + character.get().getName());
        initiative.setModifier(character.get().getInitiative());
        initiative.setSelectAction(i -> {
          TransitionManager.beginDelayedTransition(view, transition);
          character.get().setEncounterInitiative(encounter.get().getNumber(), i);
        });

        initiative.setVisibility(View.VISIBLE);
        creatures.getView().setVisibility(View.GONE);
      } else {
        initiative.setVisibility(View.GONE);
        creatures.getView().setVisibility(View.VISIBLE);

        String currentCreatureId = encounter.get().getCurrentCreatureId();
        creatures.ensureOnly(encounter.get().getCreatureIds(),
            id -> {
              Optional<? extends Creature<?>> creature =
                  CompanionApplication.get().monsters().getMonsterOrCharacter(id);
              if (creature.isPresent() && creature.get() instanceof Character) {
                return new EncounterCharacterTitleView(getContext());
              } else if (creature.isPresent() && creature.get() instanceof Monster) {
                return new EncounterMonsterTitleView(getContext());
              } else {
                return null;
              }
            });
        creatures.update(encounter.get().getCreatureIds(),
            (id, view) -> {
              Optional<? extends Creature<?>> creature =
                  CompanionApplication.get().monsters().getMonsterOrCharacter(id);
              if (creature.isPresent()) {
                // This weird cast is necessary because the type of creature we get here
                // is not guaranteed to match the type of title view; it should match in
                // practice, though.
                ((EncounterTitleView) view).update(encounter.get(), creature.get());
                view.update(creature.get().getAdjustedConditions());
                view.showSelected(id.equals(currentCreatureId));
                creatures.getView().setVisibility(View.VISIBLE);
              }
            });
      }

      // Monsters.
      encounter.get().update(monsters());

      // Conditions.
      encounter.get().update(conditions());
    }

    // Messages.
    creatures.simpleUpdate(v -> {
      if (v instanceof EncounterCharacterTitleView) {
        ((EncounterCharacterTitleView) v).update(messages());
      }
    });

    // Images.
    creatures.simpleUpdate(v -> v.update(images()));

    // Conditions.
    creatures.simpleUpdate(v -> {
      if (v.getCreature().isPresent()) {
        v.update(v.getCreature().get().getAdjustedConditions());
      }
    });
  }
}