/*
 * Copyright (c) 2017-2019 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Tabletop Companion.
 *
 * The Tabletop Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Tabletop Companion is distributed in the hope that it will be useful,
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
import android.view.View;
import android.widget.Button;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Level;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.templates.FeatTemplate;
import net.ixitxachitls.companion.data.templates.LevelTemplate;
import net.ixitxachitls.companion.rules.Levels;
import net.ixitxachitls.companion.ui.fragments.ListSelectDialog;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledTextView;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * A dialog to setup a single level.
 */
public class LevelDialog extends Dialog<LevelDialog, Level> {

  private static final String ARG_ID = "id";
  private static final String ARG_LEVEL = "level";

  private Character character;
  private int characterLevel = 0;
  private int classLevel = 0;
  private Level level;

  // UI.
  private LabelledTextView className;
  private LabelledEditTextView hp;
  private LabelledTextView abilityIncrease;
  private LabelledTextView feat;
  private LabelledTextView racialFeat;
  private LabelledTextView classFeat;

  @Override
  protected Level getValue() {
    if (className == null || hp == null || abilityIncrease == null || feat == null ||
        racialFeat == null || classFeat == null) {
      return new Level();
    }

    return new Level(className.getText(), Integer.parseInt(hp.getText()), abilityIncrease.getText(),
        feat.getText(), racialFeat.getText(), classFeat.getText());
  }

  @Override
  protected void createContent(View view) {
    if (!characters().get(getArguments().getString(ARG_ID)).isPresent()) {
      Status.error("Cannot find character to show level");
      super.save();
    }

    character = characters().get(getArguments().getString(ARG_ID)).get();
    characterLevel = getArguments().getInt(ARG_LEVEL);

    if (character.getLevel(characterLevel).isPresent()) {
      level = character.getLevel(characterLevel).get();
    } else {
      level = new Level();
    }

    Wrapper.<Button>wrap(view, R.id.save).onClick(this::save);

    className = view.findViewById(R.id.class_name);
    className.onClick(this::selectClass);
    hp = view.findViewById(R.id.hp);
    hp.validate(new EditTextWrapper.RangeValidator(1, level.getMaxHp()));
    abilityIncrease = view.findViewById(R.id.ability_increase);
    if (Levels.allowsAbilityIncrease(characterLevel) || level.hasAbilityIncrease()) {
      abilityIncrease.onClick(this::selectAbility);
    } else {
      abilityIncrease.disabled();
    }
    feat = view.findViewById(R.id.feat);
    racialFeat = view.findViewById(R.id.racial_feat);
    classFeat = view.findViewById(R.id.class_feat);
    if (Levels.allowsFeat(characterLevel)) {
      feat.onClick(this::selectFeat);
    } else {
      feat.disabled();
    }
    if (character.getRace().isPresent()
        && character.getRace().get().hasBonusFeat(characterLevel)) {
      racialFeat.onClick(this::selectRacialFeat);
    } else {
      racialFeat.disabled();
    }

    className.text(level.getTemplate().getName());
    hp.text(String.valueOf(level.getHp()));
    abilityIncrease.text(level.getIncreasedAbility().isPresent()
        ? level.getIncreasedAbility().get().getName() : "");
    feat.text(level.getFeat().isPresent() ? level.getFeat().get().getName() : "");
    racialFeat.text(level.getRacialFeat().isPresent() ? level.getRacialFeat().get().getName(): "");
    classFeat.text(level.getClassFeat().isPresent() ? level.getClassFeat().get().getName() : "");

    refresh();
  }

  @Override
  public void save() {
    character.setLevel(characterLevel, getValue());

    super.save();
  }

  private List<FeatTemplate> bonusFeats() {
    Optional<LevelTemplate> level = Templates.get().getLevelTemplates().get(className.getText());
    if (level.isPresent()) {
      return level.get().collectBonusFeats(classLevel);
    } else {
      return Collections.emptyList();
    }
  }

  private void editAbility(String ability) {
    abilityIncrease.text(ability);
  }

  private void editClass(String className) {
    this.className.text(className);
    refresh(); // The class level could have changed.
  }

  private void editClassFeat(String featName) {
    this.classFeat.text(featName);
  }

  private void editFeat(String featName) {
    this.feat.text(featName);
  }

  private void editRacialFeat(String featName) {
    this.racialFeat.text(featName);
  }

  private void refresh() {
    classLevel = character.getClassLevel(className.getText(), characterLevel);
    // If we change the class in this level, then the class level is actually one level higher.
    if (!level.getTemplate().getName().equals(className.getText())) {
      classLevel++;
    }

    Optional<LevelTemplate> level = Templates.get().getLevelTemplates().get(className.getText());
    if (level.isPresent() && level.get().hasBonusFeat(classLevel)) {
      classFeat.enabled();
      classFeat.onClick(this::selectClassFeat);
    } else {
      classFeat.disabled();
    }
  }

  private void selectAbility() {
    ListSelectDialog.newStringInstance(
        R.string.character_select_ability,
        abilityIncrease.getText(),
        Ability.names(), R.color.character)
        .setSelectListener(this::editAbility)
        .display();
  }

  private void selectClass() {
    ListSelectDialog.newStringInstance(
        R.string.character_select_class, className.getText(),
        Templates.get().getLevelTemplates().getValues().stream()
            .filter(LevelTemplate::isFromPHB)
            .map(LevelTemplate::getName)
            .collect(Collectors.toList()), R.color.character)
        .setSelectListener(this::editClass)
        .display();
  }

  private void selectClassFeat() {
    ListSelectDialog.newStringInstance(
        R.string.character_select_feat, feat.getText(),
        bonusFeats().stream().map(FeatTemplate::getName).collect(Collectors.toList()),
        R.color.character)
        .setSelectListener(this::editClassFeat)
        .display();
  }

  private void selectFeat() {
    ListSelectDialog.newStringInstance(
        R.string.character_select_feat, feat.getText(),
        Templates.get().getFeatTemplates().getValues().stream()
            .filter(FeatTemplate::isFromPHB)
            .map(FeatTemplate::getName)
            .collect(Collectors.toList()), R.color.character)
        .setSelectListener(this::editFeat)
        .display();
  }

  private void selectRacialFeat() {
    ListSelectDialog.newStringInstance(
        R.string.character_select_feat, feat.getText(),
        Templates.get().getFeatTemplates().getValues().stream()
            .filter(FeatTemplate::isFromPHB)
            .map(FeatTemplate::getName)
            .collect(Collectors.toList()), R.color.character)
        .setSelectListener(this::editRacialFeat)
        .display();
  }

  protected static Bundle arguments(@LayoutRes int layoutId, String title,
                                    @ColorRes int colorId, String characterId, int level) {
    Bundle arguments = Dialog.arguments(layoutId, title, colorId);
    arguments.putString(ARG_ID, characterId);
    arguments.putInt(ARG_LEVEL, level);
    return arguments;
  }

  public static LevelDialog newInstance(String characterId, int level) {
    LevelDialog dialog = new LevelDialog();
    dialog.setArguments(arguments(R.layout.dialog_level, "Level " + level,
        R.color.character, characterId, level));
    return dialog;
  }
}
