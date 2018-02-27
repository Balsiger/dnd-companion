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

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.EditCampaignDialog;
import net.ixitxachitls.companion.ui.views.CampaignTitleView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Collections;
import java.util.List;

/**
 * The fragment displaying the campaign list
 */
public class CampaignsFragment extends CompanionFragment {

  private Wrapper<LinearLayout> campaignsView;

  public CampaignsFragment() {
    super(Type.campaigns);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_campaigns, container, false);

    campaignsView = Wrapper.wrap(view, R.id.campaigns);
    Wrapper.wrap(view, R.id.campaign_add)
        .onClick(this::addCampaign)
        .description("Add Campaign", "This button allows you to create a new campaign. "
            + "You will be the Dungeon Master of the campaign.");


    Campaigns.getAllCampaignIds().observe(this, this::update);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

    update(Campaigns.getAllCampaignIds().getValue());
  }


  private void addCampaign() {
    EditCampaignDialog.newInstance().display();
  }

  private void update(ImmutableList<String> campaignIds) {
    campaignsView.get().removeAllViews();

    // Sort all the campaigns. We have to recreate the campaigns as the transition away
    // from this fragment seems to break them.
    TransitionManager.beginDelayedTransition(campaignsView.get());
    campaignsView.get().removeAllViews();
    List<Campaign> campaigns = Campaigns.getAllCampaigns();
    Collections.sort(campaigns);
    for (Campaign campaign : campaigns) {
      LiveData<Optional<Campaign>> liveCampaign = Campaigns.getCampaign(campaign.getCampaignId());
      CampaignTitleView title = new CampaignTitleView(getContext());
      liveCampaign.removeObservers(this);
      liveCampaign.observe(this, title::update);
      campaignsView.get().addView(title);
      title.setAction(() -> {
        CompanionFragments.get().showCampaign(campaign, Optional.of(title));
      });
      if (campaign.getCampaignId().equals(Campaigns.getCurrentCampaignId().getValue())) {
        title.setTransitionName("sharedMove");
      }
    }
  }

  @Override
  public boolean goBack() {
    return false;
  }
}
