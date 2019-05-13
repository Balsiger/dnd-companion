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

package net.ixitxachitls.companion.data.documents;

import android.support.annotation.CallSuper;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.data.templates.MonsterTemplate;
import net.ixitxachitls.companion.data.values.Adjustment;
import net.ixitxachitls.companion.data.values.InitiativeAdjustment;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.data.values.ModifiedValue;
import net.ixitxachitls.companion.data.values.Modifier;
import net.ixitxachitls.companion.data.values.SavesAdjustment;
import net.ixitxachitls.companion.data.values.SpeedAdjustment;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.rules.Conditions;
import net.ixitxachitls.companion.rules.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The base for all monsters or characters in the game.
 */
public class Creature<T extends Creature<T>> extends Document<T> {
  private static final String FIELD_CAMPAIGN = "campaign";
  private static final String DEFAULT_CAMPAIGN = "";
  private static final String FIELD_NAME = "name";
  private static final String FIELD_GENDER = "gender";
  private static final String FIELD_RACE = "race";
  private static final String DEFAULT_RACE = "Human";
  private static final int DEFAULT_ATTRIBUTE = 0;
  private static final String FIELD_STRENGTH = "strength";
  private static final String FIELD_DEXTERITY = "dexterity";
  private static final String FIELD_CONSTITUTION = "constitution";
  private static final String FIELD_INTELLIGENCE = "intelligence";
  private static final String FIELD_WISDOM = "wisdom";
  private static final String FIELD_CHARISMA = "charisma";
  private static final String FIELD_HP = "hp";
  private static final String FIELD_MAX_HP = "max_hp";
  private static final String FIELD_NONLETHAL = "nonlethal";
  private static final String FIELD_INITIATIVE = "initiative";
  private static final String FIELD_ENCOUNTER_NUMBER = "encounter_number";
  private static final String FIELD_ITEMS = "items";
  private static final String FIELD_SLOTS = "item_slots";
  private static final String FIELD_ITEMS_PER_SLOT = "items_per_slot";

  private String campaignId = "";
  private String name;
  private Optional<MonsterTemplate> race = Optional.empty();
  private Gender gender = Gender.UNKNOWN;
  private int strength;
  private int dexterity;
  private int constitution;
  private int intelligence;
  private int wisdom;
  private int charisma;
  private int hp;
  private int maxHp;
  private int nonlethalDamage;
  private List<Item> items = new ArrayList<>();
  private Carrying carrying = new Carrying("default");
  private int encounterInitiative = 0;
  private int encounterNumber = 0;

  public List<CreatureCondition> getAdjustedConditions() {
    List<CreatureCondition> conditions = new ArrayList<>(getConditions());

    // Monsters don't yet have ability scores...
    if (this instanceof Monster) {
      return conditions;
    }

    if (getStrength().total() <= 0) {
      conditions.add(
          CreatureCondition.create(CompanionApplication.get().context(), getId(),
              new TimedCondition(Conditions.HELPLESS_STRENGTH_0, getCampaignId())));
    }
    if (getDexterity().total() <= 0) {
      conditions.add(
          CreatureCondition.create(CompanionApplication.get().context(), getId(),
              new TimedCondition(Conditions.PARALYZED_DEXTERITY_0, getCampaignId())));
    }
    if (getConstitution().total() <= 0) {
      conditions.add(
          CreatureCondition.create(CompanionApplication.get().context(), getId(),
              new TimedCondition(Conditions.DEAD_CONSTITUTION_0, getCampaignId())));
    }
    if (getIntelligence().total() <= 0) {
      conditions.add(
          CreatureCondition.create(CompanionApplication.get().context(), getId(),
              new TimedCondition(Conditions.UNCONSCIOUS_INTELLIGENCE_0, getCampaignId())));
    }
    if (getWisdom().total() <= 0) {
      conditions.add(
          CreatureCondition.create(CompanionApplication.get().context(), getId(),
              new TimedCondition(Conditions.UNCONSCIOUS_WISDOM_0, getCampaignId())));
    }
    if (getCharisma().total() <= 0) {
      conditions.add(
          CreatureCondition.create(CompanionApplication.get().context(), getId(),
              new TimedCondition(Conditions.UNCONSCIOUS_CHARISMA_0, getCampaignId())));
    }

    return conditions;
  }

