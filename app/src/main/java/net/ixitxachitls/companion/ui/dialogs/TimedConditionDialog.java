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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * Dialog to select a timed condition for party members.
 */
public class TimedConditionDialog extends Dialog {

  private static final String TAG = "TimedConditionDialog";
  private static final String ARG_ID = "id";

  private Optional<Character> character;

  private EditTextWrapper<AutoCompleteTextView> condition;
  private Wrapper<Button> save;

  public TimedConditionDialog() {}

  public static TimedConditionDialog newInstance(String characterId) {
    TimedConditionDialog dialog = new TimedConditionDialog();
    dialog.setArguments(arguments(R.layout.dialog_timed_condition,
        R.string.edit_timed_condition, R.color.character, characterId));
    return dialog;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String characterId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, characterId);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    character = Characters.local().get(getArguments().getString(ARG_ID));
  }

  @Override
  protected void createContent(View view) {
    condition = EditTextWrapper.<AutoCompleteTextView>wrap(view, R.id.condition)
        .lineColor(R.color.character);
    condition.get().setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
          condition.get().showDropDown();
        }
      }
    });
    save = Wrapper.<Button>wrap(view, R.id.save).onClick(this::save);

    if (character.isPresent()) {
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
          R.layout.list_item_select, character.get().conditionHistoryNames());
      condition.get().setAdapter(adapter);
    }
  }

  @Override
  protected void save() {
    super.save();
  }
}
