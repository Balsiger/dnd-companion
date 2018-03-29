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

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * Widget for editing a single ability
 */
public class EditAbility extends LinearLayout {

  private String label;
  private Wrapper.Action change;

  // UI elements.
  private EditTextWrapper<EditText> edit;
  private TextWrapper<TextView> modifier;

  public EditAbility(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    init(attributes);
  }

  public void setOnChange(Wrapper.Action action) {
    this.change = action;
  }

  private void init(AttributeSet attributes) {
    TypedArray array = getContext().obtainStyledAttributes(attributes, R.styleable.EditAbility);

    label = array.getString(R.styleable.EditAbility_label);

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_edit_ability,
        null, false);
    addView(view);

    TextWrapper.wrap(view, R.id.label).text(label);
    Wrapper.wrap(view, R.id.minus).onClick(this::minus);
    Wrapper.wrap(view, R.id.plus).onClick(this::plus);
    edit = EditTextWrapper.wrap(view, R.id.value)
        .text("").onClick(this::update).onChange(this::update);
    modifier = TextWrapper.wrap(view, R.id.modifier);
  }

  private void plus() {
    edit.text(String.valueOf(getValue() + 1));
    update();
  }

  private void minus() {
    edit.text(String.valueOf(getValue() - 1));
    update();
  }

  public int getValue() {
    if (edit.getText().isEmpty()) {
      return 0;
    }

    return Integer.parseInt(edit.getText());
  }

  public void setValue(int value) {
    edit.text(String.valueOf(value));
  }

  private void update() {
    int bonus = Ability.modifier(getValue());
    modifier.text("(" + (bonus < 0 ? bonus : "+" + bonus) + ")");

    if (change != null) {
      change.execute();
    }
  }
}
