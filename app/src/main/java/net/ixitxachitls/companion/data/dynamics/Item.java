/*
 * Copyright (c) 2017-2018 Peter Balsiger
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

package net.ixitxachitls.companion.data.dynamics;

import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.statics.ItemTemplate;
import net.ixitxachitls.companion.data.values.Duration;
import net.ixitxachitls.companion.data.values.Money;
import net.ixitxachitls.companion.data.values.Weight;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An item in the game.
 */
public class Item {

  private String id;
  private String name;
  private List<ItemTemplate> templates;
  private int hp;
  private Money value;
  private String appearance;
  private String playerName;
  private String playerNotes;
  private String dmNotes;
  private int multiple;
  private int multiuse;
  private Duration timeLeft;
  private boolean identified;
  private List<Item> contents;

  public Item(String id, String name, List<ItemTemplate> templates, int hp, Money value,
              String appearance, String playerName, String playerNotes, String dmNotes,
              int multiple, int multiuse, Duration timeLeft, boolean identified,
              List<Item> contents) {
    this.id = id;
    this.name = name;
    this.templates = new ArrayList(templates);
    this.hp = hp;
    this.value = value;
    this.appearance = appearance;
    this.playerName = playerName.isEmpty() && !templates.isEmpty()
        ? templates.get(0).getName() : playerName;
    this.playerNotes = playerNotes;
    this.dmNotes = dmNotes;
    this.multiple = multiple;
    this.multiuse = multiuse;
    this.timeLeft = timeLeft;
    this.identified = identified;
    this.contents = new ArrayList(contents);
  }

  public String getId() {
    return id;
  }

