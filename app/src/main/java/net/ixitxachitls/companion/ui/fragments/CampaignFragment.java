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

import com.google.common.base.Optional;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.ui.ConfirmationDialog;
import net.ixitxachitls.companion.ui.ListAdapter;
import net.ixitxachitls.companion.ui.Wrapper;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.DateDialog;
import net.ixitxachitls.companion.ui.dialogs.EditCampaignDialog;
import net.ixitxachitls.companion.ui.dialogs.EditCharacterDialog;
import net.ixitxachitls.companion.ui.views.ActionButton;
import net.ixitxachitls.companion.ui.views.CampaignTitleView;
import net.ixitxachitls.companion.ui.views.IconView;
import net.ixitxachitls.companion.ui.views.PartyView;

import java.util.ArrayList;
import java.util.List;

/** A fragment displaying campaign information. */
public class CampaignFragment extends CompanionFragment {

  private Optional<Campaign> campaign = Optional.absent();
  private List<Character> characters = new ArrayList<>();

  // UI elements.
  private CampaignTitleView title;
  private ListAdapter<Character> charactersAdapter;
  private IconView delete;
  private Wrapper<FloatingActionButton> addCharacter;
  private Wrapper<TextView> date;
  private Wrapper<ActionButton> battle;
  private PartyView party;

  public CampaignFragment() {
    super(Type.campaign);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_campaign, container, false);

    title = (CampaignTitleView) view.findViewById(R.id.title);
    title.setAction(this::edit);
    delete = (IconView) view.findViewById(R.id.delete);
    delete.setAction(this::deleteCampaign);
    party = (PartyView) view.findViewById(R.id.party);
    addCharacter = Wrapper.wrap(view, R.id.add_character);
    addCharacter.onClick(this::createCharacter);
    battle = Wrapper.wrap(view, R.id.battle);
    battle.onClick(this::startBattle);
    date = Wrapper.wrap(view, R.id.date);
    date.onClick(this::editDate);

    return view;
  }

  public void showCampaign(Campaign campaign) {
    this.campaign = Optional.of(campaign);

    refresh();
  }

  private void startBattle() {
    if (campaign.isPresent() && campaign.get().isLocal()) {
      CompanionFragments.get().showBattle(campaign.get());
    }
  }

  private void editDate() {
    if (campaign.isPresent() && campaign.get().isLocal()) {
      DateDialog.newInstance(campaign.get().getCampaignId()).display(getFragmentManager());
    }
  }

  private void edit() {
    if (!campaign.isPresent() || campaign.get().isDefault() || !campaign.get().isLocal()) {
      return;
    }

    EditCampaignDialog.newInstance(campaign.get().getCampaignId()).display(getFragmentManager());
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
      Campaigns.get(campaign.get().isLocal()).remove(campaign.get());
      Toast.makeText(getActivity(), getString(R.string.campaign_deleted),
          Toast.LENGTH_SHORT).show();
      show(Type.campaigns);
    }
  }

  private void createCharacter() {
    if (campaign.isPresent()) {
      EditCharacterDialog.newInstance("",
          campaign.get().getCampaignId()).display(getFragmentManager());
    }
  }

  @Override
  public void refresh() {
    super.refresh();

    if (!campaign.isPresent()) {
      return;
    }

    campaign = Campaigns.get(campaign.get().isLocal()).getCampaign(campaign.get().getCampaignId());
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

    if (campaign.get().isLocal() && !campaign.get().isDefault()) {
      addCharacter.gone();
      battle.visible();
    } else {
      addCharacter.visible();
      battle.gone();
    }

    party.setCampaign(campaign);

    /*
    characters.clear();
    if (campaign.get().isDefault()) {
      characters.addAll(Characters.local().getOrphanedCharacters());
    } else if (campaign.get().isLocal()) {
      characters.addAll(Characters.remote().getCharacters(campaign.get().getCampaignId()));
    } else {
      characters.addAll(Characters.local().getCharacters(campaign.get().getCampaignId()));
      characters.addAll(Characters.remote().getCharacters(campaign.get().getCampaignId()));
    }

    party.removeAllViews();
    for (Character character : characters) {
      CharacterChipView chip = new CharacterChipView(getContext(), character);
      chip.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          CompanionFragments.get().showCharacter(character);
        }
      });
      party.addView(chip);
    }
    */
  }

  private boolean canDeleteCampaign() {
    if (campaign.get().isDefault()) {
      return false;
    }

    if (campaign.get().isLocal()) {
      return !campaign.get().isPublished();
    }

    return Characters.local().getCharacters(campaign.get().getCampaignId()).isEmpty();
  }
}
