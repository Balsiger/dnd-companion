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

package net.ixitxachitls.companion.util.commands;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;

import java.util.List;

/**
 * A command to combine multiple other commands
 */
public class CompoundCommand extends TextCommand {
  private final TextCommand[] commands;

  public CompoundCommand(TextCommand... commands) {
    this.commands = commands;
  }

  @Override
  public Spanned render(RenderingContext context, List<SpannableStringBuilder> optionals,
                        List<SpannableStringBuilder> arguments) {
    if (commands.length == 0) {
      return SpannedString.valueOf("");
    }

    for (TextCommand command : commands) {
      command.render(context, optionals, arguments);
    }

    return arguments.get(0);
  }
}

