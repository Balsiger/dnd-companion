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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.values.ModifiedValue;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

import androidx.annotation.Nullable;

/**
 * View to show a single ability.
 */
public class AbilityView extends LinearLayout {

  private Ability ability = Ability.UNKNOWN;
  private Optional<ModifiedValue> modifiedValue = Optional.empty();
  private Optional<ModifiedValue> modifiedCheck = Optional.empty();

  // UI elements.
  private TextWrapper<ModifiedValueView> value;
  private TextWrapper<TextView> modifier;
  private LinearLayout checkContainer;
  private TextWrapper<ModifiedValueView> check;

  public AbilityView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    init(attributes);
  }

  public void setAction(Wrapper.Action action) {
    setOnClickListener(v -> action.execute());
    value.onClick(action);
  }

  public void removeAction() {
    setOnClickListener(null);
    value.removeClick();
  }

  public AbilityView update(Ability ability, ModifiedValue value, ModifiedValue check) {
    this.ability = ability;
    this.modifiedValue = Optional.of(value);
    this.modifiedCheck = Optional.of(check);

    int modifier = Ability.modifier(value.total());
    this.value.get().set(value);
    this.modifier.text(formatSigned(modifier));
    this.check.get().set(check);
    this.checkContainer.setVisibility(modifier == check.total() ? GONE : VISIBLE);

    return this;
  }

  private String formatSigned(int value) {
    return (value < 0 ? "" : "+") + value;
  }

  private void init(@Nullable AttributeSet attributes) {
    TypedArray array = getContext().obtainStyledAttributes(attributes, R.styleable.AbilityView);

    View view = LayoutInflater.from(getContext())
        .inflate(R.layout.view_ability, null, false);
    TextWrapper.wrap(view, R.id.name).text(array.getString(R.styleable.AbilityView_attribute_name))
        .textColor(R.color.characterDark);
    value = TextWrapper.<ModifiedValueView>wrap(view, R.id.value).textColor(R.color.characterText);
    modifier = TextWrapper.wrap(view, R.id.modifier).textColor(R.color.characterText);
    check = TextWrapper.<ModifiedValueView>wrap(view, R.id.check).textColor(R.color.characterText);
    checkContainer = view.findViewById(R.id.check_container);
    checkContainer.setVisibility(GONE);

    addView(view);
  }
}
