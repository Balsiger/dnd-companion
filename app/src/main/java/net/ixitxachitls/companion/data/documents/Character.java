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
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.enums.Alignment;
import net.ixitxachitls.companion.data.enums.MagicEffectType;
import net.ixitxachitls.companion.data.templates.LevelTemplate;
import net.ixitxachitls.companion.data.templates.SkillTemplate;
import net.ixitxachitls.companion.data.values.AbilityAdjustment;
import net.ixitxachitls.companion.data.values.Adjustment;
import net.ixitxachitls.companion.data.values.CampaignDate;
import net.ixitxachitls.companion.data.values.ConditionData;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.data.values.ModifiedValue;
import net.ixitxachitls.companion.data.values.Modifier;
import net.ixitxachitls.companion.rules.XP;
import net.ixitxachitls.companion.util.Lazy;
import net.ixitxachitls.companion.util.Strings;
import net.ixitxachitls.companion.util.Texts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import androidx.annotation.CallSuper;

/**
 * A player character in the game.
 */
public class Character extends Creature<Character> implements Comparable<Character> {

  public static final String PATH = "characters";
  public static final Character DEFAULT = createDefault();

  private static final DocumentFactory<Character> FACTORY = () -> new Character();

  private static final String FIELD_XP = "xpAction";
  private static final String FIELD_LEVEL = "level";
  private static final String FIELD_LEVELS = "levels";
  private static final String FIELD_HEIGHT = "height";
  private static final String FIELD_WEIGHT = "weight";
  private static final String FIELD_AGE = "age";
  private static final String FIELD_LOOKS = "looks";
  private static final String FIELD_ALIGNMENT = "alignment";
  private static final String FIELD_RELIGION = "religion";
  private static final int DEFAULT_LEVEL = 1;

  protected List<ConditionData> conditionsHistory = new ArrayList<>();
  private User player;
  private int xp = 0;
  private int level;
  private List<Level> levels = new ArrayList<>();
  private String height = "";
  private String weight = "";
  private int age = 0;
  private String looks = "";
  private Alignment alignment = Alignment.UNKNOWN;
  private String religion = "";

