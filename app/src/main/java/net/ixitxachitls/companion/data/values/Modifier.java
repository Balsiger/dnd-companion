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

/**
 * A D&D value for another value. These are usually a modification number (+ or -) and
 * the type of the value.
 */
public class Modifier {

  private final int value;
  private final Type type;
  private final String source;
  public Modifier(int value, Type type, String source) {
    this.value = value;
    this.type = type;
    this.source = source;
  }

  // See DMG p. 21.
  public enum Type {
    GENERAL, ABILITY, ALCHEMICAL, ARMOR, CIRCUMSTANCE, COMPETENCE, DEFLECTION, DODGE, ENHANCEMENT, EQUIPMENT,
    INHERENT, INSIGHT, LUCK, MORALE, NATURAL_ARMOR, PROFANE, RACIAL, RAGE, RESISTANCE, SACRED,
    SHIELD, SIZE, SYNERGY,
  }

  public int getValue() {
    return value;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (value >= 0)
      result.append("+");

    result.append(value);

    if (type != Type.GENERAL) {
      result.append(" " + name());
    }

    if (!source.isEmpty()) {
      result.append(" (" + source + ")");
    }

    return result.toString();
  }

  private String name() {
    return type.name().toLowerCase().replace("_", " ");
  }

  public static Modifier precedence(Modifier first, Modifier second) {
    if (stacks(first, second)) {
      throw new IllegalArgumentException("The modifiers don't stack!");
    }

    if (first.value < 0 && first.value <= second.value) {
      return first;
    }

    if (second.value < 0 && second.value < first.value) {
      return second;
    }

    if (first.value >= second.value) {
      return first;
    }

    return second;
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

    return false;
  }
}
