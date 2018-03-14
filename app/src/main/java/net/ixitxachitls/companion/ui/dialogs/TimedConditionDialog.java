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
import net.ixitxachitls.companion.data.dynamics.Creature;
import net.ixitxachitls.companion.data.dynamics.Creatures;
import net.ixitxachitls.companion.data.values.Condition;
import net.ixitxachitls.companion.data.values.Duration;
import net.ixitxachitls.companion.data.values.TargetedTimedCondition;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.net.CompanionMessenger;
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
  private String id;
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
  private LabelledEditTextView summary;
  private Wrapper<Button> save;

  public TimedConditionDialog() {
  }

  public static TimedConditionDialog newInstance(String creatureOrCampaignId, int currentRound) {
    TimedConditionDialog dialog = new TimedConditionDialog();
    dialog.setArguments(arguments(R.layout.dialog_timed_condition,
        R.string.edit_timed_condition, R.color.character, creatureOrCampaignId, currentRound));
    return dialog;
  }

  public static TimedConditionDialog displaySurprised(Campaign campaign) {
    TimedConditionDialog dialog = newInstance(campaign.getCampaignId(), 0);
    dialog.display();
    dialog.selectCondition(Optional.of(Conditions.SURPRISED));
    dialog.rounds.text("1");
    dialog.save.get().setFocusableInTouchMode(true);
    dialog.save.get().requestFocus();

    return dialog;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String creatureOrCampaignId, int currentRound) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, creatureOrCampaignId);
    arguments.putInt(ARG_ROUND, currentRound);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    id = getArguments().getString(ARG_ID); // The creature OR campaign id.
    creature = Creatures.getCreatureOrCharacter(id);
    currentRound = getArguments().getInt(ARG_ROUND);
    if (creature.isPresent()) {
      campaign = Campaigns.getCampaign(creature.get().getCampaignId()).getValue();
    } else {
      campaign = Campaigns.getCampaign(id).getValue();
    }
  }

  @Override
  protected void createContent(View view) {
    condition = view.findViewById(R.id.condition);
    condition.onChange(this::selectCondition).onFocus(condition::showDropDown);
    rounds = view.findViewById(R.id.rounds);
    rounds.onChange(this::updateSave);
    description = view.findViewById(R.id.description);
    summary = view.findViewById(R.id.summary);
    party = Wrapper.<LinearLayout>wrap(view, R.id.party);
    save = Wrapper.<Button>wrap(view, R.id.save).onClick(this::save).enabled(false);

    ArrayAdapter<String> adapter;
    if (creature.isPresent()) {
      adapter = new ArrayAdapter<String>(getContext(),
          R.layout.list_item_select, conditionNames(creature.get()));
    } else {
      adapter = new ArrayAdapter<String>(getContext(),
          R.layout.list_item_select,
          Conditions.CONDITIONS.stream()
              .map(Condition::getName)
              .collect(Collectors.toList()));
    }
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

  private List<Condition> conditions(BaseCreature creature) {
    List<Condition> conditions = new ArrayList<>();

    if (creature instanceof Character) {
      conditions.addAll(((Character) creature).getConditionsHistory());
    }

    conditions.addAll(Conditions.CONDITIONS);

    return conditions;
  }

  private List<String> conditionNames(BaseCreature creature) {
    return conditions(creature).stream()
        .map(Condition::getName)
        .collect(Collectors.toList());
  }

  private void addCheckbox(View view, String creatureId, String name) {
    CheckBox checkbox = new CheckBox(getContext());
    checkbox.setText(name);
    checkbox.setTextAppearance(R.style.LargeText);
    checkbox.setButtonTintList(ColorStateList.valueOf(view.getResources()
        .getColor(color, null)));
    checkbox.setOnCheckedChangeListener((v, c) -> updateSave());
    party.get().addView(checkbox);
    checkboxesByCreatureId.put(creatureId, checkbox);
  }

  private void selectCondition() {
    if (creature.isPresent()) {
      displayCondition(findCondition(creature.get(), condition.getText()));
    }
  }

  private void selectCondition(Optional<Condition> condition) {
    if (condition.isPresent()) {
      this.condition.text(condition.get().getName());
    } else {
      this.condition.text("");
    }
    displayCondition(condition);
  }

  private void displayCondition(Optional<Condition> condition) {
    if (condition.isPresent()) {
      predefined = condition.get().isPredefined();
      int roundsNbr = condition.get().getDuration().getRounds();
      rounds.text(roundsNbr > 0 ? String.valueOf(roundsNbr) : "")
          .enabled(!predefined || condition.get().getDuration().getRounds() == 0);
      description.text(condition.get().getDescription()).enabled(!predefined);
      summary.text(condition.get().getSummary()).enabled(!predefined);
    } else {
      rounds.text("").enabled(true);
      description.text("").enabled(true);
      summary.text("").enabled(true);
    }
  }

  private Optional<Condition> findCondition(BaseCreature creature, String name) {
    for (Condition condition : conditions(creature)) {
      if (condition.getName().equals(name)) {
        return Optional.of(condition);
      }
    }

    return Optional.absent();
  }

  private void updateSave() {
    save.enabled((!rounds.getText().isEmpty() && targetSelected())
        || condition.getText().equals(Conditions.SURPRISED.getName()));
  }

  private boolean targetSelected() {
    for (Map.Entry<String, CheckBox> entry : checkboxesByCreatureId.entrySet()) {
      if (entry.getValue().isChecked()) {
        return true;
      }
    }

    return false;
  }

  @Override
  protected void save() {
    List<String> ids = new ArrayList<>();
    for (Map.Entry<String, CheckBox> entry : checkboxesByCreatureId.entrySet()) {
      if (entry.getValue().isChecked()) {
        ids.add(entry.getKey());
      }
    }

    if (!ids.isEmpty() && !rounds.getText().isEmpty()) {
      int rounds = Integer.parseInt(this.rounds.getText());
      TimedCondition timed = new TimedCondition(new Condition(condition.getText(),
          description.getText(), summary.getText(), Duration.rounds(rounds), predefined),
          id, currentRound + rounds);
      if (creature.isPresent() && !condition.getText().isEmpty()) {
        if (rounds > 0) {
          creature.get().addInitiatedCondition(new TargetedTimedCondition(timed, ids));
        }
      } else {
        for (String id : ids) {
          CompanionMessenger.get().send(id, timed);
        }
      }
    }

    super.save();
  }
}