  // Lazy values.
  private Lazy.Resettable<Integer> baseAttackBonus = new Lazy.Resettable<>(state, () -> {
    int attack = 0;
    Multiset<String> names = HashMultiset.create();
    for (Level level : levels) {
      names.add(level.getTemplate().getName());
      attack += level.getBaseAttack(names.count(level.getTemplate().getName()));
    }

    return attack;
  });
  private Lazy.Resettable<ModifiedValue> grapple = new Lazy.Resettable<>(state, () -> {
    ModifiedValue grapple = new ModifiedValue("grapple", getBaseAttackBonus(), true);

    // Strength modifier.
    grapple.add(new Modifier(getStrengthModifier(), Modifier.Type.GENERAL, "STR"));

    // Size modifier.
    grapple.add(new Modifier(getSize().getModifier(), Modifier.Type.GENERAL, "Size"));

    return grapple;
  });
  private Lazy.Resettable<ModifiedValue> charisma = new Lazy.Resettable<>(state, () ->
      adjustAbility(super.getCharisma(), Ability.CHARISMA));
  private Lazy.Resettable<ModifiedValue> charismaCheck = new Lazy.Resettable<>(state, () ->
      adjustAbilityCheck(super.getCharismaCheck(), Ability.CHARISMA));
  private Lazy.Resettable<ModifiedValue> constitution = new Lazy.Resettable<>(state, () ->
      adjustAbility(super.getConstitution(), Ability.CONSTITUTION));
  private Lazy.Resettable<ModifiedValue> constitutionCheck = new Lazy.Resettable<>(state, () ->
      adjustAbilityCheck(super.getConstitutionCheck(), Ability.CONSTITUTION));
  private Lazy.Resettable<ModifiedValue> dexterity = new Lazy.Resettable<>(state, () ->
      adjustAbility(super.getDexterity(), Ability.DEXTERITY));
  private Lazy.Resettable<ModifiedValue> dexterityCheck = new Lazy.Resettable<>(state, () ->
      adjustAbilityCheck(super.getDexterityCheck(), Ability.DEXTERITY));
  private Lazy.Resettable<ModifiedValue> fortitude = new Lazy.Resettable<>(state, () -> {
    ModifiedValue value = super.getFortitude();

    // Modifiers from class.
    Multiset<LevelTemplate> counted = Level.countedNames(levels);
    for (LevelTemplate level : counted.elementSet()) {
      value.add(new Modifier(level.getFortitudeModifier(counted.count(level)),
          Modifier.Type.GENERAL, level.getName()));
    }

    // Modifiers from items.
    for (Item item : getItems()) {
      if (isWearing(item) && item.isMagic()) {
        value.add(item.getMagicModifiers(MagicEffectType.FORTITUDE));
      }
    }

    return value;
  });
  private Lazy.Resettable<ModifiedValue> initiative = new Lazy.Resettable<>(state, () -> {
    ModifiedValue value = super.getInitiative();

    for (Level level : levels) {
      if (level.getFeat().isPresent()) {
        value.add(level.getFeat().get().getInitiativeAdjustment());
      }
    }

    return value;
  });
  private Lazy.Resettable<ModifiedValue> intelligence = new Lazy.Resettable<>(state, () ->
      adjustAbility(super.getIntelligence(), Ability.INTELLIGENCE));
  private Lazy.Resettable<ModifiedValue> intelligenceCheck = new Lazy.Resettable<>(state, () ->
      adjustAbilityCheck(super.getIntelligenceCheck(), Ability.INTELLIGENCE));
  private Lazy.Resettable<Integer> maxHp = new Lazy.Resettable<>(state, () -> {
    int hp = 0;
    for (Level level : levels) {
      hp += Math.max(1, level.getHp() + getConstitutionModifier());
    }

    return hp;
  });
  private Lazy.Resettable<ModifiedValue> reflex = new Lazy.Resettable<>(state, () -> {
    ModifiedValue value = super.getReflex();

    // Modifiers from class.
    Multiset<LevelTemplate> counted = Level.countedNames(levels);
    for (LevelTemplate level : counted.elementSet()) {
      value.add(new Modifier(level.getReflexModifier(counted.count(level)),
          Modifier.Type.GENERAL, level.getName()));
    }

    // Modifiers from items.
    for (Item item : getItems()) {
      if (isWearing(item) && item.isMagic()) {
        value.add(item.getMagicModifiers(MagicEffectType.REFLEX));
      }
    }

    return value;
  });
  private Lazy.Resettable<ModifiedValue> strength = new Lazy.Resettable<>(state, () ->
      adjustAbility(super.getStrength(), Ability.STRENGTH));
  private Lazy.Resettable<ModifiedValue> strengthCheck = new Lazy.Resettable<>(state, () ->
      adjustAbilityCheck(super.getStrengthCheck(), Ability.STRENGTH));
  private Lazy.Resettable<ModifiedValue> will = new Lazy.Resettable<>(state, () -> {
    ModifiedValue value = super.getWill();

    // Modifiers from class.
    Multiset<LevelTemplate> counted = Level.countedNames(levels);
    for (LevelTemplate level : counted.elementSet()) {
      value.add(new Modifier(level.getWillModifier(counted.count(level)),
          Modifier.Type.GENERAL, level.getName()));
    }

    // Modifiers from items.
    for (Item item : getItems()) {
      if (isWearing(item) && item.isMagic()) {
        value.add(item.getMagicModifiers(MagicEffectType.WILL));
      }
    }


    return value;
  });
  private Lazy.Resettable<ModifiedValue> wisdom = new Lazy.Resettable<>(state, () ->
      adjustAbility(super.getWisdom(), Ability.WISDOM));
  private Lazy.Resettable<ModifiedValue> wisdomCheck = new Lazy.Resettable<>(state, () ->
      adjustAbilityCheck(super.getWisdomCheck(), Ability.WISDOM));
  private Lazy.Resettable<Set<Feat>> feats = new Lazy.Resettable<>(state, () -> {
    Set<Feat> feats = new HashSet<>();

    // Feats set in levels.
    for (Level level : levels) {
      if (level.getFeat().isPresent()) {
        feats.add(level.getFeat().get());
      }
      if (level.getClassFeat().isPresent()) {
        feats.add(level.getClassFeat().get());
      }
      if (level.getRacialFeat().isPresent()) {
        feats.add(level.getRacialFeat().get());
      }

      // Automatic feats by class.
      feats.addAll(level.getAutomaticFeats());
    }

    // Automatic feats by race.
    if (getRace().isPresent()) {
      feats.addAll(getRace().get().getAutomaticFeats());
    }

    return feats;
  });
  private Lazy.Resettable<Map<String, Texts.Value>> formatValues =
      new Lazy.Resettable<>(state, () -> ImmutableMap.<String, Texts.Value>builder()
          .put("strength_modifier", new Texts.IntegerValue(getStrengthModifier()))
          .put("dexterity_modifier", new Texts.IntegerValue(getDexterityModifier()))
          .put("constitution_modifier", new Texts.IntegerValue(getConstitutionModifier()))
          .put("intelligenve_modifier", new Texts.IntegerValue(getIntelligenceModifier()))
          .put("widsom_modifier", new Texts.IntegerValue(getWisdomModifier()))
          .put("charisma_modifier", new Texts.IntegerValue(getCharismaModifier()))
          .build());
  private Lazy.Resettable<Multimap<String, Quality>> qualities =
      new Lazy.Resettable<>(state, () -> {
        // Need to copy, as super might return an immutable map.
        Multimap<String, Quality> qualities = HashMultimap.create(super.collectQualities());

        // Qualities from levels.
        Multiset<String> names = HashMultiset.create();
        for (Level level : levels) {
          names.add(level.getTemplate().getName());
          qualities.putAll(
              Multimaps.index(level.getQualities(names.count(level.getTemplate().getName())),
                  Quality::getName));
        }

        // TODO(merlin): Add qualities by items.

        return qualities;
      });
  private Lazy.Resettable<Map<String, ModifiedValue>> skillRanks =
      new Lazy.Resettable<>(state, () -> {
        Map<String, ModifiedValue> ranks = new HashMap<>();

        Multiset<String> levelNames = HashMultiset.create();
        for (Level level : levels) {
          levelNames.add(level.getTemplate().getName());
          for (Map.Entry<String, Integer> skillRank : level.getSkills().entrySet()) {
            ranks.put(skillRank.getKey(),
                ranks.getOrDefault(skillRank.getKey(), new ModifiedValue(skillRank.getKey(), 0,
                    true))
                    .add(new Modifier(skillRank.getValue(), Modifier.Type.GENERAL,
                        level.getTemplate().getName()
                            + levelNames.count(level.getTemplate().getName()))));
          }
        }

        return ranks;
      });
  private Lazy.Resettable<SortedMap<String, ModifiedValue>> skills =
      new Lazy.Resettable<>(state, () -> {
        Map<String, ModifiedValue> skillRanks = collectSkillRanks();
        SortedMap<String, ModifiedValue> skills = new TreeMap<>();
        for (SkillTemplate skill : Templates.get().getSkillTemplates().getValues()) {
          ModifiedValue ranks =
              skillRanks.getOrDefault(skill.getName(), new ModifiedValue(skill.getName(), 0, true))
                  .add(new Modifier(getAbilityModifier(skill.getAbility()), Modifier.Type.GENERAL,
                      skill.getAbility().getName()));
          if (ranks.total() != 0) {
            skills.put(skill.getName(), ranks);
          }
        }

        return skills;
      });

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  @Override
  public Alignment getAlignment() {
    return alignment;
  }

