/*
 * Copyright (c) 2017-2020 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
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

package net.ixitxachitls.companion.data.values;

import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.util.Strings;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A duration with a potential random element.
 */
public class RandomDuration implements Comparable<RandomDuration> {

  public static RandomDuration NULL = new RandomDuration(Collections.emptyList(), Duration.NULL);

  private List<Dice> die;
  private Duration duration;

  public RandomDuration(List<Dice> die, Duration duration) {
    this.die = die;
    this.duration = duration;
  }

  public boolean isNone() {
    return duration.isNone();
  }

  public boolean isRandom() {
    if (die.isEmpty()) {
      return true;
    }

    for (Dice dice : die) {
      if (!dice.isEmpty() && !dice.isOne()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public int compareTo(RandomDuration other) {
    return duration.compareTo(other.duration);
  }

  public Duration roll() {
    int rolled = die.stream().mapToInt(Dice::roll).sum();
    if (rolled > 0) {
      return duration.multiply(rolled);
    }

    return duration;
  }

  public Value.RandomDurationProto toProto() {
    return Value.RandomDurationProto.newBuilder()
        .addAllDice(die.stream().map(Dice::toProto)::iterator)
        .setDuration(duration.toProto())
        .build();
  }

  @Override
  public String toString() {
    if (die.isEmpty()) {
      return duration.toString();
    }

    return Strings.SPACE_JOINER.join(die.stream().map(Dice::toString).collect(Collectors.toList()))
        + " " + duration.toString();
  }

  public static RandomDuration fromProto(Value.RandomDurationProto proto) {
    return new RandomDuration(
        proto.getDiceList().stream().map(Dice::fromProto).collect(Collectors.toList()),
        Duration.fromProto(proto.getDuration()));
  }
}
