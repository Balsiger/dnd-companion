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

import net.ixitxachitls.companion.data.templates.MiniatureTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A location miniatures can be stored in.
 */
public class MiniatureLocation extends NestedDocument
    implements Comparable<MiniatureLocation> {

  private static final String FIELD_NAME = "name";
  private static final String FIELD_FILTERS = "filters";
  private static final String FIELD_COLOR = "color";

  private final List<MiniatureFilter> filters;
  private String name;
  private int color;

  public MiniatureLocation() {
    this("", Collections.emptyList(), 0);
  }

  public MiniatureLocation(String name, List<MiniatureFilter> filters, int color) {
    this.name = name;
    this.filters = filters;
    this.color = color;
  }

  public Integer getColor() {
    return color;
  }

  public void setColor(int color) {
    this.color = color;
  }

  public List<MiniatureFilter> getFilters() {
    return Collections.unmodifiableList(filters);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void add(MiniatureFilter filter) {
    filters.add(filter);
  }

  @Override
  public int compareTo(MiniatureLocation other) {
    return this.name.compareTo(other.name);
  }

  public Optional<MiniatureFilter> matches(User me, MiniatureTemplate template) {
    for (MiniatureFilter filter : filters) {
      if (filter.matches(me, template)) {
        return Optional.of(filter);
      }
    }

    return Optional.empty();
  }

  public void remove(MiniatureFilter filter) {
    filters.remove(filter);
  }

  @Override
  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_NAME, name);
    data.put(FIELD_FILTERS, filters.stream()
        .map(MiniatureFilter::write)
        .collect(Collectors.toList()));
    data.put(FIELD_COLOR, color);

    return data;
  }

  public static MiniatureLocation read(Data data) {
    return new MiniatureLocation(
        data.get(FIELD_NAME, ""),
        data.getNestedList(FIELD_FILTERS).stream()
            .map(MiniatureFilter::read)
            .collect(Collectors.toList()),
        data.get(FIELD_COLOR, 0));
  }
}
