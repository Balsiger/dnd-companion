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

package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Campaigns;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.Creature;
import net.ixitxachitls.companion.data.documents.Images;
import net.ixitxachitls.companion.data.documents.Monster;
import net.ixitxachitls.companion.data.values.Encounter;
import net.ixitxachitls.companion.data.values.Encounters;
import net.ixitxachitls.companion.ui.dialogs.CharacterDialog;
import net.ixitxachitls.companion.ui.dialogs.XPDialog;
import net.ixitxachitls.companion.ui.views.BattleView;
import net.ixitxachitls.companion.ui.views.CharacterChipView;
import net.ixitxachitls.companion.ui.views.ChipView;
import net.ixitxachitls.companion.ui.views.ConditionCreatureView;
import net.ixitxachitls.companion.ui.views.CreatureChipView;
import net.ixitxachitls.companion.ui.views.DiceView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A fragment displaying the complete party of the current campaign.
 */
public class PartyFragment extends Fragment {

  // External data.
  private Campaigns campaigns;
  private Characters characters;
  private Encounters encounters;
  private Images images;
  private Optional<Campaign> campaign = Optional.empty();
  private Optional<Encounter> battle;

  // UI.
  private ViewGroup view;
  private Wrapper<View> scroll;
  private LinearLayout party;
  private TextWrapper<TextView> title;
  private Wrapper<FloatingActionButton> startEncounter;
  private BattleView battleView;
  private Wrapper<FloatingActionButton> addCharacter;
  private Wrapper<FloatingActionButton> xp;
  private DiceView initiative;
  private LinearLayout conditions;
  private Transition transition = new AutoTransition();

  // State.
  private Map<String, CreatureChipView> chipsById = new ConcurrentHashMap<>();
  private Map<String, Character> charactersNeedingInitiative = new HashMap<>();

  public PartyFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    campaigns = CompanionApplication.get().campaigns();
    characters = CompanionApplication.get().characters();
    encounters = CompanionApplication.get().encounters();
    images = CompanionApplication.get().images();
    characters.observe(this, this::update);
    images.observe(this, this::update);

