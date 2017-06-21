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

import com.google.common.base.Optional;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * View representing a whole party.
 */
public class PartyView extends LinearLayout {
  private final List<Character> characters = new ArrayList<>();

  private final ViewGroup view;
  private final LinearLayout party;
  private final Wrapper<FloatingActionButton> startBattle;
  private final Wrapper<LinearLayout> actions;
  private final Wrapper<ImageButton> add;
  private final Wrapper<ImageButton> next;
  private final Wrapper<ImageButton> delay;
  private final Wrapper<ImageButton> stop;

  private Optional<Campaign> campaign = Optional.absent();

  public PartyView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    view = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.view_party, null, false);
    view.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));

    party = (LinearLayout) view.findViewById(R.id.party);
    startBattle = Wrapper.wrap(view, R.id.start_battle);
    startBattle.onClick(this::startBattle);
    actions = Wrapper.wrap(view, R.id.actions);
    add = Wrapper.wrap(view, R.id.add_monster);
    next = Wrapper.wrap(view, R.id.next);
    delay = Wrapper.wrap(view, R.id.delay);
    stop = Wrapper.wrap(view, R.id.stop);

    addView(view);
  }

  public void setCampaign(Optional<Campaign> campaign) {
    this.campaign = campaign;
    refresh();
  }

  private void startBattle() {
    if (!campaign.isPresent()) {
      return;
    }

    // Setup the ui elements.
    TransitionManager.beginDelayedTransition(view);
    startBattle.gone();
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) actions.get().getLayoutParams();
    params.removeRule(RelativeLayout.ALIGN_BOTTOM);
    params.addRule(RelativeLayout.BELOW, R.id.scroll);
    actions.visible();
  }

  public void refresh() {
    characters.clear();
    party.removeAllViews();

    if (campaign.isPresent()) {
      if (campaign.get().isDefault()) {
        characters.addAll(Characters.local().getOrphanedCharacters());
      } else if (campaign.get().isLocal()) {
        characters.addAll(Characters.remote().getCharacters(campaign.get().getCampaignId()));
      } else {
        characters.addAll(Characters.local().getCharacters(campaign.get().getCampaignId()));
        characters.addAll(Characters.remote().getCharacters(campaign.get().getCampaignId()));
      }

      for (Character character : characters) {
        CharacterChipView chip = new CharacterChipView(getContext(), character);
        chip.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
          CompanionFragments.get().showCharacter(character);
        }
        });
        party.addView(chip);
      }
    }

    startBattle.visible(campaign.isPresent() && campaign.get().isLocal());
  }
}
