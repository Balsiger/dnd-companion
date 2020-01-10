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

package net.ixitxachitls.companion.data.documents;

import net.ixitxachitls.companion.data.templates.ProductTemplate;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A filter to filter products.
 */
public class ProductFilter extends TemplateFilter<ProductTemplate> {

  private static final String FIELD_OWNED = "owned";
  private static final String FIELD_NOT_OWNED = "not-owned";
  private static final String FIELD_ID = "id";
  private static final String FIELD_DESCRIPTION = "description";
  private static final String FIELD_PERSON = "person";
  private static final String FIELD_WORLDS = "worlds";
  private static final String FIELD_PRODUCERS = "producers";
  private static final String FIELD_DATES = "dates";
  private static final String FIELD_SYSTEMS = "systems";
  private static final String FIELD_TYPES = "types";
  private static final String FIELD_AUDIENCES = "audiences";
  private static final String FIELD_STYLES = "styles";
  private static final String FIELD_LAYOUTS = "layouts";
  private static final String FIELD_SERIES = "series";

  private final boolean owned;
  private final boolean notOwned;
  private final String id;
  private final String description;
  private final String person;
  private final List<String> worlds;
  private final List<String> producers;
  private final List<String> dates;
  private final List<String> systems;
  private final List<String> types;
  private final List<String> audiences;
  private final List<String> styles;
  private final List<String> layouts;
  private final String series;

  public ProductFilter() {
    this("", "", false, false, "", "", Collections.emptyList(), Collections.emptyList(),
      Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
      Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "");
  }

  public ProductFilter(String id, String title, boolean owned, boolean notOwned, String description,
                       String person, List<String> worlds, List<String> producers,
                       List<String> dates, List<String> systems, List<String> types,
                       List<String> audiences, List<String> styles, List<String> layouts,
                       String series) {
    super(title);

    this.id = id;
    this.owned = owned;
    this.notOwned = notOwned;
    this.description = description;
    this.person = person;
    this.worlds = worlds;
    this.producers = producers;
    this.dates = dates;
    this.systems = systems;
    this.types = types;
    this.audiences = audiences;
    this.styles = styles;
    this.layouts = layouts;
    this.series = series;
  }

  public String getId() {
    return id;
  }

  public boolean isOwned() {
    return owned;
  }

  public boolean isNotOwned() {
    return notOwned;
  }

  public String getDescription() {
    return description;
  }

  public String getPerson() {
    return person;
  }

  public List<String> getWorlds() {
    return worlds;
  }

  public List<String> getProducers() {
    return producers;
  }

  public List<String> getDates() {
    return dates;
  }

  public List<String> getSystems() {
    return systems;
  }

  public List<String> getTypes() {
    return types;
  }

  public List<String> getAudiences() {
    return audiences;
  }

  public List<String> getStyles() {
    return styles;
  }

  public List<String> getLayouts() {
    return layouts;
  }

  public String getSeries() {
    return series;
  }

  public String getSummary() {
    List<String> parts = new ArrayList<>();
    String parent = super.getSummary();
    if (!parent.isEmpty()) {
      parts.add(parent);
    }
    if (owned) {
      parts.add("owned");
    }
    if (notOwned) {
      parts.add("not owned");
    }
    if (!id.isEmpty()) {
      parts.add("id contains " + id);
    }
    if (!description.isEmpty()) {
      parts.add("description contains " + description);
    }
    if (!person.isEmpty()) {
      parts.add("any person involved contains " + person);
    }
    if (!worlds.isEmpty()) {
      parts.add("world is " + Strings.PIPE_JOINER.join(worlds));
    }
    if (!producers.isEmpty()) {
      parts.add("producer is " + Strings.PIPE_JOINER.join(producers));
    }
    if (!dates.isEmpty()) {
      parts.add("dates is " + Strings.PIPE_JOINER.join(dates));
    }
    if (!systems.isEmpty()) {
      parts.add("system is " + Strings.PIPE_JOINER.join(systems));
    }
    if (!types.isEmpty()) {
      parts.add("type is " + Strings.PIPE_JOINER.join(types));
    }
    if (!audiences.isEmpty()) {
      parts.add("audience is " + Strings.PIPE_JOINER.join(audiences));
    }
    if (!styles.isEmpty()) {
      parts.add("style is " + Strings.PIPE_JOINER.join(styles));
    }
    if (!layouts.isEmpty()) {
      parts.add("layout is " + Strings.PIPE_JOINER.join(layouts));
    }
    if (!series.isEmpty()) {
      parts.add("series contains " + series);
    }

    return Strings.COMMA_JOINER.join(parts);
  }

