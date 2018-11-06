/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Campaigns;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.Images;
import net.ixitxachitls.companion.data.documents.Invites;
import net.ixitxachitls.companion.data.documents.Messages;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.CharacterDialog;
import net.ixitxachitls.companion.ui.dialogs.EditCampaignDialog;
import net.ixitxachitls.companion.ui.views.CampaignTitleView;
import net.ixitxachitls.companion.ui.views.CharacterTitleView;
import net.ixitxachitls.companion.ui.views.TitleView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The fragment displaying the list of campaigns and orphaned characters.
 */
public class CampaignsFragment extends CompanionFragment {

  private TitleView user;
  private Wrapper<TextView> note;
  private LinearLayout campaigns;
  private LinearLayout characters;

  public CampaignsFragment() {
    super(Type.campaigns);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    LinearLayout view = (LinearLayout)
        inflater.inflate(R.layout.fragment_campaigns, container, false);

    user = view.findViewById(R.id.user);
    user.setAction(() -> show(Type.settings));
    campaigns = view.findViewById(R.id.campaigns);
    characters = view.findViewById(R.id.characters);
    note = Wrapper.<TextView>wrap(view, R.id.note).gone();
    Wrapper.wrap(view, R.id.campaign_add)
        .onClick(this::addCampaign)
        .description("Add Campaign", "Create a new campaign. "
            + "You will be the Dungeon Master of the campaign. The campaign will only be visible "
            + "to player you invite to it.");
    Wrapper.wrap(view, R.id.character_add)
        .onClick(this::addCharacter)
        .description("Add Character", "Create a new character. "
            + "Characters created here will not be in a campaign, but can be moved to any existing "
            + "campaign later.");

    campaigns().observe(this, this::update);
    characters().observe(this, this::update);
    images().observe(this, this::update);
    messages().observe(this, this::update);
    invites().observe(this, this::update);
    return view;
  }

  private void addCampaign() {
    EditCampaignDialog.newInstance().display();
  }

  private void addCharacter() {
    CharacterDialog.newInstance("", "").display();
  }

  private void update(Campaigns campaigns) {
    this.campaigns.removeAllViews();

    // We have to recreate the campaigns as the transition away from this fragment seems to break
    // them.
    boolean campaignFound = false;
    for (Campaign campaign : campaigns.getCampaigns()) {
      CampaignTitleView title = new CampaignTitleView(getContext());
      campaign.observe(this, title::update);
      campaign.getDm().observe(this, title::update);
      title.update(campaign);
      this.campaigns.addView(title);
      title.setAction(() -> {
        CompanionFragments.get().showCampaign(campaign, Optional.of(title));
      });
      campaignFound = true;
    }

    note.visible(!campaignFound);

    user.setTitle(me().getNickname());
    user.setSubtitle(subtitle());
    user.loadImageUrl(me().getPhotoUrl());
  }

  private void update(Characters characters) {
    this.characters.removeAllViews();

    boolean characterFound = false;
    for (Character character : characters.getAll()) {
      if (character.amPlayer()) {
        CharacterTitleView title = new CharacterTitleView(getContext());
        character.observe(this, title::update);
        messages().observe(this, title::update);
        this.characters.addView(title);
        characterFound = true;
      }
    }

    note.visible(!characterFound);

    messages().readMessages(characters.getPlayerCharacters(me().getId()).stream()
        .map(Character::getId)
        .collect(Collectors.toList()));
  }

  private void update(Images images) {
    for (int i = 0; i < campaigns.getChildCount(); i++) {
      CampaignTitleView title = (CampaignTitleView) campaigns.getChildAt(i);
      title.update(images);
    }
    for (int i = 0; i < characters.getChildCount(); i++) {
      CharacterTitleView title = (CharacterTitleView) characters.getChildAt(i);
      title.update(images);
    }
  }

  private void update(Messages message) {
    update(characters());
  }

  private void update(Invites invites) {
    update(campaigns());
  }

  private String subtitle() {
    List<String> parts = new ArrayList<>();
    if (me().getCampaigns().isEmpty()) {
      parts.add("Not yet invited to any campaigns");
    } else if (me().getCampaigns().size() == 1){
      parts.add("Invited to 1 campaign");
    } else {
      parts.add("Invited to " + me().getCampaigns().size() + " campaigns");
    }
    if (!me().getFeatures().isEmpty()) {
      parts.add(Strings.COMMA_JOINER.join(me().getFeatures()));
    }

    return Strings.SEMICOLON_JOINER.join(parts);
  }

  @Override
  public boolean goBack() {
    return false;
  }
}
