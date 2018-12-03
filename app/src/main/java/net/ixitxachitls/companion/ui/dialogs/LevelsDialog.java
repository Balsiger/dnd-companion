/*
 * Copyright (c) 2017-2018 Peter Balsiger
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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Level;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.statics.LevelTemplate;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.fragments.ListSelectDialog;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledTextView;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog for editing and adding character levels.
 */
public class LevelsDialog extends Dialog {

  private static final String ARG_ID = "id";
  private static final String ARG_MAX_LEVEL = "max_level";

  private Character character;
  private LinearLayout levels;
  private int maxLevel;
  private int nextLevel;

  public static LevelsDialog newInstance(String characterId, int maxLevel) {
    LevelsDialog dialog = new LevelsDialog();
    dialog.setArguments(arguments(R.layout.dialog_levels, R.string.dialog_title_levels,
        R.color.character, characterId, maxLevel));
    return dialog;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String characterId, int maxLevel) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, characterId);
    arguments.putInt(ARG_MAX_LEVEL, maxLevel);
    return arguments;
  }

  @Override
  protected void createContent(View view) {
    levels = view.findViewById(R.id.levels);
    Wrapper<ImageView> add = Wrapper.<ImageView>wrap(view, R.id.add).onClick(this::add);
    Wrapper.<Button>wrap(view, R.id.save).onClick(this::save);

    if (!characters().get(getArguments().getString(ARG_ID)).isPresent()) {
      Status.toast("Cannot find character to show levels!");
      super.save();
    }

    character = characters().get(getArguments().getString(ARG_ID)).get();
    maxLevel = getArguments().getInt(ARG_MAX_LEVEL);
    nextLevel = 1;
    for (Level level : character.getLevels()) {
      levels.addView(new LineView(getContext(), level, nextLevel++));
    }

    add.visible(nextLevel < maxLevel || (character.getXp() == 0 && nextLevel == 1));
  }

  @Override
  protected void save() {
    List<Level> levels = new ArrayList<>();
    for (int i = 0; i < this.levels.getChildCount(); i++) {
      View view = this.levels.getChildAt(i);
      if (view instanceof LineView) {
        levels.add(((LineView) view).toLevel());
      }
    }

    character.setLevels(levels);
    super.save();
  }

  private void add() {
    levels.addView(new LineView(getContext(), new Level(), nextLevel++));
  }

  private class LineView extends LinearLayout {
    private final TextWrapper<TextView> summary;
    private final Wrapper<LinearLayout> details;
    private final int number;
    private final LabelledTextView className;
    private final LabelledEditTextView hp;
    private final LabelledTextView abilityIncrease;

    public LineView(Context context, Level level, int number) {
      super(context);
      this.number = number;

      View view = LayoutInflater.from(getContext()).inflate(R.layout.view_line_level, this, false);
      summary = TextWrapper.wrap(view, R.id.summary).onClick(this::edit);
      Wrapper.wrap(view, R.id.edit).onClick(this::edit);
      Wrapper.wrap(view, R.id.delete).onClick(this::delete);

      details = Wrapper.wrap(view, R.id.details);
      if (!level.getTemplate().getName().isEmpty()) {
        details.gone();
      }
      className = view.findViewById(R.id.class_name);
      className.onClick(this::selectClass);
      hp = view.findViewById(R.id.hp);
      hp.validate(new EditTextWrapper.RangeValidator(1, level.getMaxHp())).onBlur(this::refresh);
      abilityIncrease = view.findViewById(R.id.ability_increase);
      if (Level.allowsAbilityIncrease(number) || level.hasAbilityIncrease()) {
        abilityIncrease.onClick(this::selectAbility);
      } else {
        abilityIncrease.gone();
      }

      className.text(level.getTemplate().getName());
      hp.text(String.valueOf(level.getHp()));
      abilityIncrease.text(level.getIncreasedAbility().isPresent()
          ? level.getIncreasedAbility().get().getName() : "");
      refresh();

      addView(view);
    }

    private String summary() {
      String result = "Level " + number + ": " + className.getText();
      if (!hp.getText().isEmpty()) {
        result += ", " + hp.getText() + " hp";
      }
      if (!abilityIncrease.getText().isEmpty()
          && !abilityIncrease.getText().equals(Ability.UNKNOWN.getName())
          && !abilityIncrease.getText().equals(Ability.NONE.getName())) {
        result += ", +1 " + abilityIncrease.getText();
      }

      return result;
    }

    private void edit() {
      details.toggleVisiblity();
    }

    private void delete() {
      ConfirmationPrompt.create(getContext())
          .title("Delete Level")
          .message("Do you really want to delete this level?")
          .yes(this::doDelete)
          .show();
    }

    private void doDelete() {
      ((ViewGroup) getParent()).removeView(this);
    }

    private void selectClass() {
      ListSelectDialog.newStringInstance(
          R.string.character_select_class, className.getText(),
          Entries.get().getLevelTemplates().getValues().stream()
              .filter(LevelTemplate::isFromPHB)
              .map(LevelTemplate::getName)
              .collect(Collectors.toList()), R.color.character)
          .setSelectListener(this::editClass)
          .display();
    }

    private void editClass(String className) {
      this.className.text(className);
      refresh();
    }

    private void selectAbility() {
      ListSelectDialog.newStringInstance(
          R.string.character_select_ability,
          abilityIncrease.getText(),
          Ability.names(), R.color.character)
          .setSelectListener(this::editAbility)
          .display();
    }

    private void editAbility(String ability) {
      abilityIncrease.text(ability);
      refresh();
    }

    private void refresh() {
      summary.text(summary());
    }

    public Level toLevel() {
      return new Level(className.getText(), Integer.parseInt(hp.getText()),
          abilityIncrease.getText());
    }
  }
}
