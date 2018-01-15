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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.ui.ConfirmationDialog;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.DateDialog;
import net.ixitxachitls.companion.ui.dialogs.EditCampaignDialog;
import net.ixitxachitls.companion.ui.views.CampaignTitleView;
import net.ixitxachitls.companion.ui.views.IconView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/** A fragment displaying campaign information. */
public class CampaignFragment extends CompanionFragment {

  private static final String TAG = "CampaignFragment";

  private Campaign campaign = Campaigns.defaultCampaign;

  // UI elements.
  private CampaignTitleView title;
  private IconView delete;
  private TextWrapper<TextView> date;

  public CampaignFragment() {
    super(Type.campaign);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_campaign, container, false);

    Wrapper.<ImageView>wrap(view, R.id.back).onClick(this::back);
    title = view.findViewById(R.id.title);
    title.setAction(this::edit);
    delete = view.findViewById(R.id.delete);
    delete.setAction(this::deleteCampaign);
    date = TextWrapper.wrap(view, R.id.date);
    date.onClick(this::editDate);

    Campaigns.getCurrentCampaignId().observe(this, this::showCampaign);

    return view;
  }

  public void showCampaign(String campaignId) {
    Log.d(TAG, "setting campaign " + campaignId);

    Campaigns.getCampaign(campaign.getCampaignId()).removeObserver(this::update);
    Campaigns.getCampaign(campaignId).observe(this, this::update);
  }

  public void update(Optional<Campaign> campaign) {
    if (campaign.isPresent()) {
      this.campaign = campaign.get();
      title.setCampaign(campaign.get());

      refresh();
    }
  }

  private void back() {
    CompanionFragments.get().show(Type.campaigns, Optional.absent());
  }

  private void editDate() {
    if (campaign.isLocal()) {
      DateDialog.newInstance(campaign.getCampaignId()).display();
    }
  }

  private void edit() {
    if (campaign.isDefault() || !campaign.isLocal()) {
      return;
    }

    EditCampaignDialog.newInstance(campaign.getCampaignId()).display();
  }

  protected void deleteCampaign() {
    ConfirmationDialog.create(getContext())
        .title(getResources().getString(R.string.campaign_delete_title))
        .message(getResources().getString(R.string.campaign_delete_message))
        .yes(this::deleteCampaignOk)
        .show();
  }

  private void deleteCampaignOk() {
    campaign.delete();
    Toast.makeText(getActivity(), getString(R.string.campaign_deleted),
        Toast.LENGTH_SHORT).show();
    show(Type.campaigns);
  }

  @Override
  public void refresh() {
    Log.d(TAG, "refreshing campaign fragment");
    super.refresh();

    if (canDeleteCampaign()) {
      delete.setVisibility(View.VISIBLE);
    } else {
      delete.setVisibility(View.GONE);
    }

    date.text(campaign.getDate().toString());
  }

  private boolean canDeleteCampaign() {
    if (campaign.isDefault()) {
      return false;
    }

    if (campaign.isLocal()) {
      return !campaign.isPublished();
    }

    return !Characters.hasLocalCharacterForCampaign(campaign.getCampaignId());
  }
}
