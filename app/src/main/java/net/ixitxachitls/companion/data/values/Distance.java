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

package net.ixitxachitls.companion.data.values;

import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * A physical distance in the game
 */
public class Distance {

  public static Distance ZERO = new Distance(Rational.ZERO, Rational.ZERO, Rational.ZERO);

  private final Rational miles;
  private final Rational feet;
  private final Rational inches;

  // TODO(merlin): Support other units than imperial!
  protected Distance(Rational miles, Rational feet, Rational inches) {
    this.miles = miles;
    this.feet = feet;
    this.inches = inches;
  }

  public Value.DistanceProto toProto() {
    return Value.DistanceProto.newBuilder()
        .setImperial(Value.DistanceProto.Imperial.newBuilder()
            .setMiles(miles.toProto())
            .setFeet(feet.toProto())
            .setInches(inches.toProto())
            .build())
        .build();
  }

  @Override
  public String toString() {
    List<String> parts = new ArrayList<>();

    if (!miles.isNull()) {
      parts.add(miles + " miles");
    }
    if (!feet.isNull()) {
      parts.add(feet + " feet");
    }
    if (!inches.isNull()) {
      parts.add(inches + " inches");
    }

    return Strings.SPACE_JOINER.join(parts);
  }

  public static Distance fromProto(Value.DistanceProto proto) {
    return new Distance(Rational.fromProto(proto.getImperial().getMiles()),
        Rational.fromProto(proto.getImperial().getFeet()),
        Rational.fromProto(proto.getImperial().getInches()));
  }
}
