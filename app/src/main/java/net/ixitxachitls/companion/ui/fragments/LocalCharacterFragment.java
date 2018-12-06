/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.CharacterDialog;
import net.ixitxachitls.companion.ui.dialogs.MessageDialog;
import net.ixitxachitls.companion.ui.dialogs.TimedConditionDialog;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment for a local character.
 */
public class LocalCharacterFragment extends CharacterFragment {

  private final int PICK_IMAGE = 1;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);

    title.setAction(this::editBase);
    title.setImageAction(this::editImage);
    edit.visible()
        .onClick(this::editBase)
        .description("Edit Character", "Edit the basic character traits");
    delete.description("Delete Character", "Delete this character. This will irrevocably delete "
        + "the character and will send a deletion request to the DM and all other players.");
    move.visible()
        .onClick(this::move)
        .description("Move Character", "This button moves the character to an other campaign.");
    timed.visible()
        .onClick(this::timed)
        .description("Add Condition", "Add a timed condition to this and/or other characters.");
    message.visible()
        .onClick(this::sendMessage)
        .description("Send Message", "Send a message to other characters and the DM");

    return view;
  }

  private void editImage() {
    if (character.isPresent()) {
      Intent intent = new Intent();
      // Show only images, no videos or anything else
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      // Always show the chooser (if there are multiple options available)
      startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }
  }

  private void editBase() {
    if (!canEdit() || !character.isPresent()) {
      return;
    }

    CharacterDialog.newInstance(character.get().getId(),
        character.get().getCampaignId()).display();
  }

  private void move() {
    ListSelectDialog fragment = ListSelectDialog.newInstance(
        R.string.character_select_campaign, "",
        campaigns().getCampaigns().stream()
            .map(m -> new ListSelectDialog.Entry(m.getName(), m.getId()))
            .collect(Collectors.toList()),
        R.color.campaign);
    fragment.setSelectListener(this::move);
    fragment.display();
  }

  private void move(String campaignId) {
    if (character.isPresent()) {
      character.get().setCampaignId(campaignId);
    }

    Optional<Campaign> campaign = campaigns().get(campaignId);
    if (campaign.isPresent()) {
      CompanionFragments.get().showCampaign(campaign.get(), Optional.empty());
    }
  }

  private void timed() {
    if (character.isPresent()) {
      TimedConditionDialog.newInstance(character.get().getId(), 0).display();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == PICK_IMAGE && resultCode == RESULT_OK &&
        data != null && data.getData() != null && character.isPresent()) {
      try {
        Uri uri = data.getData();
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
        images().set(character.get().getId(), bitmap);
      } catch (IOException e) {
        Status.toast("Cannot load image bitmap: " + e);
      }
    }
  }

  public boolean canEdit() {
    return campaign.isPresent() && character.isPresent();
  }

  private void sendMessage() {
    if (campaign.isPresent() && character.isPresent()) {
      MessageDialog.newInstance(campaign.get().getId(), character.get().getId()).display();
    }
  }
}
