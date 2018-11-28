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

import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.data.statics.MonsterTemplate;
import net.ixitxachitls.companion.data.values.ConditionData;
import net.ixitxachitls.companion.data.values.Encounter;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.data.values.TargetedTimedCondition;
import net.ixitxachitls.companion.data.values.TimedCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  private static final long DEFAULT_ATTRIBUTE = 0;
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

  private String campaignId = "";
  private String name;
  private Optional<MonsterTemplate> race = Optional.empty();
  private Gender gender = Gender.UNKNOWN;
  private int strength;
  private int constitution;
  protected int dexterity;
  private int intelligence;
  private int wisdom;
  private int charisma;
  private int hp;
  private int maxHp;
  private int nonlethalDamage;
  private List<Item> items = new ArrayList<>();
  private int initiative = 0;
  private int encounterNumber = 0;
  private List<TargetedTimedCondition> initiatedConditions = new ArrayList<>();
  private List<TimedCondition> affectedConditions = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCampaignId() {
    return campaignId;
  }

  public Optional<Campaign> getCampaign() {
    return context.campaigns().get(campaignId);
  }

  public void setCampaignId(String campaignId) {
    this.campaignId = campaignId;
  }

  public int getInitiative() {
    return initiative;
  }

  public int getHp() {
    return hp;
  }

  public void setHp(int hp) {
    this.hp = hp;
  }

  public void addHp(int number) {
    hp += number;
  }

  public int getMaxHp() {
    return maxHp;
  }

  public void setMaxHp(int maxHp) {
    this.maxHp = maxHp;
  }

  public int getNonlethalDamage() {
    return nonlethalDamage;
  }

  public void addNonlethalDamage(int number) {
    nonlethalDamage += number;
  }

  public void setNonlethalDamage(int nonlethalDamage) {
    this.nonlethalDamage = nonlethalDamage;
  }

  public int getStrength() {
    return strength;
  }

  public void setStrength(int strength) {
    this.strength = strength;
  }

  public int getDexterity() {
    return dexterity;
  }

  public void setDexterity(int dexterity) {
    this.dexterity = dexterity;
  }

  public int getConstitution() {
    return constitution;
  }

  public int getConstitutionModifier() {
    return Ability.modifier(constitution);
  }

  public void setConstitution(int constitution) {
    this.constitution = constitution;
  }

  public int getIntelligence() {
    return intelligence;
  }

  public void setIntelligence(int intelligence) {
    this.intelligence = intelligence;
  }

  public int getWisdom() {
    return wisdom;
  }

  public void setWisdom(int wisdom) {
    this.wisdom = wisdom;
  }

  public int getCharisma() {
    return charisma;
  }

  public void setCharisma(int charisma) {
    this.charisma = charisma;
  }

  public Gender getGender() {
    return gender;
  }

  public void setInitiative(int encounterNumber, int initiative) {
    this.encounterNumber = encounterNumber;
    this.initiative = initiative;

    store();
  }

  public boolean hasInitiative(int encounterNumber) {
    return this.encounterNumber == encounterNumber;
  }

  public Optional<Integer> getInitiative(int encounterNumber) {
    if (this.encounterNumber == encounterNumber) {
      return Optional.of(initiative);
    }

    return Optional.empty();
  }

  public int getEncounterNumber() {
    return encounterNumber;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public Optional<MonsterTemplate> getRace() {
    return race;
  }

  public void setRace(String race) {
    this.race = Entries.get().getMonsterTemplates().get(race);
  }

  public void setRace(MonsterTemplate race) {
    this.race = Optional.of(race);
  }

  public boolean amPlayer() {
    return false;
  }

  public boolean amDM() {
    Optional<Campaign> campaign = context.campaigns().get(campaignId);
    return campaign.isPresent() && campaign.get().amDM();
  }

  public boolean canEdit() {
    return false;
  }

  public List<TargetedTimedCondition> getInitiatedConditions() {
    return initiatedConditions;
  }

  public void addInitiatedCondition(TargetedTimedCondition condition) {
    /*
    if (!condition.getTimedCondition().getSourceId().equals(getTargetId())) {
      throw new IllegalArgumentException("source id does not match creature id!");
    }

    if (isLocal()) {
      initiatedConditions.add(condition);
      store();

      // Send the condition to all affected characters/creatures.
      for (String id : condition.getTargetIds()) {
        Optional<? extends BaseCreature> creature = context.creatures().getCreatureOrCharacter(id);
        if (creature.isPresent()) {
          creature.get().addAffectedCondition(condition.getTimedCondition());
        } else {
          Status.error("Cannot affect " + id + " with condition " + condition);
        }
      }

    } else {
      Status.error("Cannot add initiated condition to remote character or creature.");
    }
     */
  }

  public void removeInitiatedCondition(String name) {
    /*
    for (Iterator<TargetedTimedCondition> i = initiatedConditions.iterator(); i.hasNext(); ) {
      TargetedTimedCondition condition = i.next();
      if (condition.getName().equals(name)) {
        i.remove();
        store();

        for (String targetId : condition.getTargetIds()) {
          Optional<? extends BaseCreature> creature =
              context.creatures().getCreatureOrCharacter(targetId);
          if (creature.isPresent()) {
            creature.get().removeAffectedCondition(name, getTargetId());
          } else {
            Status.error("Cannot get creature to remove condition " + name + " from: "
                + Status.nameFor(targetId));
          }
        }

        break;
      }
    }
     */
  }

  public List<TimedCondition> getAffectedConditions() {
    return affectedConditions;
  }

  public void addCondition(TimedCondition condition) {
    CreatureCondition.create(context, getId(), condition).store();
  }

  public void addAffectedCondition(TimedCondition condition) {
    affectedConditions.add(condition);
  }

  public void removeAffectedCondition(String name, String sourceId) {
    /*
    if (isLocal()) {
      for (Iterator<TimedCondition> i = affectedConditions.iterator(); i.hasNext(); ) {
        TimedCondition condition = i.next();
        if (condition.getName().equals(name) && condition.getSourceId().equals(sourceId)) {
          i.remove();
          store();
          break;
        }
      }
    } else {
      context.messenger().sendDeletion(name, sourceId, this);
    }
    */
  }

  public List<ConditionData> getActiveConditions(Encounter encounter) {
    return affectedConditions.stream()
        .filter(c -> c.active(encounter))
        .map(TimedCondition::getCondition)
        .collect(Collectors.toList());
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

  public List<Item> getItems() {
    return Collections.unmodifiableList(items);
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

  public int itemIndex(String id) {
    for (int i = 0; i < items.size(); i++) {
      if (items.get(i).getId().equals(id)) {
        return i;
      }
    }

    return -1;
  }

  public boolean hasItem(String id) {
    for (Item item : items) {
      if (item.getId().equals(id)) {
        return true;
      }
    }

    return false;
  }

  public boolean removeItem(Item item) {
    if (amPlayer()) {
      if (!items.remove(item)) {
        return removeItem(item.getId());
      }
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

  public void updated(Item item) {
    store();
  }

  public boolean moveItemBefore(Item item, Item move) {
    if (removeItem(move)) {
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

  public void combine(Item item, Item other) {
    removeItem(other);
    if (item.similar(other)) {
      item.setMultiple(item.getMultiple() + other.getMultiple());
      store();
    }
  }

  public boolean moveItemAfter(Item item, Item move) {
    if (removeItem(move)) {
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

  public void moveItemFirst(Item item) {
    if (removeItem(item)) {
      items.add(0, item);
      store();
    }
  }

  public void moveItemLast(Item item) {
    if (removeItem(item)) {
      items.add(item);
      store();
    }
  }

  public void moveItemInto(Item container, Item item) {
    if (removeItem(item)) {
      container.add(item);
      store();
    }
  }

  public boolean hasCondition(String name) {
    return context.conditions().hasCondition(getId(), name);
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();
    name = get(FIELD_NAME, "");
    gender = get(FIELD_GENDER, Gender.UNKNOWN);
    race = Entries.get().getMonsterTemplates().get(get(FIELD_RACE, DEFAULT_RACE));
    campaignId = get(FIELD_CAMPAIGN, DEFAULT_CAMPAIGN);
    strength = (int) get(FIELD_STRENGTH, DEFAULT_ATTRIBUTE);
    dexterity= (int) get(FIELD_DEXTERITY, DEFAULT_ATTRIBUTE);
    constitution = (int) get(FIELD_CONSTITUTION, DEFAULT_ATTRIBUTE);
    intelligence = (int) get(FIELD_INTELLIGENCE, DEFAULT_ATTRIBUTE);
    wisdom = (int) get(FIELD_WISDOM, DEFAULT_ATTRIBUTE);
    charisma = (int) get(FIELD_CHARISMA, DEFAULT_ATTRIBUTE);
    hp = (int) get(FIELD_HP, 0);
    maxHp = (int) get(FIELD_MAX_HP, 0);
    nonlethalDamage = (int) get(FIELD_NONLETHAL, 0);
    initiative = (int) get(FIELD_INITIATIVE, 0);
    encounterNumber = (int) get(FIELD_ENCOUNTER_NUMBER, 0);
    items = new ArrayList<>();
    for (Map<String, Object> item
        : get(FIELD_ITEMS, Collections.<Map<String, Object>>emptyList())) {
      items.add(Item.read(item));
    }
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
    data.put(FIELD_INITIATIVE, initiative);
    data.put(FIELD_ENCOUNTER_NUMBER, encounterNumber);
    data.put(FIELD_ITEMS, items.stream().map(Item::write).collect(Collectors.toList()));

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
      int compare = Integer.compare(second.initiative, first.initiative);
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
}
