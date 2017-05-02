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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.ui.CampaignPublisher;
import net.ixitxachitls.companion.ui.ConfirmationDialog;
import net.ixitxachitls.companion.ui.ListAdapter;
import net.ixitxachitls.companion.ui.Setup;
import net.ixitxachitls.companion.ui.dialogs.DateDialog;
import net.ixitxachitls.companion.ui.views.IconView;
import net.ixitxachitls.companion.ui.views.NetworkIcon;
import net.ixitxachitls.companion.ui.views.TitleView;

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
  private IconView delete;
  private TitleView title;
  private NetworkIcon networkIcon;
  private FloatingActionButton addCharacter;
  private TextView date;
  private FloatingActionButton battle;

  public CampaignFragment() {
    super(Type.campaign);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_campaign, container, false);

    title = (TitleView) view.findViewById(R.id.title);
    title.setAction(this::edit);
    delete = (IconView) view.findViewById(R.id.button_delete);
    delete.setAction(this::deleteCampaign);
    networkIcon = (NetworkIcon) view.findViewById(R.id.network);
    addCharacter = Setup.floatingButton(view, R.id.add_character, this::createCharacter);
    battle = Setup.floatingButton(view, R.id.battle, this::startBattle);
    date = Setup.textView(view, R.id.date, this::showDateFragment);

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

  private void startBattle() {
    if (campaign.isLocal()) {
      getMain().showBattle(campaign);
    }
  }

  public void showDateFragment() {
    if (campaign.isLocal()) {
      DateDialog.newInstance(campaign.getCampaignId()).display(getFragmentManager());
    }
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
    ConfirmationDialog.create(getContext())
        .title(getResources().getString(R.string.campaign_delete_title))
        .message(getResources().getString(R.string.campaign_delete_message))
        .yes(this::deleteCampaignOk)
        .show();
  }

  private void deleteCampaignOk() {
    Campaigns.get().remove(campaign);
    Toast.makeText(getActivity(), getString(R.string.campaign_deleted),
        Toast.LENGTH_SHORT).show();
    show(Type.campaigns);
  }

  private void createCharacter() {
    EditCharacterFragment.newInstance("", campaign.getCampaignId()).display(getFragmentManager());
  }

  @Override
  public void refresh() {
    super.refresh();

    if (campaign == null) {
      return;
    }

    Campaign newCampaign = Campaigns.get().getCampaign(campaign.getCampaignId());
    if (newCampaign != campaign) {
      networkIcon.bleep();
    }
    campaign = newCampaign;

    if (campaign.isDefault() || campaign.isPublished()) {
      delete.setVisibility(View.GONE);
    } else {
      delete.setVisibility(View.VISIBLE);
    }

    title.setTitle(campaign.getName());
    title.setSubtitle(campaign.getWorld() + ", " + campaign.getDm());
    networkIcon.setLocation(campaign.isLocal());
    if (!campaign.isDefault()) {
      networkIcon.setStatus(campaign.isPublished());
    }
    if (campaign.isLocal()) {
      networkIcon.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          // Cannot publish default campaign.
          if (!campaign.isDefault()) {
            CampaignPublisher.toggle(getContext(), campaign, CampaignFragment.this::refresh,
                CampaignPublisher.EmptyCancelAction);
          }
        }
      });
    }

    date.setText(campaign.getDate().toString());

    if (campaign.isLocal() && !campaign.isDefault()) {
      addCharacter.setVisibility(View.GONE);
      battle.setVisibility(View.VISIBLE);
    } else {
      addCharacter.setVisibility(View.VISIBLE);
      battle.setVisibility(View.GONE);
    }

    if (charactersAdapter != null) {
      characters.clear();
      characters.addAll(Characters.get().getCharacters(campaign.getCampaignId()));
      charactersAdapter.notifyDataSetChanged();
    }
  }
}
