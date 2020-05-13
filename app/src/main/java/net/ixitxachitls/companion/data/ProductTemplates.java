/*
 * Copyright (c) 2017-2019 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
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

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.data.documents.ProductFilter;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.data.templates.ProductTemplate;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.util.Strings;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Store for all product templates
 */
public class ProductTemplates extends FilteredTemplatesStore<ProductTemplate, ProductFilter> {
  protected ProductTemplates() {
    super(ProductTemplate.class, new ProductFilter());
  }

  public List<String> getAudiences() {
    return configured.stream()
        .map(p -> p.getFormattedAudience())
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  public List<String> getDates() {
    return configured.stream()
        .flatMap(p -> ImmutableList.of(p.getFormattedDate(), Integer.toString(p.getYear()))
            .stream())
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  public List<String> getLayouts() {
    return configured.stream()
        .map(p -> p.getFormattedLayout())
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  public List<String> getProducers() {
    return configured.stream()
        .map(p -> p.getProducer())
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  public List<String> getStyles() {
    return configured.stream()
        .map(p -> p.getFormattedStyle())
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  public List<String> getSystems() {
    return configured.stream()
        .map(p -> p.getFormattedSystem())
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  public List<String> getTypes() {
    return configured.stream()
        .map(p -> p.getFormattedType())
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  public List<String> getWorlds() {
    return configured.stream()
        .flatMap(p -> p.getWorlds().stream())
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  public SortedSet<String> extractAllProducers() {
    return byName.values().stream()
        .map(ProductTemplate::getProducer)
        .collect(Collectors.toCollection(TreeSet::new));
  }

  public SortedSet<String> extractAllSystems() {
    return Arrays.stream(Template.ProductTemplateProto.System.values())
        .map(v -> Strings.toWords(v.name()))
        .collect(Collectors.toCollection(TreeSet::new));
  }

  public SortedSet<String> extractAllTypes() {
    return Arrays.stream(Template.ProductTemplateProto.Type.values())
        .map(v -> Strings.toWords(v.name()))
        .collect(Collectors.toCollection(TreeSet::new));
  }

  public SortedSet<String> extractAllWorlds() {
    return byName.values().stream()
        .flatMap(p -> p.getWorlds().stream())
        .collect(Collectors.toCollection(TreeSet::new));
  }

  public void updateHidden(User me, Set<String> producers, Set<String> worlds, Set<String> systems,
                           Set<String> types) {
    configured.clear();
    configured.addAll(byName.values().stream()
        .filter(p -> !producers.contains(p.getProducer())
            && !containsAny(worlds, p.getWorlds())
            && !systems.contains(p.getFormattedSystem())
            && !types.contains(p.getFormattedType()))
        .collect(Collectors.toList()));
    filter(me, filter);
  }

  @Override
  protected int computeFilteredOwned(User me) {
    return (int) filtered.stream().filter(t -> me.ownsProduct(t.getName())).count();
  }

  private boolean containsAny(Collection<String> first, Collection<String> second) {
    for (String value : second) {
      if (first.contains(value)) {
        return true;
      }
    }

    return false;
  }
}
