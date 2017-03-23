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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Campaign;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.ui.Setup;

/**
 * Fragment for editing a campaign
 */
public class EditCampaignFragment extends EditFragment {

  private static final String ARG_PROTO = "proto";
  private static final String ARG_ID = "id";

  private Campaign campaign;
  private EditText name;
  private TextView world;
  private Button save;

  public EditCampaignFragment() {}

  public static EditCampaignFragment newInstance() {
    return newInstance(0, Data.CampaignProto.getDefaultInstance());
  }

  public static EditCampaignFragment newInstance(int id, Data.CampaignProto proto) {
    EditCampaignFragment fragment = new EditCampaignFragment();
    fragment.setArguments(arguments(R.layout.fragment_campaign,
        proto.getName().isEmpty() ? R.string.campaign_title_add : R.string.campaign_title_edit,
        R.color.campaign, id, proto));
    return fragment;
  }

  protected static Bundle arguments(int layoutId, int titleId, int colorId,
                                    int id, Data.CampaignProto proto) {
    Bundle arguments = EditFragment.arguments(layoutId, titleId, colorId);
    arguments.putByteArray(ARG_PROTO, proto.toByteArray());
    arguments.putInt(ARG_ID, id);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      try {
        int id = getArguments().getInt(ARG_ID);
        campaign = Campaign.fromProto(id,
            Data.CampaignProto.parseFrom(getArguments().getByteArray(ARG_PROTO)));
      } catch (InvalidProtocolBufferException e) {
        Toast.makeText(getContext(), "Cannot parse proto: " + e, Toast.LENGTH_SHORT).show();
        campaign = new Campaign(0, "");
      }
    } else {
      campaign = new Campaign(0, "");
    }
  }

  @Override
  protected void createContent(View view) {
    name = Setup.editText(view, R.id.edit_name, campaign.getName(), R.string.campaign_edit_name,
        R.color.campaign, null, this::update);
    world = Setup.textView(view, R.id.world, this::editWorld);
    if(!campaign.getWorld().isEmpty()) {
      world.setText(campaign.getWorld());
    }
    save = Setup.button(view, R.id.save, this::save);

    if (campaign.isDefined()) {
      view.findViewById(R.id.campaign_edit_intro).setVisibility(View.GONE);
    }
  }

  private void editWorld() {
    campaign.setWorld(world.getText().toString());
  }

  private void update() {
    if (name.getText().length() == 0) {
      save.setVisibility(View.INVISIBLE);
    } else {
      save.setVisibility(View.VISIBLE);
    }
  }

  private void save() {
  }
}
