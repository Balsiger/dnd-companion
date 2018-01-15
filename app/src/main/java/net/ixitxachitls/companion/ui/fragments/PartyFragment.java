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

package net.ixitxachitls.companion.ui.fragments;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.CharacterDialog;
import net.ixitxachitls.companion.ui.dialogs.XPDialog;
import net.ixitxachitls.companion.ui.views.BattleView;
import net.ixitxachitls.companion.ui.views.CharacterChipView;
import net.ixitxachitls.companion.ui.views.ChipView;
import net.ixitxachitls.companion.ui.views.DiceView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment displaying the complete party of the current campaign.
 */
public class PartyFragment extends Fragment {

  private static final String TAG = "PartyFragment";

  // External data.
  private LiveData<ImmutableList<Character>> liveCharacters;
  private List<Character> characters;
  private Campaign campaign = Campaigns.defaultCampaign;

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
  private TextWrapper<TextView> conditions;

  public PartyFragment() {
    Campaigns.getCurrentCampaignId().observe(this, this::updateCampaignId);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    view = (ViewGroup) inflater.inflate(R.layout.fragment_party, container, false);
    view.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));

    title = TextWrapper.wrap(view, R.id.title);
    party = view.findViewById(R.id.party);
    scroll = Wrapper.wrap(view, R.id.scroll);
    startBattle = Wrapper.wrap(view, R.id.start_battle);
    startBattle.onClick(this::setupBattle);

    battleView = new BattleView(getContext(), this);
    battleView.setVisibility(View.GONE);
    view.addView(battleView, 0);

    addCharacter = Wrapper.<FloatingActionButton>wrap(view, R.id.add_character)
        .onClick(this::createCharacter);
    xp = Wrapper.<FloatingActionButton>wrap(view, R.id.xp)
        .onClick(this::chooseXp);
    initiative = view.findViewById(R.id.initiative);
    initiative.setDice(20);
    conditions = TextWrapper.wrap(view, R.id.conditions);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

    updateCampaignId(Campaigns.getCurrentCampaignId().getValue());
  }

  private void updateCampaignId(String campaignId) {
    Log.d(TAG, "updating campaign id to " + campaignId);
    Campaigns.getCampaign(campaign.getCampaignId()).removeObservers(this);
    Campaigns.getCampaign(campaignId).observe(this, this::updateCampaign);
  }

  private void updateCampaign(Optional<Campaign> campaign) {
    if (campaign.isPresent()) {
      Log.d(TAG, "updading campaign " + campaign);
      this.campaign.getCharacterIds().removeObservers(this);

      this.campaign = campaign.get();
      battleView.update(this.campaign);

      // No need to refresh the view, as we get a new set of characters anyway.
      this.campaign.getCharacterIds().observe(this, this::updateCharacterIds);
    }
  }

  private void updateCharacterIds(ImmutableList<String> characterIds) {
    Log.d(TAG, "updating characters for " + campaign + ": " + characterIds);
    this.characters = new ArrayList<>();
    for (String characterId : characterIds) {
      this.characters.add(Characters.getCharacter(characterId).getValue().get());
    }
    this.characters.sort((first, second) -> {
      if (first.getCharacterId().equals(second.getCharacterId())) {
        return 0;
      }

      int compare = Integer.compare(first.getInitiative(), second.getInitiative());
      if (compare != 0) {
        return compare;
      }

      // TODO(merlin): Add real check to compare initiative falling back to dexterity and 'random'
      // otherwise (but make sure to keep it stable with 'random').
      return first.getName().compareTo(second.getName());
    });

    refresh();
  }

  private void createCharacter() {
    CharacterDialog.newInstance("", campaign.getCampaignId()).display();
  }

  private void chooseXp() {
    XPDialog.newInstance(campaign.getCampaignId()).display();
  }

  private void setupBattle() {
    if (!campaign.isLocal()) {
      return;
    }

    // Setup the campaign for battle.
    Battle battle = campaign.getBattle();
    battle.start();

    for (Character character : characters) {
      battle.refreshCombatant(character.getCharacterId(), character.getName(),
          Character.NO_INITIATIVE);
    }
  }

  private static Map<String, ChipView> removeChips(ViewGroup view) {
    Map<String, ChipView> chips = new HashMap<>();

    while (view.getChildCount() > 0) {
      ChipView chip = (ChipView) view.getChildAt(0);
      view.removeViewAt(0);
      chips.put(chip.getDataId(), chip);
      if (chip instanceof CharacterChipView) {
        Characters.getCharacter(chip.getDataId())
            .removeObserver(((CharacterChipView) chip)::update);
      }
    }

    return chips;
  }

  private void refresh() {
    Log.d(TAG, "refreshing party fragment for " + characters);
    TransitionManager.beginDelayedTransition(view);

    addCharacter.visible(!campaign.inBattle());
    xp.visible(campaign.isLocal() && !campaign.inBattle());

    Map<String, ChipView> chips = removeChips(party);

    if (inBattleMode()) {
      refreshBattle(chips);
    } else {
      refreshPeace(chips);
    }
  }

  private void refreshPeace(Map<String, ChipView> chips) {
    Log.d(TAG, "refreshing peace chips " + chips.size() + ", " + characters.size() + " characters");
    // Refresh the chips, without recreation to get some smooth, animated updates.
    for (Character character : characters) {
      CharacterChipView chip = (CharacterChipView) chips.get(character.getCharacterId());
      if (chip == null) {
        chip = new CharacterChipView(getContext(), character);
        final ChipView finalChip = chip;
        chip.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            CompanionFragments.get().showCharacter(character, Optional.of(finalChip));
          }
        });
      }

      party.addView(chip);
      chip.setBackground(R.color.characterDark);
      Characters.getCharacter(chip.getDataId()).observe(this, chip::update);
    }

    title.text("Party");
    title.backgroundColor(R.color.characterLight);
    scroll.backgroundColor(R.color.characterDark);
    initiative.setVisibility(View.GONE);
    startBattle.visible(campaign.isLocal() && !campaign.isDefault());
    conditions.gone();
  }

  private static Optional<Character> needsInitiative(List<Character> characters) {
    for (Character character : characters) {
      if (!character.hasInitiative() && character.isLocal()) {
        return Optional.of(character);
      }
    }

    return Optional.absent();
  }

  private void refreshBattle(Map<String, ChipView> chips) {
    Log.d(TAG, "refreshing battle chips " + chips.size());

    // Refresh the chips, without recreation to get some smooth, animated updates.
    Battle battle = campaign.getBattle();
    battle.refreshCombatants();

    if (campaign.isLocal() && battle.isStarting() && battleReady(characters)) {
      battle.battle();
    }

    List<Battle.Combatant> combatants = campaign.getBattle().isStarting()
        ? battle.combatantsByInitiative() : battle.combatants();
    for (Battle.Combatant combatant : combatants) {
      ChipView chip = chips.get(combatant.getId());
      if (chip == null) {
        if (combatant.isMonster()) {
          if (campaign.isLocal()) {
            chip = new ChipView(getContext(),
                combatant.getName(), combatant.getName(), "init " + combatant.getInitiative(),
                R.color.monster, R.color.monsterDark);
          }
        } else {
          LiveData<Optional<Character>> character = Characters.getCharacter(combatant.getId());
          if (character.getValue().isPresent()) {
            chip = new CharacterChipView(getContext(), character.getValue().get());
            final ChipView finalChip = chip;
            chip.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                CompanionFragments.get().showCharacter(character.getValue().get(),
                    Optional.of(title.get()));
              }
            });
          }
        }
      }

      if (chip != null) {
        chip.setBackground(R.color.battleDark);
        if (combatant == battle.getCurrentCombatant()) {
          chip.select();
        } else {
          chip.unselect();
        }

        party.addView(chip);
      }
    }


    title.text("Battle - " +
        (battle.isSurprised() ? "Surprise" : "Turn " + battle.getTurn()))
        .backgroundColor(R.color.battleLight);
    scroll.backgroundColor(R.color.battleDark);

    startBattle.gone();
    Optional<Character> initCharacter = needsInitiative(characters);

    if (initCharacter.isPresent()) {
      initiative.setVisibility(View.VISIBLE);
      initiative.setLabel("Initiative for " + initCharacter.get().getName());
      initiative.setModifier(initCharacter.get().initiativeModifier());
      initiative.setSelectAction(i -> {
        initCharacter.get().setBattle(i, battle.getNumber());
        TransitionManager.beginDelayedTransition(view);
        battle.refreshCombatant(initCharacter.get().getCharacterId(),
            initCharacter.get().getName(), i);
        //refreshWithTransition();
      });
    } else {
      initiative.setVisibility(View.GONE);
    }

    conditions.text(conditions(battle.getCurrentCombatant().getId()));
  }

  private String conditions(String currentId) {
    List<String> conditions = new ArrayList<>();

    for (Character character : characters) {
      for (Character.TimedCondition condition : character.conditionsFor(currentId)) {
        int remainingRounds = condition.getEndRound() - campaign.getBattle().getTurn();
        if (remainingRounds > 0) {
          conditions.add(condition.getText() + " (" + character.getName() + "), " +
              remainingRounds + " rounds");
        }
      }
    }

    return Joiner.on("\n").join(conditions);
  }

  private boolean battleReady(List<Character> characters) {
    for (Character character : characters) {
      if (!character.hasInitiative()) {
        return false;
      }
    }

    return true;
  }

  private boolean inBattleMode() {
    return campaign.inBattle();
  };

}
