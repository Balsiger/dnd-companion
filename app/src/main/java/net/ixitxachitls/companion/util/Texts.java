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

package net.ixitxachitls.companion.util;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import net.ixitxachitls.companion.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods dealing with texts (strings including commands).
 */
public class Texts {

  private static final char COMMAND = '\\';
  private static final char START = '{';
  private static final char MARKER_START = '\001';
  private static final char END = '}';
  private static final char MARKER_END = '\002';
  private static final char OPTIONAL_START = '[';
  private static final char MARKER_OPTIONAL_START = '\003';
  private static final char OPTIONAL_END = ']';
  private static final char MARKER_OPTONAL_END = '\004';
  private static final char ESCAPE = '\\';
  private static final char MARKER_ESCAPE_START = '\005';
  private static final char MARKER_ESCAPE_END = '\006';
  private static final String SPECIAL = "<>=!~*#$%@?+|";

  public static Spanned toSpanned(Context context, String text) {
    return processCommands(context, processWhitespace(text));
  }

  public static String processWhitespace(String text) {
    return text.replaceAll("\n", "").replaceAll(" +", " ");
  }

  public static SpannableStringBuilder processCommands(Context context, String text) {
    if(text.isEmpty()) {
      return new SpannableStringBuilder(text);
    }

    // Allow \n\n for paragraphs.
    text = text.replace("\n\n", "\\par ");

    // Do we have any commands at all?
    if(text.indexOf(COMMAND) < 0) {
      return new SpannableStringBuilder(text);
    }

    // Mark the brackets in the text
    SpannableStringBuilder builder = new SpannableStringBuilder();
    text = markOptionalBrackets(markBrackets(text));

    for(int start = 0; start < text.length(); ) {
      // Search the first command (don't accept escaped commands)
      int pos = -1;
      for(pos = text.indexOf(COMMAND, start); pos >= 0;
          pos = text.indexOf(COMMAND, pos + 1))
        if((pos == 0 || text.charAt(pos - 1) != ESCAPE)
            && (ESCAPE!= COMMAND || text.charAt(pos + 1) != COMMAND)
            && (Character.isLetterOrDigit(text.charAt(pos + 1))
            || SPECIAL.indexOf(text.charAt(pos + 1)) >= 0))
          break;

      if(pos < 0) {
        // No commands any more.
        builder.append(clean(text.substring(start)));
        break;
      }

      // Intermediate text.
      if(pos > 0) {
        builder.append(clean(text.substring(start, pos)));
      }

      // Ok, we really have a command now.
      int end = pos + 1;
      if(Character.isLetterOrDigit(text.charAt(end))) {
        for( ; end < text.length(); end++) {
          if (!Character.isLetterOrDigit(text.charAt(end))) {
            break;
          }
        }
      } else {
        for (; end < text.length(); end++) {
          if (SPECIAL.indexOf(text.charAt(end)) < 0) {
            break;
          }
        }
      }

      String name = text.substring(pos + 1, end);

      // Extract the optional arguments.
      List<SpannableStringBuilder> optionals = new ArrayList<>();

      Pattern pattern = Pattern.compile("^\\s*" + MARKER_OPTIONAL_START
          + "<(\\d+)>((?:[^" + MARKER_OPTONAL_END
          + "]*?" + MARKER_OPTONAL_END
          + "<\\1>\\s*" + MARKER_OPTIONAL_START
          + "<\\1>)*[^" + MARKER_OPTONAL_END
          + "]*?)" + MARKER_OPTONAL_END + "<\\1>");

      Matcher matcher = pattern.matcher(text.substring(end));
      if(matcher.find())
      {
        String []args = matcher.group(2).split(MARKER_OPTONAL_END + "<"
            + matcher.group(1) + ">\\s*"
            + MARKER_OPTIONAL_START + "<"
            + matcher.group(1) + ">");

        end += matcher.end();

        // now we have all the arguments, we need to parse them as well
        for(int i = 0; i < args.length; i++)
          optionals.add(processCommands(context, args[i]));
      }

      // Extract the arguments (we have to copy the above because we need to
      // update the position in the String as well as extracting the arguments;
      // to solve that, the method would not be able to be static).
      List<SpannableStringBuilder> arguments = new ArrayList<>();

      pattern = Pattern.compile("^\\s*" + MARKER_START + "<(\\d+)>((?:[^"
          + MARKER_END + "]*?" + MARKER_END
          + "<\\1>\\s*" + MARKER_START
          + "<\\1>)*[^" + MARKER_END + "]*?)"
          + MARKER_END + "<\\1>");

      matcher = pattern.matcher(text.substring(end));
      if(matcher.find()) {
        String[] args = matcher.group(2).split(MARKER_END + "<"
            + matcher.group(1) + ">\\s*"
            + MARKER_START + "<"
            + matcher.group(1) + ">");

        end += matcher.end();

        // now we have all the arguments, we need to parse them as well
        for(int i = 0; i < args.length; i++) {
          arguments.add(processCommands(context, args[i]));
        }
      }

      // Now we try to render the command (we have to use an additional
      // template to avoid exceptions because a rendering is already in place)
      builder.append(render(context, name, optionals, arguments));

      // If no arguments were found, then skip the character directly following
      // the command (must be a white space)
      if(arguments.size() == 0 && optionals.size() == 0
          && text.length() > end
          && Character.isWhitespace(text.charAt(end)))
        end++;

      start = end;
    }

    return builder;
  }

