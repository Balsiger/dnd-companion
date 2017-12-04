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
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.ui.dialogs.Dialog;
import net.ixitxachitls.companion.ui.views.EditAbility;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * Dialog fragment to edit the abilities of a character or monster.
 */
public class AbilitiesDialog extends Dialog {

  private static final String ARG_ID = "id";
  private static final String ARG_CAMPAIGN_ID = "campaign_id";

  // Ui elements.
  private EditAbility strength;
  private EditAbility constitution;
  private EditAbility dexterity;
  private EditAbility intelligence;
  private EditAbility wisdom;
  private EditAbility charisma;

  private Optional<Campaign> campaign = Optional.absent();
  private Optional<Character> character = Optional.absent();

  public AbilitiesDialog() {}

  public static AbilitiesDialog newInstance(String characterId, String campaignId) {
    AbilitiesDialog fragment = new AbilitiesDialog();
    fragment.setArguments(arguments(R.layout.dialog_edit_abilities,
        R.string.edit_abilities, R.color.character, characterId, campaignId));
    return fragment;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String characterId, String campaignId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, characterId);
    arguments.putString(ARG_CAMPAIGN_ID, campaignId);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    campaign = Campaigns.getCampaign(getArguments().getString(ARG_CAMPAIGN_ID));
    if (campaign.isPresent()) {
      character = Characters.getCharacter(getArguments().getString(ARG_ID)).getValue();
    } else {
      character = Optional.absent();
    }
  }

  @Override
  protected void createContent(View view) {
    strength = (EditAbility) view.findViewById(R.id.strength);
    strength.setOnChange(this::change);
    dexterity = (EditAbility) view.findViewById(R.id.dexterity);
    dexterity.setOnChange(this::change);
    constitution = (EditAbility) view.findViewById(R.id.constitution);
    constitution.setOnChange(this::change);
    intelligence = (EditAbility) view.findViewById(R.id.intelligence);
    intelligence.setOnChange(this::change);
    wisdom = (EditAbility) view.findViewById(R.id.wisdom);
    wisdom.setOnChange(this::change);
    charisma = (EditAbility) view.findViewById(R.id.charisma);
    charisma.setOnChange(this::change);
    Wrapper.<Button>wrap(view, R.id.save).onClick(this::save);

    // Setup the layout parameters again after adding dynamic content.
    view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT));

    update();
  }

  private void change() {
    if (character.isPresent()) {
      character.get().setStrength(strength.getValue());
      character.get().setDexterity(dexterity.getValue());
      character.get().setConstitution(constitution.getValue());
      character.get().setIntelligence(intelligence.getValue());
      character.get().setWisdom(wisdom.getValue());
      character.get().setCharisma(charisma.getValue());
    }
  }

  protected void update() {
    if (character.isPresent()) {
      strength.setValue(character.get().getStrength());
      dexterity.setValue(character.get().getDexterity());
      constitution.setValue(character.get().getConstitution());
      intelligence.setValue(character.get().getIntelligence());
      wisdom.setValue(character.get().getWisdom());
      charisma.setValue(character.get().getCharisma());
    }
  }
}
