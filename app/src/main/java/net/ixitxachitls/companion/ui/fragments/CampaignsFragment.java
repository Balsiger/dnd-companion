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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Campaign;
import net.ixitxachitls.companion.data.Campaigns;
import net.ixitxachitls.companion.ui.CampaignPublisher;
import net.ixitxachitls.companion.ui.ListAdapter;
import net.ixitxachitls.companion.ui.Setup;

import java.util.ArrayList;
import java.util.List;

/**
 * The fragment displaying the campaign list
 */
public class CampaignsFragment extends CompanionFragment {

  private ListAdapter<Campaign> campaignsAdapter;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_campaign_list, container, false);
    // Copy the campaign list to prevent unexpected changes in the list.
    List<Campaign> campaigns = new ArrayList<>(Campaigns.get().getCampaigns());
    campaignsAdapter =
        new ListAdapter<>(container.getContext(), R.layout.list_item_campaign, campaigns,
            new ListAdapter.ViewBinder<Campaign>() {
              @Override
              public void bind(View view, Campaign campaign, int position) {
                ((TextView) view.findViewById(R.id.name)).setText(campaign.getName());
                ((TextView) view.findViewById(R.id.world)).setText(campaign.getWorld());

                ImageView local = Setup.imageView(view, R.id.local);
                Setup.imageView(view, R.id.local,
                    () -> publishCampaign(local, campaign));
                ImageView remote = Setup.imageView(view, R.id.remote);
                if (campaign.isLocal()) {
                  local.setVisibility(View.VISIBLE);
                  remote.setVisibility(View.INVISIBLE);

                  if (!campaign.isDefault()) {
                    local.setColorFilter(getResources().getColor(
                        campaign.isPublished() ? R.color.on : R.color.off, null));
                    if (campaign.isPublished()) {
                      campaign.publish();
                    }
                  }
                } else {
                  view.findViewById(R.id.local).setVisibility(View.INVISIBLE);
                  view.findViewById(R.id.remote).setVisibility(View.VISIBLE);
                }
              }
            });
    Setup.listView(view, R.id.campaignsList, campaignsAdapter,
        (i) -> showCampaign(campaigns.get(i)));

    Setup.floatingButton(view, R.id.campaign_add, this::addCampaign);

    return view;
  }

  private void addCampaign() {
    EditCampaignFragment.newInstance().display(getFragmentManager());
  }

  private void publishCampaign(ImageView view, Campaign campaign) {
    if (campaign.isDefault()) {
      return;
    }

    view.setColorFilter(getResources().getColor(
        campaign.isPublished() ? R.color.off : R.color.on, null));
    CampaignPublisher.toggle(getContext(), campaign,
        () -> view.setColorFilter(getResources().getColor(
            campaign.isPublished() ? R.color.off : R.color.on, null)));
  }

  @Override
  public void onResume() {
    super.onResume();

    refresh();
  }

  @Override
  public void refresh() {
    if (campaignsAdapter != null) {
      campaignsAdapter.clear();
      campaignsAdapter.addAll(Campaigns.get().getCampaigns());
      campaignsAdapter.notifyDataSetChanged();
    }
  }
}
