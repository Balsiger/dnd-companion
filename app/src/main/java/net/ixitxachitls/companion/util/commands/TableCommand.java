/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

package net.ixitxachitls.companion.util.commands;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TypefaceSpan;

import net.ixitxachitls.companion.util.Strings;

import java.util.List;

/**
 * Text comamnd for rendering a table.
 */
public class TableCommand extends TextCommand {
  @Override
  public Spanned render(Context context, List<SpannableStringBuilder> optionals,
                        List<SpannableStringBuilder> arguments) {
    int columns = optionals.size();
    int []sizes = sizes(arguments, columns);

    SpannableStringBuilder result = new SpannableStringBuilder();
    for (int i = 0; i < arguments.size(); i++) {
      result.append(arguments.get(i));
      result.append(Strings.spaces(sizes[i % columns] - arguments.get(i).length() + 1));

      if ((i + 1) % columns == 0) {
        result.append("\n");
      }
    }

    result.setSpan(new TypefaceSpan("monospace"), 0, result.length(),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    return result;
  }

  private int[] sizes(List<SpannableStringBuilder> arguments, int columns) {
    int []results = new int[columns];

    for (int i = 0; i < arguments.size(); i++) {
      results[i % columns] = Math.max(results[i % columns], arguments.get(i).length());
    }

    return results;
  }
}
