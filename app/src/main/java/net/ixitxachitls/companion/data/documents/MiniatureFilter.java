/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

package net.ixitxachitls.companion.data.documents;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.data.templates.MiniatureTemplate;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * A filter for miniatures.
 */
public class MiniatureFilter extends TemplateFilter<MiniatureTemplate>
    implements Comparable<MiniatureFilter> {

  private static final String FIELD_RACES = "races";
  private static final String FIELD_SETS = "sets";
  private static final String FIELD_TYPES = "types";
  private static final String FIELD_CLASSES = "classes";
  private static final String FIELD_SIZES = "sizes";
  private static final String FIELD_OWNED = "owned";
  private static final String FIELD_LOCATIONS = "locations";

  private final ImmutableList<String> races;
  private final ImmutableList<String> sets;
  private final ImmutableList<String> types;
  private final ImmutableList<String> classes;
  private final ImmutableList<String> sizes;
  private final ImmutableList<String> owned;
  private final ImmutableList<String> locations;

  public MiniatureFilter() {
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
    super(name);

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
    String parent = super.getSummary();
    if (!parent.isEmpty()) {
      parts.add(parent);
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

  @Override
  public boolean matches(User me, MiniatureTemplate miniature) {
    return super.matches(me, miniature)
        && (races.isEmpty() || races.contains(miniature.getRace()))
        && (sets.isEmpty() || sets.contains(miniature.getSet()))
        && (types.isEmpty() || matchesType(miniature, types))
        && (classes.isEmpty() || matchesClass(miniature, classes))
        && (sizes.isEmpty() || sizes.contains(miniature.getSize().getName()))
        && (owned.isEmpty()
        || owned.contains(String.valueOf(me.getMiniatureCount(miniature.getName())))
        || owned.contains("> 0") && me.getMiniatureCount(miniature.getName()) > 0)
        && (locations.isEmpty() || locations.contains(me.locationFor(miniature)));
  }

  @Override
  public Data write() {
    return super.write()
        .set(FIELD_RACES, races)
        .set(FIELD_SETS, sets)
        .set(FIELD_TYPES, types)
        .set(FIELD_CLASSES, classes)
        .set(FIELD_SIZES, sizes)
        .set(FIELD_OWNED, owned)
        .set(FIELD_LOCATIONS, locations);
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

  public static MiniatureFilter read(Data data) {
    return new MiniatureFilter(
        data.get(TemplateFilter.FIELD_NAME, ""),
        data.getList(FIELD_RACES, Collections.emptyList()),
        data.getList(FIELD_SETS, Collections.emptyList()),
        data.getList(FIELD_TYPES, Collections.emptyList()),
        data.getList(FIELD_CLASSES, Collections.emptyList()),
        data.getList(FIELD_SIZES, Collections.emptyList()),
        data.getList(FIELD_OWNED, Collections.emptyList()),
        data.getList(FIELD_LOCATIONS, Collections.emptyList()));
  }
}
