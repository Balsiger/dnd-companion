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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.common.base.Optional;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.EditCampaignDialog;
import net.ixitxachitls.companion.ui.views.CampaignTitleView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * The fragment displaying the campaign list
 */
public class CampaignsFragment extends CompanionFragment {

  private Wrapper<LinearLayout> campaignsView;

  private List<Campaign> campaigns = new ArrayList<>();

  public CampaignsFragment() {
    super(Type.campaigns);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_campaigns, container, false);

    campaignsView = Wrapper.wrap(view, R.id.campaigns);
    Wrapper.wrap(view, R.id.campaign_add).onClick(this::addCampaign);

    return view;
  }

  private void addCampaign() {
    EditCampaignDialog.newInstance().display(getFragmentManager());
  }

  @Override
  public void refresh() {
    super.refresh();

    campaigns.clear();
    campaigns.add(Campaigns.defaultCampaign);
    campaigns.addAll(Campaigns.local().getCampaigns());
    campaigns.addAll(Campaigns.remote().getCampaigns());

    if (campaignsView != null) {
      campaignsView.get().removeAllViews();
      for (Campaign campaign : campaigns) {
        CampaignTitleView title = new CampaignTitleView(getContext(), campaign);
        title.setAction(() -> CompanionFragments.get().showCampaign(campaign, Optional.of(title)));
        campaignsView.get().addView(title);
      }
    }
  }
}
