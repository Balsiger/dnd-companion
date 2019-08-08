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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.common.base.Preconditions;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.ui.dialogs.Dialog;
import net.ixitxachitls.companion.ui.views.EditAbility;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

/**
 * Dialog fragment to editAction the abilities of a character or monster.
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

  private Optional<Character> character = Optional.empty();

  public AbilitiesDialog() {}

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    character = characters().get(getArguments().getString(ARG_ID));
  }

  @Override
  protected void createContent(View view) {
    strength = view.findViewById(R.id.strength);
    dexterity = view.findViewById(R.id.dexterity);
    constitution = view.findViewById(R.id.constitution);
    intelligence = view.findViewById(R.id.intelligence);
    wisdom = view.findViewById(R.id.wisdom);
    charisma = view.findViewById(R.id.charisma);
    Wrapper.<Button>wrap(view, R.id.save).onClick(this::save);

    // Setup the layout parameters again after adding dynamic content.
    view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT));

    update();
  }

  @Override
  public void save() {
    if (character.isPresent()) {
      character.get().setBaseStrength(strength.getValue());
      character.get().setBaseDexterity(dexterity.getValue());
      character.get().setBaseConstitution(constitution.getValue());
      character.get().setBaseIntelligence(intelligence.getValue());
      character.get().setBaseWisdom(wisdom.getValue());
      character.get().setBaseCharisma(charisma.getValue());
      character.get().store();
    }

    super.save();
  }

  protected void update() {
    if (character.isPresent()) {
      strength.setValue(character.get().getStrength().getBase());
      dexterity.setValue(character.get().getDexterity().getBase());
      constitution.setValue(character.get().getConstitution().getBase());
      intelligence.setValue(character.get().getIntelligence().getBase());
      wisdom.setValue(character.get().getWisdom().getBase());
      charisma.setValue(character.get().getCharisma().getBase());
    }
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String characterId, String campaignId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, characterId);
    arguments.putString(ARG_CAMPAIGN_ID, campaignId);
    return arguments;
  }

  public static AbilitiesDialog newInstance(String characterId, String campaignId) {
    AbilitiesDialog fragment = new AbilitiesDialog();
    fragment.setArguments(arguments(R.layout.dialog_edit_abilities,
        R.string.edit_abilities, R.color.character, characterId, campaignId));
    return fragment;
  }
}
