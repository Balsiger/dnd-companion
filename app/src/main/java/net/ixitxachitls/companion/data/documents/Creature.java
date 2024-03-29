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

package net.ixitxachitls.companion.data.documents;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.enums.Alignment;
import net.ixitxachitls.companion.data.enums.AlignmentStatus;
import net.ixitxachitls.companion.data.enums.DamageType;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.data.enums.Size;
import net.ixitxachitls.companion.data.templates.MonsterTemplate;
import net.ixitxachitls.companion.data.values.Adjustment;
import net.ixitxachitls.companion.data.values.Damage;
import net.ixitxachitls.companion.data.values.InitiativeAdjustment;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.data.values.ModifiedValue;
import net.ixitxachitls.companion.data.values.Modifier;
import net.ixitxachitls.companion.data.values.Money;
import net.ixitxachitls.companion.data.values.SavesAdjustment;
import net.ixitxachitls.companion.data.values.Speed;
import net.ixitxachitls.companion.data.values.SpeedAdjustment;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.data.values.Weight;
import net.ixitxachitls.companion.rules.Armor;
import net.ixitxachitls.companion.rules.Conditions;
import net.ixitxachitls.companion.rules.Items;
import net.ixitxachitls.companion.util.Die;
import net.ixitxachitls.companion.util.Lazy;
import net.ixitxachitls.companion.util.Optionals;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import androidx.annotation.CallSuper;

/**
 * The base for all monsters or characters in the game.
 */
public class Creature<T extends Creature<T>> extends Document<T> implements Item.Owner {
  protected static final String FIELD_NAME = "name";
  private static final String FIELD_CAMPAIGN = "campaign";
  private static final String DEFAULT_CAMPAIGN = "";
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
  private static final String DEFAULT_DISTRIBUTION = "Default";

  protected Lazy.State state = new Lazy.State();

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
  private Wearing wearing = new Wearing(DEFAULT_DISTRIBUTION);
  private int encounterInitiative = 0;
  private int encounterNumber = 0;

