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

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.google.common.base.Preconditions;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Creature;
import net.ixitxachitls.companion.data.documents.Monster;
import net.ixitxachitls.companion.data.values.Adjustment;
import net.ixitxachitls.companion.data.values.ConditionData;
import net.ixitxachitls.companion.data.values.Duration;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.rules.Conditions;
import net.ixitxachitls.companion.ui.views.LabelledAutocompleteTextView;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

/**
 * Dialog to select a timed condition for party members.
 */
public class TimedConditionDialog extends Dialog {

  private static final String ARG_ID = "id";
  private static final String ARG_ROUND = "round";

  // State.
  private String id;
  private Optional<? extends Creature<?>> creature = Optional.empty();
  private Optional<Campaign> campaign = Optional.empty();
  private int currentRound = 0;
  private boolean predefined = false;

  // UI Elements.
  private LabelledAutocompleteTextView condition;
  private Wrapper<LinearLayout> party;
  private Map<String, CheckBox> checkboxesByCreatureId = new HashMap<>();
  private LabelledEditTextView rounds;
  private LabelledEditTextView minutes;
  private LabelledEditTextView hours;
  private LabelledEditTextView days;
  private LabelledEditTextView years;
  private Wrapper<CheckBox> permanent;
  private LabelledEditTextView description;
  private LabelledEditTextView summary;
  private Wrapper<Button> save;

