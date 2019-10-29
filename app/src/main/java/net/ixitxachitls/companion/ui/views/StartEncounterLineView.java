/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import androidx.annotation.ColorRes;

/**
 * View representing a line in the start battle dialog.
 */
public class StartEncounterLineView extends LinearLayout {

  private final String creatureId;
  private boolean setting = false;

  // UI elements.
  private final Wrapper<CheckBox> included;
  private final Wrapper<CheckBox> surprised;
  private final Wrapper.Action changed;

  public StartEncounterLineView(Context context, String creatureId, String name,
                                @ColorRes int textColor, Wrapper.Action changed) {
    super(context);
    this.creatureId = creatureId;
    this.changed = changed;

    View view =
        LayoutInflater.from(getContext()).inflate(R.layout.view_start_battle_line, this, false);

    TextWrapper.wrap(view, R.id.name).text(name).textColor(textColor);
    included = Wrapper.<CheckBox>wrap(view, R.id.included);
    included.get().setOnCheckedChangeListener(this::onIncluded);
    included.get().setButtonTintList(ColorStateList.valueOf(view.getResources()
        .getColor(textColor, null)));
    surprised = Wrapper.<CheckBox>wrap(view, R.id.surprised).onClick(changed);
    surprised.get().setOnCheckedChangeListener(this::onSurprised);
    surprised.get().setButtonTintList(ColorStateList.valueOf(view.getResources()
        .getColor(textColor, null)));

    addView(view);
  }

  public String getCreatureId() {
    return creatureId;
  }

  public boolean isIncluded() {
    return included.get().isChecked();
  }

  public void setIncluded(boolean checked) {
    boolean old = setting;
    setting = true;
    included.get().setChecked(checked);
    setting = old;
  }

  public boolean isSurprised() {
    return surprised.get().isChecked();
  }

  public void setSurprised(boolean checked) {
    boolean old = setting;
    setting = true;
    surprised.get().setChecked(checked);
    setting = old;
  }

  private void onIncluded(View view, boolean isChecked) {
    setSurprised(isChecked);
    surprised.enabled(isChecked);
    if (!setting) {
      changed.execute();
    }
  }

  private void onSurprised(View view, boolean isChecked) {
    if (!setting) {
      changed.execute();
    }
  }
}
