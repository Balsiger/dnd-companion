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

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import net.ixitxachitls.companion.util.Texts;

import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

/**
 * A command to display the text colored.
 */
public class ClickableCommand extends TextCommand {
  private final @ColorRes int color;
  private final Action action;

  public ClickableCommand(@ColorRes int color, Action action) {
    this.color = color;
    this.action = action;
  }

  @FunctionalInterface
  public interface Action {
    public void execute(String argument, Texts.Values values);
  }

  @Override
  public Spanned render(RenderingContext context, List<SpannableStringBuilder> optionals,
                        List<SpannableStringBuilder> arguments) {
    // If the first argument contains values, merge these with the context.
    Texts.Values values = context.getValues()
        .add(optionals.stream().map(o -> o.toString()).collect(Collectors.toList()));

    // Take the first optional argument (if any) as the name to use when executing the action.
    String name =
        optionals.isEmpty() || optionals.get(0).toString().contains("=") ?
            arguments.get(0).toString() : optionals.get(0).toString();
    arguments.get(0).setSpan(new ClickableSpan() {
                               @Override
                               public void onClick(@NonNull View widget) {
                                 action.execute(name, values);
                               }

                               @Override
                               public void updateDrawState(@NonNull TextPaint paint) {
                                 paint.setColor(context.getContext().getResources()
                                     .getColor(color, null));
                               }
                             },
        0, arguments.get(0).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    return arguments.get(0);
  }
}

