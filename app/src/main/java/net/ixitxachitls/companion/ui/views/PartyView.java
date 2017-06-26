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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.MonsterInitiativeDialog;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * View representing a whole party.
 */
public class PartyView extends LinearLayout {
  private List<Character> characters = new ArrayList<>();


  private final ViewGroup view;
  private final Wrapper<View> scroll;
  private final LinearLayout party;
  private final TextWrapper<TextView> title;
  private final Wrapper<FloatingActionButton> startBattle;
  private final Wrapper<LinearLayout> actions;
  private final Wrapper<ImageButton> add;
  private final Wrapper<View> addDelimiter;
  private final Wrapper<ImageButton> next;
  private final Wrapper<View> nextDelimiter;
  private final Wrapper<ImageButton> delay;
  private final Wrapper<View> delayDelimiter;
  private final Wrapper<ImageButton> stop;
  private final Wrapper<View> stopDelimiter;
  private final DiceView initiative;

  private Optional<Campaign> campaign = Optional.absent();

  public PartyView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    view = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.view_party, null, false);
    view.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));

    title = TextWrapper.wrap(view, R.id.title);
    party = (LinearLayout) view.findViewById(R.id.party);
    scroll = Wrapper.wrap(view, R.id.scroll);
    startBattle = Wrapper.wrap(view, R.id.start_battle);
    startBattle.onClick(this::setupBattle);
    actions = Wrapper.wrap(view, R.id.actions);
    add = Wrapper.wrap(view, R.id.add);
    add.onClick(this::addMonster);
    addDelimiter = Wrapper.wrap(view, R.id.delimiter_add);
    next = Wrapper.wrap(view, R.id.next);
    next.onClick(this::nextCombatant);
    nextDelimiter = Wrapper.wrap(view, R.id.delimiter_next);
    delay = Wrapper.wrap(view, R.id.delay);
    delayDelimiter = Wrapper.wrap(view, R.id.delimiter_delay);
    stop = Wrapper.wrap(view, R.id.stop);
    stop.onClick(this::stopBattle);
    stopDelimiter = Wrapper.wrap(view, R.id.delimiter_stop);
    initiative = (DiceView) view.findViewById(R.id.initiative);
    initiative.setDice(20);

    addView(view);
  }

  private void nextCombatant() {
    if (campaign.isPresent()) {
      campaign.get().getBattle().combatantDone();
      refresh();
    }
  }

  public void setCampaign(Optional<Campaign> campaign) {
    this.campaign = campaign;
    this.characters.clear();
    party.removeAllViews();

    refresh();
  }

  private void setupBattle() {
    if (!campaign.isPresent() || !campaign.get().isLocal()) {
      return;
    }

    // Setup the campaign for battle.
    Battle battle = campaign.get().getBattle();
    battle.start();

    for (Character character : characters) {
      battle.refreshCombatant(character.getCharacterId(), character.getName(),
          Character.NO_INITIATIVE);
    }

    TransitionManager.beginDelayedTransition(view);
    refresh();
  }

  private void stopBattle() {
    if (campaign.isPresent() && campaign.get().isLocal()) {
      campaign.get().getBattle().end();
      TransitionManager.beginDelayedTransition(view);
      refresh();
    }
  }

  private void addMonster() {
    if (campaign.isPresent()) {
      MonsterInitiativeDialog.newInstance(campaign.get().getCampaignId(), -1)
          .display(CompanionFragments.get().getFragmentManager());
    }
  }

  private static List<Character> party(Campaign campaign) {
    List<Character> characters = campaign.getCharacters();
    if (!campaign.isLocal()) {
      characters.addAll(Characters.remote().getCharacters(campaign.getCampaignId()));
    }

    return characters;
  }

  private static Map<String, ChipView> removeChips(ViewGroup view) {
    Map<String, ChipView> chips = new HashMap<>();

    while (view.getChildCount() > 0) {
      ChipView chip = (ChipView) view.getChildAt(0);
      view.removeViewAt(0);
      chips.put(chip.getDataId(), chip);
    }

    return chips;
  }

  public void refresh() {
    if (campaign.isPresent()) {
      characters = party(campaign.get());

      Map<String, ChipView> chips = removeChips(party);

      if (inBattleMode()) {
        refreshBattle(chips);
      } else {
        refreshPeace(chips);
      }
    }
  }

  private void refreshPeace(Map<String, ChipView> chips) {
    Preconditions.checkArgument(campaign.isPresent());

    // Refresh the chips, without recreation to get some smooth, animated updates.
    for (Character character : characters) {
      ChipView chip = chips.get(character.getCharacterId());
      if (chip == null) {
        chip = new CharacterChipView(getContext(), character);
        chip.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            CompanionFragments.get().showCharacter(character);
          }
        });
      }

      chip.setBackground(R.color.characterDark);
      chip.setSubtitle("");
      party.addView(chip);
    }

    title.backgroundColor(R.color.characterLight);
    scroll.backgroundColor(R.color.characterDark);
    RelativeLayout.LayoutParams params =
        (RelativeLayout.LayoutParams) actions.get().getLayoutParams();
    if (campaign.get().isLocal()) {
      startBattle.visible();
      params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.scroll);
      params.removeRule(RelativeLayout.BELOW);
      initiative.setVisibility(GONE);
    } else {
      startBattle.gone();
      params.addRule(RelativeLayout.BELOW, R.id.scroll);
      params.removeRule(RelativeLayout.ALIGN_BOTTOM);
      initiative.setVisibility(GONE);
    }
  }

  private static Optional<Character> needsInitiative(List<Character> characters) {
    for (Character character : characters) {
      if (!character.hasInitiative()) {
        return Optional.of(character);
      }
    }

    return Optional.absent();
  }

  private void refreshBattle(Map<String, ChipView> chips) {
    Preconditions.checkArgument(campaign.isPresent());

    // Refresh the chips, without recreation to get some smooth, animated updates.
    Battle battle = campaign.get().getBattle();
    battle.refreshCombatants();

    if (campaign.get().isLocal() && battle.isStarting() && battleReady(characters)) {
      battle.battle();
    }

    for (Battle.Combatant combatant : battle.combatantsByInitiative()) {
      ChipView chip = chips.get(combatant.getId());
      if (chip == null) {
        if (combatant.isMonster()) {
          if (campaign.get().isLocal()) {
            chip = new ChipView(getContext(),
                combatant.getName(), combatant.getName(), "init " + combatant.getInitiative(),
                R.color.monster, R.color.monsterDark);
          }
        } else {
          Optional<Character> character =
              Characters.get(!campaign.get().isLocal()).get(combatant.getId());
          if (character.isPresent()) {
            chip = new CharacterChipView(getContext(), character.get());
            chip.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                CompanionFragments.get().showCharacter(character.get());
              }
            });
          }
        }
      }

      if (chip != null) {
        chip.setBackground(R.color.battleDark);
        if (combatant.hasInitiative()) {
          chip.setSubtitle("init " + combatant.getInitiative());
        } else {
          chip.setSubtitle("");
        }
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

    // Actions.
    RelativeLayout.LayoutParams params =
        (RelativeLayout.LayoutParams) actions.get().getLayoutParams();
    if (campaign.get().isLocal()) {
      startBattle.gone();
      params.removeRule(RelativeLayout.ALIGN_BOTTOM);
      params.addRule(RelativeLayout.BELOW, R.id.scroll);
      add.visible(battle.isStarting());
      addDelimiter.visible(battle.isStarting());
      next.visible(battle.isSurprised() || battle.isOngoing());
      nextDelimiter.visible(battle.isSurprised() || battle.isOngoing());
      delay.visible(battle.isOngoing());
      delayDelimiter.visible(battle.isOngoing());
      stop.visible(!battle.isEnded());
      stopDelimiter.visible(battle.isSurprised() || battle.isOngoing());
      initiative.setVisibility(GONE);
    } else {
      startBattle.gone();
      params.removeRule(RelativeLayout.BELOW);
      params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.scroll);
      Optional<Character> initCharacter = needsInitiative(characters);

      if (initCharacter.isPresent()) {
        initiative.setVisibility(VISIBLE);
        initiative.setLabel("Initiative for " + initCharacter.get().getName());
        initiative.setModifier(initCharacter.get().initiativeModifier());
        initiative.setSelectAction(i -> {
          initCharacter.get().setBattle(i, battle.getNumber());
          TransitionManager.beginDelayedTransition(view);
          battle.refreshCombatant(initCharacter.get().getCharacterId(),
              initCharacter.get().getName(), i);
          refresh();
        });
      } else {
        initiative.setVisibility(GONE);
      }
    }
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
    return campaign.isPresent() && !campaign.get().getBattle().isEnded();
  };
}
