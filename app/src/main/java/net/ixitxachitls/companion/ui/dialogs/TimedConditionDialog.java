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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Creature;
import net.ixitxachitls.companion.data.dynamics.Creatures;
import net.ixitxachitls.companion.rules.Conditions;
import net.ixitxachitls.companion.ui.views.LabelledAutocompleteTextView;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dialog to select a timed condition for party members.
 */
public class TimedConditionDialog extends Dialog {

  private static final String ARG_ID = "id";
  private static final String ARG_ROUND = "round";

  // State.
  private Optional<? extends BaseCreature> creature = Optional.absent();
  private Optional<Campaign> campaign = Optional.absent();
  private int currentRound = 0;
  private boolean predefined = false;

  // UI Elements.
  private LabelledAutocompleteTextView condition;
  private Wrapper<LinearLayout> party;
  private Map<String, CheckBox> checkboxesByCreatureId = new HashMap<>();
  private LabelledEditTextView rounds;
  private LabelledEditTextView description;

  public TimedConditionDialog() {}

  public static TimedConditionDialog newInstance(String creatureId, int currentRound) {
    TimedConditionDialog dialog = new TimedConditionDialog();
    dialog.setArguments(arguments(R.layout.dialog_timed_condition,
        R.string.edit_timed_condition, R.color.character, creatureId, currentRound));
    return dialog;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String creatureId, int currentRound) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, creatureId);
    arguments.putInt(ARG_ROUND, currentRound);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    String id = getArguments().getString(ARG_ID);
    creature = id.startsWith(Character.TYPE)
        ? Characters.getCharacter(id).getValue() : Creatures.getCreature(id).getValue();

    currentRound = getArguments().getInt(ARG_ROUND);
    if (creature.isPresent()) {
      campaign = Campaigns.getCampaign(creature.get().getCampaignId()).getValue();
    }
  }

  @Override
  protected void createContent(View view) {
    condition = view.findViewById(R.id.condition);
    condition.onChange(this::selectCondition).onFocus(condition::showDropDown);
    rounds = view.findViewById(R.id.rounds);
    description = view.findViewById(R.id.description);
    party = Wrapper.<LinearLayout>wrap(view, R.id.party);
    Wrapper.<Button>wrap(view, R.id.save).onClick(this::save);

    if (creature.isPresent()) {
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
          R.layout.list_item_select,
          conditionNames(creature.get()));
      condition.setAdapter(adapter);

      if (campaign.isPresent()) {
        for (Character character : campaign.get().getCharacters()) {
          addCheckbox(view, character.getCharacterId(), character.getName());
        }

        for (Creature creature : campaign.get().getCreatures()) {
          addCheckbox(view, creature.getCreatureId(), creature.getName());
        }
      }
    }
  }

  private List<BaseCreature.TimedCondition> conditions(BaseCreature creature) {
    List<BaseCreature.TimedCondition> conditions = new ArrayList<>();

    if (creature instanceof Character) {
      conditions.addAll(((Character) creature).getConditionsHistory());
    }

    conditions.addAll(Conditions.asTimedConditions());

    return conditions;
  }

  private List<String> conditionNames(BaseCreature creature) {
    return conditions(creature).stream()
        .map(BaseCreature.TimedCondition::getName)
        .collect(Collectors.toList());
  }

  private void addCheckbox(View view, String creatureId, String name) {
    CheckBox checkbox = new CheckBox(getContext());
    checkbox.setText(name);
    checkbox.setTextAppearance(R.style.LargeText);
    checkbox.setButtonTintList(ColorStateList.valueOf(view.getResources()
        .getColor(color, null)));
    party.get().addView(checkbox);
    checkboxesByCreatureId.put(creatureId, checkbox);
  }

  private void selectCondition() {
    if (creature.isPresent()) {
      Optional<Character.TimedCondition> selected =
          findCondition(creature.get(), condition.getText());
      if (selected.isPresent()) {
        predefined = selected.get().isPredefined();
        rounds.text(String.valueOf(selected.get().getRounds()))
            .enabled(!predefined || selected.get().getRounds() == 0);
        description.text(selected.get().getDescription()).enabled(!predefined);
        for (String id : selected.get().getCharacterIds()) {
          if (checkboxesByCreatureId.containsKey(id)) {
            checkboxesByCreatureId.get(id).setChecked(true);
          }
        }
      }
    }
  }

  private Optional<BaseCreature.TimedCondition> findCondition(BaseCreature creature, String name) {
    for (BaseCreature.TimedCondition condition : conditions(creature)) {
      if (condition.getName().equals(name)) {
        return Optional.of(condition);
      }
    }

    return Optional.absent();
  }

  @Override
  protected void save() {
    if (creature.isPresent() && !condition.getText().isEmpty()) {
      List<String> ids = new ArrayList<>();
      for (Map.Entry<String, CheckBox> entry : checkboxesByCreatureId.entrySet()) {
        if (entry.getValue().isChecked()) {
          ids.add(entry.getKey());
        }
      }

      if (!ids.isEmpty() && !rounds.getText().isEmpty() && Integer.parseInt(rounds.getText()) > 0) {
        int rounds = Integer.parseInt(this.rounds.getText());
        creature.get().addTimedCondition(new Character.TimedCondition(
            rounds, currentRound + rounds + 1, ids, condition.getText(),
            description.getText(), predefined));
      }
    }

    super.save();
  }
}