  // Lazy values.
  private Lazy.Resettable<String> type = new Lazy.Resettable<>(state, () -> {
    if (race.isPresent()) {
      String type = Strings.toWords(race.get().getType().toString());

      if (!race.get().getSubtypes().isEmpty()) {
        type += " [" + race.get().getSubtypes().stream()
            .map(t -> Strings.toWords(t.toString())).collect(Collectors.joining(", ")) + "]";
      }

      return type;
    }

    return "Unknown";
  });
  private Lazy.Resettable<ModifiedValue> flatFootedArmorClass = new Lazy.Resettable<>(state, () -> {
    ModifiedValue value = new ModifiedValue("AC", 10, false);

    // Natural armor.
    if (race.isPresent() && race.get().hasNaturalArmor()) {
      value.add(race.get().getNaturalArmor());
    }

    // Size.
    if (race.isPresent() && Armor.sizeModifier(race.get().getSize()) != 0) {
      value.add(new Modifier(Armor.sizeModifier(race.get().getSize()), Modifier.Type.GENERAL,
          "Size"));
    }

    // Qualities.
    for (Collection<Quality> qualities : collectQualities().asMap().values()) {
      value.add(qualities.iterator().next().getTemplate().getAcModifiers());
    }

    // Items.
    for (Item item : items) {
      if (isWearing(item)) {
        value.add(item.getArmorModifiers());
      }
    }

    return value;
  });
  private Lazy.Resettable<List<CreatureCondition>> creatureConditions =
      new Lazy.Resettable<>(state, () -> {
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
      });
  private Lazy.Resettable<ModifiedValue> fortitude = new Lazy.Resettable<>(state, () -> {
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
  });
  private Lazy.Resettable<ModifiedValue> initiative = new Lazy.Resettable<>(state, () -> {
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
  });
  private Lazy.Resettable<Integer> maxDexterityModifierFromItems = new Lazy.Resettable(state, () ->
      items.stream()
          .filter(this::isWearing)
          .mapToInt(i -> i.getMaxDexterityModifier())
          .min()
          .orElse(Integer.MAX_VALUE));
  private Lazy.Resettable<ModifiedValue> normalArmorClass = new Lazy.Resettable<>(state, () -> {
    ModifiedValue value = new ModifiedValue("AC", 10, false);

    // Natural armor.
    if (race.isPresent() && race.get().hasNaturalArmor()) {
      value.add(race.get().getNaturalArmor());
    }

    // Dexterity.
    int dexterityModifier = getDexterityModifier();
    if (dexterityModifier != 0) {
      dexterityModifier = Math.min(dexterityModifier, getMaxDexerityModifierFromItems());
      value.add(new Modifier(dexterityModifier, Modifier.Type.ABILITY, "Dexterity"));
    }

    // Size.
    if (race.isPresent() && Armor.sizeModifier(race.get().getSize()) != 0) {
      value.add(new Modifier(Armor.sizeModifier(race.get().getSize()), Modifier.Type.GENERAL,
          "Size"));
    }

    // Qualities.
    for (Collection<Quality> qualities : collectQualities().asMap().values()) {
      value.add(qualities.iterator().next().getTemplate().getAcModifiers());
    }

    // Items.
    for (Item item : items) {
      if (isWearing(item)) {
        value.add(item.getArmorModifiers());
      }
    }

    return value;
  });
  private Lazy.Resettable<Money> totalValue = new Lazy.Resettable<>(state, () -> {
    Money total = Money.ZERO;
    for (Item item : items) {
      total = total.add(item.getValue());
    }

    return total;
  });
  private Lazy.Resettable<Weight> totalWeight = new Lazy.Resettable<>(state, () -> {
    Weight total = Weight.ZERO;
    for (Item item : items) {
      total = total.add(item.getWeight());
    }

    return total;
  });
  private Lazy.Resettable<Integer> maxDexterityFromEncumbrance =
      new Lazy.Resettable<>(state, () -> {
        switch (Items.encumbrance((int) getTotalWeight().asPounds(), getStrength().total())) {
          default:
          case light:
            return Integer.MAX_VALUE;

          case medium:
            return 3;

          case heavy:
            return 1;

          case overloaded:
            return 0;
        }
      });
  private Lazy.Resettable<Integer> dexterityModifier = new Lazy.Resettable<>(state, () -> {
    int normal = Ability.modifier(getDexterity().total());

    int maxDexterityFromItems = getMaxDexerityModifierFromItems();
    int maxDexterityFromEncumbrance = getMaxDexterityFromEncumbrance();

    return Math.min(normal, Math.min(maxDexterityFromItems, maxDexterityFromEncumbrance));
  });
  Lazy.Resettable<ModifiedValue> reflex = new Lazy.Resettable<>(state, () -> {
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
  });
  private Lazy.Resettable<Integer> maxSpeedSquares = new Lazy.Resettable<>(state, () -> {
    final boolean fast =
        !race.isPresent() || race.get().getWalkingSpeed() >= 6;

    int encumbranceSquares = Integer.MAX_VALUE;
    switch (Items.encumbrance((int) getTotalWeight().asPounds(), getStrength().total())) {
      case light:
        encumbranceSquares = Integer.MAX_VALUE;
        break;

      case medium:
      case heavy:
        encumbranceSquares = fast ? 4 : 3;
        break;

      case overloaded:
        return 0;
    }

    return Math.min(encumbranceSquares, items.stream()
        .filter(this::isWearing)
        .mapToInt(i -> i.getMaxSpeedSquares(fast))
        .min()
        .orElse(Integer.MAX_VALUE));
  });
  private Lazy.Resettable<ModifiedValue> speed = new Lazy.Resettable<>(state, () -> {
    int squares = 0;
    if (race.isPresent()) {
      squares = race.get().getWalkingSpeed();
    }

    squares = Math.min(squares, getMaxSpeedSquares());

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

    for (Quality quality : collectQualities().values()) {
      int qualitySquares = quality.getSpeedSquares(Speed.Mode.run);
      if (qualitySquares != 0) {
        value.add(new Modifier(qualitySquares, Modifier.Type.GENERAL, quality.getName()));
      }
    }

    return value;
  });
  private Lazy.Resettable<ModifiedValue> touchArmorClass = new Lazy.Resettable<>(state, () -> {
    ModifiedValue value = new ModifiedValue("AC", 10, false);

    // Dexterity.
    int dexterityModifier = getDexterityModifier();
    if (dexterityModifier != 0) {
      dexterityModifier = Math.min(dexterityModifier, getMaxDexerityModifierFromItems());
      value.add(new Modifier(dexterityModifier, Modifier.Type.ABILITY, "Dexterity"));
    }

    // Size.
    if (race.isPresent() && Armor.sizeModifier(race.get().getSize()) != 0) {
      value.add(new Modifier(Armor.sizeModifier(race.get().getSize()), Modifier.Type.GENERAL,
          "Size"));
    }

    // Qualities.
    for (Collection<Quality> qualities : collectQualities().asMap().values()) {
      value.add(qualities.iterator().next().getTemplate().getAcModifiers().stream()
          .filter(m -> m.getType() == Modifier.Type.DEFLECTION)
          .collect(Collectors.toList()));
    }

    // Items.
    for (Item item : items) {
      if (isWearing(item)) {
        value.add(item.getArmorDeflectionModifiers());
      }
    }

    return value;
  });
  private Lazy.Resettable<ModifiedValue> will = new Lazy.Resettable<>(state, () -> {
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
  });


