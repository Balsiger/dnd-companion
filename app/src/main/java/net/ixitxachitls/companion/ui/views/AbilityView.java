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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.values.ModifiedValue;
import net.ixitxachitls.companion.ui.MessageDialog;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * View to show a single ability.
 */
public class AbilityView extends LinearLayout {

  private Ability ability = Ability.UNKNOWN;
  private Optional<ModifiedValue> modifiedValue = Optional.absent();

  // UI elements.
  private TextWrapper<TextView> value;
  private TextWrapper<TextView> modifier;

  public AbilityView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    init(attributes);
  }

  public void setAction(Wrapper.Action action) {
    setOnClickListener(v -> action.execute());
  }

  public AbilityView update(Ability ability, ModifiedValue value) {
    this.ability = ability;
    this.modifiedValue = Optional.of(value);

    int total = value.total();
    int modifier = Ability.modifier(total);
    this.value.text(String.valueOf(total));
    this.modifier.text((modifier < 0 ? "" : "+") + modifier);

    return this;
  }

  private void init(@Nullable AttributeSet attributes) {
    TypedArray array = getContext().obtainStyledAttributes(attributes, R.styleable.AbilityView);

    View view = LayoutInflater.from(getContext())
        .inflate(R.layout.view_ability, null, false);
    TextWrapper.wrap(view, R.id.name).text(array.getString(R.styleable.AbilityView_attribute_name))
        .textColor(R.color.characterDark);
    value = TextWrapper.wrap(view, R.id.value).textColor(R.color.characterText);
    modifier = TextWrapper.wrap(view, R.id.modifier).textColor(R.color.characterText);

    setOnLongClickListener(this::showDescription);

    addView(view);
  }

  private boolean showDescription(View view) {
    if (modifiedValue.isPresent()) {
      MessageDialog.create(getContext())
          .title(ability.getName())
          .message(modifiedValue.get().describeModifiers())
          .show();
      return true;
    }

    return false;
  }
}
