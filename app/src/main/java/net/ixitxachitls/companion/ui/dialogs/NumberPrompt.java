/*
 * Copyright (c) 2017-2018 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.ui.Prompt;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;

/**
 * A prompt to ask the user for a single number
 */
public class NumberPrompt extends Prompt<NumberPrompt> {

  private int value = Integer.MAX_VALUE;
  private EditTextWrapper<EditText> number;

  public NumberPrompt(Context context) {
    super(context);

    dialog.setPositiveButton(android.R.string.ok, null)
        .setNegativeButton(android.R.string.cancel, null);
    dialog.setView(R.layout.prompt_number);
  }

  public NumberPrompt number(int value) {
    this.value = value;
    return this;
  }

  public int getNumber() {
    try {
      return Integer.parseInt(number.getText().toString());
    } catch (NumberFormatException e) {
      Status.error("Cannot parse given text as number");
      return 0;
    }
  }

  @Override
  public AlertDialog show() {
    AlertDialog shown = super.show();
    number = EditTextWrapper.wrap(shown.findViewById(R.id.number));
    if (value != Integer.MAX_VALUE) {
      number.text(String.valueOf(value));
    }

    return shown;
  }
}
