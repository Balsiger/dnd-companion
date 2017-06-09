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

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Optional;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.ui.Setup;
import net.ixitxachitls.companion.ui.dialogs.MonsterInitiativeDialog;
import net.ixitxachitls.companion.ui.views.ChipView;
import net.ixitxachitls.companion.ui.views.DiceView;

import java.util.List;

import static android.view.View.GONE;

/**
 * Display for an ongoing battle.
 */
public class BattleFragment extends CompanionFragment {

  private Optional<Campaign> campaign = Optional.absent();
  private Optional<Character> character = Optional.absent();

  // UI elements.
  private TextView turn;
  private TextView status;
  private Button start;
  private Button addMonster;
  private LinearLayout characters;
  private HorizontalScrollView charactersScroll;
  private DiceView dice;
  private Button end;
  private Button battleButton;
  private LinearLayout initiative;
  private TextView initiativeNumber;
  private Button next;
  private Button delay;
  private Button remove;

  public BattleFragment() {
    super(Type.battle);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_battle, container, false);
    turn = Setup.textView(view, R.id.turn);
    status = Setup.textView(view, R.id.status);
    start = Setup.button(view, R.id.start, this::start);
    battleButton = Setup.button(view, R.id.battle, this::battle);
    end = Setup.button(view, R.id.end, this::end);
    addMonster = Setup.button(view, R.id.add_monster, this::addMonster);
    characters = (LinearLayout) view.findViewById(R.id.characters);
    charactersScroll = (HorizontalScrollView) view.findViewById(R.id.charactersScroll);
    dice = (DiceView) view.findViewById(R.id.dice);
    initiative = (LinearLayout) view.findViewById(R.id.initiative);
    initiativeNumber = Setup.textView(view, R.id.initiativeNumber, this::changeInitiative);
    next = Setup.button(view, R.id.next, this::next);
    delay = Setup.button(view, R.id.delay, this::delay);
    remove = Setup.button(view, R.id.remove, this::remove);

