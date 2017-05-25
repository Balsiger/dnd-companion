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

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.ui.CampaignPublisher;
import net.ixitxachitls.companion.ui.ListAdapter;
import net.ixitxachitls.companion.ui.Setup;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.EditCampaignDialog;
import net.ixitxachitls.companion.ui.views.NetworkIcon;
import net.ixitxachitls.companion.ui.views.TitleView;

import java.util.ArrayList;
import java.util.List;

/**
 * The fragment displaying the campaign list
 */
public class CampaignsFragment extends CompanionFragment {

  private List<Campaign> campaigns = new ArrayList<>();
  private ListAdapter<Campaign> campaignsAdapter;

  public CampaignsFragment() {
    super(Type.campaigns);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_campaign_list, container, false);
    campaignsAdapter =
        new ListAdapter<>(container.getContext(), R.layout.list_item_campaign, campaigns,
            new ListAdapter.ViewBinder<Campaign>() {
              @Override
              public void bind(View view, Campaign campaign, int position) {
                TitleView title = (TitleView) view.findViewById(R.id.title);
                title.setTitle(campaign.getName());
                title.setSubtitle(subtitle(campaign));

                NetworkIcon networkIcon = (NetworkIcon) view.findViewById(R.id.network);
                networkIcon.setLocation(campaign.isLocal());
                if (!campaign.isDefault()) {
                  networkIcon.setStatus(campaign.isPublished());
                }
                if (campaign.isLocal() && !campaign.isDefault()) {
                  networkIcon.setAction(() -> publishCampaign(campaign));
                }
              }
            });
    Setup.listView(view, R.id.campaignsList, campaignsAdapter,
        (i) -> CompanionFragments.get().showCampaign(campaigns.get(i)));

    Setup.floatingButton(view, R.id.campaign_add, this::addCampaign);

    return view;
  }

  private void addCampaign() {
    EditCampaignDialog.newInstance().display(getFragmentManager());
  }

  private void publishCampaign(Campaign campaign) {
    if (campaign.isDefault() || !campaign.isLocal()) {
      return;
    }

    CampaignPublisher.toggle(getContext(), campaign, this::refresh,
        CampaignPublisher.EmptyCancelAction);
  }

  private String subtitle(Campaign campaign) {
    if (campaign.getDate().isEmpty()) {
      return campaign.getWorld();
    }

    return campaign.getWorld() + ", " + campaign.getDate();
  }

  @Override
  public void refresh() {
    super.refresh();

    if (campaignsAdapter != null) {
      campaigns.clear();
      campaigns.addAll(Campaigns.local().getCampaigns());
      campaigns.addAll(Campaigns.remote().getCampaigns());
      campaignsAdapter.notifyDataSetChanged();
    }
  }
}
