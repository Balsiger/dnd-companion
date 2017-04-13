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
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Campaign;
import net.ixitxachitls.companion.data.Campaigns;
import net.ixitxachitls.companion.data.Character;
import net.ixitxachitls.companion.ui.Setup;

/**
 * Fragment for displaying character information.
 */
public class CharacterFragment extends CompanionFragment {
  private Character character;
  private Campaign campaign;

  // UI elements.
  private TextView title;
  private TextView subtitle;

  public CharacterFragment() {
    super(Type.character);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_character, container, false);

    title = Setup.textView(view, R.id.title, this::edit);
    subtitle = Setup.textView(view, R.id.subtitle, this::edit);

    return view;
  }

  public void showCharacter(Character character) {
    this.character = character;
    this.campaign = Campaigns.get().getCampaign(character.getCampaignId());

    refresh();
  }

  private void edit() {
    if (!campaign.isDefault() && campaign.isLocal()) {
      return;
    }

    EditCharacterFragment.newInstance(character.getCharacterId(), character.getCampaignId())
        .display(getFragmentManager());
  }

  @Override
  public void refresh() {
    if (character == null) {
      return;
    }

    title.setText(character.getName() + " / " + character.getCharacterId());
    subtitle.setText("not yet done");
  }
}
