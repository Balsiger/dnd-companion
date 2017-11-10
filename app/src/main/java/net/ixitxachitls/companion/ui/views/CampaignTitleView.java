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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.ui.CampaignPublisher;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * View for a campaign title.
 */
public class CampaignTitleView extends LinearLayout {

  private Campaign campaign;

  // Ui elements.
  private TitleView title;
  private NetworkIcon networkIcon;
  private TextView dm;

  public CampaignTitleView(Context context, Campaign campaign) {
    super(context);

    setup();
    setCampaign(campaign);
  }

  public CampaignTitleView(Context context, AttributeSet attributes) {
    super(context, attributes);

    setup();
  }

  private void setup() {
    RelativeLayout view = (RelativeLayout)
        LayoutInflater.from(getContext()).inflate(R.layout.view_campaign_title, null, false);
    // Adding the margin in the xml layout does not work.
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 0, 0, 2);
    view.setLayoutParams(params);

    title = (TitleView) view.findViewById(R.id.title);
    networkIcon = (NetworkIcon) view.findViewById(R.id.network);
    dm = (TextView) view.findViewById(R.id.dm);

    addView(view);
  }

  public void setCampaign(Campaign campaign) {
    this.campaign = campaign;
    refresh();
  }

  private String subtitle(Campaign campaign) {
    if (campaign.isDefault()) {
      return "Playground and orphaned characters";
    }

    return campaign.getWorld() + ", " + campaign.getDm();
  }

  public void setAction(Wrapper.Action action) {
    title.setAction(action);
  }

  private void publish() {
    if (campaign.isDefault() || !campaign.isLocal()) {
      return;
    }

    CampaignPublisher.toggle(getContext(), campaign, this::refresh,
        CampaignPublisher.EmptyCancelAction);
  }

  public void refresh() {
    campaign = campaign.refresh();

    if (campaign.isDefault()) {
      networkIcon.setVisibility(INVISIBLE);
    }

    if (!campaign.isLocal() || campaign.isDefault()) {
      dm.setVisibility(INVISIBLE);
    }

    title.setTitle(campaign.getName());
    title.setSubtitle(subtitle(campaign));

    networkIcon.setStatus(campaign.isLocal(), campaign.isOnline());
    if (campaign.isLocal() && !campaign.isDefault()) {
      networkIcon.setAction(this::publish);
    }
  }
}
