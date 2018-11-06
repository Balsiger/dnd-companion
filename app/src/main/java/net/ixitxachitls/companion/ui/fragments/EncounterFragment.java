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
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Campaigns;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.Creature;
import net.ixitxachitls.companion.data.documents.CreatureConditions;
import net.ixitxachitls.companion.data.documents.Images;
import net.ixitxachitls.companion.data.documents.Messages;
import net.ixitxachitls.companion.data.documents.Monster;
import net.ixitxachitls.companion.data.documents.Monsters;
import net.ixitxachitls.companion.ui.views.DiceView;
import net.ixitxachitls.companion.ui.views.EncounterCharacterTitleView;
import net.ixitxachitls.companion.ui.views.EncounterMonsterTitleView;
import net.ixitxachitls.companion.ui.views.EncounterTitleView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A fragment to show on ongoing startEncounter (battle).
 */
public class EncounterFragment extends NestedCompanionFragment {

  private Optional<Campaign> campaign = Optional.empty();

  // UI.
  private DiceView initiative;
  private LinearLayout creatures;
  private Map<String, EncounterTitleView<?>> creatureViewsById = new HashMap<>();
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

    creatures = view.findViewById(R.id.monsters);

    turn = TextWrapper.wrap(view, R.id.turn);

    characters().observe(this, this::update);
    images().observe(this, this::update);
    conditions().observe(this, this::update);
    monsters().observe(this, this::update);
    messages().observe(this, this::update);

    return view;
  }

  public void show(Campaign campaign) {
    this.campaign = Optional.of(campaign);

    update(characters());
    update(campaigns());
    update(conditions());
  }

  public void update(Campaigns campaigns) {
    if (campaign.isPresent()) {
      if (campaign.get().getEncounter().isStarting()) {
        turn.text("Waiting for initiative...");
      } else if (campaign.get().getEncounter().isSurprised()) {
        turn.text("Surprise round");
      } else {
        turn.text("Turn " + campaign.get().getEncounter().getTurn());
      }

      // TODO(merlin): Check whether we can ensure just doing the update once, not
      update(characters());
    }
  }

  private void update(Characters characters) {
    if (campaign.isPresent()) {
      campaign.get().getEncounter().update(characters);
      Optional<Character> character =
          campaign.get().getEncounter().firstPlayerCharacterNeedingInitiative();
      if (character.isPresent()) {
        initiative.setLabel("Initiative for " + character.get().getName());
        initiative.setModifier(character.get().initiativeModifier());
        initiative.setSelectAction(i -> {
          TransitionManager.beginDelayedTransition(view, transition);
          character.get().setInitiative(campaign.get().getEncounter().getNumber(), i);
        });

        initiative.setVisibility(View.VISIBLE);
        creatures.setVisibility(View.GONE);
      } else {
        initiative.setVisibility(View.GONE);
        creatures.setVisibility(View.VISIBLE);

        Map<String, EncounterTitleView<?>> currentViews = creatureViewsById;
        creatureViewsById = new HashMap<>();
        creatures.removeAllViews();
        int current = campaign.get().getEncounter().getCurrentCreatureIndex();
        int i = 0;
        for (Creature creature : campaign.get().getEncounter().getCreatures()) {
          EncounterTitleView view = currentViews.get(creature.getId());
          if (view == null) {
            if (creature instanceof Character) {
              view = new EncounterCharacterTitleView(getContext());
            } else if (creature instanceof Monster) {
              view = new EncounterMonsterTitleView(getContext());
            }
          }

          creatureViewsById.put(creature.getId(), view);
          view.update(campaign.get(), creature);
          view.update(conditions().getCreatureConditions(creature.getId()));
          creatures.addView(view);
          creatures.setVisibility(View.VISIBLE);
          view.showSelected(i++ == current);
        }
      }
    }
  }

  private void update(Messages messages) {
    update(characters());
  }

  private void update(Monsters monsters) {
    if (campaign.isPresent()) {
      campaign.get().getEncounter().update(monsters);
    }

    update(monsters.getContext().characters());
  }

  private void update(Images images) {
    for (int i = 0; i < creatures.getChildCount(); i++) {
      View view = creatures.getChildAt(i);
      if (view instanceof EncounterCharacterTitleView) {
        ((EncounterTitleView) view).update(images);
      }
    }
  }

  private void update(CreatureConditions conditions) {
    if (campaign.isPresent()) {
      campaign.get().getEncounter().update(conditions);
    }
    for (EncounterTitleView view : creatureViewsById.values()) {
      view.update(conditions().getCreatureConditions(view.getCreatureId()));
    }
  }
}