  public Optional<Campaign> getCampaign() {
    return context.campaigns().get(campaignId);
  }

  public String getCampaignId() {
    return campaignId;
  }

  public void setCampaignId(String campaignId) {
    this.campaignId = campaignId;
  }

  public ModifiedValue getCharisma() {
    return new ModifiedValue("Charisma", charisma, 0, false);
  }

  public ModifiedValue getCharismaCheck() {
    return new ModifiedValue("Charisma Check", Ability.modifier(getCharisma().total()), true);
  }

  public List<CreatureCondition> getConditions() {
    return CompanionApplication.get().conditions().getCreatureConditions(getId());
  }

  public ModifiedValue getConstitution() {
    return new ModifiedValue("Constitution", constitution, 0, false);
  }

  public ModifiedValue getConstitutionCheck() {
    return new ModifiedValue("Constitution Check", Ability.modifier(getConstitution().total()),
        true);
  }

  public int getConstitutionModifier() {
    return Ability.modifier(getConstitution().total());
  }

  public ModifiedValue getDexterity() {
    return new ModifiedValue("Dexterity", dexterity, 0, false);
  }

  public ModifiedValue getDexterityCheck() {
    return new ModifiedValue("Dexterity Check", Ability.modifier(getDexterity().total()), true);
  }

  public int getDexterityModifier() {
    return Ability.modifier(getDexterity().total());
  }

  public int getEncounterInitiative() {
    return encounterInitiative;
  }

