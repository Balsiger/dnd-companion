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

import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * A base D&D value with potential stackingModifiers.
 */
public class ModifiedValue {

  private final String name;
  private final int base;
  private final int min;
  private final boolean signed;
  private final List<Modifier> stackingModifiers = new ArrayList<>();
  private final List<Modifier> unstackingModifiers = new ArrayList<>();

  public ModifiedValue(String name, int base, boolean signed) {
    this(name, base, Integer.MIN_VALUE, signed);
  }

  public ModifiedValue(String name, int base, int min, boolean signed) {
    this.name = name;
    this.base = base;
    this.min = min;
    this.signed = signed;
  }

  public int getBase() {
    return base;
  }

  public String getName() {
    return name;
  }

  public ModifiedValue add(List<Modifier> modifiers) {
    for (Modifier modifier : modifiers) {
      add(modifier);
    }

    return this;
  }

  public ModifiedValue add(Modifier modifier) {
    for (Modifier existing : stackingModifiers) {
      if (!Modifier.stacks(existing, modifier)) {
        if (Modifier.precedence(existing, modifier) == modifier) {
          // The new modifier does not stack with an existing modifier and takes precedence.
          stackingModifiers.remove(existing);
          stackingModifiers.add(modifier);
          unstackingModifiers.add(existing);
        } else {
          // The new modifier does not stack with an existing modifier and does not take precedence.
          unstackingModifiers.add(modifier);
        }

        return this;
      }
    }

    stackingModifiers.add(modifier);
    return this;
  }

  public String describeModifiers() {
    List<String> lines = new ArrayList<>();
    lines.add("Base value " + base);
    for (Modifier modifier : stackingModifiers) {
      lines.add(modifier.toString());
    }
    for (Modifier modifier : unstackingModifiers) {
      lines.add(modifier.toString() + " [does not stack]");
    }

    return Strings.NEWLINE_JOINER.join(lines);
  }

  public int total() {
    int total = base;
    for (Modifier modifier : stackingModifiers) {
      total += modifier.getValue();
    }

    if (total < min) {
      return min;
    } else {
      return total;
    }
  }

  public String totalFormatted() {
    int total = total();
    if (signed) {
      return (total < 0 ? "" : "+") + total;
    }

    return String.valueOf(total);
  }
}
