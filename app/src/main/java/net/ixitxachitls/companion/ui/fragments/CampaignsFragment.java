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

package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.FSCampaign;
import net.ixitxachitls.companion.data.documents.FSCampaigns;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.EditCampaignDialog;
import net.ixitxachitls.companion.ui.views.CampaignTitleView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

/**
 * The fragment displaying the list of campaigns and orphaned characters.
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


    fsCampaigns().observe(this, this::update);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

    update(fsCampaigns());
  }

  private void addCampaign() {
    EditCampaignDialog.newInstance().display();
  }

  private void update(FSCampaigns campaigns) {
    campaignsView.get().removeAllViews();

    // We have to recreate the campaigns as the transition away from this fragment seems to break
    // them.
    TransitionManager.beginDelayedTransition(campaignsView.get());
    for (FSCampaign campaign : campaigns.getCampaigns()) {
      CampaignTitleView title = new CampaignTitleView(getContext());
      campaign.observe(this, title::update);
      campaign.getDm().observe(this, title::update);
      title.update(campaign);
      campaignsView.get().addView(title);
      title.setAction(() -> {
        CompanionFragments.get().showCampaign(campaign, Optional.of(title));
      });
    }
  }

  @Override
  public boolean goBack() {
    return false;
  }
}
