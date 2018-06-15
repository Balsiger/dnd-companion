/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

package net.ixitxachitls.companion.data.values;

import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * A duration value.
 */
public class Duration {

  public static final Duration PERMANENT = new Duration(-1, -1, -1, -1, -1);
  public static final Duration NULL = new Duration(0, 0, 0, 0, 0);

  private static final Pattern ROUNDS_ONLLY = Pattern.compile("\\s*(\\d+)\\s*");
  private static final Pattern PATTERN =
      Pattern.compile("\\s*(\\d+)\\s+(r|rounds?|m|min|minutes?|h|hours?|d|days?|y|years?)");

  private final int rounds;
  private final int minutes;
  private final int hours;
  private final int days;
  private final int years;

  private Duration(int rounds, int minutes, int hours, int days, int years) {
    this.rounds = rounds;
    this.minutes = minutes;
    this.hours = hours;
    this.days = days;
    this.years = years;
  }

  public static Duration rounds(int rounds) {
    return new Duration(rounds, 0, 0, 0, 0);
  }

  public static Duration time(int years, int days, int hours, int minutes) {
    return new Duration(0, minutes, hours, days, years);
  }

  public static Duration parse(String input) {
    int rounds = 0;
    int minutes = 0;
    int hours = 0;
    int days = 0;
    int years = 0;

    Matcher matcher = ROUNDS_ONLLY.matcher(input);
    if (matcher.matches()) {
      rounds = Integer.parseInt(matcher.group(1));
    } else {
      matcher = PATTERN.matcher(input);
      while (matcher.find()) {
        int value = Integer.parseInt(matcher.group(1));
        String unit = matcher.group(2);

        switch (unit) {
          case "r":
          case "round":
          case "rounds":
            rounds += value;
            break;

          case "m":
          case "min":
          case "minute":
          case "minutes":
            minutes += value;
            break;

          case "h":
          case "hour":
          case "hours":
            hours += value;
            break;

          case "d":
          case "day":
          case "days":
            days += value;
            break;

          case "y":
          case "year":
          case "years":
            years += value;
            break;
        }
      }
    }

    return new Duration(rounds, minutes, hours, days, years);
  }

  public boolean isNone() {
    return rounds == 0 && minutes == 0 && hours == 0 && days == 0 && years == 0;
  }

  public boolean isPermanent() {
    return rounds == -1 && minutes == -1 && hours == -1 && days == -1 && years == -1;
  }

  public boolean isRounds() {
    return rounds != 0 && minutes == 0;
  }

  public boolean isNegative() {
    if (isPermanent()) {
      return false;
    }

    if (isRounds()) {
      return rounds < 0;
    }

    // NOTE(merlin): We assume that either all values are positive or all values are negative.
    return minutes < 0 || hours < 0 || days < 0 || years < 0;
  }

  public int getDays() {
    return days;
  }

  public int getHours() {
    return hours;
  }

  public int getMinutes() {
    return minutes;
  }

  public int getRounds() {
    return rounds;
  }

  public int getYears() {
    return years;
  }

  public static Duration fromProto(Value.DurationProto proto) {
    return new Duration(proto.getRounds(), proto.getMinutes(), proto.getHours(),
        proto.getDays(), proto.getYears());
  }

  public Value.DurationProto toProto() {
    return Value.DurationProto.newBuilder()
        .setRounds(rounds)
        .setMinutes(minutes)
        .setHours(hours)
        .setDays(days)
        .setYears(years)
        .build();
  }

  public String toString() {
    if (isNone()) {
      return "ending";
    }

    if (isPermanent()) {
      return "permanent";
    }

    if (isNegative()) {
      return formatUnsigned() + " ago";
    }

    return formatUnsigned();
  }

  private String formatUnsigned() {
    List<String> output = new ArrayList<String>();
    output.add(formatUnsigned(years, "year", "years"));
    output.add(formatUnsigned(days, "day", "days"));
    output.add(formatUnsigned(hours, "hour", "hours"));
    output.add(formatUnsigned(minutes, "minute", "minutes"));
    output.add(formatUnsigned(rounds, "round", "rounds"));

    return Strings.SPACE_JOINER.join(output);
  }

  private static @Nullable String formatUnsigned(int value, String unit, String pluralUnit) {
    if (value == 0) {
      return null;
    }

    return Math.abs(value) + " " + (Math.abs(value) == 1 ? unit : pluralUnit);
  }
}
