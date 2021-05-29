/*
 * Copyright (c) 2017-2019 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
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

package net.ixitxachitls.companion.util.commands;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;

import java.util.List;

/**
 * Text command for rendering a list.
 */
public class ListCommand extends TextCommand {

  @Override
  public Spanned render(RenderingContext context, List<SpannableStringBuilder> optionals,
                        List<SpannableStringBuilder> arguments) {
    SpannableStringBuilder result = new SpannableStringBuilder();
    for (SpannableStringBuilder argument : arguments) {
      int start = result.length();
      result.append("\u2022 ");
      result.append(argument);
      result.append("\n");
      result.setSpan(new LeadingMarginSpan.Standard(25, 50), start, start + argument.length() + 2,
          Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    return result;
  }
}
