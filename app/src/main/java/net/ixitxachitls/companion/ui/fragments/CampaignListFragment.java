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

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.common.base.Optional;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Campaign;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBase;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.ui.CampaignActivity;
import net.ixitxachitls.companion.ui.ConfirmationDialog;
import net.ixitxachitls.companion.ui.ListProtoAdapter;
import net.ixitxachitls.companion.ui.Setup;
import net.ixitxachitls.companion.ui.activities.MainActivity;

/**
 * The fragment displaying the campaign list
 */
public class CampaignListFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

  private ListProtoAdapter<Data.CampaignProto> campaignsAdapter;
  private boolean publishing = false;
  private boolean confirmation = true;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_campaign_list, container, false);
    // Setup list view.
    ListView campaigns = (ListView) view.findViewById(R.id.campaignsList);
    campaignsAdapter = new ListProtoAdapter<>(getContext(), R.layout.list_item_campaign,
        Data.CampaignProto.getDefaultInstance(),
        new ListProtoAdapter.ContentCreator<Data.CampaignProto>() {
          @Override
          public void create(View view, long id, Data.CampaignProto proto) {
            Campaign campaign = Campaign.fromProto(id, proto);
            ((TextView) view.findViewById(R.id.name)).setText(campaign.getName());
            ((TextView) view.findViewById(R.id.world)).setText(campaign.getWorld());
            if (campaign.isLocal()) {
              view.findViewById(R.id.local).setVisibility(View.VISIBLE);
              view.findViewById(R.id.remote).setVisibility(View.INVISIBLE);
            } else {
              view.findViewById(R.id.local).setVisibility(View.INVISIBLE);
              view.findViewById(R.id.remote).setVisibility(View.VISIBLE);
            }

            if (campaign.isLocal() && !campaign.isDefault()) {
              view.findViewById(R.id.publish).setVisibility(View.VISIBLE);
            } else {
              view.findViewById(R.id.publish).setVisibility(View.GONE);
            }

            Switch publish = Setup.switchButton(view, R.id.publish, (w) -> publishCampaign(w, id));
          }
        });
    campaigns.setAdapter(campaignsAdapter);
    getLoaderManager().initLoader(0, null, this);

    campaigns.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), CampaignActivity.class);
        intent.putExtra(DataBase.COLUMN_ID, id);
        getActivity().startActivity(intent);
      }
    });

    Setup.floatingButton(view, R.id.campaign_add, this::addCampaign);

    return view;
  }

  private void addCampaign() {
    EditCampaignFragment.newInstance().display(getFragmentManager());
  }

  private void publishCampaign(Switch widget, long id) {
    if (publishing) {
      return;
    }

    if (confirmation) {
      if (widget.isChecked()) {
        ConfirmationDialog.show(getContext(),
            getString(R.string.main_campaign_publish_title),
            getString(R.string.main_campaign_publish_message),
            new ConfirmationDialog.Callback() {
              @Override
              public void yes() {
                Optional<Campaign> campaign = Campaign.load(getContext(), id);
                if (campaign.isPresent()) {
                  campaign.get().publish();
                }
              }

              @Override
              public void no() {
                publishing = true;
                widget.toggle();
                publishing = false;
              }
            });
      } else {
        ConfirmationDialog.show(getContext(),
            getString(R.string.main_campaign_unpublish_title),
            getString(R.string.main_campaign_unpublish_message),
            new ConfirmationDialog.Callback() {
              @Override
              public void yes() {
                Optional<Campaign> campaign = Campaign.load(getContext(), id);
                if (campaign.isPresent()) {
                  campaign.get().unpublish();
                }
              }

              @Override
              public void no() {
                publishing = true;
                widget.toggle();
                publishing = false;
              }
            });
      }
    } else {
      Optional<Campaign> campaign = Campaign.load(getContext(), id);
      if (campaign.isPresent()) {
        if (widget.isChecked()) {
          campaign.get().publish();
        } else {
          campaign.get().unpublish();
        }
      }
    }
  }

  private void refresh() {
    getLoaderManager().restartLoader(0, null, this);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new CursorLoader(getContext(), DataBaseContentProvider.CAMPAIGNS,
        DataBase.COLUMNS, null, null, null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    campaignsAdapter.swapCursor(data);

    ((MainActivity) getActivity()).setStatus("Campaigns loaded.");
    if (!Settings.get().isDefined()) {
      ((MainActivity) getActivity()).show(MainActivity.Fragments.settings);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    campaignsAdapter.swapCursor(null);
  }
}
