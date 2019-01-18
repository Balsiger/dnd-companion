/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

package net.ixitxachitls.companion.data.documents;

import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.data.templates.MiniatureTemplate;
import net.ixitxachitls.companion.data.values.Values;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A filter for miniatures.
 */
public class MiniatureFilter extends NestedDocument implements Comparable<MiniatureFilter> {

  private static final String FIELD_NAME = "name";
  private static final String FIELD_RACES = "races";
  private static final String FIELD_SETS = "sets";
  private static final String FIELD_TYPES = "types";
  private static final String FIELD_CLASSES = "classes";
  private static final String FIELD_ORIGINS = "origins";

  private final String name;
  private final ImmutableList<String> races;
  private final ImmutableList<String> sets;
  private final ImmutableList<String> types;
  private final ImmutableList<String> classes;
  private final ImmutableList<String> origins;

  public MiniatureFilter() {
    this.name = "";
    this.races = ImmutableList.of();
    this.sets = ImmutableList.of();
    this.types = ImmutableList.of();
    this.classes = ImmutableList.of();
    this.origins = ImmutableList.of();
  }

  public MiniatureFilter(String name, List<String> races, List<String> sets, List<String> types,
                         List<String> classes, List<String> origins) {
    this.name = name;
    this.races = ImmutableList.copyOf(races);
    this.sets = ImmutableList.copyOf(sets);
    this.types = ImmutableList.copyOf(types);
    this.classes = ImmutableList.copyOf(classes);
    this.origins = ImmutableList.copyOf(origins);
  }

  public List<String> getClasses() {
    return classes;
  }

  public String getName() {
    return name;
  }

  public List<String> getOrigins() {
    return origins;
  }

  public List<String> getRaces() {
    return races;
  }

  public List<String> getSets() {
    return sets;
  }

  public String getSummary() {
    List<String> parts = new ArrayList<>();
    if (!name.isEmpty()) {
      parts.add("name has " + name);
    }
    if (!races.isEmpty()) {
      parts.add("race is " + Strings.PIPE_JOINER.join(races));
    }
    if (!sets.isEmpty()) {
      parts.add("sets is " + Strings.PIPE_JOINER.join(races));
    }
    if (!types.isEmpty()) {
      parts.add("types is " + Strings.PIPE_JOINER.join(races));
    }
    if (!classes.isEmpty()) {
      parts.add("classes is " + Strings.PIPE_JOINER.join(races));
    }
    if (!origins.isEmpty()) {
      parts.add("origins is " + Strings.PIPE_JOINER.join(races));
    }

    return Strings.COMMA_JOINER.join(parts);
  }

  public List<String> getTypes() {
    return types;
  }

  @Override
  public int compareTo(@NonNull MiniatureFilter that) {
    int byScore = Integer.compare(this.sortScore(), that.sortScore());
    if (byScore != 0) {
      return byScore;
    }

    // TODO(merlin): This is not enough, but might work in practice?
    return this.name.compareTo(that.name);
  }

  public boolean matches(MiniatureTemplate miniature) {
    return (name.isEmpty() || miniature.getName().toLowerCase().contains(name.toLowerCase()))
        && (races.isEmpty() || races.contains(miniature.getRace()))
        && (sets.isEmpty() || sets.contains(miniature.getSet()))
        && (types.isEmpty() || matchesType(miniature, types))
        && (classes.isEmpty() || matchesClass(miniature, classes))
        && (origins.isEmpty() || origins.contains(miniature.getOrigin()));
  }

  @Override
  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_NAME, name);
    data.put(FIELD_RACES, races);
    data.put(FIELD_SETS, sets);
    data.put(FIELD_TYPES, types);
    data.put(FIELD_CLASSES, classes);
    data.put(FIELD_ORIGINS, origins);

    return data;
  }

  private boolean matchesClass(MiniatureTemplate miniature, List<String> classes) {
    for (String className : miniature.getClasses()) {
      if (classes.contains(className)) {
        return true;
      }
    }

    return false;
  }

  private boolean matchesType(MiniatureTemplate miniature, List<String> types) {
    if (types.contains(miniature.getType())) {
      return true;
    }

    for (String subtype : miniature.getSubtypes()) {
      if (types.contains(subtype)) {
        return true;
      }
    }

    return false;
  }

  private int sortScore() {
    return name.length() + races.size() + sets.size() + types.size() + classes.size()
        + origins.size();
  }

  public static MiniatureFilter read(Map<String, Object> data) {
    String name = Values.get(data, FIELD_NAME, "");
    List<String> races = Values.get(data, FIELD_RACES, Collections.emptyList());
    List<String> sets = Values.get(data, FIELD_SETS, Collections.emptyList());
    List<String> types = Values.get(data, FIELD_TYPES, Collections.emptyList());
    List<String> classes = Values.get(data, FIELD_CLASSES, Collections.emptyList());
    List<String> origins = Values.get(data, FIELD_ORIGINS, Collections.emptyList());

    return new MiniatureFilter(name, races, sets, types, classes, origins);
  }
}
