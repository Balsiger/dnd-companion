/*
 * Copyright (c) 2017-2019 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.Prompt;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;

/**
 * A prompt to ask the user for a single string.
 */
public class StringPrompt extends Prompt<StringPrompt> {

  private String value = "";
  private EditTextWrapper<EditText> text;

  public StringPrompt(Context context) {
    super(context);

    dialog.setPositiveButton(android.R.string.ok, null)
        .setNegativeButton(android.R.string.cancel, null);
    dialog.setView(R.layout.prompt_string);
  }

  public String getText() {
    return text.getText();
  }

  @Override
  public AlertDialog show() {
    AlertDialog shown = super.show();
    text = EditTextWrapper.wrap(shown.findViewById(R.id.text));
    text.text(value);

    return shown;
  }

  public StringPrompt text(String text) {
    this.value = text;
    return this;
  }
}
