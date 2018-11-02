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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.values.Encounter;
import net.ixitxachitls.companion.ui.dialogs.MonsterInitiativeDialog;
import net.ixitxachitls.companion.ui.dialogs.TimedConditionDialog;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

/**
 * View representing battle information (and buttons) for a party view.
 */
public class BattleView extends LinearLayout {

  private Optional<Campaign> campaign = Optional.empty();

  private final Wrapper<FloatingActionButton> add;
  private final Wrapper<FloatingActionButton> next;
  private final Wrapper<FloatingActionButton> delay;
  private final Wrapper<FloatingActionButton> stop;
  private final Wrapper<FloatingActionButton> timed;

  public BattleView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.view_battle, this, false);

    add = Wrapper.<FloatingActionButton>wrap(view, R.id.add)
        .onClick(this::addMonster)
        .description("Add Monster", "Add a monster to the initiative order. You select the "
            + "monsters initiative bonus and the exact initiative will be randomly computed. If "
            + "you want a single initiative for all monsters, simply use a single monster as a "
            + "placeholder.");
    next = Wrapper.<FloatingActionButton>wrap(view, R.id.next)
        .onClick(this::next)
        .description("Next Combatant", "Switch to the next combatant. This ends the current "
            + "combatants round and starts the round for the next combatant. When ending the last "
            + "combatants round, the turn is ended and the next one starts again with the first "
            + "combatant.");
    delay = Wrapper.<FloatingActionButton>wrap(view, R.id.delay)
        .onClick(this::delay)
    .description("Delay Combatant", "Delay the current combatants round. The combatant will be "
        + "moved after the next combatant. Note that you should not allow a combatant to delay its "
        + "round when all other combatans are either done or also delaying.");
    timed = Wrapper.<FloatingActionButton>wrap(view, R.id.timed)
        .onClick(this::addTimed)
        .description("Time Condition", "Create a timed condition for the current combatant. The "
            + "condition can apply to other characters as well, but has to originate from the "
            + "current combatant. Conditions are automatically tracked until their duration runs "
            + "out.");
    stop = Wrapper.<FloatingActionButton>wrap(view, R.id.stop)
        .onClick(this::stopBattle)
        .description("Stop Battle", "Stop the battle completely. Note that you cannot restart a "
            + "battle but instead will have to start a completely new battle.");

    addView(view);
  }

  public void update(Optional<Campaign> campaign) {
    this.campaign = campaign;

    Status.log("refreshing battle view with " + campaign);
    Encounter encounter = campaign.get().getEncounter();
    if (inBattle()) {
      boolean currentCreatureIsLocal;
      String currentCreatureId = encounter.getCurrentCreatureId();
      if (currentCreatureId.startsWith("creature")) {
        currentCreatureIsLocal = true;
      } else {
        Optional<Character> character = CompanionApplication.get(getContext())
            .characters().get(currentCreatureId);
        currentCreatureIsLocal = character.isPresent() && character.get().amPlayer();
      }

      setVisibility(VISIBLE);
      boolean isDM = campaign.get().amDM();
      add.visible(isDM && inBattle());
      next.visible(isDM && (encounter.isSurprised() || encounter.isOngoing()));
      boolean canDelay = isDM && encounter.isOngoing()
          && !encounter.currentIsLast();
      delay.visible(canDelay);
      stop.visible(isDM && !encounter.isEnded());
      timed.visible(currentCreatureIsLocal && encounter.isOngoingOrSurprised());
    } else {
      add.gone();
      next.gone();
      delay.gone();
      stop.gone();
      timed.gone();
    }
  }

  private boolean inBattle() {
    return !campaign.get().getEncounter().isEnded();
  }

  private void addMonster() {
    if (inBattle()) {
      MonsterInitiativeDialog.newInstance(campaign.get().getId(), -1).display();
    }
  }

  private void next() {
    if (inBattle()) {
      campaign.get().getEncounter().creatureDone();
    }
  }

  private void delay() {
    if (inBattle()) {
      campaign.get().getEncounter().creatureWait();
    }
  }

  private void stopBattle() {
    if (inBattle()) {
      campaign.get().getEncounter().end();
    }
  }

  private void addTimed() {
    if (inBattle()) {
      TimedConditionDialog.newInstance(
          campaign.get().getEncounter().getCurrentCreatureId(),
          campaign.get().getEncounter().getTurn())
          .display();
    }
  }
}
