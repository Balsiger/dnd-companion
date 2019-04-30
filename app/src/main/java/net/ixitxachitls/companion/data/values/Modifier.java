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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A D&D value for another value. These are usually a modification number (+ or -) and
 * the type of the value.
 */
public class Modifier {

  // See DMG p. 21.
  public enum Type {
    GENERAL, ABILITY, ALCHEMICAL, ARMOR, CIRCUMSTANCE, COMPETENCE, DEFLECTION, DODGE, ENHANCEMENT,
    EQUIPMENT, INHERENT, INSIGHT, LUCK, MORALE, NATURAL_ARMOR, PROFANE, RACIAL, RAGE, RESISTANCE,
    SACRED, SHIELD, SIZE, SYNERGY, UNKNOWN
  }

  public static final String TYPE_PATTERN =
      Arrays.asList(Type.values()).stream().map(Type::name).reduce("\\s*", (a, b) -> a + "|" + b);

  private static final String FIELD_VALUE = "value";
  private static final String FIELD_TYPE = "type";
  private static final String FIELD_SOURCE = "source";
  private static final String FIELD_CONDITION = "condition";
  private final int value;
  private final Type type;
  private final String source;
  private final Optional<String> condition;

  public Modifier(int value, Type type, String source) {
    this(value, type, source, Optional.empty());
  }

  public Modifier(int value, Type type, String source, String condition) {
    this(value, type, source, Optional.of(condition));
  }

  private Modifier(int value, Type type, String source, Optional<String> condition) {
    this.value = value;
    this.type = type;
    this.source = source;
    this.condition = condition;
  }

  public String getSource() {
    return source;
  }

  public Type getType() {
    return type;
  }

  public int getValue() {
    return value;
  }

  public boolean hasCondition() {
    return condition.isPresent();
  }

  public String toShortString() {
    StringBuilder result = new StringBuilder();
    if (value >= 0)
      result.append("+");

    result.append(value);

    if (type != Type.GENERAL) {
      result.append(" " + name());
    }

    if (condition.isPresent()) {
      result.append(" if " + condition.get());
    }

    return result.toString();
  }

  @Override
  public String toString() {
    if (!source.isEmpty()) {
      return toShortString() + " (" + source + ")";
    }

    return toShortString();
  }

  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_VALUE, value);
    data.put(FIELD_TYPE, type.name());
    data.put(FIELD_SOURCE, source);
    if (condition.isPresent()) {
      data.put(FIELD_CONDITION, condition.get());
    }

    return data;
  }

  private String name() {
    return type.name().toLowerCase().replace("_", " ");
  }

  public static boolean before(Modifier first, Modifier second) {
    if (first.getType() != second.getType()) {
      throw new IllegalArgumentException("must have modifiers of the same type");
    }

    // The most negative value comes first.
    if (first.value < 0 && first.value < second.value) {
      return true;
    }

    if (second.value < 0 && second.value < first.value) {
      return false;
    }

    // The most postive values next.
    if (first.value > 0 && first.value > second.value) {
      return true;
    }

    if (second.value > 0 && second.value > first.value) {
      return false;
    }

    // The values are the same, now look at conditions.
    if (!first.hasCondition() && second.hasCondition()) {
      return true;
    }

    if (!second.hasCondition() && first.hasCondition()) {
      return false;
    }

    // The values are the same and both either have a condition or both have none.
    return first.source.compareTo(second.source) < 0;
  }

  public static Type convert(Value.ModifierProto.Type type) {
    switch (type) {
      default:
      case UNRECOGNIZED:
      case UNKNOWN:
        return Type.UNKNOWN;

      case DODGE:
        return Type.DODGE;

      case ARMOR:
        return Type.ARMOR;

      case EQUIPMENT:
        return Type.EQUIPMENT;

      case SHIELD:
        return Type.SHIELD;

      case GENERAL:
        return Type.GENERAL;

      case NATURAL_ARMOR:
        return Type.NATURAL_ARMOR;

      case ABILITY:
        return Type.ABILITY;

      case SIZE:
        return Type.SIZE;

      case RACIAL:
        return Type.RACIAL;

      case CIRCUMSTANCE:
        return Type.CIRCUMSTANCE;

      case ENHANCEMENT:
        return Type.ENHANCEMENT;

      case DEFLECTION:
        return Type.DEFLECTION;

      case RAGE:
        return Type.RAGE;

      case COMPETENCE:
        return Type.COMPETENCE;

      case SYNERGY:
        return Type.SYNERGY;
    }
  }

  public static Modifier fromProto(Value.ModifierProto.Modifier proto, String source) {
    return new Modifier(proto.getValue(), convert(proto.getType()), source,
        Strings.optionalIfEmpty(proto.getCondition()));
  }

  public static Modifier precedence(Modifier first, Modifier second) {
    if (!stacks(first, second)) {
      throw new IllegalArgumentException("The modifiers don't stack!");
    }

    if (first.value < 0 && first.value <= second.value) {
      return first;
    }

    if (second.value < 0 && second.value < first.value) {
      return second;
    }

    if (!first.condition.isPresent() && second.condition.isPresent()) {
      return first;
    }

    if (first.condition.isPresent() && !second.condition.isPresent()) {
      return second;
    }

    if (first.value >= second.value) {
      return first;
    }

    return second;
  }

  public static Modifier read(Map<String, Object> data) {
    int value = (int) Values.get(data, FIELD_VALUE, 0);
    Type type = Values.get(data, FIELD_TYPE, Type.GENERAL);
    String source = Values.get(data, FIELD_SOURCE, "");
    Optional<String> condition = Strings.optionalIfEmpty(Values.get(data, FIELD_CONDITION, ""));

    return new Modifier(value, type, source);
  }

  public static boolean stacks(Modifier first, Modifier second) {
    // Modifier from the same source, eg. the same spell, don't stack.
    if (first.source.equals(second.source)) {
      return false;
    }

    // Modifiers of the same type don't stack, unless they are general, circumstance or dodge
    // (DMG p.21).
    if (first.type == second.type
        && first.type != Type.DODGE
        && first.type != Type.GENERAL
        && first.type != Type.CIRCUMSTANCE) {
      return false;
    }

    // Modifiers with conditions don't stack.
    if (first.condition.isPresent() && second.condition.isPresent()) {
      return false;
    }

    // Modifiers with conditions only stack with modifiers without conditions if the one without
    // condition has a higher modifier.
    if ((first.condition.isPresent() && first.value > second.value)
      || second.condition.isPresent() && second.value > first.value) {
      return false;
    }

    return true;
  }

  public static boolean stacks(Type type) {
    return type == Type.DODGE || type == Type.GENERAL || type == Type.CIRCUMSTANCE;
  }
}
