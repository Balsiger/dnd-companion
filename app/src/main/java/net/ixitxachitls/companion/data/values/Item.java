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

package net.ixitxachitls.companion.data.values;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.NestedDocument;
import net.ixitxachitls.companion.data.templates.ItemTemplate;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An item in the game.
 */
public class Item extends NestedDocument {

  private static final String FIELD_ID = "id";
  private static final String FIELD_NAME = "name";
  private static final String FIELD_TEMPLATES = "templates";
  private static final String FIELD_HP = "hp";
  private static final String FIELD_VALUE = "value";
  private static final String FIELD_APPEARANCE = "appearance";
  private static final String FIELD_PLAYER_NAME = "player_name";
  private static final String FIELD_PLAYER_NOTES = "player_notes";
  private static final String FIELD_DM_NOTES = "dm_notes";
  private static final String FIELD_MULTIPLE = "multiple";
  private static final String FIELD_MULTIUSE = "multiuse";
  private static final String FIELD_TIME_LEFT = "time_left";
  private static final String FIELD_IDENTIFIED = "identified";
  private static final String FIELD_CONTENTS = "contents";

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

  public String getAppearance() {
    return appearance;
  }

  public void setAppearance(String appearance) {
    this.appearance = appearance;
  }

  public Optional<ItemTemplate> getBaseTemplate() {
    if (templates.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(templates.get(0));
  }

  public List<Item> getContents() {
    return Collections.unmodifiableList(this.contents);
  }

  public String getDMNotes() {
    return dmNotes;
  }

  public void setDMNotes(String notes) {
    dmNotes = notes;
  }

  public String getDescription() {
    return description(templates);
  }

  public int getHp() {
    return hp;
  }

  public void setHp(int hp) {
    this.hp = hp;
  }

  public String getId() {
    return id;
  }

  // TODO(merlin): Remove this once the single caller is gone.
  @Deprecated
  public void setId(String id) {
    this.id = id;
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

  public String getPlayerNotes() {
    return playerNotes;
  }

  public void setPlayerNotes(String notes) {
    playerNotes = notes;
  }

  public List<ItemTemplate> getTemplates() {
    return Collections.unmodifiableList(templates);
  }

  public void setTemplates(List<ItemTemplate> templates) {
    this.templates = new ArrayList<>(templates);
  }

  public Duration getTimeLeft() {
    return timeLeft;
  }

  public void setTimeLeft(Duration duration) {
    this.timeLeft = duration;
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

  public Weight getWeight() {
    Weight weight = weight(templates).multiply(multiple);
    for (Item content : contents) {
      weight = weight.add(content.getWeight());
    }

    return weight;
  }

  public boolean isContainer() {
    for (ItemTemplate template : templates) {
      if (template.isContainer()) {
        return true;
      }
    }

    return false;
  }

  public boolean isMonetary() {
    return templates.stream().filter(t -> t.isMonetary()).findAny().isPresent();
  }

  public void add(Item item) {
    contents.add(item);
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

  public String summary() {
    return getValue().toString() + " / " + getWeight().toString();
  }

  @Override
  public String toString() {
    return name + " (" + value + ")";
  }

  @Override
  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_ID, id);
    data.put(FIELD_NAME, name);
    data.put(FIELD_TEMPLATES,
        templates.stream().map(ItemTemplate::getName).collect(Collectors.toList()));
    data.put(FIELD_HP, hp);
    data.put(FIELD_VALUE, value.write());
    data.put(FIELD_APPEARANCE, appearance);
    data.put(FIELD_PLAYER_NAME, playerName);
    data.put(FIELD_PLAYER_NOTES, playerNotes);
    data.put(FIELD_DM_NOTES, dmNotes);
    data.put(FIELD_MULTIPLE, multiple);
    data.put(FIELD_MULTIUSE, multiuse);
    data.put(FIELD_TIME_LEFT, timeLeft.write());
    data.put(FIELD_IDENTIFIED, identified);
    data.put(FIELD_CONTENTS, contents.stream().map(Item::write).collect(Collectors.toList()));

    return data;
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

  public static String appearance(List<ItemTemplate> templates) {
    return Strings.SPACE_JOINER.join(templates.stream()
        .map(ItemTemplate::computeAppearance)
        .collect(Collectors.toList()));
  }

  public static Item create(CompanionContext context, String ... names) {
    List<ItemTemplate> templates =
        Arrays.asList(names).stream().map(Item::template).collect(Collectors.toList());
    String name = name(templates);
    return new Item(generateId(context), name, templates, hp(templates), value(templates),
        appearance(templates), names[0], "", "", 0, 0, Duration.ZERO, false,
        Collections.emptyList());
  }

  public static String description(List<ItemTemplate> templates) {
    return Strings.COMMA_JOINER.join(templates.stream()
        .map(ItemTemplate::getDescription)
        .collect(Collectors.toList()));
  }

  public static String generateId(CompanionContext context) {
    return context.me().getId() + "-" + new Date().getTime();
  }

  public static int hp(List<ItemTemplate> templates) {
    return templates.stream().mapToInt(ItemTemplate::computeHp).sum();
  }

  public static String name(List<ItemTemplate> templates) {
    return Strings.SPACE_JOINER.join(templates.stream()
        .map(ItemTemplate::getNamePart)
        .collect(Collectors.toList()));
  }

  public static Item read(Map<String, Object> data) {
    String id = Values.get(data, FIELD_ID, "(no id)");
    String name = Values.get(data, FIELD_NAME, "(no name)");
    List<ItemTemplate> templates = new ArrayList<>();
    for (String templateName : Values.get(data, FIELD_TEMPLATES, Collections.emptyList())) {
      Optional<ItemTemplate> template = Templates.get().getItemTemplates().get(templateName);
      if (template.isPresent()) {
        templates.add(template.get());
      } else {
        Status.error("Cannot find item template " + templateName);
      }
    }
    int hp = (int) Values.get(data, FIELD_HP, 0);
    Money value = Money.read(Values.get(data, FIELD_VALUE));
    String appearance = Values.get(data, FIELD_APPEARANCE, "");
    String playerName = Values.get(data, FIELD_PLAYER_NAME, "");
    String playerNotes = Values.get(data, FIELD_PLAYER_NOTES, "");
    String dmNotes = Values.get(data, FIELD_DM_NOTES, "");
    int multiple = (int) Values.get(data, FIELD_MULTIPLE, 0);
    int multiuse = (int) Values.get(data, FIELD_MULTIUSE, 0);
    Duration timeLeft = Duration.read(Values.get(data, FIELD_TIME_LEFT));
    boolean identified = Values.get(data, FIELD_IDENTIFIED, false);
    List<Item> contents = new ArrayList<>();
    for (Map<String, Object> item : Values.getRawList(data, FIELD_CONTENTS)) {
      contents.add(Item.read(item));
    }

    return new Item(id, name, templates, hp, value, appearance, playerName, playerNotes, dmNotes,
        multiple, multiuse, timeLeft, identified, contents);
  }

  private static ItemTemplate template(String name) {
    Optional<ItemTemplate> template = Templates.get().getItemTemplates().get(name);
    if (template.isPresent()) {
      return template.get();
    }

    return ItemTemplate.createEmpty(name);
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
}
