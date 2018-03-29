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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.ui.dialogs.XPDialog;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;

/**
 * View for character specific øxp data.
 */
public class XPCharacterView extends LinearLayout {

  private final XPDialog dialog;
  private final Character character;
  private final TextWrapper<CheckBox> name;
  private final TextWrapper<TextView> xp;
  private int xpValue = 0;

  public XPCharacterView(Context context, XPDialog dialog, Character character) {
    super(context);
    this.dialog = dialog;

    this.character = character;

    View view = LayoutInflater.from(getContext())
        .inflate(R.layout.view_xp_character, null, false);
    name = TextWrapper.<CheckBox>wrap(view, R.id.name)
        .text(character.getName() + ", level " + character.getLevel())
        .onClick(this::select);
    name.get().setChecked(true);
    xp = TextWrapper.wrap(view, R.id.xp)
        .text(String.valueOf(0));

    addView(view);
  }

  private void select() {
    dialog.refresh();
  }

  public Character getCharacter() {
    return character;
  }

  public boolean isSelected() {
    return name.get().isChecked();
  }

  public int getXP() {
    return xpValue;
  }

  public void setXP(int xpValue) {
    this.xpValue = xpValue;
    xp.text(String.valueOf(xpValue));
  }

  public int getLevel() {
    return character.getLevel();
  }
}
