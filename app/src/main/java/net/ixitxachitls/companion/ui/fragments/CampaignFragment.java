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
import android.support.design.widget.FloatingActionButton;
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
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/** A fragment displaying campaign information. */
public class CampaignFragment extends CompanionFragment {

  private static final String TAG = "CampaignFragment";

  private Campaign campaign = Campaigns.defaultCampaign;

  // UI elements.
  private CampaignTitleView title;
  private TextWrapper<TextView> date;
  private Wrapper<FloatingActionButton> delete;
  private Wrapper<FloatingActionButton> publish;
  private Wrapper<FloatingActionButton> unpublish;
  private Wrapper<FloatingActionButton> edit;
  private Wrapper<FloatingActionButton> calendar;

  public CampaignFragment() {
    super(Type.campaign);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_campaign, container, false);

    Wrapper.<ImageView>wrap(view, R.id.back)
        .onClick(this::goBack)
        .description("Back", "Go back to the list of campaigns.");
    title = view.findViewById(R.id.title);
    title.setAction(this::edit);
    delete = Wrapper.<FloatingActionButton>wrap(view, R.id.delete)
        .onClick(this::deleteCampaign)
        .description("Delete", "Delete this campaign. This action cannot be undone and will send "
            + "a deletion request to players to delete this campaign on their devices too. "
            + "You cannot delete a campaign that is currently published.");
    publish = Wrapper.<FloatingActionButton>wrap(view, R.id.publish)
        .onClick(this::publish)
        .description("Publish", "Publish this campaign on the local WiFi. Players on the same WiFi "
            + "can add characters to the campaign and generally interact with the campaign while "
            + "it is published.");
    unpublish = Wrapper.<FloatingActionButton>wrap(view, R.id.unpublish)
        .onClick(this::unpublish)
        .description("Unpublish", "Remove the campaign from the local WiFi. The campaign will "
            + "become unavailable to players, but they can still make changes to their characters. "
            + "These changes will not be propagated to you or to other players, though.");
    edit = Wrapper.<FloatingActionButton>wrap(view, R.id.edit)
        .onClick(this::edit)
        .description("Edit", "Change the basic information of the campaign.");
    calendar = Wrapper.<FloatingActionButton>wrap(view, R.id.calendar)
        .onClick(this::editDate)
        .description("Calendar", "Open the calendar for the campaign to allow you to change the "
            + "current date and time of your campaign.");
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

      delete.visible(canDeleteCampaign());
      publish.visible(campaign.get().isLocal() && !campaign.get().isDefault()
          && !campaign.get().isPublished());
      unpublish.visible(campaign.get().isLocal() && campaign.get().isPublished());
      edit.visible(campaign.get().isLocal() && !campaign.get().isDefault());
      calendar.visible(campaign.get().isLocal() && !campaign.get().isDefault());
      if (campaign.get().isDefault()) {
        date.text("");
      } else {
        date.text(campaign.get().getDate().toString());
      }
    }
  }

  private void editDate() {
    if (campaign.isLocal() && !campaign.isDefault()) {
      DateDialog.newInstance(campaign.getCampaignId()).display();
    }
  }

  private void edit() {
    if (campaign.isDefault() || !campaign.isLocal()) {
      return;
    }

    EditCampaignDialog.newInstance(campaign.getCampaignId()).display();
  }

  private void publish() {
    if (campaign.isDefault() || !campaign.isLocal()) {
      return;
    }

    campaign.publish();
  }

  private void unpublish() {
    if (campaign.isDefault() || !campaign.isLocal()) {
      return;
    }

    campaign.unpublish();
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

  private boolean canDeleteCampaign() {
    if (campaign.isDefault()) {
      return false;
    }

    if (campaign.isLocal()) {
      return !campaign.isPublished();
    }

    return !Characters.hasLocalCharacterForCampaign(campaign.getCampaignId());
  }

  @Override
  public boolean goBack() {
    CompanionFragments.get().show(Type.campaigns, Optional.of(title));
    return true;
  }
}
