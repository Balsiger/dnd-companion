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

import android.arch.lifecycle.LiveData;
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
import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Creature;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.ui.dialogs.CharacterDialog;
import net.ixitxachitls.companion.ui.dialogs.XPDialog;
import net.ixitxachitls.companion.ui.views.BattleView;
import net.ixitxachitls.companion.ui.views.CharacterChipView;
import net.ixitxachitls.companion.ui.views.ConditionCreatureView;
import net.ixitxachitls.companion.ui.views.CreatureChipView;
import net.ixitxachitls.companion.ui.views.DiceView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A fragment displaying the complete party of the current campaign.
 */
public class PartyFragment extends Fragment {

  // External data.
  private Campaigns campaigns;
  private Campaign campaign;

  // UI.
  private ViewGroup view;
  private Wrapper<View> scroll;
  private LinearLayout party;
  private TextWrapper<TextView> title;
  private Wrapper<FloatingActionButton> startBattle;
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

    campaigns = CompanionApplication.get(getContext()).campaigns();
    campaign = campaigns.getDefaultCampaign();
    campaigns.getCurrentCampaignId().observe(this, this::updateCampaignId);

    view = (ViewGroup) inflater.inflate(R.layout.fragment_party, container, false);
    view.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));

    title = TextWrapper.wrap(view, R.id.title);
    party = view.findViewById(R.id.party);
    scroll = Wrapper.wrap(view, R.id.scroll);
    startBattle = Wrapper.wrap(view, R.id.start_battle);
    startBattle.onClick(this::startBattle)
        .description("Start Battle", "Start a battle with the party. Monsters can be added once "
            + "the battle has started.");
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

    updateCampaignId(campaigns.getCurrentCampaignId().getValue());
  }

  private void updateCampaignId(String campaignId) {
    Status.log("updating campaign id to " + campaignId);
    campaigns.getCampaign(campaign.getCampaignId()).removeObservers(this);
    campaigns.getCampaign(campaignId).observe(this, this::updateCampaign);
  }

  private void updateCampaign(Optional<Campaign> campaign) {
    if (campaign.isPresent()) {
      Status.log("updading campaign " + campaign);
      this.campaign.getCharacterIds().removeObservers(this);

      this.campaign = campaign.get();
      battleView.update(this.campaign);

      // No need to refresh the view, as we get a new set of characters anyway.
      this.campaign.getCharacterIds().observe(this, this::updateCharacterIds);
      this.campaign.getCreatureIds().observe(this, this::updateCreatureIds);

      // Refresh the view buttons and such.
      TransitionManager.beginDelayedTransition(view, transition);
      addCharacter.visible(!this.campaign.inBattle());
      xp.visible(this.campaign.isLocal() && !this.campaign.inBattle()
          && !this.campaign.isDefault());

      Battle battle = this.campaign.getBattle();
      if (this.campaign.inBattle()) {
        title.text("Battle - " +
            (battle.isSurprised() ? "Surprise" : "Turn " + battle.getTurn()))
            .backgroundColor(R.color.battleLight);
        scroll.backgroundColor(R.color.battleDark);
        startBattle.gone();
        conditions.setVisibility(
            this.campaign.getBattle().isStarting() ? View.GONE : View.VISIBLE);
      } else {
        title.text("Party");
        title.backgroundColor(R.color.characterLight);
        scroll.backgroundColor(R.color.cell);
        initiative.setVisibility(View.GONE);
        startBattle.visible(this.campaign.isLocal() && !this.campaign.isDefault());
        conditions.setVisibility(View.VISIBLE);
      }

      for (int i = 0; i < conditions.getChildCount(); i++) {
        View view = conditions.getChildAt(i);
        if (view instanceof ConditionCreatureView) {
          ((ConditionCreatureView) view).update(campaign.get());
        }
      }
    }
  }

  private void updateCharacterIds(ImmutableList<String> characterIds) {
    Status.log("updating characters for " + campaign + ": " + characterIds);
    for (String characterId : characterIds) {
      LiveData<Optional<Character>> character = CompanionApplication.get(getContext())
          .characters().getCharacter(characterId);
      character.removeObservers(this);
      character.observe(this, this::updateCharacter);
      if (character.getValue().isPresent()) {
        character.getValue().get().loadImage().observe(this, this::updateImage);
      }
    }

    // Remove all chips for which we don't have characters anymore.
    for (Iterator<String> i = chipsById.keySet().iterator(); i.hasNext(); ) {
      String chipId = i.next();
      if (chipId.startsWith(Character.TYPE) && !characterIds.contains(chipId)) {
        chipsById.remove(chipId);
      }
    }

    // Add all new chips.
    for (Character character : campaign.getCharacters()) {
      if (!chipsById.containsKey(character.getCharacterId())) {
        chipsById.put(character.getCharacterId(), new CharacterChipView(getContext(), character));
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
    if (campaign.isLocal()) {
      // Remove all chips for which we don't have creatures anymore.
      chipsById.keySet()
          .removeIf(chipId -> chipId.startsWith(Creature.TYPE) && !creatureIds.contains(chipId));

      // Add all new chips.
      for (String creatureId : creatureIds) {
        if (!chipsById.containsKey(creatureId)) {
          LiveData<Optional<Creature>> creature =
              CompanionApplication.get(getContext()).creatures().getCreature(creatureId);
          creature.observe(this, this::updateCreature);
          if (creature.getValue().isPresent()) {
            chipsById.put(creatureId, new CreatureChipView(getContext(), creature.getValue().get()));
            campaign.getBattle().addCreature(creatureId);
          }
        }
      }
    } else {
      chipsById.keySet().removeIf(chipId -> chipId.startsWith(Creature.TYPE));
    }

    redrawChips();
  }

  @Nullable
  private CharacterChipView chip(Character character) {
    return (CharacterChipView) chipsById.get(character.getCharacterId());
  }

  @Nullable
  private CreatureChipView chip(Creature creature) {
    return chipsById.get(creature.getCreatureId());
  }

  private void updateCharacter(Optional<Character> character) {
    if (character.isPresent()) {
      if (character.get().hasInitiative()) {
        charactersNeedingInitiative.remove(character.get().getCharacterId());
      } else if (character.get().isLocal()
          && campaign.getBattle().getCreatureIds().contains(character.get().getCharacterId())) {
        charactersNeedingInitiative.put(character.get().getCharacterId(), character.get());
      }

      CharacterChipView chip = chip(character.get());
      if (chip != null) {
        chip.update(character.get());
      }

      // Initiative could have been changed, thus we might have to resort.
      redrawChips();
    }

    if (campaign.inBattle()) {
      if (charactersNeedingInitiative.isEmpty()) {
        initiative.setVisibility(View.GONE);
        if (campaign.isLocal() && campaign.getBattle().isStarting()) {
          campaign.getBattle().start();
        }
      } else {
        // Only local characters can need initiative.
        Character initCharacter = charactersNeedingInitiative.values().iterator().next();
        initiative.setVisibility(View.VISIBLE);
        initiative.setLabel("Initiative for " + initCharacter.getName());
        initiative.setModifier(initCharacter.initiativeModifier());
        initiative.setSelectAction(i -> {
          TransitionManager.beginDelayedTransition(view, transition);
          Battle battle = campaign.getBattle();
          initCharacter.asLocal().setBattle(i, battle.getNumber());
        });
      }

      conditions.removeAllViews();
      addAllConditions();
    }
  }

  private void updateCreature(Optional<Creature> creature) {
    conditions.removeAllViews();
    addAllConditions();
  }

  private void updateImage(Optional<Image> image) {
    if (image.isPresent()) {
      CreatureChipView chip = chipsById.get(image.get().getId());
      if (chip instanceof CharacterChipView) {
        ((CharacterChipView) chip).update(image.get());
      }
    }
  }

  private void createCharacter() {
    CharacterDialog.newInstance("", campaign.getCampaignId()).display();
  }

  private void chooseXp() {
    XPDialog.newInstance(campaign.getCampaignId()).display();
  }

  private void startBattle() {
    if (!campaign.isLocal()) {
      Status.error("Cannot start battle in a remote campaign.");
      return;
    }

    if (campaign.getCharacters().isEmpty()) {
      Status.error("Cannot start battle without characters");
      return;
    }

    charactersNeedingInitiative.clear();
    campaign.getBattle().setup();
  }

  private void redrawChips() {
    Status.log("redrawing party chips");

    TransitionManager.beginDelayedTransition(view, transition);
    party.removeAllViews();

    List<String> ids = campaign.getBattle().obtainCreatureIds();
    for (String id : ids) {
      CreatureChipView chip = chipsById.get(id);
      if (chip != null) {
        chip.addTo(party);
        chip.setBattleMode(campaign.getBattle().getStatus());
        chip.select(campaign.getBattle().isOngoingOrSurprised()
            && chip.getCreatureId().equals(campaign.getBattle().getCurrentCreatureId()));
      }
    }
  }

  private void addAllConditions() {
    for (Character character : campaign.getCharacters()) {
      if (character.isLocal() || campaign.isLocal()) {
        addConditions(character);
      }
    }

    if (campaign.isLocal()) {
      for (Creature creature : campaign.getCreatures()) {
        addConditions(creature);
      }
    }
  }

  private void addConditions(BaseCreature shown) {
    ConditionCreatureView creatureView =
        new ConditionCreatureView(getContext(), shown.getName(), campaign.getBattle());

    creatureView.addConditions(shown, campaign.isLocal());

    if (creatureView.hasConditions()) {
      conditions.addView(creatureView);
    }
  }
}
