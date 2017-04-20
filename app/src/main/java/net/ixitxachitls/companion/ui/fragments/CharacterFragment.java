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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.ui.Setup;

/**
 * Fragment for displaying character information.
 */
public class CharacterFragment extends CompanionFragment {
  private Character character;
  private Campaign campaign;

  // UI elements.
  private TextView title;
  private TextView subtitle;
  private TextView strength;
  private TextView dexterity;
  private TextView constitution;
  private TextView intelligence;
  private TextView wisdom;
  private TextView charisma;

  public CharacterFragment() {
    super(Type.character);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_character, container, false);

    title = Setup.textView(view, R.id.title, this::editBase);
    subtitle = Setup.textView(view, R.id.subtitle, this::editBase);
    Setup.textView(view, R.id.strength_label, this::editAbilities);
    strength = Setup.textView(view, R.id.strength, this::editAbilities);
    Setup.textView(view, R.id.dexterity_label, this::editAbilities);
    dexterity = Setup.textView(view, R.id.dexterity, this::editAbilities);
    Setup.textView(view, R.id.constitution_label, this::editAbilities);
    constitution = Setup.textView(view, R.id.constitution, this::editAbilities);
    Setup.textView(view, R.id.intelligence_label, this::editAbilities);
    intelligence = Setup.textView(view, R.id.intelligence, this::editAbilities);
    Setup.textView(view, R.id.wisdom_label, this::editAbilities);
    wisdom = Setup.textView(view, R.id.wisdom, this::editAbilities);
    Setup.textView(view, R.id.charisma_label, this::editAbilities);
    charisma = Setup.textView(view, R.id.charisma, this::editAbilities);

    return view;
  }

  public void showCharacter(Character character) {
    this.character = character;
    this.campaign = Campaigns.get().getCampaign(character.getCampaignId());

    refresh();
  }

  private void editBase() {
    if (!canEdit()) {
      return;
    }

    EditCharacterFragment.newInstance(character.getCharacterId(), character.getCampaignId())
        .display(getFragmentManager());
  }

  private void editAbilities() {
    if (!canEdit()) {
      return;
    }

    EditAbilitiesFragment.newInstance(character.getCharacterId(), character.getCampaignId())
        .display(getFragmentManager());
  }

  public boolean canEdit() {
    return campaign.isDefault() || !campaign.isLocal();
  }

  @Override
  public void refresh() {
    if (character == null) {
      return;
    }

    title.setText(character.getName());
    subtitle.setText(character.getGender().getName() + " " + character.getRace());
    strength.setText(
        character.getStrength() + " (" + Ability.modifier(character.getStrength()) + ")");
    dexterity.setText(
        character.getDexterity() + " (" + Ability.modifier(character.getDexterity()) + ")");
    constitution.setText(
        character.getConstitution() + " (" + Ability.modifier(character.getConstitution()) + ")");
    intelligence.setText(
        character.getIntelligence() + " (" + Ability.modifier(character.getIntelligence()) + ")");
    wisdom.setText(
        character.getWisdom() + " (" + Ability.modifier(character.getWisdom()) + ")");
    charisma.setText(
        character.getCharisma() + " (" + Ability.modifier(character.getCharisma()) + ")");
  }
}
