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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.templates.LevelTemplate;
import net.ixitxachitls.companion.data.values.Values;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A level a character has earned.
 */
public class Level extends NestedDocument {

  private static final String FIELD_TEMPLATE = "template";
  private static final String FIELD_HP = "hp";
  private static final String FIELD_ABILITY_INCREASE = "ability";
  private static final String FIELD_FEAT = "feat";
  private static final String FIELD_RACIAL_FEAT = "racial_feat";
  private static final String FIELD_CLASS_FEAT = "class_feat";

  private LevelTemplate template;
  private int hp;
  private Optional<Ability> abilityIncrease = Optional.empty();
  private Optional<Feat> feat = Optional.empty();
  private Optional<Feat> racialFeat = Optional.empty();
  private Optional<Feat> classFeat = Optional.empty();

  public Level() {
    this(new LevelTemplate(), 0, Optional.empty(), Optional.empty(), Optional.empty(),
        Optional.empty());
  }

  public Level(String templateName, int number, int hp, String abilityName, String featName,
               String racialFeatName, String classFeatName) {
    this.template = Templates.get().getOrCreateLevel(templateName);
    this.hp = hp;
    this.abilityIncrease = abilityName.isEmpty()
        ? Optional.empty() : Optional.of(Ability.fromName(abilityName));
    this.feat = featName.isEmpty() ? Optional.empty() : Optional.of(new Feat(featName,
        "Selected at level " + number));
    this.racialFeat =
        racialFeatName.isEmpty() ? Optional.empty() : Optional.of(new Feat(racialFeatName,
            "Racial feat from level " + number));
    this.classFeat =
        classFeatName.isEmpty() ? Optional.empty() : Optional.of(new Feat(classFeatName,
            "Special class feat from level " + number));
  }

  public Level(LevelTemplate template, int hp, Optional<Ability> abilityIncrease,
               Optional<Feat> feat, Optional<Feat> racialFeat,
               Optional<Feat> classFeat) {
    this.template = template;
    this.hp = hp;
    this.abilityIncrease = abilityIncrease;
    this.feat = feat;
    this.racialFeat = racialFeat;
    this.classFeat = classFeat;
  }

  public List<Feat> getAutomaticFeats() {
    return template.getAutomaticFeats().stream()
        .map(t -> new Feat(t, "Automatic Feat from " + template.getName()))
        .collect(Collectors.toList());
  }

  public Optional<Feat> getClassFeat() {
    return classFeat;
  }

  public Optional<Feat> getFeat() {
    return feat;
  }

  public int getHp() {
    return hp;
  }

  public void setHp(int hp) {
    this.hp = hp;
  }

  public Optional<Ability> getIncreasedAbility() {
    return abilityIncrease;
  }

  public void setIncreasedAbility(Ability ability) {
    this.abilityIncrease = Optional.of(ability);
  }

  public int getMaxHp() {
    return template.getMaxHp();
  }

  public Optional<Feat> getRacialFeat() {
    return racialFeat;
  }

  public LevelTemplate getTemplate() {
    return template;
  }

  public void setTemplate(String name) {
    this.template = readTemplate(name);
  }

  public boolean hasAbilityIncrease() {
    return abilityIncrease.isPresent()
        && abilityIncrease.get() != Ability.UNKNOWN
        && abilityIncrease.get() != Ability.NONE;
  }

  public String summary(int number) {
    String result = "Level " + number + ": " + template.getName();
    if (hp != 0) {
      result += ", " + hp + " hp";
    }
    if (abilityIncrease.isPresent()
        && abilityIncrease.get() != Ability.UNKNOWN
        && abilityIncrease.get() != Ability.NONE) {
      result += ", +1 " + abilityIncrease.get().getShortName();
    }
    if (feat.isPresent()) {
      result += ", " + feat.get().getTitle();
    }
    if (racialFeat.isPresent()) {
      result += ", " + racialFeat.get().getTitle();
    }
    if (classFeat.isPresent()) {
      result += ", " + classFeat.get().getTitle();
    }

    return result;
  }

  @Override
  public String toString() {
    return template.getName();
  }

  @Override
  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_TEMPLATE, template.getName());
    data.put(FIELD_HP, hp);
    if (abilityIncrease.isPresent()) {
      data.put(FIELD_ABILITY_INCREASE, abilityIncrease.get().getName());
    }
    if (feat.isPresent()) {
      data.put(FIELD_FEAT, feat.get().write());
    }
    if (racialFeat.isPresent()) {
      data.put(FIELD_RACIAL_FEAT, racialFeat.get().write());
    }
    if (classFeat.isPresent()) {
      data.put(FIELD_CLASS_FEAT, classFeat.get().write());
    }

    return data;
  }

  private static Multiset<String> countedNames(List<Level> levels) {
    Multiset<String> names = HashMultiset.create();
    for (Level level : levels) {
      names.add(level.getTemplate().getName());
    }

    return names;
  }

  public static Level read(Map<String, Object> data, int number) {
    LevelTemplate template = Templates.get().getOrCreateLevel(Values.get(data, FIELD_TEMPLATE, ""));
    int hp = (int) Values.get(data, FIELD_HP, 0);

    Optional<Ability> abilityIncrease = Values.get(data, FIELD_ABILITY_INCREASE, "").isEmpty()
        ? Optional.empty()
        : Optional.of(Ability.fromName(Values.get(data, FIELD_ABILITY_INCREASE, "")));
    Optional<Feat> feat = Values.has(data, FIELD_FEAT) ?
        Optional.of(Feat.read(Values.get(data, FIELD_FEAT), "Level " + number))
        : Optional.empty();
    Optional<Feat> racialFeat = Values.has(data, FIELD_RACIAL_FEAT) ?
        Optional.of(Feat.read(Values.get(data, FIELD_RACIAL_FEAT), "Level " + number))
        : Optional.empty();
    Optional<Feat> classFeat = Values.has(data, FIELD_CLASS_FEAT) ?
        Optional.of(Feat.read(Values.get(data, FIELD_CLASS_FEAT), "Level " + number))
        : Optional.empty();
    return new Level(template, hp, abilityIncrease, feat, racialFeat, classFeat);
  }

  private static LevelTemplate readTemplate(String name) {
    Optional<LevelTemplate> template = Templates.get().getLevelTemplates().get(name);
    if (template.isPresent()) {
      return template.get();
    }

    return new LevelTemplate(LevelTemplate.defaultProto(), name, 1);
  }

  public static String summarized(List<Level> levels) {
    Multiset<String> summarized = Level.countedNames(levels);
    List<String> names = new ArrayList<>();
    for (String name : summarized.elementSet()) {
      names.add(name + " " + summarized.count(name));
    }

    return Strings.COMMA_JOINER.join(names);
  }
}
