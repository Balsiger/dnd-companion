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
import net.ixitxachitls.companion.data.templates.FeatTemplate;
import net.ixitxachitls.companion.data.templates.LevelTemplate;
import net.ixitxachitls.companion.data.values.Values;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A level a character has earned.
 */
public class Level extends NestedDocument {

  private static final String FIELD_TEMPLATE = "template";
  private static final String FIELD_HP = "hp";
  private static final String FIELD_ABILITY_INCREASE = "ability";
  private static final String FIELD_FEAT = "feat";

  private LevelTemplate template;
  private int hp;
  private Optional<Ability> abilityIncrease = Optional.empty();
  private Optional<FeatTemplate> feat = Optional.empty();

  public Level() {
    this(new LevelTemplate(), 0, Optional.empty(), Optional.empty());
  }

  public Level(String templateName, int hp, String abilityName, String featName) {
    Optional<LevelTemplate> template = Templates.get().getLevelTemplates().get(templateName);
    if (template.isPresent()) {
      this.template = template.get();
    } else {
      this.template = new LevelTemplate(LevelTemplate.defaultProto(), templateName, 0);
    }
    this.hp = hp;
    this.abilityIncrease = abilityName.isEmpty()
        ? Optional.empty() : Optional.of(Ability.fromName(abilityName));
    this.feat = Templates.get().getFeatTemplates().get(featName);
  }

  public Level(LevelTemplate template, int hp, Optional<Ability> abilityIncrease,
               Optional<FeatTemplate> feat) {
    this.template = template;
    this.hp = hp;
    this.abilityIncrease = abilityIncrease;
    this.feat = feat;
  }

  public Optional<FeatTemplate> getFeat() {
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
      data.put(FIELD_FEAT, feat.get().getName());
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

  public static Level read(Map<String, Object> data) {
    String templateName = Values.get(data, FIELD_TEMPLATE, "");
    LevelTemplate template = readTemplate(templateName);
    int hp = (int) Values.get(data, FIELD_HP, 0);
    Optional<Ability> abilityIncrease = Optional.empty();
    if (Values.has(data, FIELD_ABILITY_INCREASE)) {
      abilityIncrease = Optional.of(Ability.fromName(Values.get(data, FIELD_ABILITY_INCREASE, "")));
    }
    Optional<FeatTemplate> feat =
        Templates.get().getFeatTemplates().get(Values.get(data, FIELD_FEAT, ""));

    return new Level(template, hp, abilityIncrease, feat);
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
