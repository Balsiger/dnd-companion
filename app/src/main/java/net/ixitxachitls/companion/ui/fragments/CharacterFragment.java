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
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.data.dynamics.Images;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.rules.XP;
import net.ixitxachitls.companion.ui.ConfirmationDialog;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.CharacterDialog;
import net.ixitxachitls.companion.ui.views.AbilityView;
import net.ixitxachitls.companion.ui.views.RoundImageView;
import net.ixitxachitls.companion.ui.views.TitleView;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.io.IOException;
import java.util.stream.Collectors;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment for displaying character information.
 */
public class CharacterFragment extends CompanionFragment {

  private final String TAG = "CharacterFragment";
  private final int PICK_IMAGE = 1;

  private Optional<Character> character = Optional.absent();
  private Optional<Campaign> campaign = Optional.absent();
  private boolean storeOnPause = true;

  // UI elements.
  private TitleView title;
  private TextWrapper<TextView> campaignTitle;
  private AbilityView strength;
  private AbilityView dexterity;
  private AbilityView constitution;
  private AbilityView intelligence;
  private AbilityView wisdom;
  private AbilityView charisma;
  private RoundImageView image;
  private Wrapper<FloatingActionButton> delete;
  private Wrapper<FloatingActionButton> move;
  private EditTextWrapper<EditText> xp;
  private TextWrapper<TextView> xpNext;
  private EditTextWrapper<EditText> level;
  private Wrapper<FloatingActionButton> back;

  public CharacterFragment() {
    super(Type.character);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    storeOnPause = true;

    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_character, container, false);

    back = Wrapper.<FloatingActionButton>wrap(view, R.id.back).onClick(this::goBack);
    image = view.findViewById(R.id.image);
    image.setAction(this::editImage);
    title = view.findViewById(R.id.title);
    title.setAction(this::editBase);
    campaignTitle = TextWrapper.wrap(view, R.id.campaign);
    delete = Wrapper.wrap(view, R.id.delete);
    delete.onClick(this::delete);
    if (character.isPresent() && !character.get().isLocal() &&
        campaign.isPresent() && !campaign.get().amDM()) {
      delete.gone();
    }
    move = Wrapper.wrap(view, R.id.move);
    move.onClick(this::move);
    strength = view.findViewById(R.id.strength);
    strength.setAction(this::editAbilities);
    dexterity = view.findViewById(R.id.dexterity);
    dexterity.setAction(this::editAbilities);
    constitution = view.findViewById(R.id.constitution);
    constitution.setAction(this::editAbilities);
    intelligence = view.findViewById(R.id.intelligence);
    intelligence.setAction(this::editAbilities);
    wisdom = view.findViewById(R.id.wisdom);
    wisdom.setAction(this::editAbilities);
    charisma = view.findViewById(R.id.charisma);
    charisma.setAction(this::editAbilities);

    xp = EditTextWrapper.wrap(view, R.id.xp)
        .lineColor(R.color.character)
        .onChange(this::changeXp);
    xpNext = TextWrapper.wrap(view, R.id.xp_next);
    level = EditTextWrapper.wrap(view, R.id.level)
        .lineColor(R.color.character)
        .onChange(this::changeLevel);

    return view;
  }

  @Override
  public void onPause() {
    super.onPause();

    if (storeOnPause && character.isPresent()) {
      character.get().store();
    }
  }

  @Override
  public void onResume() {
    super.onResume();


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
      Characters.remove(character.get());
      Toast.makeText(getActivity(), getString(R.string.character_deleted),
          Toast.LENGTH_SHORT).show();

      storeOnPause = false;
      show(Type.campaign);
    }
  }

  private void move() {
    ListSelectFragment fragment = ListSelectFragment.newInstance(
        R.string.character_select_campaign, "",
        Campaigns.getAllCampaigns().stream()
            .map(m -> new ListSelectFragment.Entry(m.getName(), m.getCampaignId()))
            .collect(Collectors.toList()),
        R.color.campaign);
    fragment.setSelectListener(this::move);
    fragment.display();
  }

  private void move(String campaignId) {
    if (character.isPresent()) {
      character.get().setCampaignId(campaignId);
    }

    Optional<Campaign> campaign = Campaigns.getCampaign(campaignId).getValue();
    if (campaign.isPresent()) {
      CompanionFragments.get().showCampaign(campaign.get(), Optional.absent());
    }
  }

  public void showCharacter(Character character) {
    this.character = Optional.of(character);
    this.campaign = Campaigns.getCampaign(character.getCampaignId()).getValue();

    refresh();
  }

  private void editBase() {
    if (!canEdit() || !character.isPresent()) {
      return;
    }

    CharacterDialog.newInstance(character.get().getCharacterId(),
        character.get().getCampaignId()).display();
  }

  private void editImage() {
    if (character.isPresent() && character.get().isLocal()) {
      Intent intent = new Intent();
      // Show only images, no videos or anything else
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      // Always show the chooser (if there are multiple options available)
      startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }
  }

  private void changeXp() {
    if (character.isPresent() && !xp.getText().isEmpty()) {
      character.get().setXp(Integer.parseInt(xp.getText()));
    }
  }

  private void changeLevel() {
    if (character.isPresent() && !level.getText().isEmpty()) {
      character.get().setLevel(Integer.parseInt(level.getText()));
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
        Image characterImage = new Image(Character.TABLE, character.get().getCharacterId(), bitmap);
        characterImage.save(character.get().isLocal());
        characterImage.publish();
        image.setImageBitmap(characterImage.getBitmap());
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

    AbilitiesDialog.newInstance(character.get().getCharacterId(),
        character.get().getCampaignId()).display();
  }

  public boolean canEdit() {
    return campaign.isPresent() && character.isPresent() && character.get().isLocal();
  }

  @Override
  public void refresh() {
    super.refresh();

    if (!character.isPresent() || !campaign.isPresent()) {
      return;
    }

    character = Characters.getCharacter(character.get().getCharacterId()).getValue();
    if (!character.isPresent()) {
      return;
    }

    campaign = Campaigns.getCampaign(campaign.get().getCampaignId()).getValue();

    Optional<Image> characterImage = Images.get(character.get().isLocal()).getImage(
        Character.TABLE, character.get().getCharacterId()).getValue();
    if (characterImage.isPresent()) {
      image.setImageBitmap(characterImage.get().getBitmap());
    } else {
      image.clearImage();
    }
    title.setTitle(character.get().getName());
    title.setSubtitle(character.get().getGender().getName() + " " + character.get().getRace());
    campaignTitle.text(campaign.get().getName());
    move.visible(character.get().isLocal());
    delete.visible(character.get().isLocal());
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
    xp.text(String.valueOf(character.get().getXp()));
    xpNext.text("(next level " + XP.xpForLevel(character.get().getLevel()) + ")");
    level.text(String.valueOf(character.get().getLevel()));
  }

  @Override
  public boolean goBack() {
    if (campaign.isPresent()) {
      CompanionFragments.get().showCampaign(campaign.get(), Optional.of(title));
    } else {
      CompanionFragments.get().show(Type.campaigns, Optional.absent());
    }
    return true;
  }
}
