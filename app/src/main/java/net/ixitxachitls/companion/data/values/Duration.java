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

import net.ixitxachitls.companion.data.documents.Data;
import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * A duration value.
 */
public class Duration implements Comparable<Duration> {

  public static final Duration PERMANENT = new Duration(-1, -1, -1, -1, -1, -1, -1, -1);
  public static final Duration NULL = new Duration(0, 0, 0, 0, 0, 0, 0, 0);
  public static final Duration ZERO = NULL;
  private static final String FIELD_ROUNDS = "rounds";
  private static final String FIELD_MINUTES = "minutes";
  private static final String FIELD_HOURS = "hours";
  private static final String FIELD_DAYS = "days";
  private static final String FIELD_YEARS = "years";
  private static final String FIELD_STANDARD_ACTIONS = "standard_actions";
  private static final String FIELD_SWIFT_ACTIONS = "swift_actions";
  private static final String FIELD_FREE_ACTIONS = "free_actions";
  private static final Pattern ROUNDS_ONLLY = Pattern.compile("\\s*(\\d+)\\s*");
  private static final ValueParser<Integer> PARSER = new IntegerValueParser(
      new ValueParser.Unit("round", "rounds", "r"),
      new ValueParser.Unit("minute", "minutes", "min", "m"),
      new ValueParser.Unit("hour", "hours", "h"),
      new ValueParser.Unit("day", "days", "d"),
      new ValueParser.Unit("year", "years", "y"),
      new ValueParser.Unit("standard action", "standard actions", "standard"),
      new ValueParser.Unit("swift action", "swift actions", "swift"),
      new ValueParser.Unit("free action", "free actions", "free"));

  private final int rounds;
  private final int minutes;
  private final int hours;
  private final int days;
  private final int years;
  private final int standardActions;
  private final int swiftActions;
  private final int freeActions;

  protected Duration(int minutes, int hours, int days, int years,
                     int rounds, int standardActions, int swiftActions, int freeActions) {
    this.minutes = minutes;
    this.hours = hours;
    this.days = days;
    this.years = years;
    this.rounds = rounds;
    this.standardActions = standardActions;
    this.swiftActions = swiftActions;
    this.freeActions = freeActions;
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

  public int getStandardActions() {
    return standardActions;
  }

  public int getSwiftActions() {
    return swiftActions;
  }

  public int getFreeActions() {
    return freeActions;
  }

  public boolean isNegative() {
    if (isPermanent()) {
      return false;
    }

    if (isRounds()) {
      return rounds < 0 || standardActions < 0 || swiftActions < 0 || freeActions < 0;
    }

    // NOTE(merlin): We assume that either all values are positive or all values are negative.
    return minutes < 0 || hours < 0 || days < 0 || years < 0;
  }

  public boolean isNone() {
    return equals(NULL);
  }

  public boolean isPermanent() {
    return equals(PERMANENT);
  }

  public boolean isRounds() {
    return rounds != 0 || standardActions != 0 || swiftActions != 0 || freeActions != 0;
  }

  public Duration add(Duration duration) {
    if (isNone()) {
      return duration;
    }

    return new Duration(minutes + duration.minutes, hours + duration.hours, days + duration.days,
        years + duration.years, rounds + duration.rounds,
        standardActions + duration.standardActions,
        swiftActions + duration.swiftActions, freeActions + duration.freeActions);
  }

  @Override
  public int compareTo(Duration other) {
    // TODO(merlin): This is a simplification, as it does not take into account that
    // durations could have more days than a year, for example.
    if (years != other.years) {
      return years - other.years;
    }

    if (days != other.days) {
      return days - other.days;
    }

    if (hours != other.hours) {
      return hours - other.hours;
    }

    if (days != other.days) {
      return days - other.days;
    }

    if (minutes != other.minutes) {
      return minutes - other.minutes;
    }

    if (rounds != other.rounds) {
      return rounds - other.rounds;
    }

    if (standardActions != other.standardActions) {
      return standardActions - other.standardActions;
    }

    if (swiftActions != other.swiftActions) {
      return swiftActions - other.swiftActions;
    }

    if (freeActions != other.freeActions) {
      return freeActions - other.freeActions;
    }

    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Duration other = (Duration) o;
    return rounds == other.rounds
        && minutes == other.minutes
        && hours == other.hours
        && days == other.days
        && years == other.years
        && standardActions == other.standardActions
        && swiftActions == other.swiftActions
        && freeActions == other.freeActions;
  }

  @Override
  public int hashCode() {
    return Objects.hash(rounds, minutes, hours, days, years, standardActions, swiftActions,
        freeActions);
  }

  public Duration multiply(int factor) {
    if (isNone()) {
      return this;
    }

    return new Duration(minutes * factor, hours * factor, days * factor,
          years * factor, rounds * factor, standardActions * factor, swiftActions * factor,
        freeActions * factor);
  }

  public Value.DurationProto toProto() {
    return Value.DurationProto.newBuilder()
        .setRounds(rounds)
        .setMinutes(minutes)
        .setHours(hours)
        .setDays(days)
        .setYears(years)
        .setStandardActions(standardActions)
        .setSwiftActions(swiftActions)
        .setFreeActions(freeActions)
        .build();
  }

  @Override
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

  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    if (rounds > 0) {
      data.put(FIELD_ROUNDS, rounds);
    }
    if (minutes > 0) {
      data.put(FIELD_MINUTES, minutes);
    }
    if (hours > 0) {
      data.put(FIELD_HOURS, hours);
    }
    if (days > 0) {
      data.put(FIELD_DAYS, days);
    }
    if (years > 0) {
      data.put(FIELD_YEARS, years);
    }
    if (standardActions > 0) {
      data.put(FIELD_STANDARD_ACTIONS, standardActions);
    }
    if (swiftActions > 0) {
      data.put(FIELD_SWIFT_ACTIONS, swiftActions);
    }
    if (freeActions > 0) {
      data.put(FIELD_FREE_ACTIONS, freeActions);
    }

    return data;
  }

