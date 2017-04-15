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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.ui.Setup;

/**
 * Widget for editing a single ability
 */
public class EditAbility extends LinearLayout {

  private String label;
  private Setup.Action change;

  // UI elements.
  private EditText edit;
  private TextView modifier;

  public EditAbility(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    init(attributes);
  }

  public void setOnChange(Setup.Action action) {
    this.change = action;
  }

  private void init(AttributeSet attributes) {
    TypedArray array = getContext().obtainStyledAttributes(attributes, R.styleable.EditAbility);

    label = array.getString(R.styleable.EditAbility_label);

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_edit_ability,
        null, false);
    addView(view);

    Setup.textView(view, R.id.label).setText(label);
    Setup.button(view, R.id.minus, this::minus);
    Setup.button(view, R.id.plus, this::plus);
    edit = Setup.editText(view, R.id.value, "", this::update);
    modifier = Setup.textView(view, R.id.modifier);
  }

  private void plus() {
    edit.setText(String.valueOf(getValue() + 1));
    update();
  }

  private void minus() {
    edit.setText(String.valueOf(getValue() - 1));
    update();
  }

  public int getValue() {
    if (edit.getText().toString().isEmpty()) {
      return 0;
    }

    return Integer.parseInt(edit.getText().toString());
  }

  public void setValue(int value) {
    edit.setText(String.valueOf(value));
  }

  private void update() {
    int bonus = Ability.modifier(getValue());
    modifier.setText("(" + (bonus < 0 ? bonus : "+" + bonus) + ")");

    if (change != null) {
      change.execute();
    }
  }
}
