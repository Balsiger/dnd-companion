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

  private Optional<Campaign> campaign = Optional.absent();

  // UI elements.
  private CampaignTitleView title;
  private IconView delete;
  private TextWrapper<TextView> date;
  private PartyFragment party;

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
    title = (CampaignTitleView) view.findViewById(R.id.title);
    title.setAction(this::edit);
    delete = (IconView) view.findViewById(R.id.delete);
    delete.setAction(this::deleteCampaign);
    party = (PartyFragment) getChildFragmentManager().findFragmentById(R.id.party);
    date = TextWrapper.wrap(view, R.id.date);
    date.onClick(this::editDate);

    return view;
  }

  public void showCampaign(Campaign campaign) {
    this.campaign = Optional.of(campaign);

    refresh();
  }

  private void back() {
    CompanionFragments.get().show(Type.campaigns, Optional.absent());
  }

  private void editDate() {
    if (campaign.isPresent() && campaign.get().isLocal()) {
      DateDialog.newInstance(campaign.get().getCampaignId()).display();
    }
  }

  private void edit() {
    if (!campaign.isPresent() || campaign.get().isDefault() || !campaign.get().isLocal()) {
      return;
    }

    EditCampaignDialog.newInstance(campaign.get().getCampaignId()).display();
  }

  protected void deleteCampaign() {
    ConfirmationDialog.create(getContext())
        .title(getResources().getString(R.string.campaign_delete_title))
        .message(getResources().getString(R.string.campaign_delete_message))
        .yes(this::deleteCampaignOk)
        .show();
  }

  private void deleteCampaignOk() {
    if (campaign.isPresent()) {
      campaign.get().delete();
      Toast.makeText(getActivity(), getString(R.string.campaign_deleted),
          Toast.LENGTH_SHORT).show();
      show(Type.campaigns);
    }
  }

  @Override
  public void refresh() {
    super.refresh();

    if (!campaign.isPresent()) {
      return;
    }

    campaign = Campaigns.getCampaign(campaign.get().getCampaignId()).getValue();
    if (!campaign.isPresent()) {
      return;
    }

    title.setCampaign(campaign.get());

    if (canDeleteCampaign()) {
      delete.setVisibility(View.VISIBLE);
    } else {
      delete.setVisibility(View.GONE);
    }

    date.text(campaign.get().getDate().toString());

    party.setup(campaign.get());
  }

  private boolean canDeleteCampaign() {
    if (campaign.get().isDefault()) {
      return false;
    }

    if (campaign.get().isLocal()) {
      return !campaign.get().isPublished();
    }

    return !Characters.hasLocalCampaignCharacters(campaign.get().getCampaignId());
  }
}
