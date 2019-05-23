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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.templates.LevelTemplate;
import net.ixitxachitls.companion.data.values.AbilityAdjustment;
import net.ixitxachitls.companion.data.values.Adjustment;
import net.ixitxachitls.companion.data.values.CampaignDate;
import net.ixitxachitls.companion.data.values.ConditionData;
import net.ixitxachitls.companion.data.values.ModifiedValue;
import net.ixitxachitls.companion.data.values.Modifier;
import net.ixitxachitls.companion.rules.XP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A player character in the game.
 */
public class Character extends Creature<Character> implements Comparable<Character> {

  public static final String PATH = "characters";
  private static final DocumentFactory<Character> FACTORY = () -> new Character();

  private static final String FIELD_XP = "xpAction";
  private static final String FIELD_LEVEL = "level";
  private static final String FIELD_LEVELS = "levels";
  private static final int DEFAULT_LEVEL = 1;

  protected List<ConditionData> conditionsHistory = new ArrayList<>();
  private User player;
  private int xp = 0;
  private int level;
  private List<Level> levels = new ArrayList<>();

  @Override
  public int getBaseAttackBonus() {
    int attack = 0;
    Multiset<String> names = HashMultiset.create();
    for (Level level : levels) {
      names.add(level.getTemplate().getName());
      attack += level.getBaseAttack(names.count(level.getTemplate().getName()));
    }

    return attack;
  }

  @Override
  public ModifiedValue getCharisma() {
    return adjustAbility(super.getCharisma(), Ability.CHARISMA);
  }

  @Override
  public ModifiedValue getCharismaCheck() {
    return adjustAbilityCheck(super.getCharismaCheck(), Ability.CHARISMA);
  }

  @Override
  public ModifiedValue getConstitution() {
    return adjustAbility(super.getConstitution(), Ability.CONSTITUTION);
  }

  @Override
  public ModifiedValue getConstitutionCheck() {
    return adjustAbilityCheck(super.getConstitutionCheck(), Ability.CONSTITUTION);
  }

  @Override
  public ModifiedValue getDexterity() {
    return adjustAbility(super.getDexterity(), Ability.DEXTERITY);
  }

  @Override
  public ModifiedValue getDexterityCheck() {
    return adjustAbilityCheck(super.getDexterityCheck(), Ability.DEXTERITY);
  }

  @Override
  public ModifiedValue getIntelligence() {
    return adjustAbility(super.getIntelligence(), Ability.INTELLIGENCE);
  }

  @Override
  public ModifiedValue getIntelligenceCheck() {
    return adjustAbilityCheck(super.getIntelligenceCheck(), Ability.INTELLIGENCE);
  }

  @Override
  public int getMaxHp() {
    int hp = 0;
    for (Level level : levels) {
      hp += Math.max(1, level.getHp() + getConstitutionModifier());
    }

    return hp;
  }

  @Override
  public ModifiedValue getStrength() {
    return adjustAbility(super.getStrength(), Ability.STRENGTH);
  }

  @Override
  public ModifiedValue getStrengthCheck() {
    return adjustAbilityCheck(super.getStrengthCheck(), Ability.STRENGTH);
  }

  @Override
  public ModifiedValue getWisdom() {
    return adjustAbility(super.getWisdom(), Ability.WISDOM);
  }

  @Override
  public ModifiedValue getWisdomCheck() {
    return adjustAbilityCheck(super.getWisdomCheck(), Ability.WISDOM);
  }

  @Override
  public boolean amDM() {
    return getCampaignId().startsWith(context.me().getId());
  }

  @Override
  public boolean amPlayer() {
    return player == context.me();
  }

  // TODO(merlin): Move this into Document generally?
  @Override
  public boolean canEdit() {
    return amPlayer() || amDM();
  }

  @Override
  public Set<Feat> collectFeats() {
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
  }

  @Override
  public Set<Quality> collectQualities() {
    Set<Quality> qualities = super.collectQualities();

    // Qualities from levels.
    Multiset<String> names = HashMultiset.create();
    for (Level level : levels) {
      names.add(level.getTemplate().getName());
      qualities.addAll(level.getQualities(names.count(level.getTemplate().getName())));
    }

    // TODO(merlin): Add qualities by items.

    return qualities;
  }

  @Override
  public ModifiedValue fortitude() {
    ModifiedValue value = super.fortitude();

    // Modifiers from class.
    Multiset<LevelTemplate> counted = Level.countedNames(levels);
    for (LevelTemplate level : counted.elementSet()) {
      value.add(new Modifier(level.getFortitudeModifier(counted.count(level)),
          Modifier.Type.GENERAL, level.getName()));
    }

    return value;
  }

  @Override
  public ModifiedValue initiative() {
    ModifiedValue value = super.initiative();

    for (Level level : levels) {
      if (level.getFeat().isPresent()) {
        value.add(level.getFeat().get().getInitiativeAdjustment());
      }
    }

    return value;
  }

  @Override
  public ModifiedValue reflex() {
    ModifiedValue value = super.reflex();

    // Modifiers from class.
    Multiset<LevelTemplate> counted = Level.countedNames(levels);
    for (LevelTemplate level : counted.elementSet()) {
      value.add(new Modifier(level.getReflexModifier(counted.count(level)),
          Modifier.Type.GENERAL, level.getName()));
    }

    return value;
  }

  @Override
  public ModifiedValue will() {
    ModifiedValue value = super.will();

    // Modifiers from class.
    Multiset<LevelTemplate> counted = Level.countedNames(levels);
    for (LevelTemplate level : counted.elementSet()) {
      value.add(new Modifier(level.getWillModifier(counted.count(level)),
          Modifier.Type.GENERAL, level.getName()));
    }

    return value;
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
  }

  @Override
  @CallSuper
  protected Map<String, Object> write(Map<String, Object> data) {
    data = super.write(data);
    data.put(FIELD_XP, xp);
    data.put(FIELD_LEVEL, level);
    data.put(FIELD_LEVELS, levels.stream().map(Level::write).collect(Collectors.toList()));

    return data;
  }

  public List<ConditionData> getConditionsHistory() {
    return ImmutableList.copyOf(conditionsHistory);
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

  public int getMaxLevel() {
    return XP.maxLevelForXp(xp);
  }

  public User getPlayer() {
    return player;
  }

  public int getXp() {
    return xp;
  }

  public void setXp(int xp) {
    this.xp = xp;
  }

  public void addLevel() {
    levels.add(new Level());
    store();
  }

  public void addXp(int number) {
    xp += number;
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

  public Map<String, Object> write() {
    return write(new HashMap<>());
  }

  private ModifiedValue adjustAbility(ModifiedValue value, Ability ability) {
    adjustAbilityForLevels(value, ability);
    adjustAbilityForConditions(value, ability);
    adjustAbilityForQualities(value, ability);
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
        value.add(quality.getTemplate().getAbilityModifiers(Ability.DEXTERITY));
      }
    }
  }

  protected static Character create(CompanionContext context, String campaignId) {
    Character character = Document.create(FACTORY, context, context.me().getId() + "/" + PATH);

    character.player = context.me();
    character.setCampaignId(campaignId);

    return character;
  }

  protected static Character fromData(CompanionContext context, User player,
                                      DocumentSnapshot snapshot) {
    Character character = Document.fromData(FACTORY, context, snapshot);
    character.player = player;

    return character;
  }
}
