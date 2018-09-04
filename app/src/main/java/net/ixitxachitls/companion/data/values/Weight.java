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
import java.util.Optional;

/**
 * A weight value.
 */
public class Weight {

  public static Weight ZERO = new Weight(Rational.ZERO, Rational.ZERO);
  private static final RationalValueParser PARSER = new RationalValueParser(
      new ValueParser.Unit("lb", "lbs", "pound", "pounds"),
      new ValueParser.Unit("oz", "ozs", "ounce", "ounces"));

  private final Rational pounds;
  private final Rational ounces;

  private Weight(Rational pounds, Rational ounces) {
    this.pounds = pounds;
    this.ounces = ounces;
  }

  public boolean isZero() {
    return pounds.isNull() && ounces.isNull();
  }

  public Weight add(Weight other) {
    return new Weight(pounds.add(other.pounds), ounces.add(other.ounces));
  }

  public Weight multiply(int factor) {
    if (factor == 1) {
      return this;
    }
    if (factor == 0) {
      return ZERO;
    }

    return new Weight(pounds.multiply(factor), ounces.multiply(factor));
  }

  private static Optional<Weight> parse(String text) {
    try {
      List<Rational> values = PARSER.parse(text);
      return Optional.of(new Weight(values.get(0), values.get(1)));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public static Weight fromProto(Value.WeightProto proto) {
    // TODO(merlin): Support metric units and carats!
    return new Weight(Rational.fromProto(proto.getImperial().getPounds()),
        Rational.fromProto(proto.getImperial().getOunces()));
  }

  public Value.WeightProto toProto() {
    return Value.WeightProto.newBuilder()
        .setImperial(Value.WeightProto.Imperial.newBuilder()
            .setPounds(pounds.toProto())
            .setOunces(ounces.toProto())
            .build())
        .build();
  }

  public double asPounds() {
    return pounds.asDouble() + ounces.asDouble() / 16.0;
  }

  @Override
  public String toString() {
    List<String> parts = new ArrayList<>();

    if ((!pounds.isNull())) {
      parts.add(pounds + " lb");
    }

    if (!ounces.isNull()) {
      parts.add(ounces + " oz");
    }

    if (parts.isEmpty()) {
      return "0 lb";
    }

    return Strings.SPACE_JOINER.join(parts);
  }
}
