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

package net.ixitxachitls.companion.util;

import com.google.common.base.Joiner;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utilities for handling strings.
 */
public class Strings {
  public static final Joiner COMMA_JOINER = Joiner.on(", ").skipNulls();
  public static final Joiner NEWLINE_JOINER = Joiner.on("\n").skipNulls();
  public static final Joiner SPACE_JOINER = Joiner.on(" ").skipNulls();
  public static final Joiner SEMICOLON_JOINER = Joiner.on("; ").skipNulls();
  public static final Joiner PIPE_JOINER = Joiner.on("|").skipNulls();
  public static final Joiner PLUS_JOINER = Joiner.on(" plus ").skipNulls();
  public static final Joiner AND_JOINER = Joiner.on("&").skipNulls();
  public static final Joiner SLASH_JOINER = Joiner.on("/").skipNulls();
  private static final String SPACES =
      "                                                                      "
          + "                                                                     "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      ";
  private static final String ZEROES =
      "000000000000000000000000000000000000000000000000000000000000000000000000"
          + "0000000000000000000000000000000000000000000000000000000000000000000000"
          + "0000000000000000000000000000000000000000000000000000000000000000000000"
          + "0000000000000000000000000000000000000000000000000000000000000000000000";

  private Strings() {
  }

  public static String ensureSentence(String text) {
    text = text.trim();

    if (text.isEmpty()) {
      return text;
    }

    if (text.endsWith(".")) {
      return text;
    }

    return text.replaceAll("[,;]$", "") + ".";
  }

  public static String extractPattern(String text, String patternText) {
    if (text.isEmpty() || patternText.isEmpty()) {
      return "";
    }

    if (patternText.indexOf("(") < 0 || patternText.indexOf(")") < 0) {
      patternText = "(" + patternText + ")";
    }

    Matcher matcher = Pattern.compile(patternText).matcher(text);
    if (matcher.find()) {
      return matcher.group(1);
    }

    return "";
  }

  public static String formatAgo(long time) {
    long seconds = (new Date().getTime() - time) / 1000;

    if (seconds < 60) {
      return seconds + "s";
    }

    long minutes = seconds / 60;
    seconds = seconds % 60;

    return minutes + "m " + seconds + "s";
  }

  public static String formatSigned(int number) {
    if (number < 0) {
      return String.valueOf(number);
    }

    return "+" + number;
  }

  public static String numeral(int number) {
    if (number == 1) {
      return "1st";
    }

    if (number == 2) {
      return "2nd";
    }

    if (number == 3) {
      return "3rd";
    }

    return number + "th";
  }

  public static Optional<String> optionalIfEmpty(String value) {
    if (value == null || value.trim().isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(value);
  }

  public static String orEmpty(Optional<? extends Object> value) {
    if (value.isPresent()) {
      return value.get().toString();
    }

    return "";
  }

  public static String pad(String inText, int inLength, boolean inLeft) {
    if (inText.length() >= inLength)
      return inText;

    if (inLeft)
      return SPACES.substring(0, inLength - inText.length()) + inText;
    else
      return inText + SPACES.substring(0, inLength - inText.length());
  }

  public static String pad(long inNumber, int inLength, boolean inLeft) {
    String text = Long.toString(inNumber);

    if (text.length() >= inLength)
      return text;

    if (inLeft)
      return ZEROES.substring(0, inLength - text.length()) + text;
    else
      return text + SPACES.substring(0, inLength - text.length());
  }

  public static String plural(int number, String singular, String plural) {
    return number + " " + (number == 1 ? singular : plural);
  }

  public static String signed(int number) {
    if (number < 0) {
      return String.valueOf(number);
    }

    return "+" + number;
  }

  public static String spaces(int length) {
    return SPACES.substring(0, length);
  }

  public static String toWords(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }

    return Arrays.stream(text.split("[ _]"))
        .map(word -> word.isEmpty() ? word : Character.toTitleCase(word.charAt(0)) +
            word.substring(1).toLowerCase())
        .collect(Collectors.joining(" "));
  }
}
