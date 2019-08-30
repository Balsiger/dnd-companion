/*
 * Copyright (c) 2017-2018 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.values;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A parser from string with units to values.
 */
public abstract class ValueParser<V> {

  public static class Unit {
    private final String singular;
    private final String plural;
    private final List<String> names;

    public Unit(String singular, String plural, String ... alternatives) {
      this.singular = singular;
      this.plural = plural;
      this.names = new ArrayList<>();
      this.names.add(singular);
      this.names.add(plural);
      this.names.addAll(Arrays.asList(alternatives));

      // Sort the names by length, to ensure that longer patterns match first (greedy).
      this.names.sort((o1, o2) -> {
        int length = Integer.compare(o2.length(), o1.length());
        if (length != 0) {
          return length;
        }

        return o1.compareTo(o2);
      });
    }

    public String pattern() {
      return PIPE_JOINER.join(names);
    }

    public boolean matches(String unit) {
      return names.contains(unit);
    }
  }

  protected static final Joiner PIPE_JOINER = Joiner.on("|");

  private final Unit[] units;
  private final Pattern pattern;

  public ValueParser(Unit ... units) {
    this.units = units;
    pattern = Pattern.compile(
        "\\s*(\\d+)\\s+(" +
            PIPE_JOINER.join(Arrays.asList(units).stream()
                .map(Unit::pattern)
                .collect(Collectors.toList())) +
            ")\\s*");
  }

  public List<V> parse(String text) throws IllegalArgumentException {
    Matcher matcher = pattern.matcher(text);
    List<V> values = initList(units.length);
    int start = 0;
    while (matcher.find()) {
      if (matcher.start() != start) {
        throw new IllegalArgumentException();
      }

      Optional<V> value = parseValue(matcher.group(1));
      int unit = index(matcher.group(2));

      if (value.isPresent() && unit >= 0) {
        values.set(unit, add(values.get(unit), value.get()));
        start = matcher.end();
      } else {
        throw new IllegalArgumentException();
      }
    }

    if (start == 0 || start != text.length()) {
      throw new IllegalArgumentException();
    }
    return values;
  }

  private List<V> initList(int size) {
    List<V> list = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      list.add(zero());
    }
    return list;
  }

  protected abstract V zero();
  protected abstract V add(V first, V second);
  protected abstract Optional<V> parseValue(String value);

  private int index(String unit) {
    for (int i = 0; i < units.length; i++) {
      if (units[i].matches(unit)) {
        return i;
      }
    }

    return -1;
  }
}