  public int getEncounterNumber() {
    return encounterNumber;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public int getHp() {
    return hp;
  }

  public void setHp(int hp) {
    this.hp = hp;
  }

  public ModifiedValue getIntelligence() {
    return new ModifiedValue("Intelligence", intelligence, 0, false);
  }

  public ModifiedValue getIntelligenceCheck() {
    return
        new ModifiedValue("Intelligence Check", Ability.modifier(getIntelligence().total()), true);
  }

  public int getIntelligenceModifier() {
    return Ability.modifier(getIntelligence().total());
  }

  public List<Item> getItems() {
    return Collections.unmodifiableList(items);
  }

  public int getMaxHp() {
    return maxHp;
  }

  public void setMaxHp(int maxHp) {
    this.maxHp = maxHp;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getNonlethalDamage() {
    return nonlethalDamage;
  }

  public void setNonlethalDamage(int nonlethalDamage) {
    this.nonlethalDamage = nonlethalDamage;
  }

  public Optional<MonsterTemplate> getRace() {
    return race;
  }

  public void setRace(MonsterTemplate race) {
    this.race = Optional.of(race);
  }

  public void setRace(String race) {
    this.race = Templates.get().getMonsterTemplates().get(race);
  }

  public ModifiedValue getStrength() {
    return new ModifiedValue("Strength", strength, 0, false);
  }

  public ModifiedValue getStrengthCheck() {
    return new ModifiedValue("Strength Check", Ability.modifier(getStrength().total()), true);
  }

  public ModifiedValue getWisdom() {
    return new ModifiedValue("Wisdom", wisdom, 0, false);
  }

  public ModifiedValue getWisdomCheck() {
    return new ModifiedValue("Wisdom Check", Ability.modifier(getWisdom().total()), true);
  }

  public int getWisdomModifier() {
    return Ability.modifier(getWisdom().total());
  }

  public void setBaseCharisma(int charisma) {
    this.charisma = charisma;
  }

  public void setBaseConstitution(int constitution) {
    this.constitution = constitution;
  }

  public void setBaseDexterity(int dexterity) {
    this.dexterity = dexterity;
  }

  public void setBaseIntelligence(int intelligence) {
    this.intelligence = intelligence;
  }

  public void setBaseStrength(int strength) {
    this.strength = strength;
  }

  public void setBaseWisdom(int wisdom) {
    this.wisdom = wisdom;
  }

  public boolean isCarrying(Item item) {
    return carrying.isCarrying(item);
  }

  public void add(Item item) {
    if (amPlayer()) {
      int index = itemIndex(item.getId());
      if (index < 0) {
        items.add(item);
      } else {
        items.set(index, item);
      }
      store();
    }
  }

  public void addCondition(TimedCondition condition) {
    CreatureCondition.create(context, getId(), condition).store();
  }

  public void addHp(int number) {
    hp += number;
  }

  public void addNonlethalDamage(int number) {
    nonlethalDamage += number;
  }

  public boolean amDM() {
    Optional<Campaign> campaign = context.campaigns().get(campaignId);
    return campaign.isPresent() && campaign.get().amDM();
  }

  public boolean amPlayer() {
    return false;
  }

  public boolean canEdit() {
    return false;
  }

  public void carry(Items.Slot slot, Item item) {
    carrying.carry(slot, item);
  }

  public List<String> carrying(Items.Slot slot) {
    return carrying.carrying(slot);
  }

  public Set<Feat> collectFeats() {
    if (race.isPresent()) {
      return new HashSet<>(race.get().getFeats());
    }

    return Collections.emptySet();
  }

  public Set<Quality> collectQualities() {
    if (race.isPresent()) {
      return new HashSet<>(race.get().getQualities());
    }

    return Collections.emptySet();
  }

  public void combine(Item item, Item other) {
    removeItem(other);
    if (item.similar(other)) {
      item.setMultiple(item.getMultiple() + other.getMultiple());
      store();
    }
  }

  public ModifiedValue fortitude() {
    ModifiedValue value = new ModifiedValue("Fortitude Save", 0, true);

    // Modifiers from race.
    if (!hasLevels() && race.isPresent()) {
      value.add(new Modifier(race.get().getFortitudeSave(), Modifier.Type.GENERAL,
          race.get().getName()));
    }

    // Modifiers from abilities.
    value.add(new Modifier(getConstitutionModifier(), Modifier.Type.GENERAL, "Constitution"));

    // Modifiers from feats.
    for (Feat feat : collectFeats()) {
      value.add(feat.getTemplate().getFortitudeModifiers());
    }

    // Modifiers from conditions.
    for (CreatureCondition condition : getConditions()) {
      for (Adjustment adjustment : condition.getCondition().getCondition().getAdjustments()) {
        if (adjustment instanceof SavesAdjustment) {
          value.add(new Modifier(((SavesAdjustment) adjustment).getSaves(), Modifier.Type.GENERAL,
              condition.getCondition().getName()));
        }
      }
    }

    return value;
  }

  public Optional<Integer> getEncounterInitiative(int encounterNumber) {
    if (this.encounterNumber == encounterNumber) {
      return Optional.of(encounterInitiative);
    }

    return Optional.empty();
  }

  public Optional<Item> getItem(String itemId) {
    for (Item item : items) {
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

  public boolean hasCondition(String name) {
    return context.conditions().hasCondition(getId(), name);
  }

  public boolean hasInitiative(int encounterNumber) {
    return this.encounterNumber == encounterNumber;
  }

  public boolean hasItem(String id) {
    for (Item item : items) {
      if (item.getId().equals(id)) {
        return true;
      }
    }

    return false;
  }

  public ModifiedValue initiative() {
    ModifiedValue value = new ModifiedValue("Initiative", 0, true);
    value.add(new Modifier(Ability.modifier(getDexterity().total()), Modifier.Type.ABILITY,
        "Dexterity"));

    for (CreatureCondition condition : getConditions()) {
      for (Adjustment adjustment : condition.getCondition().getCondition().getAdjustments()) {
        if (adjustment instanceof InitiativeAdjustment) {
          value.add(new Modifier(((InitiativeAdjustment) adjustment).getAdjustment(),
              Modifier.Type.GENERAL, condition.getCondition().getCondition().getName()));
        }
      }
    }

    return value;
  }

  public int itemIndex(String id) {
    for (int i = 0; i < items.size(); i++) {
      if (items.get(i).getId().equals(id)) {
        return i;
      }
    }

    return -1;
  }

  public boolean moveItemAfter(Item item, Item move) {
    if (isCarrying(item)) {
      carrying.moveItemAfter(item, move);
    } else if (removeItem(move)) {
      if (items.contains(item)) {
        items.add(items.indexOf(item) + 1, move);
        store();
        return true;
      } else {
        for (Item container : items) {
          if (container.addItemAfter(item, move)) {
            store();
            return true;
          }
        }
      }
    }

    return false;
  }

  public boolean moveItemBefore(Item item, Item move) {
    if (isCarrying(item)) {
      carrying.moveItemBefore(item, move);
    } else if (removeItem(move)) {
      if (items.contains(item)) {
        items.add(items.indexOf(item), move);
        store();
        return true;
      } else {
        for (Item container : items) {
          if (container.addItemBefore(item, move)) {
            store();
            return true;
          }
        }
      }
    }

    return false;
  }

  public void moveItemFirst(Item item) {
    if (removeItem(item)) {
      items.add(0, item);
      store();
    }
  }

  public void moveItemInto(Item container, Item item) {
    if (removeItem(item)) {
      container.add(item);
      store();
    }
  }

  public void moveItemLast(Item item) {
    if (removeItem(item)) {
      items.add(item);
      store();
    }
  }

  public Carrying readCarrying(Data data) {
    Map<String, List<String>> raw = data.getMap(FIELD_ITEMS_PER_SLOT, Collections.emptyList());
    EnumMap<Items.Slot, List<String>> carrying = new EnumMap<>(Items.Slot.class);
    for (Map.Entry<String, List<String>> entry : raw.entrySet()) {
      carrying.put(Items.Slot.valueOf(entry.getKey()), entry.getValue());
    }

    return new Carrying(
        data.get(FIELD_NAME, "default"),
        carrying);
  }

  public ModifiedValue reflex() {
    ModifiedValue value = new ModifiedValue("Reflex Save", 0, true);

    // Modifiers from race.
    if (!hasLevels() && race.isPresent()) {
      value.add(new Modifier(race.get().getReflexSave(), Modifier.Type.GENERAL,
          race.get().getName()));
    }

    // Modifiers from abilities.
    value.add(new Modifier(getDexterityModifier(), Modifier.Type.GENERAL, "Dexterity"));

    // Modifiers from feats.
    for (Feat feat : collectFeats()) {
      value.add(feat.getTemplate().getReflexModifiers());
    }

    // Modifiers from conditions.
    for (CreatureCondition condition : getConditions()) {
      for (Adjustment adjustment : condition.getCondition().getCondition().getAdjustments()) {
        if (adjustment instanceof SavesAdjustment) {
          value.add(new Modifier(((SavesAdjustment) adjustment).getSaves(), Modifier.Type.GENERAL,
              condition.getCondition().getName()));
        }
      }
    }

    return value;
  }

  public boolean removeItem(Item item) {
    if (amPlayer()) {
      if (!items.remove(item)) {
        return removeItem(item.getId());
      }

      carrying.remove(item);
      store();
      return true;
    }

    return false;
  }

  public boolean removeItem(String itemId) {
    if (amPlayer()) {
      for (Iterator<Item> i = items.iterator(); i.hasNext(); ) {
        if (i.next().getId().equals(itemId)) {
          i.remove();
          return true;
        }
      }
    }

    return false;
  }

  public void setEncounterInitiative(int encounterNumber, int initiative) {
    this.encounterNumber = encounterNumber;
    this.encounterInitiative = initiative;

    store();
  }

  public ModifiedValue speed() {
    int squares = 0;
    if (race.isPresent()) {
      squares = race.get().getWalkingSpeed();
    }

    ModifiedValue value = new ModifiedValue("Speed (squares)", squares, 0, false);

    for (CreatureCondition condition : getConditions()) {
      for (Adjustment adjustment : condition.getCondition().getCondition().getAdjustments()) {
        if (adjustment instanceof SpeedAdjustment) {
          if (((SpeedAdjustment) adjustment).isHalf()) {
            value.add(new Modifier(-squares / 2, Modifier.Type.GENERAL,
                condition.getCondition().getName()));
          }
        }
      }
    }

    return value;
  }

  public void updated(Item item) {
    store();
  }

  public ModifiedValue will() {
    ModifiedValue value = new ModifiedValue("Will Save", 0, true);

    // Modifiers from race.
    if (!hasLevels() && race.isPresent()) {
      value.add(new Modifier(race.get().getWillSave(), Modifier.Type.GENERAL,
          race.get().getName()));
    }

    // Modifiers from abilities.
    value.add(new Modifier(getWisdomModifier(), Modifier.Type.GENERAL, "Wisdom"));

    // Modifiers from feats.
    for (Feat feat : collectFeats()) {
      value.add(feat.getTemplate().getWillModifiers());
    }

    // Modifiers from conditions.
    for (CreatureCondition condition : getConditions()) {
      for (Adjustment adjustment : condition.getCondition().getCondition().getAdjustments()) {
        if (adjustment instanceof SavesAdjustment) {
          value.add(new Modifier(((SavesAdjustment) adjustment).getSaves(), Modifier.Type.GENERAL,
              condition.getCondition().getName()));
        }
      }
    }

    // Modifiers from qualities.
    if (race.isPresent()) {
      for (Quality quality : race.get().getQualities()) {
        value.add(quality.getTemplate().getWillModifiers());
      }
    }

    return value;
  }

  protected boolean hasLevels() {
    return false;
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();
    name = data.get(FIELD_NAME, "");
    gender = data.get(FIELD_GENDER, Gender.UNKNOWN);
    race = Templates.get().getMonsterTemplates().get(data.get(FIELD_RACE, DEFAULT_RACE));
    campaignId = data.get(FIELD_CAMPAIGN, DEFAULT_CAMPAIGN);
    strength = data.get(FIELD_STRENGTH, DEFAULT_ATTRIBUTE);
    dexterity = data.get(FIELD_DEXTERITY, DEFAULT_ATTRIBUTE);
    constitution = data.get(FIELD_CONSTITUTION, DEFAULT_ATTRIBUTE);
    intelligence = data.get(FIELD_INTELLIGENCE, DEFAULT_ATTRIBUTE);
    wisdom = data.get(FIELD_WISDOM, DEFAULT_ATTRIBUTE);
    charisma = data.get(FIELD_CHARISMA, DEFAULT_ATTRIBUTE);
    hp = data.get(FIELD_HP, 0);
    maxHp = data.get(FIELD_MAX_HP, 0);
    nonlethalDamage = data.get(FIELD_NONLETHAL, 0);
    encounterInitiative = data.get(FIELD_INITIATIVE, 0);
    encounterNumber = data.get(FIELD_ENCOUNTER_NUMBER, 0);
    items = data.getNestedList(FIELD_ITEMS).stream()
        .map(Item::read)
        .collect(Collectors.toList());
    carrying = readCarrying(data.getNested(FIELD_SLOTS));
  }

  @Override
  @CallSuper
  protected Map<String, Object> write(Map<String, Object> data) {
    data.put(FIELD_NAME, name);
    data.put(FIELD_CAMPAIGN, campaignId);
    data.put(FIELD_GENDER, gender.toString());
    if (race.isPresent()) {
      data.put(FIELD_RACE, race.get().getName());
    }
    data.put(FIELD_STRENGTH, strength);
    data.put(FIELD_DEXTERITY, dexterity);
    data.put(FIELD_CONSTITUTION, constitution);
    data.put(FIELD_INTELLIGENCE, intelligence);
    data.put(FIELD_WISDOM, wisdom);
    data.put(FIELD_CHARISMA, charisma);
    data.put(FIELD_HP, hp);
    data.put(FIELD_MAX_HP, maxHp);
    data.put(FIELD_NONLETHAL, nonlethalDamage);
    data.put(FIELD_INITIATIVE, encounterInitiative);
    data.put(FIELD_ENCOUNTER_NUMBER, encounterNumber);
    data.put(FIELD_ITEMS, items.stream().map(Item::write).collect(Collectors.toList()));
    data.put(FIELD_SLOTS, carrying.write());

    return data;
  }

  public static class InitiativeComparator implements Comparator<Creature> {
    private final int encounterNumber;

    public InitiativeComparator(int encounterNumber) {
      this.encounterNumber = encounterNumber;
    }

    @Override
    public int compare(Creature first, Creature second) {
      if (first.encounterNumber == encounterNumber && second.encounterNumber == encounterNumber) {
        return compareInit(first, second);
      }

      if (first.encounterNumber == encounterNumber && second.encounterNumber != encounterNumber) {
        return -1;
      }

      if (first.encounterNumber != encounterNumber && second.encounterNumber == encounterNumber) {
        return +2;
      }

      return compareInit(first, second);
    }

    private int compareInit(Creature first, Creature second) {
      int compare = Integer.compare(second.encounterInitiative, first.encounterInitiative);
      if (compare != 0) {
        return compare;
      }

      compare = Integer.compare(second.dexterity, first.dexterity);
      if (compare != 0) {
        return compare;
      }

      return first.getId().compareTo(second.getId());
    }
  }

  public class Carrying {
    private static final String FIELD_NAME = "name";

    private final String name;
    private final EnumMap<Items.Slot, List<String>> itemsPerSlot;

    public Carrying(String name) {
      this.name = name;

      this.itemsPerSlot = new EnumMap<Items.Slot, List<String>>(Items.Slot.class);
      for (Items.Slot slot : Items.Slot.values()) {
        itemsPerSlot.put(slot, new ArrayList<>());
      }
    }

    private Carrying(String name, EnumMap<Items.Slot, List<String>> itemsPerSlot) {
      this.name = name;
      this.itemsPerSlot = itemsPerSlot;

      for (Items.Slot slot : Items.Slot.values()) {
        if (!itemsPerSlot.containsKey(slot)) {
          itemsPerSlot.put(slot, new ArrayList<>());
        }
      }
    }

    public boolean isCarrying(Item item) {
      for (List<String> ids : itemsPerSlot.values()) {
        if (ids.contains(item.getId())) {
          return true;
        }
      }

      return false;
    }

    public void carry(Items.Slot slot, Item item) {
      remove(item);
      add(slot, item);

      Creature.this.store();
    }

    public List<String> carrying(Items.Slot slot) {
      return itemsPerSlot.get(slot);
    }

    public Optional<Items.Slot> getSlot(Item item) {
      for (Map.Entry<Items.Slot, List<String>> slot : itemsPerSlot.entrySet()) {
        if (slot.getValue().contains(item.getId())) {
          return Optional.of(slot.getKey());
        }
      }

      return Optional.empty();
    }

    public void moveItemAfter(Item item, Item move) {
      remove(move);

      Optional<Items.Slot> slot = getSlot(item);
      if (slot.isPresent()) {
        itemsPerSlot.get(slot.get())
            .add(itemsPerSlot.get(slot.get()).indexOf(item.getId()) + 1, move.getId());
        store();
      }
    }

    public void moveItemBefore(Item item, Item move) {
      remove(move);

      Optional<Items.Slot> slot = getSlot(item);
      if (slot.isPresent()) {
        itemsPerSlot.get(slot.get())
            .add(itemsPerSlot.get(slot.get()).indexOf(item.getId()), move.getId());
        store();
      }
    }

    public Map<String, Object> write() {
      Map<String, Object> data = new HashMap<>();
      data.put(FIELD_NAME, name);
      data.put(FIELD_ITEMS_PER_SLOT, itemsPerSlot.entrySet().stream()
          .collect(Collectors.toMap((e) -> e.getKey().toString(), Map.Entry::getValue)));

      return data;
    }

    private void add(Items.Slot slot, Item item) {
      itemsPerSlot.get(slot).add(item.getId());
    }

    private void remove(Item item) {
      for (List<String> ids : itemsPerSlot.values()) {
        ids.remove(item.getId());
      }
    }
  }
}
