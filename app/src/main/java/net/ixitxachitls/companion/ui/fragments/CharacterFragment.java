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
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.data.dynamics.Images;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.rules.XP;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.views.AbilityView;
import net.ixitxachitls.companion.ui.views.RoundImageView;
import net.ixitxachitls.companion.ui.views.TitleView;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

/**
 * Fragment for displaying character information.
 */
public class CharacterFragment extends CompanionFragment {

  protected Optional<Character> character = Optional.empty();
  protected Optional<Campaign> campaign = Optional.empty();
  protected boolean storeOnPause = true;

  // UI elements.
  protected TitleView title;
  protected TextWrapper<TextView> campaignTitle;
  protected AbilityView strength;
  protected AbilityView dexterity;
  protected AbilityView constitution;
  protected AbilityView intelligence;
  protected AbilityView wisdom;
  protected AbilityView charisma;
  protected RoundImageView image;
  protected Wrapper<FloatingActionButton> edit;
  protected Wrapper<FloatingActionButton> delete;
  protected Wrapper<FloatingActionButton> move;
  protected EditTextWrapper<EditText> xp;
  protected TextWrapper<TextView> xpNext;
  protected EditTextWrapper<EditText> level;
  protected Wrapper<FloatingActionButton> back;

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

    back = Wrapper.<FloatingActionButton>wrap(view, R.id.back)
        .onClick(this::goBack)
        .description("Back to Campaign", "Go back to this characters campaign view.");
    image = view.findViewById(R.id.image);
    title = view.findViewById(R.id.title);
    campaignTitle = TextWrapper.wrap(view, R.id.campaign);
    edit = Wrapper.<FloatingActionButton>wrap(view, R.id.edit).gone();
    delete = Wrapper.<FloatingActionButton>wrap(view, R.id.delete).gone();
    move = Wrapper.<FloatingActionButton>wrap(view, R.id.move).gone();
    strength = view.findViewById(R.id.strength);
    dexterity = view.findViewById(R.id.dexterity);
    constitution = view.findViewById(R.id.constitution);
    intelligence = view.findViewById(R.id.intelligence);
    wisdom = view.findViewById(R.id.wisdom);
    charisma = view.findViewById(R.id.charisma);

    xp = EditTextWrapper.wrap(view, R.id.xp)
        .lineColor(R.color.character);
    xpNext = TextWrapper.wrap(view, R.id.xp_next);
    level = EditTextWrapper.wrap(view, R.id.level)
        .lineColor(R.color.character);

    update(character);
    return view;
  }

  @Override
  public void onPause() {
    super.onPause();

    if (storeOnPause && character.isPresent()) {
      character.get().store();
    }
  }

  public void showCharacter(Character character) {
    if (this.character.isPresent()) {
      Characters.getCharacter(this.character.get().getCharacterId()).removeObservers(this);
    }

    this.character = Optional.of(character);
    this.campaign = Campaigns.getCampaign(character.getCampaignId()).getValue();

    Characters.getCharacter(character.getCharacterId()).observe(this, this::update);
  }

  private void update(Optional<Character> character) {
    this.character = character;

    if (!character.isPresent() || !campaign.isPresent()) {
      this.campaign = Optional.empty();
      return;
    }

    campaign = Campaigns.getCampaign(character.get().getCampaignId()).getValue();

    campaignTitle.text(campaign.get().getName());
    title.setTitle(character.get().getName());
    title.setSubtitle(character.get().getGender().getName() + " " + character.get().getRace());
    Optional<Image> characterImage = Images.get(character.get().isLocal()).getImage(
        Character.TABLE, character.get().getCharacterId()).getValue();
    if (characterImage.isPresent()) {
      image.setImageBitmap(characterImage.get().getBitmap());
    } else {
      image.clearImage();
    }
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
      CompanionFragments.get().show(Type.campaigns, Optional.empty());
    }
    return true;
  }
}
