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
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import net.ixitxachitls.companion.util.Texts;

import java.util.List;
import java.util.Map;

/**
 * A text command for formatting texts.
 */
public abstract class TextCommand {

  public static class RenderingContext {
    private final Context context;
    private final Texts.Values values;

    public RenderingContext(Context context, Texts.Values values) {
      this.context = context;
      this.values = values;
    }

    public Context getContext() {
      return context;
    }

    public Texts.Values getValues() {
      return values;
    }
  }

  public abstract Spanned render(RenderingContext context, List<SpannableStringBuilder> optionals,
                                 List<SpannableStringBuilder> arguments);
}