  public void setAlignment(Alignment alignment) {
    this.alignment = alignment;
  }

  @Override
  public int getBaseAttackBonus() {
    return baseAttackBonus.get();
  }

  @Override
  public ModifiedValue getCharisma() {
    return charisma.get();
  }

  @Override
  public ModifiedValue getCharismaCheck() {
    return charismaCheck.get();
  }

  public List<ConditionData> getConditionsHistory() {
    return ImmutableList.copyOf(conditionsHistory);
  }

  @Override
  public ModifiedValue getConstitution() {
    return constitution.get();
  }

  @Override
  public ModifiedValue getConstitutionCheck() {
    return constitutionCheck.get();
  }

  @Override
  public ModifiedValue getDexterity() {
    return dexterity.get();
  }

  @Override
  public ModifiedValue getDexterityCheck() {
    return dexterityCheck.get();
  }

  @Override
  public ModifiedValue getFortitude() {
    return fortitude.get();
  }

  public ModifiedValue getGrapple() {
    return grapple.get();
  }

  public String getHeight() {
    return height;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  @Override
  public ModifiedValue getInitiative() {
    return initiative.get();
  }

  @Override
  public ModifiedValue getIntelligence() {
    return intelligence.get();
  }

  @Override
  public ModifiedValue getIntelligenceCheck() {
    return intelligenceCheck.get();
  }

  public int getLevel() {
    return levels.size();
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public List<Level> getLevels() {
    return levels;
  }

  public void setLevels(List<Level> levels) {
    this.levels = levels;
    store();
  }

  public String getLooks() {
    return looks;
  }

  public void setLooks(String looks) {
    this.looks = looks;
  }

  @Override
  public int getMaxHp() {
    return maxHp.get();
  }

  public int getMaxLevel() {
    return XP.maxLevelForXp(xp);
  }

  public User getPlayer() {
    return player;
  }

  @Override
  public ModifiedValue getReflex() {
    return reflex.get();
  }

  public String getReligion() {
    return religion;
  }

  public void setReligion(String religion) {
    this.religion = religion;
  }

  @Override
  public ModifiedValue getStrength() {
    return strength.get();
  }

  @Override
  public ModifiedValue getStrengthCheck() {
    return strengthCheck.get();
  }

  public String getWeight() {
    return weight;
  }

  public void setWeight(String weight) {
    this.weight = weight;
  }

  @Override
  public ModifiedValue getWill() {
    return will.get();
  }

  @Override
  public ModifiedValue getWisdom() {
    return wisdom.get();
  }

  @Override
  public ModifiedValue getWisdomCheck() {
    return wisdomCheck.get();
  }

  public int getXp() {
    return xp;
  }

  public void setXp(int xp) {
    this.xp = xp;
  }

  @Override
  public boolean isCharacter() {
    return true;
  }

  public void addLevel() {
    levels.add(new Level());
    store();
  }

  public void addXp(int number) {
    xp += number;
  }

  @Override
  public boolean amDM() {
    return getCampaignId().startsWith(context.me().getId());
  }

  @Override
  public boolean amPlayer() {
    return player == context.me()
        || context.me().getFeatures().contains("override=" + getName());
  }

  // TODO(merlin): Move this into Document generally?
  @Override
  public boolean canEdit() {
    return amPlayer() || amDM();
  }

  @Override
  public Set<Feat> collectFeats() {
    return feats.get();
  }

  public Map<String, Texts.Value> collectFormatValues() {
    return formatValues.get();
  }

  @Override
  public Multimap<String, Quality> collectQualities() {
    return qualities.get();
  }

  // Skills not returned have a total modifier of +0.
  public SortedMap<String, ModifiedValue> collectSkills() {
    return skills.get();
  }

  @Override
  public int compareTo(Character that) {
    int name = this.getName().compareTo(that.getName());
    if (name != 0) {
      return name;
    }

    return this.getId().compareTo(that.getId());
  }

  public void delete(Level level) {
    levels.remove(level);
    store();
  }

  public String formatAttacks(Item item) {
    int bonus = computeAttackBonus(item).total();
    int attacks = numberOfAttacks(item);

    List<String> parts = new ArrayList<>();
    for (int i = 0; i < attacks; i++) {
      parts.add("+" + (bonus - i * 6));
    }

    return item.getWeaponStyle().getName() + " " + Strings.SLASH_JOINER.join(parts);
  }

  public String formatCritical(Item item) {
    int criticalLow = item.weaponCriticalLow();
    int criticalMultiplier = item.weaponCriticalMultiplier();
    if (criticalLow == 20) {
      return "x" + criticalMultiplier;
    } else {
      return criticalLow + "-20/x" + criticalMultiplier;
    }
  }

  public int getClassLevel(String className, int totalLevel) {
    int classLevel = 0;
    for (int i = 0; i < totalLevel && i < levels.size(); i++) {
      Level level = levels.get(i);
      if (level.getTemplate().getName().equals(className)) {
        classLevel++;
      }
    }

    return classLevel;
  }

  public Optional<Level> getLevel(int number) {
    if (number > 0 && number - 1 < levels.size()) {
      return Optional.of(levels.get(number - 1));
    }

    return Optional.empty();
  }

  public void setLevel(int number, Level level) {
    if (number - 1 < levels.size()) {
      levels.set(number - 1, level);
      store();
    } else if (number - 1 == levels.size()) {
      levels.add(level);
      store();
    } else {
      Status.error("Cannot add level " + number);
    }

    state.reset();
  }

  public void setRandomAge() {
    if (getRace().isPresent() && getLevel(1).isPresent()) {
      age = getRace().get().randomAge(getLevel(1).get().getTemplate().getName());
      store();
    }
  }

  public void setRandomHeightAndWeight() {
    if (getRace().isPresent()) {
      String[] heightAndWeight = getRace().get().randomHeightAndWeight(getGender());
      height = heightAndWeight[0];
      weight = heightAndWeight[1];
      store();
    }
  }

  @Override
  public String toString() {
    return getName();
  }

  public void updateConditions(CampaignDate date) {
    for (CreatureCondition condition : context.conditions().getCreatureConditions(getId())) {
      if (!condition.getCondition().isPermanent()) {
        if (condition.getCondition().hasEndDate()) {
          if (condition.getCondition().getEndDate().before(date)) {
            context.conditions().delete(condition.getId());
          }
        } else {
          context.conditions().delete(condition.getId());
        }
      }
    }

    state.reset();
  }

  public List<String> validateLevels() {
    List<String> errors = new ArrayList<>();
    int number = 1;
    for (Level level : levels) {
      errors.addAll(level.validate(this, number++));
    }

    // Check number of levels.
    if (levels.size() > getMaxLevel()) {
      errors.add(getName() + " has more levels than the XP allows. Should only have "
          + getMaxLevel() + " levels");
    } else if (levels.size() < getMaxLevel()) {
      errors.add(getName() + " has less levels than the XP allows. Should have " + getMaxLevel()
          + " levels");
    }

    return errors;
  }

  @Override
  @CallSuper
  public Data write() {
    return super.write()
        .set(FIELD_XP, xp)
        .set(FIELD_LEVEL, level)
        .setNested(FIELD_LEVELS, levels)
        .set(FIELD_HEIGHT, height)
        .set(FIELD_WEIGHT, weight)
        .set(FIELD_AGE, age)
        .set(FIELD_LOOKS, looks)
        .set(FIELD_ALIGNMENT, alignment.toString())
        .set(FIELD_RELIGION, religion);
  }

  private ModifiedValue adjustAbility(ModifiedValue value, Ability ability) {
    adjustAbilityForLevels(value, ability);
    adjustAbilityForConditions(value, ability);
    adjustAbilityForQualities(value, ability);
    adjustAbilityForItems(value, ability);
    return value;
  }

  private ModifiedValue adjustAbilityCheck(ModifiedValue value, Ability ability) {
    adjustAbilityCheckForConditions(value, ability);
    return value;
  }

  private ModifiedValue adjustAbilityCheckForConditions(ModifiedValue value, Ability ability) {
    for (CreatureCondition condition : getConditions()) {
      for (Adjustment adjustment : condition.getCondition().getCondition().getAdjustments()) {
        if (adjustment.is(Adjustment.Type.abilityCheck)) {
          if (ability == ((AbilityAdjustment) adjustment).getAbility()) {
            value.add(((AbilityAdjustment) adjustment).getModifier());
          }
        }
      }
    }

    return value;
  }

  private ModifiedValue adjustAbilityForConditions(ModifiedValue value, Ability ability) {
    for (CreatureCondition condition : getConditions()) {
      for (Adjustment adjustment : condition.getCondition().getCondition().getAdjustments()) {
        if (adjustment.is(Adjustment.Type.ability)) {
          if (ability == ((AbilityAdjustment) adjustment).getAbility()) {
            value.add(((AbilityAdjustment) adjustment).getModifier());
          }
        }
      }
    }

    return value;
  }

  private void adjustAbilityForItems(ModifiedValue value, Ability ability) {
    for (Item item : getItems()) {
      if (isWearing(item) && item.isMagic()) {
        value.add(item.computeAbilityModifers(ability));
      }
    }
  }

  private ModifiedValue adjustAbilityForLevels(ModifiedValue value, Ability ability) {
    int number = 1;
    for (Level level : levels) {
      if (level.getIncreasedAbility().isPresent() && level.getIncreasedAbility().get() == ability) {
        value.add(new Modifier(1, Modifier.Type.GENERAL,
            "Level " + number + ": " + level.toString()));
      }
      number++;
    }

    return value;
  }

  private void adjustAbilityForQualities(ModifiedValue value, Ability ability) {
    if (getRace().isPresent()) {
      for (Quality quality : getRace().get().getQualities()) {
        value.add(quality.getTemplate().getAbilityModifiers(ability));
      }
    }
  }

  private Map<String, ModifiedValue> collectSkillRanks() {
    return skillRanks.get();
  }

  @Override
  protected boolean hasLevels() {
    return !levels.isEmpty();
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();
    xp = data.get(FIELD_XP, 0);
    level = data.get(FIELD_LEVEL, DEFAULT_LEVEL);
    levels = new ArrayList<>();
    int i = 1;
    for (Data levelData : data.getNestedList(FIELD_LEVELS)) {
      levels.add(Level.read(levelData, i++));
    }
    height = data.get(FIELD_HEIGHT, "");
    weight = data.get(FIELD_WEIGHT, "");
    age = data.get(FIELD_AGE, 0);
    looks = data.get(FIELD_LOOKS, "");
    alignment = data.get(FIELD_ALIGNMENT, Alignment.UNKNOWN);
    religion = data.get(FIELD_RELIGION, "");
  }

  protected static Character create(CompanionContext context, String campaignId) {
    Character character = Document.create(FACTORY, context, context.me().getId() + "/" + PATH);

    character.player = context.me();
    character.setCampaignId(campaignId);

    return character;
  }

  private static Character createDefault() {
    Character character = new Character();
    character.context = CompanionApplication.get().context();
    character.temporary = true;

    return character;
  }

  protected static Character fromData(CompanionContext context, User player,
                                      DocumentSnapshot snapshot) {
    Character character = Document.fromData(FACTORY, context, snapshot);
    character.player = player;

    return character;
  }
}
