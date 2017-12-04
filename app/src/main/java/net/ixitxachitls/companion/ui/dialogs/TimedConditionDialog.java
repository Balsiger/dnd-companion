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

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog to select a timed condition for party members.
 */
public class TimedConditionDialog extends Dialog {

  private static final String ARG_ID = "id";
  private static final String ARG_ROUND = "round";

  private Optional<Character> character = Optional.absent();
  private Optional<Campaign> campaign = Optional.absent();
  private int currentRound = 0;

  private EditTextWrapper<AutoCompleteTextView> condition;
  private Wrapper<LinearLayout> party;
  private Map<String, CheckBox> checkboxesByCharacterId = new HashMap<>();
  private EditTextWrapper<EditText> rounds;

  public TimedConditionDialog() {}

  public static TimedConditionDialog newInstance(String characterId, int currentRound) {
    TimedConditionDialog dialog = new TimedConditionDialog();
    dialog.setArguments(arguments(R.layout.dialog_timed_condition,
        R.string.edit_timed_condition, R.color.character, characterId, currentRound));
    return dialog;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String characterId, int currentRound) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, characterId);
    arguments.putInt(ARG_ROUND, currentRound);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    character = Characters.getCharacter(getArguments().getString(ARG_ID)).getValue();
    currentRound = getArguments().getInt(ARG_ROUND);
    if (character.isPresent()) {
      campaign = Campaigns.getCampaign(character.get().getCampaignId());
    }
  }

  @Override
  protected void createContent(View view) {
    condition = EditTextWrapper.<AutoCompleteTextView>wrap(view, R.id.condition)
        .lineColor(R.color.character).onChange(this::selectCondition);
    condition.get().setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
          condition.get().showDropDown();
        }
      }
    });
    rounds = EditTextWrapper.wrap(view, R.id.rounds).lineColor(R.color.character);
    party = Wrapper.<LinearLayout>wrap(view, R.id.party);
    Wrapper.<Button>wrap(view, R.id.save).onClick(this::save);

    if (character.isPresent()) {
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
          R.layout.list_item_select, character.get().conditionHistoryNames());
      condition.get().setAdapter(adapter);

      if (campaign.isPresent()) {
        for (Character character : campaign.get().getCharacters()) {
          CheckBox checkbox = new CheckBox(getContext());
          checkbox.setText(character.getName());
          checkbox.setTextAppearance(R.style.LargeText);
          checkbox.setButtonTintList(ColorStateList.valueOf(view.getResources()
              .getColor(color, null)));
          party.get().addView(checkbox);
          checkboxesByCharacterId.put(character.getCharacterId(), checkbox);
        }
      }
    }
  }

  private void selectCondition() {
    if (character.isPresent()) {
      Optional<Character.TimedCondition> selected =
          character.get().getHistoryCondition(condition.getText());
      if (selected.isPresent()) {
        rounds.text(String.valueOf(selected.get().getRounds()));
        for (String id : selected.get().getCharacterIds()) {
          if (checkboxesByCharacterId.containsKey(id)) {
            checkboxesByCharacterId.get(id).setChecked(true);
          }
        }
      }
    }
  }

  @Override
  protected void save() {
    if (character.isPresent() && !condition.getText().isEmpty()) {
      List<String> ids = new ArrayList<>();
      for (Map.Entry<String, CheckBox> entry : checkboxesByCharacterId.entrySet()) {
        if (entry.getValue().isChecked()) {
          ids.add(entry.getKey());
        }
      }

      if (ids.isEmpty() || rounds.getText().isEmpty() || Integer.parseInt(rounds.getText()) <= 0) {
        return;
      }

      int rounds = Integer.parseInt(this.rounds.getText());
      character.get().addTimedCondition(new Character.TimedCondition(
          rounds, currentRound + rounds,  ids, condition.getText()));
    }

    super.save();
  }
}
