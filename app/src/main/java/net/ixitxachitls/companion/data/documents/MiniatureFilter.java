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
  private static final String FIELD_SIZES = "sizes";
  private static final String FIELD_OWNED = "owned";
  private static final String FIELD_LOCATIONS = "locations";

  private final String name;
  private final ImmutableList<String> races;
  private final ImmutableList<String> sets;
  private final ImmutableList<String> types;
  private final ImmutableList<String> classes;
  private final ImmutableList<String> sizes;
  private final ImmutableList<String> owned;
  private final ImmutableList<String> locations;

  public MiniatureFilter() {
    this.name = "";
    this.races = ImmutableList.of();
    this.sets = ImmutableList.of();
    this.types = ImmutableList.of();
    this.classes = ImmutableList.of();
    this.sizes = ImmutableList.of();
    this.owned = ImmutableList.of();
    this.locations = ImmutableList.of();
  }

  public MiniatureFilter(String name, List<String> races, List<String> sets,
                         List<String> types, List<String> classes, List<String> sizes,
                         List<String> owned, List<String> locations) {
    this.name = name;
    this.races = ImmutableList.copyOf(races);
    this.sets = ImmutableList.copyOf(sets);
    this.types = ImmutableList.copyOf(types);
    this.classes = ImmutableList.copyOf(classes);
    this.sizes = ImmutableList.copyOf(sizes);
    this.owned = ImmutableList.copyOf(owned);
    this.locations = ImmutableList.copyOf(locations);
  }

  public List<String> getClasses() {
    return classes;
  }

  public List<String> getLocations() {
    return locations;
  }

  public String getName() {
    return name;
  }

  public List<String> getOwned() {
    return owned;
  }

  public List<String> getRaces() {
    return races;
  }

  public List<String> getSets() {
    return sets;
  }

  public List<String> getSizes() {
    return sizes;
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
      parts.add("sets is " + Strings.PIPE_JOINER.join(sets));
    }
    if (!types.isEmpty()) {
      parts.add("types is " + Strings.AND_JOINER.join(types));
    }
    if (!classes.isEmpty()) {
      parts.add("classes is " + Strings.PIPE_JOINER.join(classes));
    }
    if (!sizes.isEmpty()) {
      parts.add("sizes is " + Strings.PIPE_JOINER.join(sizes));
    }
    if (!owned.isEmpty()) {
      parts.add("owned is " + Strings.PIPE_JOINER.join(owned));
    }
    if (!locations.isEmpty()) {
      parts.add("locations is " + Strings.PIPE_JOINER.join(locations));
    }

    return Strings.COMMA_JOINER.join(parts);
  }

  public List<String> getTypes() {
    return types;
  }

  @Override
  public int compareTo(@NonNull MiniatureFilter that) {
    int byScore = Integer.compare(that.sortScore(), this.sortScore());
    if (byScore != 0) {
      return byScore;
    }

    return this.fullText().compareTo(that.fullText());
  }

  public String fullText() {
    List<String> texts = new ArrayList<>();
    texts.addAll(locations);
    texts.addAll(sets);
    texts.addAll(sizes);
    texts.addAll(races);
    texts.addAll(types);
    texts.addAll(classes);
    texts.addAll(owned);
    texts.add(name);

    return Strings.PIPE_JOINER.join(texts);
  }

  public boolean matches(User me, MiniatureTemplate miniature) {
    return (name.isEmpty() || miniature.getName().toLowerCase().contains(name.toLowerCase()))
        && (races.isEmpty() || races.contains(miniature.getRace()))
        && (sets.isEmpty() || sets.contains(miniature.getSet()))
        && (types.isEmpty() || matchesType(miniature, types))
        && (classes.isEmpty() || matchesClass(miniature, classes))
        && (sizes.isEmpty() || sizes.contains(miniature.getSize().getName()))
        && (owned.isEmpty()
            || owned.contains(String.valueOf(me.getMiniatureCount(miniature.getName()))))
        && (locations.isEmpty() || locations.contains(me.locationFor(miniature)));
  }

  @Override
  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    write(data, FIELD_NAME, name);
    write(data, FIELD_RACES, races);
    write(data, FIELD_SETS, sets);
    write(data, FIELD_TYPES, types);
    write(data, FIELD_CLASSES, classes);
    write(data, FIELD_SIZES, sizes);
    write(data, FIELD_OWNED, owned);
    write(data, FIELD_LOCATIONS, locations);

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
    for (String type : types) {
      if (!type.equals(miniature.getType())) {
        if (!miniature.getSubtypes().contains(type)) {
          return false;
        }
      }
    }

    return true;
  }

  private int sortScore() {
    return locations.size() * 17
        + sets.size() * 13
        + sizes.size() * 11
        + races.size() * 7
        + types.size() * 5
        + classes.size() * 3
        + owned.size() + name.length();
  }

  public static MiniatureFilter read(Map<String, Object> data) {
    String name = Values.get(data, FIELD_NAME, "");
    List<String> races = Values.get(data, FIELD_RACES, Collections.emptyList());
    List<String> sets = Values.get(data, FIELD_SETS, Collections.emptyList());
    List<String> types = Values.get(data, FIELD_TYPES, Collections.emptyList());
    List<String> classes = Values.get(data, FIELD_CLASSES, Collections.emptyList());
    List<String> sizes = Values.get(data, FIELD_SIZES, Collections.emptyList());
    List<String> owned = Values.get(data, FIELD_OWNED, Collections.emptyList());
    List<String> locations = Values.get(data, FIELD_LOCATIONS, Collections.emptyList());

    return new MiniatureFilter(name, races, sets, types, classes, sizes, owned, locations);
  }
}
