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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.ui.dialogs.MonsterInitiativeDialog;
import net.ixitxachitls.companion.ui.dialogs.TimedConditionDialog;
import net.ixitxachitls.companion.ui.fragments.PartyFragment;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * View representing battle information (and buttons) for a party view.
 */
public class BattleView extends LinearLayout {

  private static final String TAG = "BattleView";

  private Campaign campaign = Campaigns.defaultCampaign;

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

  public BattleView(Context context, PartyFragment party) {
    super(context);

    view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.view_battle, (ViewGroup) party.getView(), false);
    setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));

    add = Wrapper.<Button>wrap(view, R.id.add).onClick(this::addMonster);
    addDelimiter = Wrapper.wrap(view, R.id.delimiter_add);
    next = Wrapper.<Button>wrap(view, R.id.next).onClick(this::next);
    nextDelimiter = Wrapper.wrap(view, R.id.delimiter_next);
    delay = Wrapper.<Button>wrap(view, R.id.delay).onClick(this::delay);
    delayDelimiter = Wrapper.wrap(view, R.id.delimiter_delay);
    stop = Wrapper.<Button>wrap(view, R.id.stop).onClick(this::stopBattle);
    stopDelimiter = Wrapper.wrap(view, R.id.delimiter_stop);
    timed = Wrapper.<Button>wrap(view, R.id.timed).onClick(this::addTimed);
    timedDelimiter = Wrapper.wrap(view, R.id.delimiter_timed);

    addView(view);
  }

  public void update(Campaign campaign) {
    this.campaign = campaign;

    Log.d(TAG, "refreshing battle view with " + campaign);
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
    if (inBattle()) {
      setVisibility(VISIBLE);
      Battle battle = campaign.getBattle();
      params.removeRule(RelativeLayout.ALIGN_BOTTOM);
      params.addRule(RelativeLayout.BELOW, R.id.scroll);
      boolean isDM = campaign.isDefault() || campaign.isLocal();
      add.visible(isDM && battle.isStarting());
      addDelimiter.visible(isDM && battle.isStarting());
      next.visible(isDM && (battle.isSurprised() || battle.isOngoing()));
      nextDelimiter.visible(isDM && (battle.isSurprised() || battle.isOngoing()));
      boolean canDelay = isDM && battle.isOngoing()
          && !battle.currentIsLast();
      delay.visible(canDelay);
      delayDelimiter.visible(canDelay);
      stop.visible(isDM && !battle.isEnded());
      stopDelimiter.visible(isDM && battle.isSurprised() || battle.isOngoing());
      timed.visible(battle.isOngoing());
      timedDelimiter.visible(battle.isOngoing());
    } else {
      params.removeRule(RelativeLayout.BELOW);
      params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.scroll);
    }
  }

  private boolean inBattle() {
    return !campaign.getBattle().isEnded();
  }

  private void addMonster() {
    if (inBattle()) {
      MonsterInitiativeDialog.newInstance(campaign.getCampaignId(), -1).display();
    }
  }

  private void next() {
    if (inBattle()) {
      campaign.getBattle().creatureDone();
    }
  }

  private void delay() {
    if (inBattle()) {
      campaign.getBattle().creatureWait();
    }
  }

  private void stopBattle() {
    if (inBattle()) {
      campaign.getBattle().end();
    }
  }

  private void addTimed() {
    if (inBattle()) {
      TimedConditionDialog.newInstance(campaign.getBattle().getCurrentCreatureId(),
          campaign.getBattle().getTurn())
          .display();
    }
  }
}