  public List<CreatureCondition> getAdjustedConditions() {
    return creatureConditions.get();
  }

  public Alignment getAlignment() {
    if (race.isPresent()) {
      return race.get().getAlignment();
    }

    return Alignment.UNKNOWN;
  }

  public AlignmentStatus getAlignmentStatus() {
    if (race.isPresent()) {
      return race.get().getAlignmentStatus();
    }

    return AlignmentStatus.UNKNOWN;
  }

  public int getBaseAttackBonus() {
    if (race.isPresent()) {
      return race.get().getBaseAttack();
    }

    return 0;
  }

  public Campaign getCampaign() {
    if (context == null) {
      return Campaign.DEFAULT;
    }

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

  public int getCharismaModifier() {
    return Ability.modifier(getCharisma().total());
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

  public List<Item> getDeepItems() {
    List<Item> deep = new ArrayList<>(items);
    for (Item item : items) {
      deep.addAll(item.collectDeepContents());
    }

    return deep;
  }

  public ModifiedValue getDexterity() {
    return new ModifiedValue("Dexterity", dexterity, 0, false);
  }

  public ModifiedValue getDexterityCheck() {
    return new ModifiedValue("Dexterity Check", Ability.modifier(getDexterity().total()), true);
  }

  public int getDexterityModifier() {
    return dexterityModifier.get();
  }

  public int getEncounterInitiative() {
    return encounterInitiative;
  }

  public int getEncounterNumber() {
    return encounterNumber;
  }

  public ModifiedValue getFlatFootedArmorClass() {
    return flatFootedArmorClass.get();
  }

  public ModifiedValue getFortitude() {
    return fortitude.get();
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public int getHitDice() {
    if (race.isPresent()) {
      return race.get().getHitDice();
    }

    return 0;
  }

  public int getHitDie() {
    if (race.isPresent()) {
      return race.get().getHitDie();
    }

    return 0;
  }

  public int getHp() {
    return hp;
  }

  public void setHp(int hp) {
    this.hp = hp;
  }

  public ModifiedValue getInitiative() {
    return initiative.get();
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

  public int getMaxDexerityModifierFromItems() {
    return maxDexterityModifierFromItems.get();
  }

  public int getMaxDexterityFromEncumbrance() {
    return maxDexterityFromEncumbrance.get();
  }

  public int getMaxHp() {
    return maxHp;
  }

  public void setMaxHp(int maxHp) {
    this.maxHp = maxHp;
  }

  public int getMaxSpeedSquares() {
    return maxSpeedSquares.get();
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

  public ModifiedValue getNormalArmorClass() {
    return normalArmorClass.get();
  }

  public Item.Owner getParent() {
    return this;
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

  public ModifiedValue getReflex() {
    return reflex.get();
  }

  public Size getSize() {
    if (race.isPresent()) {
      return race.get().getSize();
    }

    return Size.UNKNOWN;
  }

  public ModifiedValue getSpeed() {
    return speed.get();
  }

  public ModifiedValue getStrength() {
    return new ModifiedValue("Strength", strength, 0, false);
  }

  public ModifiedValue getStrengthCheck() {
    return new ModifiedValue("Strength Check", Ability.modifier(getStrength().total()), true);
  }

  public int getStrengthModifier() {
    return Ability.modifier(getStrength().total());
  }

  public Money getTotalValue() {
    return totalValue.get();
  }

  public Weight getTotalWeight() {
    return totalWeight.get();
  }

  public ModifiedValue getTouchArmorClass() {
    return touchArmorClass.get();
  }

  public String getType() {
    return type.get();
  }

  public String getWearingName() {
    return wearing.name;
  }

  public ModifiedValue getWill() {
    return will.get();
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

  @Override
  public boolean isCharacter() {
    return false;
  }

  public boolean isWearingDefault() {
    return wearing.name.equals(DEFAULT_DISTRIBUTION);
  }

  public void setBaseCharisma(int charisma) {
    this.charisma = charisma;
    state.reset();
  }

  public void setBaseConstitution(int constitution) {
    this.constitution = constitution;
    state.reset();
  }

  public void setBaseDexterity(int dexterity) {
    this.dexterity = dexterity;
    state.reset();
  }

  public void setBaseIntelligence(int intelligence) {
    this.intelligence = intelligence;
    state.reset();
  }

  public void setBaseStrength(int strength) {
    this.strength = strength;
    state.reset();
  }

  public void setBaseWisdom(int wisdom) {
    this.wisdom = wisdom;
    state.reset();
  }

  @Override
  public boolean isWearing(Item item) {
    return wearing.isWearing(item);
  }

  public boolean isWearing(Item item, Items.Slot slot) {
    return wearing.isWearing(item, slot);
  }

  @Override
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
    state.reset();
  }

  public void addHp(int number) {
    hp += number;
  }

  public void addNonlethalDamage(int number) {
    nonlethalDamage += number;
  }

  @Override
  public boolean amDM() {
    Optional<Campaign> campaign = context.campaigns().getOptional(campaignId);
    return campaign.isPresent() && campaign.get().amDM();
  }

  public boolean amPlayer() {
    return false;
  }

  @Override
  public boolean canEdit() {
    return false;
  }

  public void carry(Items.Slot slot, Item item) {
    wearing.carry(slot, item);
  }

  public Set<Feat> collectFeats() {
    if (race.isPresent()) {
      return new HashSet<>(race.get().getFeats());
    }

    return Collections.emptySet();
  }

  public Multimap<String, Quality> collectQualities() {
    if (race.isPresent()) {
      return Multimaps.index(race.get().getQualities(), Quality::getName);
    }

    return HashMultimap.create();
  }

  @Override
  public void combine(Item item, Item other) {
    if (item.similar(other)) {
      removeItem(other);
      item.setCount(item.getCount() + other.getCount());
      store();
    }
  }

  public ModifiedValue computeAttackBonus(Item item) {
    ModifiedValue attack = new ModifiedValue(item.getName() + " Attack", getBaseAttackBonus(),
        true);

    switch (item.getWeaponStyle()) {
      default:
      case UNKNOWN:
        break;

      case TWOHANDED_MELEE:
      case ONEHANDED_MELEE:
      case LIGHT_MELEE:
      case TOUCH:
      case UNARMED: {
        int strength = getStrength().total();
        if (strength != 0) {
          attack.add(new Modifier(getStrengthModifier(), Modifier.Type.GENERAL, "Strength"));
        }
        break;
      }

      case RANGED_TOUCH:
      case RANGED:
      case THROWN_TOUCH:
      case THROWN:
      case THROWN_TWO_HANDED: {
        int dexterity = getDexterityModifier();
        if (dexterity != 0) {
          attack.add(new Modifier(getDexterityModifier(), Modifier.Type.GENERAL, "Dexterity"));
        }
        break;
      }
    }

    if (getRace().isPresent()) {
      int size = Armor.sizeModifier(getRace().get().getSize());
      if (size != 0) {
        attack.add(new Modifier(size, Modifier.Type.GENERAL, "Size"));
      }
    }

    for (Feat feat : collectFeats()) {
      if (feat.getQualifiers().isEmpty() || feat.getQualifiers().contains(item.getName())) {
        attack.add(feat.getAttackModifiers());
      }
    }

    for (Collection<Quality> qualities : collectQualities().asMap().values()) {
      attack.add(qualities.iterator().next().getTemplate().getAttackModifiers());
    }

    attack.add(item.getMagicAttackModifiers());

    return attack;
  }

  public Damage computeDamage(Item item) {
    if (!item.isWeapon()) {
      return new Damage();
    }

    Damage damage = item.getDamage();

    // Strength or dexterity
    switch (item.getWeaponStyle()) {
      case UNKNOWN:
      case TOUCH:
      case RANGED_TOUCH:
      case THROWN_TOUCH:
        break;

      case ONEHANDED_MELEE:
      case LIGHT_MELEE:
      case UNARMED:
        damage.add(0, 0, getStrengthModifier(), DamageType.NONE, Optional.empty(),
            "Strength");
        break;

      case TWOHANDED_MELEE:
        damage.add(0, 0, (int) (1.5 * getStrengthModifier()), DamageType.NONE, Optional.empty(),
            "Strength");
        break;

      case RANGED:
      case THROWN:
      case THROWN_TWO_HANDED:
        damage.add(0, 0, getDexterityModifier(), DamageType.NONE, Optional.empty(),
            "Dexterity");
        break;
    }

    // Feats.
    for (Feat feat : collectFeats()) {
      for (Modifier modifier : feat.getDamageModifiers()) {
        damage.add(modifier);
      }
    }

    // Qualities.
    for (Collection<Quality> qualities : collectQualities().asMap().values()) {
      for (Modifier modifier : qualities.iterator().next().getTemplate().getDamageModifiers()) {
        damage.add(modifier);
      }
    }

    return damage;
  }

  public int getAbilityModifier(Ability ability) {
    switch (ability) {
      case STRENGTH:
        return getStrengthModifier();
      case DEXTERITY:
        return getDexterityModifier();
      case CONSTITUTION:
        return getConstitutionModifier();
      case INTELLIGENCE:
        return getIntelligenceModifier();
      case WISDOM:
        return getWisdomModifier();
      case CHARISMA:
        return getCharismaModifier();

      case NONE:
      case UNKNOWN:
      default:
        return 0;

    }
  }

  public Optional<Integer> getEncounterInitiative(int encounterNumber) {
    if (this.encounterNumber == encounterNumber) {
      return Optional.of(encounterInitiative);
    }

    return Optional.empty();
  }

  @Override
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

  public void initFromRace(String name) {
    setRace(name);
    if (race.isPresent()) {
      // Abilities.
      strength = race.get().getStrength();
      dexterity = race.get().getDexterity();
      constitution = race.get().getConstitution();
      intelligence = race.get().getIntelligenec();
      wisdom = race.get().getWisdom();
      charisma = race.get().getCharisma();

      // Hit points.
      maxHp = Die.rollModifierEach(race.get().getHitDie(), race.get().getHitDice(),
          getDexterityModifier());
      hp = maxHp;
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

  @Override
  public boolean moveItemAfter(Item item, Item move) {
    if (isWearing(item)) {
      wearing.moveItemAfter(item, move);
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

  @Override
  public boolean moveItemBefore(Item item, Item move) {
    if (isWearing(item)) {
      wearing.moveItemBefore(item, move);
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

  @Override
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

  public int numberOfAttacks(Item item) {
    return Math.min((getBaseAttackBonus() / 6) + 1, item.getMaxAttacks());
  }

  public Wearing readWearing(Data data) {
    Map<String, List<String>> raw = data.getMap(FIELD_ITEMS_PER_SLOT, Collections.emptyList());
    EnumMap<Items.Slot, List<String>> wearing = new EnumMap<>(Items.Slot.class);
    for (Map.Entry<String, List<String>> entry : raw.entrySet()) {
      wearing.put(Items.Slot.valueOf(entry.getKey()), entry.getValue());
    }

    return new Wearing(
        data.get(FIELD_NAME, "default"),
        wearing);
  }

  public boolean removeItem(Item item) {
    if (amPlayer()) {
      if (!items.remove(item) && removeItem(item.getId()) && items.stream().anyMatch(i -> i.remove(item))) {
        return false;
      }

      wearing.remove(item);
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

  @Override
  public void store() {
    super.store();
    state.reset();
  }

  @Override
  public void updated(Item item) {
    store();
  }

  public List<String> wearing(Items.Slot slot) {
    return wearing.wearing(slot);
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
    wearing = readWearing(data.getNested(FIELD_SLOTS));
  }

  @Override
  @CallSuper
  protected Data write() {
    return Data.empty()
        .set(FIELD_NAME, name)
        .set(FIELD_CAMPAIGN, campaignId)
        .set(FIELD_GENDER, gender.toString())
        .set(FIELD_RACE, Optionals.ifPresent(race, MonsterTemplate::getName))
        .set(FIELD_STRENGTH, strength)
        .set(FIELD_DEXTERITY, dexterity)
        .set(FIELD_CONSTITUTION, constitution)
        .set(FIELD_INTELLIGENCE, intelligence)
        .set(FIELD_WISDOM, wisdom)
        .set(FIELD_CHARISMA, charisma)
        .set(FIELD_HP, hp)
        .set(FIELD_MAX_HP, maxHp)
        .set(FIELD_NONLETHAL, nonlethalDamage)
        .set(FIELD_INITIATIVE, encounterInitiative)
        .set(FIELD_ENCOUNTER_NUMBER, encounterNumber)
        .setNested(FIELD_ITEMS, items)
        .setNested(FIELD_SLOTS, wearing);
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

  public class Wearing extends NestedDocument {
    private static final String FIELD_NAME = "name";

    private final String name;
    private final EnumMap<Items.Slot, List<String>> itemsPerSlot;

    public Wearing(String name) {
      this.name = name;

      this.itemsPerSlot = new EnumMap<Items.Slot, List<String>>(Items.Slot.class);
      for (Items.Slot slot : Items.Slot.values()) {
        itemsPerSlot.put(slot, new ArrayList<>());
      }
    }

    private Wearing(String name, EnumMap<Items.Slot, List<String>> itemsPerSlot) {
      this.name = name;
      this.itemsPerSlot = itemsPerSlot;

      for (Items.Slot slot : Items.Slot.values()) {
        if (!itemsPerSlot.containsKey(slot)) {
          itemsPerSlot.put(slot, new ArrayList<>());
        }
      }
    }

    public boolean isWearing(Item item) {
      for (Items.Slot slot : itemsPerSlot.keySet()) {
        if (isWearing(item, slot)) {
          return true;
        }
      }

      return false;
    }

    public boolean isWearing(Item item, Items.Slot slot) {
      return itemsPerSlot.get(slot).contains(item.getId());
    }

    public void carry(Items.Slot slot, Item item) {
      remove(item);
      add(slot, item);

      Creature.this.store();
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

    public List<String> wearing(Items.Slot slot) {
      return itemsPerSlot.get(slot);
    }

    @Override
    public Data write() {
      return Data.empty()
          .set(FIELD_NAME, name)
          .set(FIELD_ITEMS_PER_SLOT, itemsPerSlot.entrySet().stream()
              .collect(Collectors.toMap((e) -> e.getKey().toString(), Map.Entry::getValue)));
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
