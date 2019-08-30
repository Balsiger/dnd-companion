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
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Documents;
import net.ixitxachitls.companion.ui.Hints;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.CharacterDialog;
import net.ixitxachitls.companion.ui.dialogs.EditCampaignDialog;
import net.ixitxachitls.companion.ui.views.CampaignTitleView;
import net.ixitxachitls.companion.ui.views.CharacterTitleView;
import net.ixitxachitls.companion.ui.views.TitleView;
import net.ixitxachitls.companion.ui.views.UpdatableViewGroup;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
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

  private static final String LOADING_CHARACTERS = "characters";
  private static final String LOADING_CAMPAIGNS = "campaigns";
  private static final String LOADING_IMAGES = "images";
  private static final String LOADING_USER = "user";

  private TitleView user;
  private Wrapper<TextView> note;
  private UpdatableViewGroup<LinearLayout, CampaignTitleView, String> campaigns;
  private UpdatableViewGroup<LinearLayout, CharacterTitleView, String> characters;
  private TextWrapper<TextView> hint;

  public CampaignsFragment() {
    super(Type.campaigns);
  }

  @Override
  public boolean goBack() {
    return false;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    LinearLayout view = (LinearLayout)
        inflater.inflate(R.layout.fragment_campaigns, container, false);

    clearActions();
    user = view.findViewById(R.id.user);
    user.setAction(() -> show(Type.settings));
    campaigns = new UpdatableViewGroup<>(view.findViewById(R.id.campaigns));
    characters = new UpdatableViewGroup<>(view.findViewById(R.id.characters));
    note = Wrapper.<TextView>wrap(view, R.id.note).gone();
    Wrapper.wrap(view, R.id.campaign_add)
        .onClick(this::addCampaign)
        .description("Add Campaign", "Create a new campaign. "
            + "You will be the Dungeon Master of the campaign. The campaign will only be visible "
            + "to player you inviteAction to it.");
    Wrapper.wrap(view, R.id.character_add)
        .onClick(this::addCharacter)
        .description("Add Character", "Create a new character. "
            + "Characters created here will not be in a campaign, but can be moved to any existing "
            + "campaign later.");
    hint = TextWrapper.wrap(view, R.id.hint);
    Wrapper.wrap(view, R.id.miniatures)
        .onClick(this::showMiniatures)
        .description("Miniatures", "Brows and modify your miniatures catalog.");

    startLoading(LOADING_CAMPAIGNS);
    campaigns().observe(this, this::refreshCampaigns);
    invites().observe(this, this::refreshCampaigns);
    startLoading(LOADING_CHARACTERS);
    characters().observe(this, this::refreshCharacters);
    startLoading(LOADING_IMAGES);
    images().observe(this, this::refreshDisplay);
    messages().observe(this, this::refreshDisplay);
    startLoading(LOADING_USER);
    users().observe(this, this::refreshUser);;

    hint.text(Hints.nextHint());
    return view;
  }

  private void addCampaign() {
    EditCampaignDialog.newInstance().display();
  }

  private void addCharacter() {
    CharacterDialog.newInstance("", "").display();
  }

  private void refreshCampaigns(Documents.Update update) {
    Status.log("CampaignsFragment refreshing campaigns: " + update);
    this.campaigns.ensureOnly(campaigns().getIds(), id -> new CampaignTitleView(getContext()));
    this.campaigns.update(campaigns().getIds(),
        (id, view) -> {
          Optional<Campaign> campaign = campaigns().getOptional(id);
          if (campaign.isPresent()) {
            view.update(campaign.get());
            view.setAction(() -> {
              CompanionFragments.get().showCampaign(campaign.get(), Optional.of(view));
            });
          }
        });

    note.visible(this.characters.getView().getChildCount() == 0 || campaigns().getIds().isEmpty());
    campaigns.simpleUpdate(v -> v.refresh(update));

    finishLoading(LOADING_CAMPAIGNS);
  }

  private void refreshCharacters(Documents.Update update) {
    Status.log("CampaignsFragment refreshing characters: " + update);
    List<String> characterIds = characters().getAll().stream()
        .filter(Character::amPlayer)
        .map(Character::getId)
        .collect(Collectors.toList());
    this.characters.ensureOnly(characterIds, id -> new CharacterTitleView(getContext()));
    this.characters.update(characterIds, (id, view) -> {
      Optional<Character> character = characters().get(id);
      if (character.isPresent()) {
        view.update(character.get());
      }
    });

    note.visible(this.characters.getView().getChildCount() == 0 || campaigns().getIds().isEmpty());
    characters.simpleUpdate(v -> v.refresh(update));

    messages().readMessages(characters().getPlayerCharacters(me().getId()).stream()
        .map(Character::getId)
        .collect(Collectors.toList()));

    finishLoading(LOADING_CHARACTERS);
  }

  private void refreshDisplay(Documents.Update update) {
    Status.log("CampaignsFragment refreshing display: " + update);
    campaigns.simpleUpdate(v -> v.refresh(update));
    characters.simpleUpdate(v -> v.refresh(update));

    finishLoading(LOADING_IMAGES);
  }

  private void refreshUser(Documents.Update update) {
    Status.log("CampaignsFragment refreshing user: " + update);
    user.setTitle(me().getNickname());
    user.setSubtitle(subtitle());
    user.loadImageUrl(me().getPhotoUrl());

    finishLoading(LOADING_USER);
  }

  private void showMiniatures() {
    CompanionFragments.get().show(Type.miniatures, Optional.empty());
  }

  private String subtitle() {
    List<String> parts = new ArrayList<>();
    if (me().getCampaigns().size() == 1) {
      parts.add("Invited to 1 campaign");
    } else if (me().getCampaigns().size() > 1) {
      parts.add("Invited to " + me().getCampaigns().size() + " campaigns");
    }
    if (!me().getFeatures().isEmpty()) {
      parts.add(Strings.COMMA_JOINER.join(me().getFeatures()));
    }

    return Strings.SEMICOLON_JOINER.join(parts);
  }
}
