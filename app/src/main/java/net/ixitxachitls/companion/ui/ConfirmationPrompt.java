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

package net.ixitxachitls.companion.ui;

import android.content.Context;

/**
 * Simple prompt to show a confirmation and waiting for a users yes or not.
 */
public class ConfirmationPrompt extends Prompt<ConfirmationPrompt> {

  public ConfirmationPrompt(Context context) {
    super(context);
    this.dialog
        .setIconAttribute(android.R.attr.alertDialogIcon)
        .setPositiveButton(android.R.string.yes, null)
        .setNegativeButton(android.R.string.no, null);
  }
}
