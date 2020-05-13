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

package net.ixitxachitls.companion.data;

import net.ixitxachitls.companion.data.documents.MiniatureFilter;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.data.templates.MiniatureTemplate;
import net.ixitxachitls.companion.proto.Template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Store for all the miniature templates.
 */
public class MiniatureTemplates extends FilteredTemplatesStore<MiniatureTemplate, MiniatureFilter> {

  protected MiniatureTemplates() {
    super(MiniatureTemplate.class, new MiniatureFilter());
  }

  public List<String> getAllSets() {
    SortedSet<String> sets = new TreeSet<>();
    for (MiniatureTemplate template : byName.values()) {
      sets.add(template.getSet());
    }

    return new ArrayList<>(sets);
  }

  public List<String> getClasses() {
    SortedSet<String> classes = new TreeSet<>();
    for (MiniatureTemplate template : configured) {
      classes.addAll(template.getClasses());
    }

    return new ArrayList<>(classes);
  }

  public List<String> getRaces() {
    SortedSet<String> races = new TreeSet<>();
    for (MiniatureTemplate template : configured) {
      races.add(template.getRace());
    }

    return new ArrayList<>(races);
  }

  public List<String> getSets() {
    SortedSet<String> sets = new TreeSet<>();
    for (MiniatureTemplate template : configured) {
      sets.add(template.getSet());
    }

    return new ArrayList<>(sets);
  }

  public List<String> getSizes() {
    SortedSet<String> sizes = new TreeSet<>();
    for (MiniatureTemplate template : configured) {
      sizes.add(template.getSize().getName());
    }

    return new ArrayList<>(sizes);
  }

  public List<String> getTypes() {
    SortedSet<String> types = new TreeSet<>();
    for (MiniatureTemplate template : configured) {
      types.add(template.getType());
      types.addAll(template.getSubtypes());
    }

    return new ArrayList<>(types);
  }

  public void addDummy(String name) {
    MiniatureTemplate template =
        new MiniatureTemplate(name, Template.MiniatureTemplateProto.getDefaultInstance());
    byName.put(name, template);
    configured.add(template);
  }

  @Override
  public void filter(User me, MiniatureFilter filter) {
    super.filter(me, filter);

    if (!filter.getSets().isEmpty()) {
      // Sort by number.
      Collections.sort(filtered, (first, second) -> {
        int compare = Integer.compare(first.getNumber(), second.getNumber());
        if (compare != 0) {
          return compare;
        }

        return first.getNumberAffix().compareTo(second.getNumberAffix());
      });
    }
  }

  public void updateSets(User me, Collection<String> hidden) {
    configured.clear();
    configured.addAll(byName.values().stream()
        .filter(t -> !hidden.contains(t.getSet()))
        .collect(Collectors.toList()));
    filter(me, filter);
  }

  @Override
  protected int computeFilteredOwned(User me) {
    return filtered.stream().mapToInt(t -> me.getMiniatureCount(t.getName())).sum();
  }
}
