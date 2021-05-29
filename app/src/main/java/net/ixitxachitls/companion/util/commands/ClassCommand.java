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

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import net.ixitxachitls.companion.R;

import java.util.List;

/**
 * Format a class name.
 */
public class ClassCommand extends TextCommand {
  @Override
  public Spanned render(RenderingContext context, List<SpannableStringBuilder> optionals,
                        List<SpannableStringBuilder> arguments) {
    arguments.get(0).setSpan(new ForegroundColorSpan(
            context.getContext().getResources().getColor(R.color.className, null)),
        0, arguments.get(0).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    return arguments.get(0);
  }
}