  @Override
  public boolean matches(User me, ProductTemplate product) {
    return (name.isEmpty()
        || product.getFormattedTitle().toLowerCase().contains(name.toLowerCase()))
        && (id.isEmpty() || product.getName().toLowerCase().contains(id.toLowerCase()))
        && (!owned || me.ownsProduct(product.getName()))
        && (!notOwned || !me.ownsProduct(product.getName()))
        && (description.isEmpty()
        || product.getDescription().toLowerCase().contains(description.toLowerCase()))
        && (person.isEmpty() || matchesPerson(product, person))
        && (worlds.isEmpty() || matchesWorld(product, worlds))
        && (producers.isEmpty() || producers.contains(product.getProducer()))
        && (dates.isEmpty() || dates.contains(product.getFormattedDate()))
        && (systems.isEmpty() || systems.contains(product.getFormattedSystem()))
        && (types.isEmpty() || types.contains(product.getFormattedType()))
        && (audiences.isEmpty() || audiences.contains(product.getFormattedAudience()))
        && (styles.isEmpty() || styles.contains(product.getFormattedStyle()))
        && (layouts.isEmpty() || layouts.contains(product.getFormattedLayout()))
        && (series.isEmpty()
        || product.getFormattedSeries().toLowerCase().contains(series.toLowerCase()));
  }

  private boolean matchesPerson(ProductTemplate product, String person) {
    person = person.toLowerCase();
    return product.getFormattedAuthors().toLowerCase().contains(person)
        || product.getFormattedEditors().toLowerCase().contains(person)
        || product.getFormattedIllustrators().toLowerCase().contains(person)
        || product.getFormattedCover().toLowerCase().contains(person)
        || product.getFormattedTypographers().toLowerCase().contains(person)
        || product.getFormattedManagers().toLowerCase().contains(person);
  }

  private boolean matchesWorld(ProductTemplate product, List<String> worlds) {
    for (String world : product.getWorlds()) {
      if (worlds.contains(world)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Data write() {
    return super.write()
        .set(FIELD_OWNED, owned)
        .set(FIELD_NOT_OWNED, notOwned)
        .set(FIELD_ID, id)
        .set(FIELD_DESCRIPTION, description)
        .set(FIELD_PERSON, person)
        .set(FIELD_WORLDS, worlds)
        .set(FIELD_PRODUCERS, producers)
        .set(FIELD_DATES, dates)
        .set(FIELD_SYSTEMS, systems)
        .set(FIELD_TYPES, types)
        .set(FIELD_AUDIENCES, audiences)
        .set(FIELD_STYLES, styles)
        .set(FIELD_LAYOUTS, layouts)
        .set(FIELD_SERIES, series);
  }


  public static ProductFilter read(Data data) {
    return new ProductFilter(
        data.get(FIELD_ID, ""),
        data.get(TemplateFilter.FIELD_NAME, ""),
        data.get(FIELD_OWNED, false),
        data.get(FIELD_NOT_OWNED, false),
        data.get(FIELD_DESCRIPTION, ""), data.get(FIELD_PERSON, ""),
        data.get(FIELD_WORLDS, Collections.emptyList()),
        data.get(FIELD_PRODUCERS, Collections.emptyList()),
        data.get(FIELD_DATES, Collections.emptyList()),
        data.get(FIELD_SYSTEMS, Collections.emptyList()),
        data.get(FIELD_TYPES, Collections.emptyList()),
        data.get(FIELD_AUDIENCES, Collections.emptyList()),
        data.get(FIELD_STYLES, Collections.emptyList()),
        data.get(FIELD_LAYOUTS, Collections.emptyList()),
        data.get(FIELD_SERIES, ""));
  }
}
