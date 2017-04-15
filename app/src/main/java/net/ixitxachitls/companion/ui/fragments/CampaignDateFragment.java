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

import net.ixitachitls.companion.R;

/**
 * Fragment for editing a campaign date.
 */
public class CampaignDateFragment extends EditFragment {

  private static final String ARG_ID = "id";

  public CampaignDateFragment() {}

  public static CampaignDateFragment newInstance(String campaignId) {
    CampaignDateFragment fragment = new CampaignDateFragment();
    fragment.setArguments(arguments(R.layout.fragment_campaign_date,
        R.string.edit_campaign_date, R.color.campaign, campaignId));
    return fragment;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String campaignId) {
    Bundle arguments = EditFragment.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, campaignId);
    return arguments;
  }

  /*
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    campaign = Campaigns.get().getCampaign(getArguments().getString(ARG_CAMPAIGN_ID));
    character = Characters.get().getCharacter(getArguments().getString(ARG_ID),
        campaign.getCampaignId());
  }

*/
  @Override
  protected void createContent(View view) {
  /*
    strength = (EditAbility) view.findViewById(R.id.strength);
    constitution = (EditAbility) view.findViewById(R.id.constitution);
    dexterity = (EditAbility) view.findViewById(R.id.dexterity);
    intelligence = (EditAbility) view.findViewById(R.id.intelligence);
    wisdom = (EditAbility) view.findViewById(R.id.wisdom);
    charisma = (EditAbility) view.findViewById(R.id.charisma);

    strength.setValue(6);
    constitution.setValue(7);
    dexterity.setValue(8);
    intelligence.setValue(9);
    wisdom.setValue(10);
    charisma.setValue(11);

    update();
    */
  }
  /*

  protected void update() {
    update(strength);
    update(dexterity);
    update(constitution);
    update(intelligence);
    update(wisdom);
    update(charisma);
  }

  private void update(EditAbility ability) {
  }

  @Override
  protected void save() {
    if (saveAction.isPresent()) {
      saveAction.get().save(character);
    }

    super.save();
  }
*/
}
