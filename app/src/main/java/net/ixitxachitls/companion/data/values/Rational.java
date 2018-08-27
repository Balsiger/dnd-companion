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

import com.google.common.base.Strings;

import net.ixitxachitls.companion.proto.Value;

import java.math.BigInteger;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple rational value.
 */
public class Rational {

  public static Rational ZERO = new Rational(0, 0, 0, false);
  public static Rational ONE = new Rational(1, 0, 0, false);
  private final static Pattern PATTERN =
      Pattern.compile("\\s*(-)?\\s*(?:(\\d+)\\s*)?(?:(\\d+)\\s*/\\s*(\\d+))?\\s*");

  private final int leader;
  private final int nominator;
  private final int denominator;
  private final boolean negative;

  public Rational(int leader, int nominator, int denominator, boolean negative) {
    this.leader = leader;
    this.nominator = nominator;
    this.denominator = denominator;
    this.negative = negative;
  }

  public boolean isNull() {
    return leader == 0 && (nominator == 0 || denominator == 0);
  }

  public boolean isOne() {
    return leader == 1 && (nominator == 0 || denominator == 0)
        || (leader == 0 && nominator == denominator);
  }

  public static Optional<Rational> parse(String value) {
    Matcher matcher = PATTERN.matcher(value);
    if (matcher.matches() &&
        (!Strings.isNullOrEmpty(matcher.group(2)) ||
            (!Strings.isNullOrEmpty(matcher.group(3)) &&
                !Strings.isNullOrEmpty(matcher.group(4))))) {
      return Optional.of(new Rational(intOrZero(matcher.group(2)), intOrZero(matcher.group(3)),
          intOrZero(matcher.group(4)), matcher.group(1) != null && !matcher.group(1).isEmpty()));
    } else {
      return Optional.empty();
    }
  }

  public static Rational fromProto(Value.RationalProto proto) {
    return new Rational(proto.getLeader(), proto.getNominator(), proto.getDenominator(),
        proto.getNegative());
  }

  public Value.RationalProto toProto() {
    return Value.RationalProto.newBuilder()
        .setLeader(leader)
        .setNominator(nominator)
        .setDenominator(denominator)
        .setNegative(negative)
        .build();
  }

  private static int intOrZero(String value) {
    if (value == null || value.isEmpty()) {
      return 0;
    }

    return Integer.parseInt(value);
  }

  private int normalizedNominator() {
    if (nominator == 0 || denominator == 0) {
      return (negative ? -1 : 1) * leader;
    }

    return (negative ? -1 : 1) * (leader * denominator + nominator);
  }

  private int normalizedDenominator() {
    return denominator == 0 ? 1 : denominator;
  }

  public Rational add(Rational other) {
    int newNominator = normalizedNominator() * other.normalizedDenominator() +
        other.normalizedNominator() * normalizedDenominator();
    int newDenominator = normalizedDenominator() * other.normalizedDenominator();
    return new Rational(0, Math.abs(newNominator), newDenominator, newNominator < 0).simplify();
  }

  public Rational simplify() {
    if (nominator == 0 || denominator == 0) {
      return this;
    }

    int newLeader = leader + nominator / denominator;
    int newNominator = nominator % denominator;
    int newDenominator = denominator;

    int common = BigInteger.valueOf(newNominator)
        .gcd(BigInteger.valueOf(newDenominator))
        .intValue();

    newNominator /= common;
    newDenominator /= common;

    return new Rational(newLeader, newNominator, newDenominator, negative);
  }

  public double asDouble() {
    return leader + ((double) nominator) / denominator;
  }

  @Override
  public String toString() {
    String sign = "";
    if (negative) {
      sign = "-";
    }

    if (nominator == denominator || nominator == 0 || denominator == 0) {
      return sign + leader;
    }

    String fraction = formatFraction();
    if (leader == 0) {
      return sign + fraction;
    }

    if (fraction.length() > 1) {
      return sign + leader + " " + fraction;
    } else {
      return sign + leader + fraction;
    }
  }

  private String formatFraction() {
    switch (nominator) {
      case 1:
        switch (denominator) {
          case 2:
            return "½";
          case 3:
            return "⅓";
          case 4:
            return "¼";
          case 5:
            return "⅕";
          case 6:
            return "⅙";
          case 7:
            return "⅐";
          case 8:
            return "⅛";
          case 9:
            return "⅑";
          case 10:
            return "⅒";
        }
      case 2:
        switch (denominator) {
          case 3:
            return "⅔";
          case 5:
            return "⅖";
        }
      case 3:
        switch (denominator) {
          case 4:
            return "¾";
          case 5:
            return "⅗";
          case 8:
            return "⅜";
        }
      case 4:
        switch (denominator) {
          case 5:
            return "⅘";
        }
      case 5:
        switch (denominator) {
          case 6:
            return "⅚";
          case 8:
            return "⅝";
        }
      case 7:
        switch (denominator) {
          case 8:
            return "⅞";
        }
    }

    return nominator + "/" + denominator;
  }
}
