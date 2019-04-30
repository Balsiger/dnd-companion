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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A base D&D value with potential stackingModifiers.
 */
public class ModifiedValue {

  private final String name;
  private final int base;
  private final int min;
  private final boolean signed;
  private final Map<Modifier.Type, List<Modifier>> modifiersByType = new HashMap<>();

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
    List<Modifier> modifiers = modifiersByType.get(modifier.getType());
    if (modifiers == null) {
      modifiers = new ArrayList<>();
      modifiersByType.put(modifier.getType(), modifiers);
    }

    addSorted(modifiers, modifier);
    return this;
  }

  public String describeModifiers() {
    List<String> stackingLines = new ArrayList<>();
    List<String> nonStackingLines = new ArrayList<>();

    for (Modifier.Type type : modifiersByType.keySet()) {
      List<Modifier> stacking = stacking(type, modifiersByType.get(type), true);
      stackingLines.addAll(stacking.stream().map(Modifier::toString).collect(Collectors.toList()));
      nonStackingLines.addAll(modifiersByType.get(type).stream()
          .filter(m -> !stacking.contains(m))
          .map(m -> m.toString() + " [not stacking]")
          .collect(Collectors.toList()));
    }

    return ("Base value " + base + "\n"
        + Strings.NEWLINE_JOINER.join(stackingLines) + "\n"
        + Strings.NEWLINE_JOINER.join(nonStackingLines)).trim();
  }

  public int max() {
    return total(true);
  }

  public int total(boolean withConditions) {
    int total = base;
    for (Modifier.Type type : modifiersByType.keySet()) {
      total += sum(stacking(type, modifiersByType.get(type), withConditions));
    }

    if (total < min) {
      return min;
    } else {
      return total;
    }
  }

  public int total() {
    return total(false);
  }

  public String totalFormatted() {
    return format(total());
  }

  public String totalRangeFormatted() {
    int min = total(false);
    int max = total(true);

    if (max == min) {
      return format(min);
    }

    return format(min) + " - " + format(max);
  }

  private String format(int value) {
    if (signed) {
      return (value < 0 ? "" : "+") + value;
    }

    return String.valueOf(value);
  }

  private List<Modifier> stacking(Modifier.Type type, List<Modifier> modifiers,
                                  boolean withConditions) {
    // Remove modifiers with conditions.
    if (!withConditions) {
      modifiers = modifiers.stream().filter(m -> !m.hasCondition()).collect(Collectors.toList());
    }

    // Remove modifiers with the same source.
    Set<String> sources = new HashSet<>();
    for (Iterator<Modifier> i = modifiers.iterator(); i.hasNext(); ) {
      Modifier modifier = i.next();
      if (sources.contains(modifier.getSource())) {
        i.remove();
      } else {
        sources.add(modifier.getSource());
      }
    }

    if (Modifier.stacks(type)) {
      return modifiers;
    }

    for (int i = 0; i < modifiers.size(); i++) {
      if (!modifiers.get(i).hasCondition()) {
        return modifiers.subList(0, i + 1);
      }
    }

    return modifiers;
  }

  private int sum(List<Modifier> modifiers) {
    return modifiers.stream().mapToInt(Modifier::getValue).sum();
  }

  private static void addSorted(List<Modifier> modifiers, Modifier modifier) {
    for (int i = 0; i < modifiers.size(); i++) {
      if (Modifier.before(modifier, modifiers.get(i))) {
        modifiers.add(i, modifier);
        return;
      }
    }

    modifiers.add(modifier);
  }
}
