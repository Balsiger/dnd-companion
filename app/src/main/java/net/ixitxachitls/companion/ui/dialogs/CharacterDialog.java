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

package net.ixitxachitls.companion.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.ui.fragments.ListSelectFragment;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * Fragment for editing a character (main values).
 */
public class CharacterDialog extends Dialog {

  private static final String ARG_ID = "id";
  private static final String ARG_CAMPAIGN_ID = "campaign_id";

  // The following values are only valid after onCreate().
  private Optional<Character> character = Optional.absent();
  private Optional<Campaign> campaign = Optional.absent();

  // UI elements.
  private EditTextWrapper<EditText> name;
  private TextWrapper<TextView> gender;
  private TextWrapper<TextView> race;
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
    campaign = Campaigns.getCampaign(getArguments().getString(ARG_CAMPAIGN_ID)).getValue();
    if (campaign.isPresent()) {
      String characterId = getArguments().getString(ARG_ID);
      if (characterId.isEmpty()) {
        character = Optional.of(Character.createNew(campaign.get().getCampaignId()));
      } else {
        character = Characters.getCharacter(characterId).getValue();
      }
    } else {
      character = Optional.absent();
    }
  }

  @Override
  protected void createContent(View view) {
    if (character.isPresent()) {
      name = EditTextWrapper.wrap(view, R.id.edit_name)
          .text(character.get().getName())
          .label(R.string.campaign_edit_name)
          .lineColor(R.color.character)
          .onChange(this::update);
      gender = TextWrapper.wrap(view, R.id.edit_gender).onClick(this::editGender);
      race = TextWrapper.wrap(view, R.id.edit_race).onClick(this::editRace);
      save = Wrapper.<Button>wrap(view, R.id.save).onClick(this::save);
    }

    update();
  }

  public void editGender() {
    if (character.isPresent()) {
      ListSelectFragment edit = ListSelectFragment.newStringInstance(R.string.character_edit_gender,
          character.get().getGender().getName(), Gender.names(), R.color.character);
      edit.setSelectListener(this::updateGender);
      edit.display();
    }
  }

  private boolean updateGender(String value) {
    if (character.isPresent()) {
      character.get().setGender(Gender.fromName(value));
      update();

      return true;
    }

    return false;
  }

  public void editRace() {
    if (character.isPresent()) {
      ListSelectFragment edit = ListSelectFragment.newStringInstance(R.string.character_edit_race,
          character.get().getRace(), Entries.get().getMonsters().primaryRaces(), R.color.character);
      edit.setSelectListener(this::updateRace);
      edit.display();
    }
  }

  private boolean updateRace(String value) {
    if (character.isPresent()) {
      character.get().setRace(value);
      update();

      return true;
    }

    return false;
  }

  protected void update() {
    if (character.isPresent()) {
      if (name.getText().length() == 0
          && character.get().getGender() != Gender.UNKNOWN
          && !character.get().getRace().isEmpty()) {
        save.invisible();
      } else {
        save.visible();
      }

      if (character.get().getGender() != Gender.UNKNOWN) {
        gender.text(character.get().getGender().getName());
      }

      if (!character.get().getRace().isEmpty()) {
        race.text(character.get().getRace());
      }
    }
  }

  @Override
  protected void save() {
    if (character.isPresent()) {
      character.get().setName(name.getText().toString());
      character.get().store();

      super.save();
    }
  }
}
