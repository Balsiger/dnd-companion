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

import com.google.common.collect.ImmutableList;
import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.values.ConditionData;
import net.ixitxachitls.companion.rules.XP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A player character in the game.
 */
public class Character extends Creature<Character> implements Comparable<Character> {

  private static final DocumentFactory<Character> FACTORY = () -> new Character();
  private static final String PATH = "characters";

  private static final String FIELD_XP = "xp";
  private static final String FIELD_LEVEL = "level";
  private static final String FIELD_LEVELS = "levels";
  private static final int DEFAULT_LEVEL = 1;

  private User player;

  private int xp = 0;
  private int level;
  private List<Level> levels = new ArrayList<>();
  protected List<ConditionData> conditionsHistory = new ArrayList<>();

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

  public User getPlayer() {
    return player;
  }

  public boolean amPlayer() {
    return player == context.me();
  }

  public boolean amDM() {
    return getCampaignId().startsWith(context.me().getId());
  }

  // TODO(merlin): Move this into Document generally?
  public boolean canEdit() {
    return amPlayer() || amDM();
  }

  public int getXp() {
    return xp;
  }

  public void setXp(int xp) {
    this.xp = xp;
  }

  public void addXp(int number) {
    xp += number;
  }

  public int getLevel() {
    return levels.size();
  }

  public int getMaxLevel() {
    return XP.maxLevelForXp(xp);
  }

  @Override
  public int getMaxHp() {
    int hp = 0;
    for (Level level : levels) {
      hp += level.getHp() + getConstitutionModifier();
    }

    return hp;
  }

  public void setLevels(List<Level> levels) {
    this.levels = levels;
    store();
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public List<Level> getLevels() {
    return levels;
  }

  public void delete(Level level) {
    levels.remove(level);
    store();
  }

  public void addLevel() {
    levels.add(new Level());
    store();
  }

  public List<ConditionData> getConditionsHistory() {
    return ImmutableList.copyOf(conditionsHistory);
  }

  public int initiativeModifier() {
    // TODO: this needs treatment of things like feats and items.
    return Ability.modifier(dexterity);
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();
    xp = (int) get(FIELD_XP, 0);
    level = (int) get(FIELD_LEVEL, DEFAULT_LEVEL);
    levels = new ArrayList<>();
    for (Map<String, Object> level
        : get(FIELD_LEVELS, Collections.<Map<String, Object>>emptyList())) {
      levels.add(Level.read(level));
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

  //public static class Level {
    /*
    private int hp;
    private Ability abilityIncrease;

    public Level(String name) {
      super(name);
    }

    public int getHp() {
      return hp;
    }

    public void setHp(int hp) {
      this.hp = hp;
    }

    public String summary() {
      List<String> parts = new ArrayList<>();
      if(hp > 0) {
        parts.add(hp + " hp");
      }

      if(hasAbilityIncrease()) {
        parts.add("+1 " + abilityIncrease.getShortName());
      }

      return name + (parts.isEmpty() ? "" : " (" + Strings.COMMA_JOINER.join(parts) + ")");
    }

    public boolean hasAbilityIncrease() {
      return abilityIncrease != Ability.NONE && abilityIncrease != Ability.UNKNOWN;
    }

    public Ability getAbilityIncrease() {
      return abilityIncrease;
    }

    public void setAbilityIncrease(Ability ability) {
      abilityIncrease = ability;
    }
     */
  //}

  /*
  public ArrayList<String> levelSummaries() {
    ArrayList<String> summaries = new ArrayList<>();

    for (Character.Level level : levels) {
      summaries.add(level.summary());
    }

    return summaries;
  }

  public void copy() {
    Entry.CharacterProto.Builder proto = toProto().toBuilder();
    proto.setCreature(proto.getCreature().toBuilder()
        .setName("Copy of " + proto.getCreature().getName())
        .setId("")
        .build());

    Character copy = LocalCharacter.fromProto(context, 0, proto.build());
    copy.store();
}

   */

  @Override
  public int compareTo(Character that) {
    int name = this.getName().compareTo(that.getName());
    if (name != 0) {
      return name;
    }

    return this.getId().compareTo(that.getId());
  }
}
