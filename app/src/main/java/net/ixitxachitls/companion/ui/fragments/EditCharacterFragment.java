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
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.ui.Setup;

/**
 * Fragment for editing a character (main values).
 */
public class EditCharacterFragment extends EditFragment {

  private static final String ARG_ID = "id";
  private static final String ARG_CAMPAIGN_ID = "campaign_id";

  @FunctionalInterface
  public interface SaveAction {
    public void save(Character character);
  }

  private Optional<SaveAction> saveAction = Optional.absent();

  // The following values are only valid after onCreate().
  private Character character;
  private Campaign campaign;

  // UI elements.
  private EditText name;
  private TextView gender;
  private TextView race;
  private Button save;

  public EditCharacterFragment() {}

  public static EditCharacterFragment newInstance(String characterId, String campaignId) {
    EditCharacterFragment fragment = new EditCharacterFragment();
    fragment.setArguments(arguments(R.layout.fragment_edit_character,
        characterId.isEmpty() ? R.string.edit_character_add : R.string.edit_character_edit,
        R.color.character, characterId, campaignId));
    return fragment;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String characterId, String campaignId) {
    Bundle arguments = EditFragment.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, characterId);
    arguments.putString(ARG_CAMPAIGN_ID, campaignId);
    return arguments;
  }

  public void setSaveListener(@Nullable SaveAction save) {
    this.saveAction = Optional.of(save);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    campaign = Campaigns.get().getCampaign(getArguments().getString(ARG_CAMPAIGN_ID));
    character = Characters.get().getCharacter(getArguments().getString(ARG_ID),
        campaign.getCampaignId());
  }

  @Override
  protected void createContent(View view) {
    name = Setup.editText(view, R.id.edit_name, character.getName(), R.string.campaign_edit_name,
        R.color.character, null, this::update);
    gender = Setup.textView(view, R.id.edit_gender, this::editGender);
    race = Setup.textView(view, R.id.edit_race, this::editRace);
    save = Setup.button(view, R.id.save, this::save);

    update();
  }

  public void editGender() {
    ListSelectFragment edit = ListSelectFragment.newInstance(R.string.character_edit_gender,
        character.getGender().getName(), Gender.names(), R.color.character);
    edit.setSelectListener(this::updateGender);
    edit.display(getFragmentManager());
  }

  private boolean updateGender(String value, int position) {
    character.setGender(Gender.fromName(value));
    update();

    return true;
  }

  public void editRace() {
    ListSelectFragment edit = ListSelectFragment.newInstance(R.string.character_edit_race,
        character.getRace(), Entries.get().getMonsters().primaryRaces(), R.color.character);
    edit.setSelectListener(this::updateRace);
    edit.display(getFragmentManager());
  }

  private boolean updateRace(String value, int position) {
    character.setRace(value);
    update();

    return true;
  }

  protected void update() {
    if (name.getText().length() == 0
        && character.getGender() != Gender.UNKNOWN
        && !character.getRace().isEmpty()) {
      save.setVisibility(View.INVISIBLE);
    } else {
      save.setVisibility(View.VISIBLE);
    }

    if (character.getGender() != Gender.UNKNOWN) {
      gender.setText(character.getGender().getName());
    }

    if (!character.getRace().isEmpty()) {
      race.setText(character.getRace());
    }
  }

  @Override
  protected void save() {
    character.setName(name.getText().toString());

    if (saveAction.isPresent()) {
      saveAction.get().save(character);
    }

    super.save();
  }
}
