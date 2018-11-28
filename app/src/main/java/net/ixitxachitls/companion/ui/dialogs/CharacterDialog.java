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
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Button;

import com.google.common.base.Preconditions;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.ui.fragments.ListSelectDialog;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledTextView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

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

  public static CharacterDialog newInstance(String characterId, String campaignId) {
    CharacterDialog fragment = new CharacterDialog();
    fragment.setArguments(arguments(R.layout.fragment_edit_character,
        characterId.isEmpty() ? R.string.edit_character_add : R.string.edit_character_edit,
        R.color.character, characterId, campaignId));
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

  public void editGender() {
    if (character.isPresent()) {
      ListSelectDialog edit = ListSelectDialog.newStringInstance(R.string.character_edit_gender,
          character.get().getGender().getName(), Gender.names(), R.color.character);
      edit.setSelectListener(this::updateGender);
      edit.display();
    }
  }

  private boolean updateGender(String value) {
    if (character.isPresent()) {
      gender.text(value);
      update();

      return true;
    }

    return false;
  }

  public void editRace() {
    if (character.isPresent()) {
      ListSelectDialog edit = ListSelectDialog.newStringInstance(R.string.character_edit_race,
          character.get().getRace().isPresent() ? character.get().getRace().get().getName() : "",
          Entries.get().getMonsterTemplates().primaryRaces(), R.color.character);
      edit.setSelectListener(this::updateRace);
      edit.display();
    }
  }

  private boolean updateRace(String value) {
    if (character.isPresent()) {
      race.text(value);
      update();

      return true;
    }

    return false;
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

  @Override
  protected void save() {
    if (character.isPresent()) {
      character.get().setName(name.getText());
      character.get().setGender(Gender.fromName(gender.getText()));
      character.get().setRace(race.getText());
      character.get().store();

      super.save();
    }
  }
}
