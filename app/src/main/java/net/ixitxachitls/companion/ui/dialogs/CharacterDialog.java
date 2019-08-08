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

package net.ixitxachitls.companion.ui.dialogs;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.ui.fragments.ListSelectDialog;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledTextView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

/**
 * Fragment for editing a character (main values).
 */
public class CharacterDialog extends Dialog {

  private static final String ARG_ID = "id";
  private static final String ARG_CAMPAIGN_ID = "campaign_id";

  // The following values are only valid after onCreate().
  private Optional<Character> character = Optional.empty();

  // UI elements.
  private LabelledEditTextView name;
  private LabelledTextView gender;
  private LabelledTextView race;
  private Wrapper<Button> save;

  public CharacterDialog() {}

  public void editGender() {
    if (character.isPresent()) {
      ListSelectDialog edit = ListSelectDialog.newStringInstance(R.string.character_edit_gender,
          Lists.newArrayList(character.get().getGender().getName()), Gender.names(),
          R.color.character);
      edit.setSelectListener(this::updateGender);
      edit.display();
    }
  }

  public void editRace() {
    if (character.isPresent()) {
      ListSelectDialog edit = ListSelectDialog.newStringInstance(R.string.character_edit_race,
          character.get().getRace().isPresent()
              ? Lists.newArrayList(character.get().getRace().get().getName())
              : Collections.emptyList(),
          Templates.get().getMonsterTemplates().primaryRaces(), R.color.character);
      edit.setSelectListener(this::updateRace);
      edit.display();
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    String campaignId = getArguments().getString(ARG_CAMPAIGN_ID);
    String characterId = getArguments().getString(ARG_ID);
    if (characterId.isEmpty()) {
      character = Optional.of(characters().create(campaignId));
    } else {
      character = characters().get(characterId);
    }
  }

  @Override
  protected void createContent(View view) {
    if (character.isPresent()) {
      name = view.findViewById(R.id.edit_name);
      name.text(character.get().getName())
          .onChange(this::update);
      gender = view.findViewById(R.id.edit_gender);
      gender.onClick(this::editGender);
      race = view.findViewById(R.id.edit_race);
      race.onClick(this::editRace);
      save = Wrapper.<Button>wrap(view, R.id.save).onClick(this::save);
    }

    if (character.get().getGender() != Gender.UNKNOWN) {
      gender.text(character.get().getGender().getName());
    }

    if (character.get().getRace().isPresent()) {
      race.text(character.get().getRace().get().getName());
    }

    update();
  }

  @Override
  protected void save() {
    if (character.isPresent()) {
      character.get().setName(name.getText());
      if (gender.getTouchables().isEmpty()) {
        character.get().setGender(Gender.fromName(gender.getText()));
      }
      character.get().setRace(race.getText());
      character.get().store();

      super.save();
    }
  }

  protected void update() {
    if (character.isPresent()) {
      if (name.getText().length() == 0
          || gender.getText().startsWith("<")
          || race.getText().startsWith("<"))
      {
        save.invisible();
      } else {
        save.visible();
      }
    }
  }

  private boolean updateGender(List<String> values) {
    if (character.isPresent()) {
      gender.text(values.get(0));
      update();

      return true;
    }

    return false;
  }

  private boolean updateRace(List<String> values) {
    if (character.isPresent()) {
      race.text(values.get(0));
      update();

      return true;
    }

    return false;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String characterId, String campaignId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, characterId);
    arguments.putString(ARG_CAMPAIGN_ID, campaignId);
    return arguments;
  }

  public static CharacterDialog newInstance(String characterId, String campaignId) {
    CharacterDialog fragment = new CharacterDialog();
    fragment.setArguments(arguments(R.layout.fragment_edit_character,
        characterId.isEmpty() ? R.string.edit_character_add : R.string.edit_character_edit,
        R.color.character, characterId, campaignId));
    return fragment;
  }
}
