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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.StoredEntries;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.ui.dialogs.MonsterInitiativeDialog;
import net.ixitxachitls.companion.ui.dialogs.TimedConditionDialog;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * View representing battle information (and buttons) for a party view.
 */
public class BattleView extends LinearLayout {

  private final PartyView party;
  private Optional<Campaign> campaign;

  private final ViewGroup view;
  private final Wrapper<Button> add;
  private final Wrapper<Button> next;
  private final Wrapper<Button> delay;
  private final Wrapper<Button> stop;
  private final Wrapper<Button> timed;
  private final Wrapper<View> addDelimiter;
  private final Wrapper<View> nextDelimiter;
  private final Wrapper<View> delayDelimiter;
  private final Wrapper<View> stopDelimiter;
  private final Wrapper<View> timedDelimiter;

  public BattleView(Context context, PartyView party) {
    super(context);

    this.party = party;

    view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.view_battle, party, false);
    setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));

    add = Wrapper.<Button>wrap(view, R.id.add).onClick(this::addMonster);
    addDelimiter = Wrapper.wrap(view, R.id.delimiter_add);
    next = Wrapper.<Button>wrap(view, R.id.next).onClick(this::nextCombatant);
    nextDelimiter = Wrapper.wrap(view, R.id.delimiter_next);
    delay = Wrapper.<Button>wrap(view, R.id.delay).onClick(this::delay);
    delayDelimiter = Wrapper.wrap(view, R.id.delimiter_delay);
    stop = Wrapper.<Button>wrap(view, R.id.stop).onClick(this::stopBattle);
    stopDelimiter = Wrapper.wrap(view, R.id.delimiter_stop);
    timed = Wrapper.<Button>wrap(view, R.id.timed).onClick(this::addTimed);
    timedDelimiter = Wrapper.wrap(view, R.id.delimiter_timed);

    addView(view);
  }

  public void setCampaign(Optional<Campaign> campaign) {
    if (campaign.isPresent()) {
      this.campaign = campaign;
    } else {
      this.campaign = Optional.absent();
    }

    refresh();
  }

  private boolean inBattle() {
    return campaign.isPresent() && !campaign.get().getBattle().isEnded();
  }

  private void addMonster() {
    if (inBattle()) {
      MonsterInitiativeDialog.newInstance(campaign.get().getCampaignId(), -1).display();
    }
  }

  private void nextCombatant() {
    if (inBattle()) {
      campaign.get().getBattle().combatantDone();
      party.refreshWithTransition();
    }
  }

  private void delay() {
    if (inBattle()) {
      campaign.get().getBattle().combatantLater();
      party.refreshWithTransition();
    }
  }

  private void stopBattle() {
    if (inBattle()) {
      campaign.get().getBattle().end();
      party.refreshWithTransition();
    }
  }

  private void addTimed() {
    if (currentCharacterIsLocal()) {
      TimedConditionDialog.newInstance(campaign.get().getBattle().getCurrentCombatant().getId(),
          campaign.get().getBattle().getTurn())
          .display();
    }
  }

  public void refresh() {
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
    if (inBattle()) {
      Battle battle = campaign.get().getBattle();
      params.removeRule(RelativeLayout.ALIGN_BOTTOM);
      params.addRule(RelativeLayout.BELOW, R.id.scroll);
      boolean isDM = campaign.get().isDefault() || campaign.get().isLocal();
      add.visible(isDM && battle.isStarting());
      addDelimiter.visible(isDM && battle.isStarting());
      next.visible(isDM && (battle.isSurprised() || battle.isOngoing()));
      nextDelimiter.visible(isDM && (battle.isSurprised() || battle.isOngoing()));
      boolean canDelay = isDM && battle.isOngoing()
          && !battle.currentIsLast() && !battle.currentIsWaiting();
      delay.visible(canDelay);
      delayDelimiter.visible(canDelay);
      stop.visible(isDM && !battle.isEnded());
      stopDelimiter.visible(isDM && battle.isSurprised() || battle.isOngoing());
      timed.visible(currentCharacterIsLocal());
      timedDelimiter.visible(currentCharacterIsLocal());
    } else {
      params.removeRule(RelativeLayout.BELOW);
      params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.scroll);
    }
  }


  private boolean currentCharacterIsLocal() {
    if (!campaign.isPresent() || !inBattle()) {
      return false;
    }

    Battle.Combatant current = campaign.get().getBattle().getCurrentCombatant();
    return !current.isMonster() && StoredEntries.isLocalId(current.getId());
  }
}
