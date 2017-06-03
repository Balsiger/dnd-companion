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

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.common.base.Optional;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Images;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.ui.ConfirmationDialog;
import net.ixitxachitls.companion.ui.Setup;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.EditCharacterDialog;
import net.ixitxachitls.companion.ui.views.AbilityView;
import net.ixitxachitls.companion.ui.views.ActionButton;
import net.ixitxachitls.companion.ui.views.IconView;
import net.ixitxachitls.companion.ui.views.RoundImageView;
import net.ixitxachitls.companion.ui.views.TitleView;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment for displaying character information.
 */
public class CharacterFragment extends CompanionFragment {

  private final String TAG = "CharacterFragment";
  private final int PICK_IMAGE = 1;

  private Optional<Character> character = Optional.absent();
  private Optional<Campaign> campaign = Optional.absent();

  // UI elements.
  private TitleView title;
  private AbilityView strength;
  private AbilityView dexterity;
  private AbilityView constitution;
  private AbilityView intelligence;
  private AbilityView wisdom;
  private AbilityView charisma;
  private ActionButton battle;
  private RoundImageView image;
  private IconView delete;

  public CharacterFragment() {
    super(Type.character);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_character, container, false);

    image = (RoundImageView) view.findViewById(R.id.image);
    image.setAction(this::editImage);
    title = (TitleView) view.findViewById(R.id.title);
    title.setAction(this::editBase);
    delete = (IconView) view.findViewById(R.id.delete);
    delete.setAction(this::delete);
    strength = (AbilityView) view.findViewById(R.id.strength);
    strength.setAction(this::editAbilities);
    dexterity = (AbilityView) view.findViewById(R.id.dexterity);
    dexterity.setAction(this::editAbilities);
    constitution = (AbilityView) view.findViewById(R.id.constitution);
    constitution.setAction(this::editAbilities);
    intelligence = (AbilityView) view.findViewById(R.id.intelligence);
    intelligence.setAction(this::editAbilities);
    wisdom = (AbilityView) view.findViewById(R.id.wisdom);
    wisdom.setAction(this::editAbilities);
    charisma = (AbilityView) view.findViewById(R.id.charisma);
    charisma.setAction(this::editAbilities);

    battle = Setup.actionButton(view, R.id.battle, this::showBattle);

    return view;
  }

  private void delete() {
    ConfirmationDialog.create(getContext())
        .title(getResources().getString(R.string.character_delete_title))
        .message(getResources().getString(R.string.character_delete_message))
        .yes(this::deleteCharacterOk)
        .show();
  }

  private void deleteCharacterOk() {
    if (character.isPresent()) {
      Characters.get(character.get().isLocal()).remove(character.get());
      Toast.makeText(getActivity(), getString(R.string.character_deleted),
          Toast.LENGTH_SHORT).show();
      show(Type.campaign);
    }
  }

  private void showBattle() {
    if (canEdit() && character.isPresent()) {
      CompanionFragments.get().showBattle(character.get());
    }
  }

  public void showCharacter(Character character) {
    this.character = Optional.of(character);
    this.campaign = Campaigns.get(!character.isLocal()).getCampaign(character.getCampaignId());

    refresh();
  }

  private void editBase() {
    if (!canEdit() || !character.isPresent()) {
      return;
    }

    EditCharacterDialog.newInstance(character.get().getCharacterId(),
        character.get().getCampaignId()).display(getFragmentManager());
  }

  private void editImage() {
    Intent intent = new Intent();
    // Show only images, no videos or anything else
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    // Always show the chooser (if there are multiple options available)
    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == PICK_IMAGE && resultCode == RESULT_OK &&
        data != null && data.getData() != null && character.isPresent()) {
      try {
        Uri uri = data.getData();
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
        bitmap =
            Images.get(character.get().isLocal()).saveAndPublish(character.get().getCampaignId(),
                Character.TYPE, character.get().getCharacterId(), bitmap);
        image.setImageBitmap(bitmap);
      } catch (IOException e) {
        Log.e(TAG, "Cannot load image bitmap", e);
        e.printStackTrace();
      }
    }
  }

  private void editAbilities() {
    if (!canEdit() || !character.isPresent()) {
      return;
    }

    EditAbilitiesDialog.newInstance(character.get().getCharacterId(),
        character.get().getCampaignId()).display(getFragmentManager());
  }

  public boolean canEdit() {
    return campaign.isPresent() && (campaign.get().isDefault() || !campaign.get().isLocal());
  }

  @Override
  public void refresh() {
    super.refresh();

    if (!character.isPresent() || !campaign.isPresent()) {
      return;
    }

    character = Characters.get(character.get().isLocal())
        .getCharacter(character.get().getCharacterId(), campaign.get().getCampaignId());
    if (!character.isPresent()) {
      return;
    }

    campaign = Campaigns.get(campaign.get().isLocal()).getCampaign(campaign.get().getCampaignId());

    Optional<Bitmap> bitmap =
        Images.get(character.get().isLocal()).load(Character.TYPE, character.get().getCharacterId());
    if (bitmap.isPresent()) {
      image.setImageBitmap(bitmap.get());
    }
    title.setTitle(character.get().getName());
    title.setSubtitle(character.get().getGender().getName() + " " + character.get().getRace());
    strength.setValue(character.get().getStrength(),
        Ability.modifier(character.get().getStrength()));
    dexterity.setValue(character.get().getDexterity(),
        Ability.modifier(character.get().getDexterity()));
    constitution.setValue(character.get().getConstitution(),
        Ability.modifier(character.get().getConstitution()));
    intelligence.setValue(character.get().getIntelligence(),
        Ability.modifier(character.get().getIntelligence()));
    wisdom.setValue(character.get().getWisdom(),
        Ability.modifier(character.get().getWisdom()));
    charisma.setValue(character.get().getCharisma(),
        Ability.modifier(character.get().getCharisma()));

    battle.setVisibility(canEdit() ? View.VISIBLE : View.GONE);
    battle.pulse(!campaign.get().getBattle().isEnded());
  }
}
