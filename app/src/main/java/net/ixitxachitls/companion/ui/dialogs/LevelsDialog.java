/*
 * Copyright (c) 2017-2018 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Level;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

/**
 * Dialog for editing and adding character levels.
 */
public class LevelsDialog extends Dialog {

  private static final String ARG_ID = "id";

  private Character character;
  private LinearLayout levels;

  private void add() {
    LineView line = new LineView(getContext(), new Level(), levels.getChildCount() + 1);
    levels.addView(line);
    line.edit();
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
    int totalLevel = 1;
    for (Level level : character.getLevels()) {
      levels.addView(new LineView(getContext(), level, totalLevel++));
    }

    add.visible(true);
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

  private void refresh() {
    int totalLevel = 1;
    for (int i = 0; i < levels.getChildCount(); i++) {
      View child = levels.getChildAt(i);
      if (child instanceof LineView) {
        ((LineView) child).refresh(totalLevel++);
      }
    }
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String characterId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, characterId);
    return arguments;
  }

  public static LevelsDialog newInstance(String characterId) {
    LevelsDialog dialog = new LevelsDialog();
    dialog.setArguments(arguments(R.layout.dialog_levels, R.string.dialog_title_levels,
        R.color.character, characterId));
    return dialog;
  }

  private class LineView extends LinearLayout {

    private final TextWrapper<TextView> summary;
    private int number;
    private Level level;

    public LineView(Context context, Level level, int number) {
      super(context);
      this.level = level;
      this.number = number;

      View view = LayoutInflater.from(getContext()).inflate(R.layout.view_line_level, this, false);
      summary = TextWrapper.wrap(view, R.id.summary).onClick(this::edit);
      Wrapper.wrap(view, R.id.edit).onClick(this::edit);
      Wrapper.wrap(view, R.id.delete).onClick(this::delete);

      refresh();

      addView(view);
    }

    public void refresh(int number) {
      this.number = number;
      refresh();
    }

    public Level toLevel() {
      return level;
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
      LevelsDialog.this.refresh();
    }

    private void edit() {
      LevelDialog.newInstance(character.getId(), number).onSaved(this::saved).display();
    }

    private void refresh() {
      summary.text(level.summary(number));
    }

    private void saved(Level level) {
      this.level = level;
      refresh();
    }
  }
}