  private static Spanned render(Context context, String command, List<SpannableStringBuilder> optionals,
                                List<SpannableStringBuilder> arguments) {
    switch (command) {
      case "Class":
        arguments.get(0).setSpan(new ForegroundColorSpan(
            context.getResources().getColor(R.color.className, null)),
            0, arguments.get(0).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return arguments.get(0);

      default:
        return rebuildCommand(command, optionals, arguments);
    }
  }

  private static SpannableStringBuilder rebuildCommand(String command,
                                                       List<SpannableStringBuilder> optionals,
                                                       List<SpannableStringBuilder> arguments) {
    SpannableStringBuilder builder = new SpannableStringBuilder(command);
    for (Spanned optional : optionals) {
      builder.append("[");
      builder.append(optional);
      builder.append("]");
    }
    for (Spanned argument : arguments) {
      builder.append("{");
      builder.append(argument);
      builder.append("}");
    }

    return builder;
  }

  private static String markBrackets(String text) {
    return markBrackets(text, ESCAPE, START, END, MARKER_START, MARKER_END);
  }

  private static String markOptionalBrackets(String text) {
    return markBrackets(text, ESCAPE, OPTIONAL_START, OPTIONAL_END, MARKER_OPTIONAL_START,
        MARKER_OPTONAL_END);
  }

  @VisibleForTesting
  protected static String markBrackets(String text, char escape,
                                     char start, char end,
                                     char markerStart, char markerEnd)
  {
    // Remove all escaped markers.
    text = text.replaceAll("\\" + escape + "\\" + start, "" + MARKER_ESCAPE_START);
    text = text.replaceAll("\\" + escape + "\\" + end, "" + MARKER_ESCAPE_END);

    // We mark all bracket markers for easier replacement.
    Pattern pattern = Pattern.compile("\\" + start + "([^\\" + start + "\\" + end + "]*?)\\" + end,
        Pattern.DOTALL);

    int i = 0;
    for(Matcher matcher = pattern.matcher(text); matcher.find(0); matcher = pattern.matcher(text)) {
      // Replace the nested brackets.
      text = matcher.replaceAll(markerStart + "<#" + i + "#>$1" + markerEnd + "<#" + i++ + "#>");
    }

    // 'Invert' the number to make sure that really the nesting level starts
    // at 0 on the outside not on the inside
    // with the above, {{}} is {<1>{<0>}<0>}<1> instead of {<0>{<1>}<1>}<0>
    pattern = Pattern.compile(markerStart + "<#(\\d+)#>(.*?)" + markerEnd + "<#\\1#>", Pattern.DOTALL);

    i = 0;
    for(Matcher matcher = pattern.matcher(text); matcher.find(0); matcher = pattern.matcher(text)) {
      // Replace the nested brackets.
      text = matcher.replaceAll(markerStart + "<" + i + ">$2" + markerEnd + "<" + i++ + ">");
    }

    // Replace all non 0 markers (we can't leave nested markers or parsing
    // of multiple arguments may fail
    text = text.replaceAll(markerStart + "<[1-9]\\d*>", "" + start);
    text = text.replaceAll(markerEnd   + "<[1-9]\\d*>", "" + end);

    // Replace removed escaped markers.
    text = text.replaceAll("" + MARKER_ESCAPE_START, "\\" + escape + start);
    text = text.replaceAll("" + MARKER_ESCAPE_END, "\\" + escape+ end);

    return text;
  }

  protected static String clean(String text)
  {
    return text.replaceAll(MARKER_START + "<\\d+>", "" + START)
        .replaceAll(MARKER_END + "<\\d+>", "" + END)
        .replaceAll(MARKER_OPTIONAL_START + "<\\d+>", "" + OPTIONAL_START)
        .replaceAll(MARKER_OPTONAL_END + "<\\d+>", "" + OPTIONAL_END);
  }

}
