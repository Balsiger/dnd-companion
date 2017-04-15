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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Campaign;
import net.ixitxachitls.companion.data.Campaigns;
import net.ixitxachitls.companion.data.Character;
import net.ixitxachitls.companion.data.Characters;
import net.ixitxachitls.companion.net.CompanionSubscriber;
import net.ixitxachitls.companion.ui.CampaignPublisher;
import net.ixitxachitls.companion.ui.ConfirmationDialog;
import net.ixitxachitls.companion.ui.ListAdapter;
import net.ixitxachitls.companion.ui.Setup;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment displaying campaign information.
 */
public class CampaignFragment extends CompanionFragment {

  private Campaign campaign;
  private List<Character> characters = new ArrayList<>();

  // UI elements.
  private ListAdapter<Character> charactersAdapter;
  private ImageButton delete;
  private TextView title;
  private TextView subtitle;
  private ImageView local;
  private ImageView remote;
  private FloatingActionButton addCharacter;

  public CampaignFragment() {
    super(Type.campaign);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_campaign, container, false);

    title = Setup.textView(view, R.id.title, this::edit);
    subtitle = Setup.textView(view, R.id.subtitle, this::edit);
    delete = Setup.imageButton(view, R.id.button_delete, this::deleteCampaign);
    local = Setup.imageView(view, R.id.local);
    remote = Setup.imageView(view, R.id.remote);
    addCharacter = Setup.floatingButton(view, R.id.add_character, this::createCharacter);

    // Setup list view.
    charactersAdapter = new ListAdapter<>(container.getContext(),
        R.layout.list_item_character, characters,
        new ListAdapter.ViewBinder<Character>() {
          @Override
          public void bind(View view, Character item, int position) {
            if (campaign.isLocal() && !campaign.isDefault()) {
              Setup.textView(view, R.id.name).setText(item.getName()
                  + " (" + item.getPlayerName() + ")");
            } else {
              Setup.textView(view, R.id.name).setText(item.getName());
            }
            Setup.textView(view, R.id.gender_race).setText(
                item.getGender().getName() + " " + item.getRace());
          }
        });

    Setup.listView(view, R.id.characters, charactersAdapter,
        (i) -> getMain().showCharacter(characters.get(i)));


    return view;
  }

  public void showCampaign(Campaign campaign) {
    this.campaign = campaign;

    refresh();
  }

  private void edit() {
    if (campaign.isDefault() || !campaign.isLocal()) {
      return;
    }

    EditCampaignFragment.newInstance(campaign.getCampaignId()).display(getFragmentManager());
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
            show(Type.campaigns);
          }

          @Override
          public void no() {
            // nothing to do here
          }
        });
  }

  private void createCharacter() {
    EditCharacterFragment.newInstance("", campaign.getCampaignId()).display(getFragmentManager());
  }

  @Override
  public void refresh() {
    if (campaign == null) {
      return;
    }

    if (campaign.isDefault() || campaign.isPublished()) {
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
          campaign.isDefault() ? R.color.out : campaign.isPublished() ? R.color.on : R.color.off,
          null));
      local.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          // Cannot publish default campaign.
          if (!campaign.isDefault()) {
            CampaignPublisher.toggle(getContext(), campaign, CampaignFragment.this::refresh,
                CampaignPublisher.EmptyCancelAction);
          }
        }
      });
    } else {
      local.setVisibility(View.INVISIBLE);
      remote.setVisibility(View.VISIBLE);

      remote.setColorFilter(getResources().getColor(
          CompanionSubscriber.get().isServerActive(campaign.getServerId())
              ? R.color.on : R.color.off, null));
    }

    if (campaign.isLocal() && !campaign.isDefault()) {
      addCharacter.setVisibility(View.GONE);
    } else {
      addCharacter.setVisibility(View.VISIBLE);
    }

    if (charactersAdapter != null) {
      characters.clear();
      characters.addAll(Characters.get().getCharacters(campaign.getCampaignId()));
      charactersAdapter.notifyDataSetChanged();
    }
  }
}
