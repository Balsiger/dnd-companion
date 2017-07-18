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

package net.ixitxachitls.companion.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Optional;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.ui.fragments.ListSelectFragment;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * Fragment for editing a campaign
 */
public class EditCampaignDialog extends Dialog {

  private static final String ARG_ID = "id";

  private Optional<Campaign> campaign = Optional.absent();

  // The following values are only valid after onCreate().
  private EditTextWrapper<EditText> name;
  private TextWrapper<TextView> world;
  private Wrapper<Button> save;

  public EditCampaignDialog() {}

  public static EditCampaignDialog newInstance() {
    return newInstance("");
  }

  public static EditCampaignDialog newInstance(String id) {
    EditCampaignDialog fragment = new EditCampaignDialog();
    fragment.setArguments(arguments(R.layout.fragment_edit_campaign,
        id.isEmpty() ? R.string.campaign_title_add : R.string.campaign_title_edit,
        R.color.campaign, id));
    return fragment;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String campaignId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, campaignId);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      String id = getArguments().getString(ARG_ID);
      if (id == null || id.isEmpty()) {
        campaign = Optional.of(Campaign.createNew());
      } else {
        campaign = Campaigns.getLocalCampaign(id);
      }
    } else {
      campaign = Optional.of(Campaign.createNew());
    }
  }


  @Override
  protected void createContent(View view) {
    if (campaign.isPresent()) {
      name = EditTextWrapper.wrap(view, R.id.edit_name)
          .text(campaign.get().getName())
          .label(R.string.campaign_edit_name)
          .lineColor(R.color.campaign)
          .onChange(this::update);
      world = TextWrapper.wrap(view, R.id.world)
          .onClick(this::selectWorld);
      if (!campaign.get().getWorld().isEmpty()) {
        world.text(campaign.get().getWorld());
      }
      save = Wrapper.wrap(view, R.id.save);
      save.onClick(this::save);

      if (campaign.get().isDefined()) {
        view.findViewById(R.id.campaign_edit_intro).setVisibility(View.GONE);
      }
    }

    update();
  }

  private void selectWorld() {
    if (campaign.isPresent()) {
      ListSelectFragment fragment = ListSelectFragment.newInstance(
          R.string.campaign_select_world, campaign.get().getWorld(),
          Entries.get().getWorlds().getNames(), R.color.campaign);
      fragment.setSelectListener(this::editWorld);
      fragment.display();
    }
  }

  private void editWorld(String value, int position) {
    if (campaign.isPresent()) {
      campaign.get().setWorld(value);
      update();
    }
  }

  protected void update() {
    if (campaign.isPresent()) {
      if (name.getText().length() == 0 || campaign.get().getWorld().isEmpty()) {
        save.invisible();
      } else {
        save.visible();
      }

      if (!campaign.get().getWorld().isEmpty()) {
        world.text(campaign.get().getWorld());
      }
    }
  }

  protected void save() {
    if (campaign.isPresent()) {
      campaign.get().setName(name.getText());
      campaign.get().store();

      super.save();
    }
  }
}
