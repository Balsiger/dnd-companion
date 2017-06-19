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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.google.common.base.Optional;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;

import java.util.ArrayList;
import java.util.List;

/**
 * View representing a whole party.
 */
public class PartyView extends LinearLayout {
  private final List<Character> characters = new ArrayList<>();
  private final LinearLayout party;

  private Optional<Campaign> campaign = Optional.absent();

  public PartyView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_party, null, false);
    view.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));

    party = (LinearLayout) view.findViewById(R.id.party);

    addView(view);
  }

  public void setCampaign(Optional<Campaign> campaign) {
    this.campaign = campaign;
    refresh();
  }

  public void refresh() {
    characters.clear();

    if (campaign.isPresent()) {
      if (campaign.get().isDefault()) {
        characters.addAll(Characters.local().getOrphanedCharacters());
      } else if (campaign.get().isLocal()) {
        characters.addAll(Characters.remote().getCharacters(campaign.get().getCampaignId()));
      } else {
        characters.addAll(Characters.local().getCharacters(campaign.get().getCampaignId()));
        characters.addAll(Characters.remote().getCharacters(campaign.get().getCampaignId()));
      }
    }

    party.removeAllViews();
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
}
