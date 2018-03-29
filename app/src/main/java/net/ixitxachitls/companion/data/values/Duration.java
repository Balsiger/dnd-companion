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

/**
 * A duration value.
 */
public class Duration {
  private final int rounds;
  private final int minutes;
  private final int hours;
  private final int days;
  private final int years;

  public Duration() {
    this(0, 0, 0, 0, 0);
  }

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
}