  private String formatUnsigned() {
    List<String> output = new ArrayList<String>();
    output.add(formatUnsigned(years, "year", "years"));
    output.add(formatUnsigned(days, "day", "days"));
    output.add(formatUnsigned(hours, "hour", "hours"));
    output.add(formatUnsigned(minutes, "minute", "minutes"));
    output.add(formatUnsigned(rounds, "round", "rounds"));
    output.add(formatUnsigned(standardActions, "standard action", "standard actions"));
    output.add(formatUnsigned(swiftActions, "swift action", "swift actions"));
    output.add(formatUnsigned(freeActions, "free action", "free actions"));

    return Strings.SPACE_JOINER.join(output);
  }

  private static @Nullable String formatUnsigned(int value, String unit, String pluralUnit) {
    if (value == 0) {
      return null;
    }

    return Math.abs(value) + " " + (Math.abs(value) == 1 ? unit : pluralUnit);
  }

  public static Duration fromProto(Value.DurationProto proto) {
    return new Duration(proto.getMinutes(), proto.getHours(), proto.getDays(), proto.getYears(),
        proto.getRounds(), proto.getStandardActions(), proto.getSwiftActions(),
        proto.getFreeActions());
  }

  public static Optional<Duration> parse(String input) {
    Matcher matcher = ROUNDS_ONLLY.matcher(input);
    if (matcher.matches()) {
      return Optional.of(Duration.rounds(Integer.parseInt(matcher.group(1)), 0, 0, 0));
    } else {
      try {
        List<Integer> values = PARSER.parse(input);
        if (values.get(1) > 0 || values.get(2) > 0 || values.get(3) > 0 || values.get(4) > 0) {
          return Optional.of(new Duration(values.get(1), values.get(2), values.get(3),
              values.get(4), 0, 0, 0, 0));
        }

        return Optional.of(new Duration(0, 0, 0, 0, values.get(0), values.get(5), values.get(6),
            values.get(7)));

      } catch (IllegalArgumentException e) {
        return Optional.empty();
      }
    }
  }

  public static Duration read(@Nullable Data data) {
    if (data == null) {
      return ZERO;
    }

    int rounds = data.get(FIELD_ROUNDS, 0);
    int minutes = data.get(FIELD_MINUTES, 0);
    int hours = data.get(FIELD_HOURS, 0);
    int days = data.get(FIELD_DAYS, 0);
    int years = data.get(FIELD_YEARS, 0);
    int standardActions = data.get(FIELD_STANDARD_ACTIONS, 0);
    int swiftActions = data.get(FIELD_SWIFT_ACTIONS, 0);
    int freeActions = data.get(FIELD_FREE_ACTIONS, 0);

    if (rounds > 0 || standardActions > 0 || swiftActions > 0 || freeActions > 0) {
      return rounds(rounds, standardActions, swiftActions, freeActions);
    }

    return time(years, days, hours, minutes);
  }

  public static Duration rounds(int rounds, int standardActions, int swiftActions,
                                int freeActions) {
    return new Duration(0, 0, 0, 0, rounds, standardActions, swiftActions, freeActions);
  }

  public static Duration time(int years, int days, int hours, int minutes) {
    return new Duration(minutes, hours, days, years, 0, 0, 0, 0);
  }
}
