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
import android.util.Log;
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
import net.ixitxachitls.companion.util.Misc;

import java.util.HashMap;
import java.util.Map;

/**
 * The fragment displaying the campaign list
 */
public class CampaignsFragment extends CompanionFragment {

  private Wrapper<LinearLayout> campaignsView;
  private Map<String, CampaignTitleView> titlesByCampaignId = new HashMap<>();

  public CampaignsFragment() {
    super(Type.campaigns);

    Campaigns.getAllCampaignIds().observe(this, this::update);
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
    EditCampaignDialog.newInstance().display();
  }

  private void update(ImmutableList<String> campaignIds) {
    if (campaignsView != null) {
      for (Map.Entry<String, CampaignTitleView> entry : titlesByCampaignId.entrySet()) {
        Campaigns.getCampaign(entry.getKey()).removeObserver(entry.getValue()::setLiveCampaign);
      }

      titlesByCampaignId.clear();
      campaignsView.get().removeAllViews();

      // TODO(merlin): This could be optmizied by only recreating new campaigns and
      // removing old ones instead of always recreating all.
      for (String campaignId : campaignIds) {
        Campaign campaign = Campaigns.getCampaign(campaignId).getValue().get();
        CampaignTitleView title = new CampaignTitleView(getContext());
        titlesByCampaignId.put(campaignId, title);
        Campaigns.getCampaign(campaignId).observe(this, title::setLiveCampaign);
        title.setAction(() -> {
          if (Misc.onEmulator() && !campaign.isLocal()) {
            Misc.emulateRemote();
          } else {
            Misc.emulateLocal();
          }
          Log.d("TAG", campaignId);
          CompanionFragments.get().showCampaign(campaign, Optional.of(title));
        });
        campaignsView.get().addView(title);
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    update(Campaigns.getAllCampaignIds().getValue());
  }
}
