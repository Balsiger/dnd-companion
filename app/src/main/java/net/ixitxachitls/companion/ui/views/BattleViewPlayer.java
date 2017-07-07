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

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.StoredEntries;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.ui.dialogs.TimedConditionDialog;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * Battle controls for players.
 */
public class BattleViewPlayer extends LinearLayout {
  private final ViewGroup view;
  private final PartyView party;
  private final Wrapper<Button> timed;
  private final Wrapper<View> timedDelimiter;

  private Optional<Campaign> campaign;

  public BattleViewPlayer(Context context, PartyView party) {
    super(context);
    this.party = party;

    view = (ViewGroup)
        LayoutInflater.from(context).inflate(R.layout.view_battle_player, party, false);
    setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));

    timed = Wrapper.<Button>wrap(view, R.id.timed).onClick(this::addTimed);
    timedDelimiter = Wrapper.wrap(view, R.id.delimiter_timed);

    addView(view);
  }

  public void setCampaign(Optional<Campaign> campaign) {
    if (campaign.isPresent() && !campaign.get().isLocal()) {
      this.campaign = campaign;
    } else {
      this.campaign = Optional.absent();
    }

    refresh();
  }

  private void addTimed() {
    if (currentCharacterIsLocal()) {
      TimedConditionDialog.newInstance(campaign.get().getBattle().getCurrentCombatant().getId())
          .display();
    }
  }

  private boolean inBattle() {
    return campaign.isPresent() && !campaign.get().getBattle().isEnded();
  }

  private boolean currentCharacterIsLocal() {
    if (!campaign.isPresent() || !inBattle()) {
      return false;
    }

    Battle.Combatant current = campaign.get().getBattle().getCurrentCombatant();
    return !current.isMonster() && StoredEntries.isLocalId(current.getId());
  }

  public void refresh() {
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
    if (campaign.isPresent() && inBattle()) {
      params.removeRule(RelativeLayout.ALIGN_BOTTOM);
      params.addRule(RelativeLayout.BELOW, R.id.scroll);
      timed.visible(currentCharacterIsLocal());
      timedDelimiter.visible(currentCharacterIsLocal());
    } else {
      params.removeRule(RelativeLayout.BELOW);
      params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.scroll);
    }
  }
}
