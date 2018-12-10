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
import java.util.Iterator;
import java.util.List;

/**
 * A base D&D value with potential modifiers.
 */
public class ModifiedValue {

  private final int base;
  private final List<Modifier> modifiers = new ArrayList<>();

  public ModifiedValue(int base) {
    this.base = base;
  }

  public ModifiedValue add(Modifier modifier) {
    this.modifiers.add(modifier);

    return this;
  }

  public int getBase() {
    return base;
  }

  public int total() {
    int total = base;

    for (Modifier modifier : stackedOnly(modifiers)) {
      total += modifier.getValue();
    }

    return total;
  }

  public String describeModifiers() {
    List<String> lines = new ArrayList<>();
    for (Modifier modifier : modifiers) {
      lines.add(modifier.toString());
    }

    return Strings.NEWLINE_JOINER.join(lines);
  }

  private static List<Modifier> stackedOnly(List<Modifier> modifiers) {
    List<Modifier> results = new ArrayList<>();

    for (Modifier modifier : modifiers) {
      addStacked(results, modifier);
    }

    return results;
  }

  private static void addStacked(List<Modifier> modifiers, Modifier modifierToAdd) {
    for (Iterator<Modifier> i = modifiers.iterator(); i.hasNext(); ) {
      Modifier modifier = i.next();
      if (Modifier.stacks(modifier, modifierToAdd)) {
        i.remove();
        modifiers.add(Modifier.precedence(modifier, modifierToAdd));
        return;
      }
    }

    modifiers.add(modifierToAdd);
  }
}