    return view;
  }

  public void forCampaign(Campaign campaign) {
    if (this.campaign.orNull() == campaign) {
      return;
    }

    this.campaign = Optional.of(campaign);
    this.character = Optional.absent();

    refresh();
  }

  public void forCharacter(Character character) {
    if (this.character == Optional.of(character)) {
      return;
    }

    this.character = Optional.of(character);
    this.campaign = Campaigns.get(!character.isLocal()).getCampaign(character.getCampaignId());

    refresh();
  }

  private void setInitiative(int initiative) {
    if (character.isPresent() && campaign.isPresent()) {
      character.get().setBattle(initiative, campaign.get().getBattle().getNumber());
      refresh();
    }
  }

  private void changeInitiative() {
    if (!campaign.isPresent() || !campaign.get().getBattle().isStarting()) {
      return;
    }

    setInitiative(Character.NO_INITIATIVE);
  }

  private void start() {
    if (campaign.isPresent() && campaign.get().isLocal()) {
      campaign.get().getBattle().start();

      for (Character character : Characters.get(!campaign.get().isLocal())
          .getCharacters(campaign.get().getCampaignId())) {
        campaign.get().getBattle().refreshCombatant(character.getCharacterId(), character.getName(),
            Character.NO_INITIATIVE);
      }

      refresh();
    }
  }

  private void battle() {
    if (campaign.isPresent() && campaign.get().isLocal()) {
      campaign.get().getBattle().battle();
      refresh();
    }
  }

  private void end() {
    if (campaign.isPresent() && campaign.get().isLocal()) {
      campaign.get().getBattle().end();
      refresh();
    }
  }

  private void addMonster() {
    if (campaign.isPresent()) {
      MonsterInitiativeDialog.newInstance(campaign.get().getCampaignId(), -1)
          .display(getFragmentManager());
    }
  }

  @Override
  public void refresh() {
    super.refresh();

    if (!campaign.isPresent()) {
      return;
    }

    if (character.isPresent()) {
      character = Characters.get(character.get().isLocal())
          .getCharacter(character.get().getCharacterId(),
              campaign.get().getCampaignId());
    }
    campaign = Campaigns.get(campaign.get().isLocal()).getCampaign(campaign.get().getCampaignId());

    addMonster.setVisibility(GONE);
    dice.setVisibility(GONE);
    start.setVisibility(GONE);
    battleButton.setVisibility(GONE);
    end.setVisibility(GONE);
    characters.setVisibility(GONE);
    initiative.setVisibility(GONE);


    if (campaign.get().isLocal()) {
      refreshDM();
    } else {
      refreshPlayer();
    }

    Battle battle = campaign.get().getBattle();
    if (battle.isStarting()) {
      turn.setText("Starting");
    } else if (battle.isSurprised()) {
      turn.setText("Surprised");
    } else if (battle.isOngoing()) {
      turn.setText("Turn " + battle.getTurn());
    } else if (battle.isEnded()) {
      turn.setText("Ended");
    }
  }

  private void refreshPlayer() {
    if (!character.isPresent() || !campaign.isPresent()) {
      return;
    }

    Battle battle = campaign.get().getBattle();
    if (battle.isEnded()) {
      status.setText("Nothing to see here, please move on. Battle has ended or not yet started.");
    } else if (battle.isStarting() && !character.get().hasInitiative()) {
      status.setText("Battle is starting, select your iniiative...");
      dice.setModifier(character.get().initiativeModifier());
      dice.setDice(20);
      dice.setVisibility(View.VISIBLE);
      dice.setSelectAction(this::setInitiative);
    } else if (battle.isStarting() && character.get().hasInitiative()) {
      status.setText("Battle is starting, you have to wait your turn...");
      initiative.setVisibility(View.VISIBLE);
      initiativeNumber.setText(String.valueOf(character.get().getInitiative()));
    } else if (battle.isSurprised() || battle.isOngoing()) {
      initiative.setVisibility(View.GONE);
      characters.setVisibility(View.VISIBLE);
      if (isMyTurn()) {
        status.setText(battle.isSurprised()
            ? "You are in the surprise round and it's your turn!"
            : "You are in battle and it's your turn!.");
        initiative.getBackground().setColorFilter(getResources().getColor(R.color.on, null),
            PorterDuff.Mode.SRC);
      } else {
        status.setText(battle.isSurprised()
            ? "You are in the surprise round, please wait your turn."
            : "You are in battle, please wait your turn.");
        initiative.getBackground().setColorFilter(getResources().getColor(R.color.off, null),
            PorterDuff.Mode.DST);
      }

      renderCharactersBox(battle, battle.isSurprised() || battle.isOngoing());
    }
  }

  public boolean isMyTurn() {
    return campaign.isPresent()
        && campaign.get().getBattle().getCurrentCombatant().getName().equals(
            character.get().getName());
  }

  public void refreshDM() {
    if (!campaign.isPresent()) {
      return;
    }

    Battle battle = campaign.get().getBattle();

    if (battle.isEnded()) {
      status.setText("Battle has ended or did not yet begin.");
      start.setVisibility(View.VISIBLE);
    } else if (battle.isStarting()) {
      end.setVisibility(View.VISIBLE);
      addMonster.setVisibility(View.VISIBLE);
      status.setText("Battle is starting...");
      characters.setVisibility(View.VISIBLE);

      // Make sure all characters are stored as combatants.
      boolean allDone = true;
      for (Character character : campaign.get().getCharacters()) {
        battle.refreshCombatant(character.getCharacterId(), character.getName(),
            character.getInitiative());
        if (!character.hasInitiative()) {
          allDone = false;
          break;
        }
      }

      renderCharactersBox(battle, false);

      if (allDone) {
        battleButton.setVisibility(View.VISIBLE);
      }
    } else if (battle.isSurprised() || battle.isOngoing()) {
      end.setVisibility(View.VISIBLE);
      characters.setVisibility(View.VISIBLE);
      if (battle.isSurprised()) {
        status.setText("Surprise round, a single standard action each only.");
      } else {
        status.setText("Normal round, move and standard action each.");
      }

      renderCharactersBox(battle, true);
    }
  }

  private void renderCharactersBox(Battle battle, boolean battleRunning) {
    // Replace combatants as characters might have changed.
    if (campaign.isPresent()) {
      battle.refreshCombatants();
    }
    characters.removeAllViews();
    characters.setVisibility(View.VISIBLE);

    boolean isDM = campaign.isPresent() && campaign.get().isLocal();
    next.setVisibility(battleRunning && isDM ? View.VISIBLE : GONE);
    delay.setVisibility(battleRunning && isDM && !battle.currentIsLast() ? View.VISIBLE : GONE);
    remove.setVisibility(GONE);

    int i = 0;
    List<Battle.Combatant> combatants =
        battle.isStarting() ? battle.combatantsByInitiative() : battle.combatants();
    for (Battle.Combatant combatant : combatants) {
      ChipView chip;
      if (combatant.isMonster()) {
        chip = new ChipView(getContext(), R.drawable.ic_perm_identity_black_24dp,
            isDM ? combatant.getName() : "Monster", "init " + combatant.getInitiative(),
            R.color.monster);
      } else {
        chip = new ChipView(getContext(), R.drawable.ic_person_black_48dp,
            combatant.getName(),
            combatant.getInitiative() == Character.NO_INITIATIVE
                ? "" : "init " + combatant.getInitiative(), R.color.character);
      }

      if (!isReady(combatant)) {
        chip.disabled();
      }

      characters.addView(chip);

      if ((battle.isOngoing() || battle.isSurprised()) && i == battle.getCurrentCombatantIndex()) {
        chip.select();
        if (battleRunning && isDM && combatant.isMonster()) {
          remove.setVisibility(View.VISIBLE);
        }

        // Scroll to the chip.
        charactersScroll.post(new Runnable() {
          @Override
          public void run() {
            charactersScroll.smoothScrollTo(chip.getLeft(), 0);
          }
        });
      }

      i++;
    }
  }

  private void next() {
    if (campaign.isPresent()) {
      campaign.get().getBattle().combatantDone();
      refresh();
    }
  }

  private void delay() {
    if (campaign.isPresent()) {
      campaign.get().getBattle().combatantLater();
      refresh();
    }
  }

  private void remove() {
    if (campaign.isPresent()) {
      campaign.get().getBattle().removeCombatant();
      refresh();
    }
  }

  public boolean isReady(Battle.Combatant combatant) {
    if (combatant.isMonster()) {
      return true;
    }

    if (campaign.isPresent()) {
      for (Character character : Characters.get(!campaign.get().isLocal())
          .getCharacters(campaign.get().getCampaignId())) {
        if (character.getName().equals(combatant.getName())) {
          return character.hasInitiative();
        }
      }
    }

    return true;
  }
}
