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

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Campaign;
import net.ixitxachitls.companion.data.Campaigns;
import net.ixitxachitls.companion.proto.Entity;
import net.ixitxachitls.companion.storage.DataBase;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.ui.CampaignPublisher;
import net.ixitxachitls.companion.ui.ConfirmationDialog;
import net.ixitxachitls.companion.ui.ListProtoAdapter;
import net.ixitxachitls.companion.ui.Setup;

/**
 * A fragment displaying campaign information.
 */
public class CampaignFragment extends CompanionFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

  private Campaign campaign;
  private ListProtoAdapter<Entity.CharacterProto> charactersAdapter;
  private ImageButton delete;
  private TextView title;
  private TextView subtitle;
  private ImageView local;
  private ImageView remote;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_campaign, container, false);

    title = Setup.textView(view, R.id.title);
    subtitle = Setup.textView(view, R.id.campaign);
    delete = Setup.imageButton(view, R.id.button_delete, this::deleteCampaign);
    local = Setup.imageView(view, R.id.local);
    remote = Setup.imageView(view, R.id.remote);
    Setup.floatingButton(view, R.id.add_character, this::createCharacter);

    // Setup list view.
    ListView characters = (ListView) view.findViewById(R.id.characters);
    charactersAdapter = new ListProtoAdapter<>(getContext(), R.layout.list_item_character,
        /*
        new ListProtoAdapter.OnItemClick<Entity.CharacterProto>() {
          @Override
          public void click(long id, Entity.CharacterProto proto) {
            ConfirmationDialog.show(CampaignActivity.this,
                getString(R.string.character_delete_title),
                getString(R.string.character_delete_message),
                new ConfirmationDialog.Callback() {
                  @Override
                  public void yes() {
                    getContentResolver().delete(DataBaseContentProvider.CHARACTERS, "id = " + id,
                        null);
                    getLoaderManager().restartLoader(0, null, CampaignActivity.this);
                    Toast.makeText(CampaignActivity.this, "The character has been deleted",
                        Toast.LENGTH_SHORT).show();
                  }

                  @Override
                  public void no() {
                    // nothing to do here
                  }
                });
          }
        },*/
        Entity.CharacterProto.getDefaultInstance(),
        new ListProtoAdapter.ContentCreator<Entity.CharacterProto>() {
          @Override
          public void create(View view, long id, Entity.CharacterProto proto) {
            ((TextView) view.findViewById(R.id.text)).setText(proto.getName());
          }
        });
    characters.setAdapter(charactersAdapter);
    getLoaderManager().initLoader(0, null, this);

    characters.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        openCharacter(id);
      }
    });

    showCampaign(Campaigns.get().getCampaign(""));
    return view;
  }

  public void showCampaign(Campaign campaign) {
    this.campaign = campaign;

    if (campaign.isDefault()) {
      delete.setVisibility(View.GONE);
    } else {
      delete.setVisibility(View.VISIBLE);
    }

    title.setText(campaign.getName());
    subtitle.setText(campaign.getWorld() + ", " + campaign.getDm());
    if (campaign.isLocal()) {
      local.setVisibility(View.VISIBLE);
      remote.setVisibility(View.INVISIBLE);

      local.setColorFilter(getResources().getColor(
          campaign.isPublished() ? R.color.on : R.color.off, null));
      local.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if (campaign.isDefault()) {
            // Cannot publish default campaign.
            return;
          }

          local.setColorFilter(getResources().getColor(
              campaign.isPublished() ? R.color.off : R.color.on, null));
          CampaignPublisher.toggle(getContext(), campaign,
              () -> local.setColorFilter(getResources().getColor(
                  campaign.isPublished() ? R.color.off : R.color.on, null)));
        }
      });
    } else {
      local.setVisibility(View.INVISIBLE);
      remote.setVisibility(View.VISIBLE);
    }
  }

  protected void deleteCampaign() {
    ConfirmationDialog.show(getContext(),
        getResources().getString(R.string.campaign_delete_title),
        getResources().getString(R.string.campaign_delete_message),
        new ConfirmationDialog.Callback() {
          @Override
          public void yes() {
            Campaigns.get().remove(campaign);
            Toast.makeText(getActivity(), getString(R.string.campaign_deleted),
                Toast.LENGTH_SHORT).show();
            show(CompanionFragment.Fragments.campaigns);
          }

          @Override
          public void no() {
            // nothing to do here
          }
        });
  }

  private void createCharacter() {
    openCharacter(0);
  }

  private void openCharacter(long id) {
    /*
    Intent intent = new Intent(CampaignActivity.this, CharacterActivity.class);
    intent.putExtra(DataBase.COLUMN_ID, id);
    startActivity(intent);
    */
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new CursorLoader(getActivity(), DataBaseContentProvider.CHARACTERS,
        DataBase.COLUMNS, null, null, null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    charactersAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    charactersAdapter.swapCursor(null);
  }
}
