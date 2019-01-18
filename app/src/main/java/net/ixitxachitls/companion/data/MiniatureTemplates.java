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

package net.ixitxachitls.companion.data;

import net.ixitxachitls.companion.data.documents.MiniatureFilter;
import net.ixitxachitls.companion.data.templates.MiniatureTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Store for all the miniature templates.
 */
public class MiniatureTemplates extends TemplatesStore<MiniatureTemplate> {

  private List<MiniatureTemplate> filtered = new ArrayList<>();
  private MiniatureFilter filter = new MiniatureFilter();

  protected MiniatureTemplates() {
    super(MiniatureTemplate.class);
  }

  public List<String> getClasses() {
    SortedSet<String> classes = new TreeSet<>();
    for (MiniatureTemplate template : byName.values()) {
      classes.addAll(template.getClasses());
    }

    return new ArrayList<>(classes);
  }

  public MiniatureFilter getFilter() {
    return filter;
  }

  public int getFilteredNumber() {
    return filtered.size();
  }

  public List<String> getOrigins() {
    SortedSet<String> origins = new TreeSet<>();
    for (MiniatureTemplate template : byName.values()) {
      origins.add(template.getOrigin());
    }

    return new ArrayList<>(origins);
  }

  public List<String> getRaces() {
    SortedSet<String> races = new TreeSet<>();
    for (MiniatureTemplate template : byName.values()) {
      races.add(template.getRace());
    }

    return new ArrayList<>(races);
  }

  public List<String> getSets() {
    SortedSet<String> sets = new TreeSet<>();
    for (MiniatureTemplate template : byName.values()) {
      sets.add(template.getSet());
    }

    return new ArrayList<>(sets);
  }

  public int getTotalNumber() {
    return byName.size();
  }

  public List<String> getTypes() {
    SortedSet<String> types = new TreeSet<>();
    for (MiniatureTemplate template : byName.values()) {
      types.add(template.getType());
      types.addAll(template.getSubtypes());
    }

    return new ArrayList<>(types);
  }

  public boolean isFiltered() {
    return filtered.size() != byName.size();
  }

  public void filter(MiniatureFilter filter) {
    this.filter = filter;
    filtered = byName.values().stream().filter(filter::matches).collect(Collectors.toList());
  }

  public Optional<MiniatureTemplate> first() {
    if (filtered.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(filtered.get(0));
  }

  public Optional<MiniatureTemplate> last() {
    if (filtered.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(filtered.get(filtered.size() - 1));
  }

  @Override
  public void loaded() {
    filtered.addAll(byName.values());
  }

  public Optional<MiniatureTemplate> next(Optional<MiniatureTemplate> current) {
    if (!current.isPresent()) {
      return first();
    }

    if (filtered.isEmpty()) {
      return Optional.empty();
    }

    int found = filtered.indexOf(current.get());
    if (found >= 0 && found + 1 < filtered.size()) {
      return Optional.of(filtered.get(found + 1));
    }

    return first();
  }

  public Optional<MiniatureTemplate> previous(Optional<MiniatureTemplate> current) {
    if (!current.isPresent()) {
      return last();
    }

    if (filtered.isEmpty()) {
      return Optional.empty();
    }

    int found = filtered.indexOf(current.get());
    if (found >= 0 && found - 1 >= 0) {
      return Optional.of(filtered.get(found - 1));
    }

    return last();
  }
}
