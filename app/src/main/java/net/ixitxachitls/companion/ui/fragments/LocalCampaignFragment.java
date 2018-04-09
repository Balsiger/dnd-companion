/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.ui.ConfirmationDialog;
import net.ixitxachitls.companion.ui.dialogs.DateDialog;
import net.ixitxachitls.companion.ui.dialogs.EditCampaignDialog;

import java.util.Optional;

/**
 * Fragment showing a local campaign.
 */
public class LocalCampaignFragment extends CampaignFragment {

  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);

    title.setAction(this::edit);
    delete.onClick(this::deleteCampaign)
        .description("Delete", "Delete this campaign. This action cannot be undone and will send "
        + "a deletion request to players to delete this campaign on their devices too. "
        + "You cannot delete a campaign that is currently published.");
    publish.onClick(this::publish)
        .description("Publish", "Publish this campaign on the local WiFi. Players on the same WiFi "
            + "can add characters to the campaign and generally interact with the campaign while "
            + "it is published.");
    unpublish.onClick(this::unpublish)
        .description("Unpublish", "Remove the campaign from the local WiFi. The campaign will "
            + "become unavailable to players, but they can still make changes to their characters. "
            + "These changes will not be propagated to you or to other players, though.");
    edit.onClick(this::edit)
        .description("Edit", "Change the basic information of the campaign.");
    calendar.onClick(this::editDate)
        .description("Calendar", "Open the calendar for the campaign to allow you to change the "
            + "current date and time of your campaign.");
    date.onClick(this::editDate)
        .description("Calendar", "Open the calendar for the campaign to allow you to change the "
            + "current date and time of your campaign.");

    return view;
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

  private boolean canDeleteCampaign() {
    if (campaign.isDefault()) {
      return false;
    }

    if (campaign.isLocal()) {
      return !campaign.isPublished();
    }

    return !characters().hasLocalCharacterForCampaign(campaign.getCampaignId());
  }

  private void publish() {
    if (campaign.isDefault() || !campaign.isLocal()) {
      return;
    }

    campaign.asLocal().publish();
  }

  private void unpublish() {
    if (campaign.isDefault() || !campaign.isLocal()) {
      return;
    }

    campaign.asLocal().unpublish();
  }

  private void editDate() {
    if (campaign.isLocal() && !campaign.isDefault()) {
      DateDialog.newInstance(campaign.getCampaignId()).display();
    }
  }

  @Override
  protected void update(Optional<Campaign> campaign) {
    super.update(campaign);

    if (campaign.isPresent()) {
      delete.visible(canDeleteCampaign());
      publish.visible(campaign.get().isLocal() && !campaign.get().isDefault()
          && !campaign.get().isPublished());
      unpublish.visible(campaign.get().isLocal() && campaign.get().isPublished());
      edit.visible(campaign.get().isLocal() && !campaign.get().isDefault());
      calendar.visible(campaign.get().isLocal() && !campaign.get().isDefault());
    }
  }
}
