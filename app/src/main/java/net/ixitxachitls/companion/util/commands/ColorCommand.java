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
import android.support.annotation.ColorRes;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.List;

/**
 * A command to display the text colored.
 */
public class ColorCommand extends TextCommand {
  private @ColorRes int color;

  public ColorCommand(@ColorRes int color) {
    this.color = color;
  }

  @Override
  public Spanned render(Context context, List<SpannableStringBuilder> optionals,
                        List<SpannableStringBuilder> arguments) {
    arguments.get(0).setSpan(new ForegroundColorSpan(
            context.getResources().getColor(color, null)),
        0, arguments.get(0).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    return arguments.get(0);
  }
}

