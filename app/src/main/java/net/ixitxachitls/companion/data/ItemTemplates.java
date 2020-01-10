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

package net.ixitxachitls.companion.data;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.templates.ItemTemplate;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.data.values.Money;
import net.ixitxachitls.companion.data.values.Weight;
import net.ixitxachitls.companion.proto.Template;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Storage for all item templates.
 */
public class ItemTemplates extends TemplatesStore<ItemTemplate> {

  private static final Random RANDOM = new Random();

  protected ItemTemplates() {
    super(ItemTemplate.class);
  }

  public List<String> realItems() {
    return byName.values().stream()
        .filter(ItemTemplate::isReal)
        .map(ItemTemplate::getName)
        .sorted()
        .collect(Collectors.toList());
  }

  public List<String> templates() {
    return byName.values().stream()
        .filter(ItemTemplate::isTemplate)
        .map(ItemTemplate::getName)
        .sorted()
        .collect(Collectors.toList());
  }

  public List<String> weapons() {
    return byName.values().stream()
        .filter(ItemTemplate::isWeapon)
        .map(ItemTemplate::getName)
        .sorted()
        .collect(Collectors.toList());
  }

  public List<ItemTemplate> lookupTemplates(Template.ItemLookupProto proto) {
    if (!proto.getName().isEmpty()) {
      Optional<ItemTemplate> template = get(proto.getName());
      if (template.isPresent()) {
        return ImmutableList.of(template.get());
      } else {
        Status.error("Could not find item to lookup: " + proto.getName());
        return ImmutableList.of();
      }
    }

    // Filter out all base templates.
    List<ItemTemplate> templates = byName.values().stream()
        .filter(f -> !f.isBaseOnly()).collect(Collectors.toList());

    // Filter by categories.
    if (!proto.getCategoryOrList().isEmpty()) {
      templates = templates.stream()
          .filter(t -> matchAny(t.getCategories(), proto.getCategoryOrList()))
          .collect(Collectors.toList());
    }

    // Filter by value (min/max).
    if (proto.hasValueMin()) {
      double min = Money.fromProto(proto.getValueMin()).asGold();
      templates = templates.stream()
          .filter(t -> t.getValue().asGold() >= min)
          .collect(Collectors.toList());
    }

    if (proto.hasValueMax()) {
      double max = Money.fromProto(proto.getValueMax()).asGold();
      templates = templates.stream()
          .filter(t -> t.getValue().asGold() <= max)
          .collect(Collectors.toList());
    }

    // Filter by weight (min/max).
    if (proto.hasWeightMin()) {
      double min = Weight.fromProto(proto.getWeightMin()).asPounds();
      templates = templates.stream()
          .filter(t -> t.getWeight().asPounds() >= min)
          .collect(Collectors.toList());
    }

    if (proto.hasWeightMax()) {
      double max = Weight.fromProto(proto.getWeightMax()).asPounds();
      templates = templates.stream()
          .filter(t -> t.getWeight().asPounds() <= max)
          .collect(Collectors.toList());
    }

    // Filter by sizes.
    if (!proto.getSizeOrList().isEmpty()) {
      templates = templates.stream()
          .filter(t -> proto.getSizeOrList().contains(t.getSizeProto()))
          .collect(Collectors.toList());
    }

    // Filter by materials.
    if (!proto.getMaterialOrList().isEmpty()) {
      templates = templates.stream()
          .filter(t -> proto.getMaterialOrList().contains(t.getMaterialProto()))
          .collect(Collectors.toList());
    }

    return templates;
  }

  public Optional<Item> lookup(CompanionContext context, Template.ItemLookupProto proto) {
    List<ItemTemplate> templates = lookupTemplates(proto);

    if (templates.isEmpty()) {
      return Optional.empty();
    }

    int total = totalWeightedProbability(templates);
    int random = RANDOM.nextInt(total);

    Optional<ItemTemplate> template = randomItemTemplate(templates, random);
    if (template.isPresent()) {
      return Optional.of(Item.createLookupItem(context, template.get(), proto));
    } else {
      return Optional.empty();
    }
  }

  public Optional<ItemTemplate> randomItemTemplate(List<ItemTemplate> templates, int random) {
    // Randomly select a value by rarity.
    int running = 0;
    for (ItemTemplate template : templates) {
      running += template.weightedProbability();
      if (running > random) {
        return Optional.of(template);
      }
    }

    return Optional.empty();
  }

  public int totalWeightedProbability(List<ItemTemplate> templates) {
    return templates.stream().mapToInt(t -> t.weightedProbability()).sum();
  }

  private boolean matchAny(List<String> first, List<String> second) {
    for (String value  : first) {
      if (second.contains(value)) {
        return true;
      }
    }

    return false;
  }
}