    view = (ViewGroup) inflater.inflate(R.layout.fragment_party, container, false);
    view.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));

    title = TextWrapper.wrap(view, R.id.title);
    party = view.findViewById(R.id.party);
    scroll = Wrapper.wrap(view, R.id.scroll);
    startEncounter = Wrapper.wrap(view, R.id.start_encounter);
    startEncounter.onClick(this::startEncounter)
        .description("Start Encounter", "Start an encounter with the party. "
            + "Monsters can be added once the encounter has started.");
    battleView = view.findViewById(R.id.battle);
    battleView.setVisibility(View.GONE);

    addCharacter = Wrapper.<FloatingActionButton>wrap(view, R.id.add_character)
        .onClick(this::createCharacter)
        .description("Add Character", "Add a new character to the party. The character will "
            + "automatically be shared with other players.");
    xp = Wrapper.<FloatingActionButton>wrap(view, R.id.xp)
        .onClick(this::chooseXp)
        .description("XP", "Award experience points to one ore multiple characters in the party.");
    initiative = view.findViewById(R.id.initiative);
    initiative.setDice(20);
    conditions = view.findViewById(R.id.conditions);
    transition.excludeChildren(conditions, true);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

    update(characters);
  }

  /*
  private void updateCampaignId(String campaignId) {
    Status.log("updating campaign id to " + campaignId);
    campaigns.getCampaign(campaign.getCampaignId()).removeObservers(this);
    campaigns.getCampaign(campaignId).observe(this, this::updateCampaign);
  }
  */

  private void update(Characters characters) {
    if (campaign.isPresent()) {
      // Refresh the view buttons and such.
      TransitionManager.beginDelayedTransition(view, transition);
      addCharacter.visible();
      xp.visible(campaign.get().amDM() && battle.isPresent() && !this.battle.get().inBattle());
      if (battle.isPresent() && battle.get().inBattle()) {
        title.text("Battle - " +
            (battle.get().isSurprised() ? "Surprise" : "Turn " + battle.get().getTurn()))
            .backgroundColor(R.color.battleLight);
        scroll.backgroundColor(R.color.battleDark);
        startEncounter.gone();
        conditions.setVisibility(battle.get().isStarting() ? View.GONE : View.VISIBLE);
      } else {
        title.text("Party");
        title.backgroundColor(R.color.characterLight);
        scroll.backgroundColor(R.color.cell);
        initiative.setVisibility(View.GONE);
        startEncounter.visible(this.campaign.get().amDM());
        conditions.setVisibility(View.VISIBLE);
      }

      for (int i = 0; i < conditions.getChildCount(); i++) {
        View view = conditions.getChildAt(i);
        if (view instanceof ConditionCreatureView) {
          ((ConditionCreatureView) view).update(campaign.get());
        }
      }

      updateChips();
    }
  }

  private void update(Images images) {
    for (ChipView chip: chipsById.values()) {
      chip.update();
    }
  }

  public void show(Campaign campaign) {
    this.campaign = Optional.of(campaign);
    this.battle = Optional.empty(); //Optional.of(encounters.get(campaign.getId()));
    //battleView.update(this.campaign);

    update(characters);
  }

  private void updateChips() {
      /*
    for (String characterId : characterIds) {
      LiveData<Optional<Character>> character = CompanionApplication.get(getContext())
          .characters().getCharacter(characterId);
      character.removeObservers(this);
      character.observe(this, this::updateCharacter);
      if (character.getValue().isPresent()) {
        character.getValue().get().loadImage().observe(this, this::updateImage);
      }
    }
      */

    Collection<Character> campaignCharacters =
        characters.getCampaignCharacters(campaign.get().getId());
    Set<String> characterIds = campaignCharacters.stream()
        .map(Character::getId)
        .collect(Collectors.toSet());

    // Remove all chips for which we don't have characters anymore.
    for (Iterator<String> i = chipsById.keySet().iterator(); i.hasNext(); ) {
      String chipId = i.next();
      if (chipId.startsWith("characters") && !characterIds.contains(chipId)) {
        chipsById.remove(chipId);
      }
    }

    // Add all new chips.
    for (Character character : campaignCharacters) {
      if (!chipsById.containsKey(character.getId())) {
        chipsById.put(character.getId(),
            new CharacterChipView(getContext(), character));
      }
    }

    // Remove any character needing initative that are no more available.
    for (Iterator<String> i = charactersNeedingInitiative.keySet().iterator(); i.hasNext(); ) {
      if (!characterIds.contains(i.next())) {
        i.remove();
      }
    }

    redrawChips();
  }

  private void updateCreatureIds(ImmutableList<String> creatureIds) {
    Status.log("updating creatures for " + campaign + ": " + creatureIds);

    // Only add monster chips for local campaigns (the DM).
    if (campaign.isPresent() && campaign.get().amDM()) {
      // Remove all chips for which we don't have creatures anymore.
      chipsById.keySet()
          .removeIf(chipId -> chipId.startsWith("creature") && !creatureIds.contains(chipId));

      // Add all new chips.
      for (String creatureId : creatureIds) {
        if (!chipsById.containsKey(creatureId)) {
          /*
          LiveData<Optional<Creature>> creature =
              CompanionApplication.get(getContext()).creatures().getCreature(creatureId);
          creature.observe(this, this::updateCreature);
          if (creature.getValue().isPresent()) {
            chipsById.put(creatureId, new CreatureChipView(getContext(), creature.getValue().get
            ()));
            battle.addCreature(creatureId);
          }
          */
        }
      }
    } else {
      chipsById.keySet().removeIf(chipId -> chipId.startsWith("creature"));
    }

    redrawChips();
  }

  @Nullable
  private CharacterChipView chip(Character character) {
    return (CharacterChipView) chipsById.get(character.getId());
  }

  @Nullable
  private CreatureChipView chip(Monster monster) {
    return chipsById.get(monster.getId());
  }

  private void updateCharacter(Optional<Character> character) {
    if (character.isPresent()) {
      if (character.get().hasInitiative()) {
        charactersNeedingInitiative.remove(character.get().getId());
      } else if (character.get().amPlayer()
          && battle.get().getCreatureIds().contains(character.get().getId())) {
        charactersNeedingInitiative.put(character.get().getId(), character.get());
      }

      CharacterChipView chip = chip(character.get());
      if (chip != null) {
        chip.update(character.get());
      }

      // Initiative could have been changed, thus we might have to resort.
      redrawChips();
    }

    if (battle.get().inBattle()) {
      if (charactersNeedingInitiative.isEmpty()) {
        initiative.setVisibility(View.GONE);
        if (campaign.get().amDM() && battle.get().isStarting()) {
          battle.get().start();
        }
      } else {
        // Only local characters can need initiative.
        Character initCharacter = charactersNeedingInitiative.values().iterator().next();
        initiative.setVisibility(View.VISIBLE);
        /*
        initiative.setLabel("Initiative for " + initCharacter.getName());
        initiative.setModifier(initCharacter.initiativeModifier());
        initiative.setSelectAction(i -> {
          TransitionManager.beginDelayedTransition(view, transition);
          initCharacter.setBattle(i, battle.get().getNumber());
        });
        */
      }
    }

    conditions.removeAllViews();
    addAllConditions();
  }

  /*
  private void updateCreature(Optional<Creature> creature) {
    conditions.removeAllViews();
    addAllConditions();
  }
  */

  private void createCharacter() {
    CharacterDialog.newInstance("", campaign.get().getId()).display();
  }

  private void chooseXp() {
    XPDialog.newInstance(campaign.get().getId()).display();
  }

  private void startEncounter() {
    if (!campaign.get().amDM()) {
      Status.error("Cannot start battle in a remote campaign.");
      return;
    }

    /*
    if (campaign.getCharacters().isEmpty()) {
      Status.error("Cannot start battle without characters");
      return;
    }
    */

    charactersNeedingInitiative.clear();
    battle.get().setup();
  }

  private void redrawChips() {
    Status.log("redrawing party chips");

    TransitionManager.beginDelayedTransition(view, transition);
    party.removeAllViews();

    if (campaign.isPresent()) {
      for (Character character : characters.getCampaignCharacters(campaign.get().getId())) {
        CreatureChipView chip = chipsById.get(character.getId());
        if (chip != null) {
          chip.addTo(party);
        }
      }
    }
    /*
    List<String> ids = battle.get().obtainCreatureIds();
    for (String id : ids) {
      CreatureChipView chip = chipsById.get(id);
      if (chip != null) {
        chip.addTo(party);
        chip.setBattleMode(battle.get().getStatus());
        chip.select(battle.get().isOngoingOrSurprised()
            && chip.getCreatureId().equals(battle.get().getCurrentCreatureId()));
      }
    }
    */
  }

  private void addAllConditions() {
    for (Character character : characters.getCampaignCharacters(campaign.get().getId())) {
      if (character.amPlayer() || campaign.get().amDM()) {
        addConditions(character);
      }
    }

    /*
    if (campaign.get().amDM()) {
      for (Creature creature : characters.getCampaignCharacters(campaign.get().getId()) {
        addConditions(creature);
      }
    }
    */
  }

  private void addConditions(Creature shown) {
    ConditionCreatureView creatureView =
        new ConditionCreatureView(getContext(), shown.getName(), battle.get());

    creatureView.addConditions(shown, campaign.get().amDM());

    if (creatureView.hasConditions()) {
      conditions.addView(creatureView);
    }
  }
}