  // TODO(merlin): Remove this once the single caller is gone.
  @Deprecated
  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String name) {
    this.playerName = name;
  }

  public List<ItemTemplate> getTemplates() {
    return Collections.unmodifiableList(templates);
  }

  public void setTemplates(List<ItemTemplate> templates) {
    this.templates = new ArrayList<>(templates);
  }

  public Money getValue() {
    Money totalValue = value.multiply(multiple);
    for (Item content : contents) {
      totalValue = totalValue.add(content.getValue());
    }

    return totalValue;
  }

  public void setValue(Money value) {
    this.value = value;
  }

  public int getHp() {
    return hp;
  }

  public void setHp(int hp) {
    this.hp = hp;
  }

  public String getAppearance() {
    return appearance;
  }

  public void setAppearance(String appearance) {
    this.appearance = appearance;
  }

  public int getMultiple() {
    return multiple;
  }

  public void setMultiple(int multiple) {
    this.multiple = multiple;
  }

  public int getMultiuse() {
    return multiuse;
  }

  public void setMultiuse(int multiuse) {
    this.multiuse = multiuse;
  }

  public Weight getWeight() {
    Weight weight = weight(templates).multiply(multiple);
    for (Item content : contents) {
      weight = weight.add(content.getWeight());
    }

    return weight;
  }

  public String getDescription() {
    return description(templates);
  }

  public List<Item> getContents() {
    return Collections.unmodifiableList(this.contents);
  }

  public Optional<ItemTemplate> getBaseTemplate() {
    if (templates.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(templates.get(0));
  }

  public Optional<Item> getNestedItem(String itemId) {
    for (Item item : contents) {
      if (item.getId().equals(itemId)) {
        return Optional.of(item);
      }

      Optional<Item> nested = item.getNestedItem(itemId);
      if (nested.isPresent()) {
        return nested;
      }
    }

    return Optional.empty();
  }

  public String summary() {
    return getValue().toString() + " / " + getWeight().toString();
  }

  public void add(Item item) {
    contents.add(item);
  }

  public boolean remove(Item item) {
    if (contents.remove(item)) {
      return true;
    }

    for (Item container : contents) {
      if (container.remove(item)) {
        return true;
      }
    }

    return false;
  }

  public boolean addItemBefore(Item item, Item move) {
    if (contents.contains(item)) {
      contents.add(contents.indexOf(item), move);
      return true;
    } else {
      for (Item container : contents) {
        if (container.addItemBefore(item, move)) {
          return true;
        }
      }
    }

    return false;
  }

  public boolean addItemAfter(Item item, Item move) {
    if (contents.contains(item)) {
      contents.add(contents.indexOf(item) + 1, move);
      return true;
    } else {
      for (Item container : contents) {
        if (container.addItemAfter(item, move)) {
          return true;
        }
      }
    }

    return false;
  }

  public static Item create(CompanionContext context, String ... names) {
    List<ItemTemplate> templates =
        Arrays.asList(names).stream().map(Item::template).collect(Collectors.toList());
    String name = name(templates);
    return new Item(generateId(context), name, templates, hp(templates), value(templates),
        appearance(templates), names[0], "", "", 0, 0, Duration.ZERO, false,
        Collections.emptyList());
  }

  public static String generateId(CompanionContext context) {
    return context.settings().getAppId() + "-" + context.settings().getNextItemId();
  }

  public static Item fromProto(Entry.ItemProto proto) {
    List<ItemTemplate> templates =
        proto.getTemplateList().stream().map(Item::template).collect(Collectors.toList());

    return new Item(proto.getId(),
        proto.getName(),
        templates,
        proto.getHitPoints(),
        Money.fromProto(proto.getValue()),
        proto.getAppearance(),
        proto.getPlayerName(),
        proto.getPlayerNotes(),
        proto.getDmNotes(),
        proto.getMultiple(),
        proto.getMultiuse(),
        Duration.fromProto(proto.getTimeLeft()),
        proto.getIdentified(),
        proto.getContentList().stream().map(Item::fromProto).collect(Collectors.toList()));
  }

  public Entry.ItemProto toProto() {
    return Entry.ItemProto.newBuilder()
        .setId(id)
        .setName(name)
        .addAllTemplate(templates.stream().map(ItemTemplate::getName).collect(Collectors.toList()))
        .setHitPoints(hp)
        .setValue(value.toProto())
        .setAppearance(appearance)
        .setPlayerName(playerName)
        .setPlayerNotes(playerNotes)
        .setDmNotes(dmNotes)
        .setMultiple(multiple)
        .setMultiuse(multiuse)
        .setTimeLeft(timeLeft.toProto())
        .setIdentified(identified)
        .addAllContent(contents.stream().map(Item::toProto).collect(Collectors.toList()))
        .build();
  }

  public boolean isContainer() {
    for (ItemTemplate template : templates) {
      if (template.isContainer()) {
        return true;
      }
    }

    return false;
  }

  public boolean similar(Item other) {
    return name.equals(other.name)
        && hp == other.hp
        && value.equals(other.value)
        && appearance.equals(other.appearance)
        && playerName.equals(other.playerName)
        && playerNotes.equals(other.playerNotes)
        && dmNotes.equals(other.dmNotes)
        && timeLeft.equals(other.timeLeft)
        && identified == other.identified
        && contents.isEmpty() && other.contents.isEmpty()
        && multiuse == other.multiuse
        && similar(templates, other.templates);
  }

  private boolean similar(List<ItemTemplate> first, List<ItemTemplate> second) {
    List<String> firstNames =
        first.stream().map(ItemTemplate::getName).collect(Collectors.toList());
    List<String> secondNames =
        first.stream().map(ItemTemplate::getName).collect(Collectors.toList());

    for (String name : firstNames) {
      if (!secondNames.remove(name)) {
        return false;
      }
    }

    return secondNames.isEmpty();
  }

  @Override
  public String toString() {
    return name + " (" + value + ")";
  }

  private static ItemTemplate template(String name) {
    Optional<ItemTemplate> template = Entries.get().getItems().get(name);
    if (template.isPresent()) {
      return template.get();
    }

    return ItemTemplate.createEmpty(name);
  }

  public static String name(List<ItemTemplate> templates) {
    return Strings.SPACE_JOINER.join(templates.stream()
        .map(ItemTemplate::getNamePart)
        .collect(Collectors.toList()));
  }

  public static int hp(List<ItemTemplate> templates) {
    return templates.stream().mapToInt(ItemTemplate::computeHp).sum();
  }

  public static Money value(List<ItemTemplate> templates) {
    Money total = Money.ZERO;
    for (ItemTemplate template : templates) {
      total = total.add(template.getValue());
    }

    return total.resolveMagic();
  }

  public static Weight weight(List<ItemTemplate> templates) {
    Weight total = Weight.ZERO;
    for (ItemTemplate template : templates) {
      total = total.add(template.getWeight());
    }

    return total;
  }

  public static String appearance(List<ItemTemplate> templates) {
    return Strings.SPACE_JOINER.join(templates.stream()
        .map(ItemTemplate::computeAppearance)
        .collect(Collectors.toList()));
  }

  public static String description(List<ItemTemplate> templates) {
    return Strings.COMMA_JOINER.join(templates.stream()
        .map(ItemTemplate::getDescription)
        .collect(Collectors.toList()));
  }
}
