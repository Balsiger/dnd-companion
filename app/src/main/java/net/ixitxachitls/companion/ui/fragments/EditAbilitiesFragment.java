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
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.View;

import com.google.common.base.Preconditions;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.ui.views.EditAbility;

/**
 * Dialog fragment to edit the abilities of a character or monster.
 */
public class EditAbilitiesFragment extends EditFragment {

  private static final String ARG_ID = "id";
  private static final String ARG_CAMPAIGN_ID = "campaign_id";

  // Ui elements.
  private EditAbility strength;
  private EditAbility constitution;
  private EditAbility dexterity;
  private EditAbility intelligence;
  private EditAbility wisdom;
  private EditAbility charisma;

  private Campaign campaign;
  private Character character;

  public EditAbilitiesFragment() {}

  public static EditAbilitiesFragment newInstance(String characterId, String campaignId) {
    EditAbilitiesFragment fragment = new EditAbilitiesFragment();
    fragment.setArguments(arguments(R.layout.fragment_edit_abilities,
        R.string.edit_abilities, R.color.character, characterId, campaignId));
    return fragment;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String characterId, String campaignId) {
    Bundle arguments = EditFragment.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, characterId);
    arguments.putString(ARG_CAMPAIGN_ID, campaignId);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    campaign = Campaigns.get().getCampaign(getArguments().getString(ARG_CAMPAIGN_ID));
    character = Characters.get().getCharacter(getArguments().getString(ARG_ID),
        campaign.getCampaignId());
  }

  @Override
  protected void createContent(View view) {
    strength = (EditAbility) view.findViewById(R.id.strength);
    strength.setOnChange(this::change);
    dexterity = (EditAbility) view.findViewById(R.id.dexterity);
    dexterity.setOnChange(this::change);
    constitution = (EditAbility) view.findViewById(R.id.constitution);
    constitution.setOnChange(this::change);
    intelligence = (EditAbility) view.findViewById(R.id.intelligence);
    intelligence.setOnChange(this::change);
    wisdom = (EditAbility) view.findViewById(R.id.wisdom);
    wisdom.setOnChange(this::change);
    charisma = (EditAbility) view.findViewById(R.id.charisma);
    charisma.setOnChange(this::change);

    update();
  }

  private void change() {
    character.setStrength(strength.getValue());
    character.setDexterity(dexterity.getValue());
    character.setConstitution(constitution.getValue());
    character.setIntelligence(intelligence.getValue());
    character.setWisdom(wisdom.getValue());
    character.setCharisma(charisma.getValue());
  }

  protected void update() {
    strength.setValue(character.getStrength());
    dexterity.setValue(character.getDexterity());
    constitution.setValue(character.getConstitution());
    intelligence.setValue(character.getIntelligence());
    wisdom.setValue(character.getWisdom());
    charisma.setValue(character.getCharisma());
  }
}
