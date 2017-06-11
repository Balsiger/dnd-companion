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
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.common.base.Optional;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.ui.CampaignPublisher;
import net.ixitxachitls.companion.ui.Setup;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;

/**
 * View for a campaign title.
 */
public class CampaignTitleView extends LinearLayout {

  private Campaign campaign;
  private final TitleView title;
  private final NetworkIcon networkIcon;

  public CampaignTitleView(Context context, Campaign campaign) {
    super(context);
    this.campaign = campaign;

    RelativeLayout view = (RelativeLayout)
        LayoutInflater.from(getContext()).inflate(R.layout.view_campaign_title, null, false);
    // Adding the margin in the xml layout does not work.
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 0, 0, 2);
    view.setLayoutParams(params);
    title = Setup.view(view, R.id.title);
    title.setAction(this::select);
    networkIcon = Setup.view(view, R.id.network);
    if (campaign.isDefault()) {
      networkIcon.setVisibility(INVISIBLE);
    }

    if (!campaign.isLocal() || campaign.isDefault()) {
      view.findViewById(R.id.dm).setVisibility(GONE);
    }

    addView(view);

    refresh();
  }

  private String subtitle(Campaign campaign) {
    if (campaign.isDefault()) {
      return "";
    }

    if (campaign.getDate().isEmpty()) {
      return campaign.getWorld();
    }

    return campaign.getWorld() + ", " + campaign.getDate();
  }

  private void select() {
    CompanionFragments.get().showCampaign(campaign, Optional.of(this));
  }

  private void publish() {
    if (campaign.isDefault() || !campaign.isLocal()) {
      return;
    }

    CampaignPublisher.toggle(getContext(), campaign, this::refresh,
        CampaignPublisher.EmptyCancelAction);
  }

  public void refresh() {
    campaign = campaign.refresh().or(campaign);

    title.setTitle(campaign.getName());
    title.setSubtitle(subtitle(campaign));

    networkIcon.setStatus(campaign.isLocal(), campaign.isOnline());
    if (campaign.isLocal() && !campaign.isDefault()) {
      networkIcon.setAction(this::publish);
    }
  }
}
