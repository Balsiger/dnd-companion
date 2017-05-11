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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.ui.Setup;

/**
 * A chip with rounded corners and a text.
 */
public class InitiativeChip extends LinearLayout {

  public InitiativeChip(Context context, String name, int initiative, boolean monster,
                        boolean ready) {
    super(context);

    init(name, initiative, monster, ready);
  }

  private void init(String name, int initiative, boolean monster, boolean ready) {
    View view = LayoutInflater.from(getContext())
        .inflate(R.layout.view_chip_initiative, null, false);
    view.findViewById(R.id.back).setBackgroundTintList(
        getResources().getColorStateList(monster ? R.color.monster : R.color.character, null));

    TextView text = Setup.textView(view, R.id.name);
    text.setText(name);

    if (!ready) {
      text.setTextColor(getResources().getColor(R.color.cell, null));
    } else {
      Setup.textView(view, R.id.initiative, "initiative " + initiative);
    }

    addView(view);
  }
}
