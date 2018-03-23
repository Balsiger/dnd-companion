/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * along with the Tabletop Companion; if not, write to the Free Software
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

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * View to show a single ability.
 */
public class AbilityView extends LinearLayout {

  // UI elements.
  private TextWrapper<TextView> value;
  private TextWrapper<TextView> modifier;

  public AbilityView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    init(attributes);
  }

  private void init(@Nullable AttributeSet attributes) {
    TypedArray array = getContext().obtainStyledAttributes(attributes, R.styleable.AbilityView);

    View view = LayoutInflater.from(getContext())
        .inflate(R.layout.view_ability, null, false);
    ((TextView) view.findViewById(R.id.name))
        .setText(array.getString(R.styleable.AbilityView_attribute_name));
    value = TextWrapper.wrap(view, R.id.value);
    modifier = TextWrapper.wrap(view, R.id.modifier);

    addView(view);
  }

  public void setValue(int value, int modifier) {
    this.value.text(String.valueOf(value));
    this.modifier.text("(" + modifier + ")");
  }

  public void setAction(Wrapper.Action action) {
    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        action.execute();
      }
    });
  }
}
