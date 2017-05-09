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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import net.ixitxachitls.companion.ui.views.DiceView;
import net.ixitxachitls.companion.ui.views.InitiativeChip;

import java.util.List;

import static android.view.View.GONE;

/**
 * Display for an ongoing battle.
 */
public class BattleFragment extends CompanionFragment {

  private Campaign campaign;
  private Optional<Character> character = Optional.absent();

  // UI elements.
  private TextView turn;
  private TextView status;
  private Button start;
  private Button addMonster;
  private LinearLayout charactersBox;
  private DiceView dice;
  private Button end;
  private Button battleButton;

  public BattleFragment() {
    super(Type.battle);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_battle, container, false);
    turn = (TextView) Setup.textView(view, R.id.turn);
    status = (TextView) Setup.textView(view, R.id.status);
    start = (Button) Setup.button(view, R.id.start, this::start);
    battleButton = (Button) Setup.button(view, R.id.battle, this::battle);
    end = (Button) Setup.button(view, R.id.end, this::end);
    addMonster = (Button) Setup.button(view, R.id.add_monster, this::addMonster);
    charactersBox = (LinearLayout) view.findViewById(R.id.characters);
    dice = (DiceView) view.findViewById(R.id.dice);

    return view;
  }

  public void forCampaign(Campaign campaign) {
    if (this.campaign == campaign) {
      return;
    }

    this.campaign = campaign;
    this.character = Optional.absent();

    refresh();
  }

  public void forCharacter(Character character) {
    if (this.character == Optional.of(character)) {
      return;
    }

    this.character = Optional.of(character);
    this.campaign = Campaigns.get().getCampaign(character.getCampaignId());

    refresh();
  }

  private void setInitiative(int initiative) {
    if (character.isPresent()) {
      character.get().setBattle(initiative);
      refresh();
    }
  }

  private void start() {
    if (campaign.isLocal()) {
      campaign.getBattle().start();

      for (Character character : Characters.get().getCharacters(campaign.getCampaignId())) {
        campaign.getBattle().addCharacter(character.getName(), 0);
      }

      refresh();
    }
  }

  private void battle() {
    if (campaign.isLocal()) {
      campaign.getBattle().battle();
      refresh();
    }
  }

  private void end() {
    if (campaign.isLocal()) {
      campaign.getBattle().end();
      refresh();
    }
  }

  private void addMonster() {
    MonsterInitiativeDialog.newInstance(campaign.getCampaignId(), -1)
        .display(getFragmentManager());
  }

  @Override
  public void refresh() {
    super.refresh();

    if (campaign == null) {
      return;
    }

    campaign = Campaigns.get().getCampaign(campaign.getCampaignId());
    if (character.isPresent()) {
      character = Optional.of(Characters.get().getCharacter(character.get().getCharacterId(),
          campaign.getCampaignId()));
    }

    addMonster.setVisibility(GONE);
    dice.setVisibility(GONE);
    start.setVisibility(GONE);
    battleButton.setVisibility(GONE);
    end.setVisibility(GONE);
    charactersBox.setVisibility(GONE);

    if (campaign.isLocal()) {
      refreshDM();
    } else {
      refreshPlayer();
    }

    Battle battle = campaign.getBattle();
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
    if (!character.isPresent()) {
      return;
    }

    Battle battle = campaign.getBattle();
    if (battle.isEnded()) {
      status.setText("Nothing to see here, please move on. Battle has ended or not yet started.");
      character.get().clearBattle();
    } else if (battle.isStarting() && !character.get().hasInitiative()) {
      status.setText("Battle is starting, select your iniiative...");
      dice.setModifier(character.get().initiativeModifier());
      dice.setDice(20);
      dice.setVisibility(View.VISIBLE);
      dice.setSelectAction(this::setInitiative);
    } else if (battle.isStarting() && character.get().hasInitiative()) {
      status.setText("Battle is starting, you have to wait your turn...");
    } else if (battle.isSurprised()) {
      character.get().clearBattle();
      if (isMyTurn()) {
        status.setText("You are in the surprise round and it's your turn!");
      } else {
        status.setText("You are in the surprise round, please wait your turn.");
      }
    } else if (battle.isOngoing()) {
      character.get().clearBattle();
      if (isMyTurn()) {
        status.setText("You are in battle and it's your turn!.");
      } else {
        status.setText("You are in battle, please wait your turn.");
      }
    }
  }

  public boolean isMyTurn() {
    return campaign.getBattle().getCurrentCombatant().getName().equals(character.get().getName());
  }

  public void refreshDM() {
    if (campaign == null) {
      return;
    }

    Battle battle = campaign.getBattle();
    charactersBox.removeAllViews();

    if (battle.isEnded()) {
      status.setText("Battle has ended or did not yet begin.");
      start.setVisibility(View.VISIBLE);
    } else if (battle.isStarting()) {
      end.setVisibility(View.VISIBLE);
      addMonster.setVisibility(View.VISIBLE);
      status.setText("Battle is starting...");
      charactersBox.setVisibility(View.VISIBLE);

      // Make sure all characters are stored as combatants.
      boolean allDone = true;
      for (Character character : campaign.getCharacters()) {
        if (character.hasInitiative()) {
          battle.addCharacter(character.getName(), character.getInitiative());
        } else {
          allDone = false;
        }
      }

      if (allDone) {
        battleButton.setVisibility(View.VISIBLE);
      }
    } else if (battle.isSurprised() || battle.isOngoing()) {
      end.setVisibility(View.VISIBLE);
      charactersBox.setVisibility(View.VISIBLE);
      if (battle.isSurprised()) {
        status.setText("Surprise round, a single standard action each only.");
      } else {
        status.setText("Normal round, move and standard action each.");
      }
    }

    int i = 0;
    List<Battle.Combatant> combatants =
        battle.isStarting() ? battle.combatantsByInitiative() : battle.combatants();
    for (Battle.Combatant combatant : combatants) {
      InitiativeChip chip = new InitiativeChip(getContext(), this, battle, combatant.getName() + "/" + combatant.getInitiative(),
          combatant.isMonster(), isReady(combatant),
          (battle.isSurprised() || battle.isOngoing()) && i == battle.getCurrentCombatantIndex());
      charactersBox.addView(chip);

      if (i == battle.getCurrentCombatantIndex()) {
        View view= new View(getContext());
        view.setBackgroundColor(getResources().getColor(R.color.battle, null));
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5));
        charactersBox.addView(view);
      }
      i++;
    }
  }

  public boolean isReady(Battle.Combatant combatant) {
    if (combatant.isMonster()) {
      return true;
    }

    for (Character character : Characters.get().getCharacters(campaign.getCampaignId())) {
      if (character.getName().equals(combatant.getName())) {
        return character.hasInitiative();
      }
    }

    return true;
  }
}
