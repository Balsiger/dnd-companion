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

import com.google.common.collect.Lists;

import net.ixitxachitls.companion.data.templates.MiniatureTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.concurrent.Immutable;

/**
 * A location miniatures can be stored in.
 */
@Immutable
public class MiniatureLocation extends NestedDocument
    implements Comparable<MiniatureLocation> {

  private static final String FIELD_NAME = "name";
  private static final String FIELD_FILTERS = "filters";
  private static final String FIELD_COLOR = "color";

  private final List<MiniatureFilter> filters;
  private final String name;
  private final int color;

  public MiniatureLocation() {
    this("", new ArrayList<>(), 0);
  }

  public MiniatureLocation(String name, List<MiniatureFilter> filters, int color) {
    this.name = name;
    this.filters = Lists.newArrayList(filters);
    this.color = color;
  }

  public Integer getColor() {
    return color;
  }

  public List<MiniatureFilter> getFilters() {
    return Collections.unmodifiableList(filters);
  }

  public String getName() {
    return name;
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

  public MiniatureLocation withColor(int color) {
    return new MiniatureLocation(name, filters, color);
  }

  public MiniatureLocation withName(String name) {
    return new MiniatureLocation(name, filters, color);
  }

  @Override
  public Data write() {
    return Data.empty()
        .set(FIELD_NAME, name)
        .setNested(FIELD_FILTERS, filters)
        .set(FIELD_COLOR, color);
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
