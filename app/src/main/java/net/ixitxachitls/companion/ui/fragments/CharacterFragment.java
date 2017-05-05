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

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.ui.Setup;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.dialogs.EditCharacterDialog;
import net.ixitxachitls.companion.ui.views.AbilityView;
import net.ixitxachitls.companion.ui.views.ActionButton;
import net.ixitxachitls.companion.ui.views.TitleView;

/**
 * Fragment for displaying character information.
 */
public class CharacterFragment extends CompanionFragment {
  private Character character;
  private Campaign campaign;

  // UI elements.
  private TitleView title;
  private AbilityView strength;
  private AbilityView dexterity;
  private AbilityView constitution;
  private AbilityView intelligence;
  private AbilityView wisdom;
  private AbilityView charisma;
  private ActionButton battle;

  public CharacterFragment() {
    super(Type.character);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_character, container, false);

    title = (TitleView) view.findViewById(R.id.title);
    title.setAction(this::editBase);
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

  private void showBattle() {
    if (canEdit()) {
      CompanionFragments.get().showBattle(character);
    }
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

    EditCharacterDialog.newInstance(character.getCharacterId(), character.getCampaignId())
        .display(getFragmentManager());
  }

  private void editAbilities() {
    if (!canEdit()) {
      return;
    }

    EditAbilitiesDialog.newInstance(character.getCharacterId(), character.getCampaignId())
        .display(getFragmentManager());
  }

  public boolean canEdit() {
    return campaign.isDefault() || !campaign.isLocal();
  }

  @Override
  public void refresh() {
    super.refresh();

    if (character == null) {
      return;
    }

    character = Characters.get().getCharacter(character.getCharacterId(), campaign.getCampaignId());
    if (campaign != null) {
      campaign = Campaigns.get().getCampaign(campaign.getCampaignId());
    }

    title.setTitle(character.getName());
    title.setSubtitle(character.getGender().getName() + " " + character.getRace());
    strength.setValue(character.getStrength(), Ability.modifier(character.getStrength()));
    dexterity.setValue(character.getDexterity(), Ability.modifier(character.getDexterity()));
    constitution.setValue(character.getConstitution(),
        Ability.modifier(character.getConstitution()));
    intelligence.setValue(character.getIntelligence(),
        Ability.modifier(character.getIntelligence()));
    wisdom.setValue(character.getWisdom(), Ability.modifier(character.getWisdom()));
    charisma.setValue(character.getCharisma(), Ability.modifier(character.getCharisma()));

    battle.setVisibility(canEdit() ? View.VISIBLE : View.GONE);
    battle.pulse(!campaign.getBattle().isEnded());
  }
}