  public TimedConditionDialog() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    id = getArguments().getString(ARG_ID); // The creature OR campaign id.
    creature = characters().get(id);
    if (!creature.isPresent()) {
      creature = monsters().get(id);
    }
    currentRound = getArguments().getInt(ARG_ROUND);
    if (creature.isPresent()) {
      campaign = campaigns().get(creature.get().getCampaignId());
    } else {
      campaign = campaigns().get(id);
    }
  }

  @Override
  protected void createContent(View view) {
    condition = view.findViewById(R.id.condition);
    condition.onChange(this::selectCondition).onFocus(condition::showDropDown);
    rounds = view.findViewById(R.id.rounds);
    rounds.onChange(this::update);
    minutes = view.findViewById(R.id.minutes);
    minutes.onChange(this::update);
    hours = view.findViewById(R.id.hours);
    hours.onChange(this::update);
    days = view.findViewById(R.id.days);
    days.onChange(this::update);
    years = view.findViewById(R.id.years);
    years.onChange(this::update);
    permanent = Wrapper.<CheckBox>wrap(view, R.id.permanent).onClick(this::update);
    description = view.findViewById(R.id.description);
    summary = view.findViewById(R.id.summary);
    party = Wrapper.<LinearLayout>wrap(view, R.id.party);
    save = Wrapper.<Button>wrap(view, R.id.save).onClick(this::save).enabled(false);

    ArrayAdapter<String> adapter;
    if (creature.isPresent()) {
      adapter = new ArrayAdapter<>(getContext(),
          R.layout.list_item_select, conditionNames(creature.get()));
    } else {
      adapter = new ArrayAdapter<>(getContext(),
          R.layout.list_item_select,
          Conditions.CONDITIONS.stream()
              .map(ConditionData::getName)
              .collect(Collectors.toList()));
    }
    condition.setAdapter(adapter);

    if (campaign.isPresent()) {
      if (campaign.get().getEncounter().inBattle()) {
        for (Creature creature : campaign.get().getEncounter().getCreatures()) {
          addCheckbox(view, creature.getId(), creature.getName());
        }
      } else {
        for (Character character : characters().getCampaignCharacters(campaign.get().getId())) {
          addCheckbox(view, character.getId(), character.getName());
        }

        for (Monster monster : monsters().getCampaignMonsters(campaign.get().getId())) {
          addCheckbox(view, monster.getId(), monster.getName());
        }
      }
    }
  }

  @Override
  protected void save() {
    List<String> ids = new ArrayList<>();
    for (Map.Entry<String, CheckBox> entry : checkboxesByCreatureId.entrySet()) {
      if (entry.getValue().isChecked()) {
        ids.add(entry.getKey());
      }
    }

    Duration duration = extractDuration();
    if (!ids.isEmpty() && !duration.isNone()) {
      ConditionData cond = ConditionData.newBuilder(condition.getText())
          .description(description.getText())
          .adjustments(parseAdjustments(summary.getText(), condition.getText()))
          .duration(duration)
          .predefined(predefined)
          .build();
      TimedCondition timed;
      if (duration.isRounds()) {
        timed = new TimedCondition(cond, id, currentRound + duration.getRounds());
      } else if (duration.isPermanent()) {
        timed = new TimedCondition(cond, id);
      } else {
        timed = new TimedCondition(cond, id,
            campaign.get().getCalendar().add(campaign.get().getDate(), duration));
      }
      for (String id : ids) {
        Optional<? extends Creature> target = characters().getCreature(id);
        if (target.isPresent()) {
          target.get().addCondition(timed);
        } else {
          Status.error("Creature " + id + " not found to add condition!");
        }
      }
    }

    super.save();
  }

  private void addCheckbox(View view, String creatureId, String name) {
    CheckBox checkbox = new CheckBox(getContext());
    checkbox.setText(name);
    checkbox.setTextAppearance(R.style.LargeText);
    checkbox.setButtonTintList(ColorStateList.valueOf(view.getResources()
        .getColor(color, null)));
    checkbox.setChecked(creature.isPresent() && creatureId.equals(creature.get().getId()));
    checkbox.setOnCheckedChangeListener((v, c) -> update());
    party.get().addView(checkbox);
    checkboxesByCreatureId.put(creatureId, checkbox);
  }

  private List<String> conditionNames(Creature<?> creature) {
    return conditions(creature).stream()
        .map(ConditionData::getName)
        .collect(Collectors.toList());
  }

  private List<ConditionData> conditions(Creature<?> creature) {
    List<ConditionData> conditions = new ArrayList<>();

    if (creature instanceof Character) {
      conditions.addAll(((Character) creature).getConditionsHistory());
    }

    conditions.addAll(Conditions.CONDITIONS);

    return conditions;
  }

  private void displayCondition(Optional<ConditionData> condition) {
    if (condition.isPresent()) {
      predefined = condition.get().isPredefined();
      Duration duration = condition.get().getDuration();
      if (duration.getRounds() > 0) {
        rounds.text(String.valueOf(duration.getRounds()));
      }
      if (duration.getMinutes() > 0) {
        minutes.text(String.valueOf(duration.getMinutes()));
      }
      if (duration.getHours() > 0) {
        hours.text(String.valueOf(duration.getHours()));
      }
      if (duration.getDays() > 0) {
        days.text(String.valueOf(duration.getDays()));
      }
      if (duration.getYears() > 0) {
        years.text(String.valueOf(duration.getYears()));
      }
      permanent.get().setChecked(duration.isPermanent());

      description.text(condition.get().getDescription()).enabled(!predefined);
      summary.text(condition.get().getSummarySpanned()).enabled(!predefined);
    } else {
      rounds.text("");
      minutes.text("");
      hours.text("");
      days.text("");
      years.text("");
      description.text("");
      summary.text("");
    }

    update();
  }

  private Duration extractDuration() {
    if (permanent.get().isChecked()) {
      return Duration.PERMANENT;
    }

    if (!rounds.getText().isEmpty()) {
      return Duration.rounds(extractInt(rounds));
    }

    return Duration.time(
        extractInt(years), extractInt(days), extractInt(hours), extractInt(minutes));
  }

  private int extractInt(LabelledEditTextView view) {
    return view.getText().isEmpty() ? 0 : Integer.parseInt(view.getText());
  }

  private Optional<ConditionData> findCondition(Optional<? extends Creature<?>> creature, String name) {
    if (creature.isPresent()) {
      for (ConditionData condition : conditions(creature.get())) {
        if (condition.getName().equals(name)) {
          return Optional.of(condition);
        }
      }
    }

    return Conditions.get(name);
  }

  private List<Adjustment> parseAdjustments(String text, String source) {
    List<String> parts = Arrays.asList(text.split(",\\s*"));
    List<Adjustment> adjustments = new ArrayList<>();
    for (String part : parts) {
      adjustments.add(Adjustment.parse(part, source));
    }

    return adjustments;
  }

  private void selectCondition() {
    displayCondition(findCondition(creature, condition.getText()));
  }

  private void selectCondition(Optional<ConditionData> condition) {
    if (condition.isPresent()) {
      this.condition.text(condition.get().getName());
    } else {
      this.condition.text("");
    }
    displayCondition(condition);
  }

  private boolean targetSelected() {
    for (Map.Entry<String, CheckBox> entry : checkboxesByCreatureId.entrySet()) {
      if (entry.getValue().isChecked()) {
        return true;
      }
    }

    return false;
  }

  private void update() {
    if (permanent.get().isChecked()) {
      rounds.text("").disabled();
      minutes.text("").disabled();
      hours.text("").disabled();
      days.text("").disabled();
      years.text("").disabled();
      save.enabled(targetSelected());
    } else {
      if (!rounds.getText().isEmpty()) {
        permanent.disabled();
        minutes.text("").disabled();
        hours.text("").disabled();
        hours.text("").disabled();
        days.text("").disabled();
        years.text("").disabled();
        save.enabled(targetSelected());
      } else {
        if (!minutes.getText().isEmpty() || !hours.getText().isEmpty() || !days.getText().isEmpty()
            || !years.getText().isEmpty()) {
          permanent.disabled();
          rounds.text("").disabled();
          save.enabled(targetSelected());
        } else {
          permanent.enabled();
          rounds.enabled();
          minutes.enabled();
          hours.enabled();
          days.enabled();
          years.enabled();
          save.disabled();
        }
      }
    }
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String creatureOrCampaignId, int currentRound) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, creatureOrCampaignId);
    arguments.putInt(ARG_ROUND, currentRound);
    return arguments;
  }

  public static TimedConditionDialog newInstance(String creatureOrCampaignId, int currentRound) {
    TimedConditionDialog dialog = new TimedConditionDialog();
    dialog.setArguments(arguments(R.layout.dialog_timed_condition,
        R.string.edit_timed_condition, R.color.character, creatureOrCampaignId, currentRound));
    return dialog;
  }

}
