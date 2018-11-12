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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A player character in the game.
 */
public class Character extends Creature<Character> implements Comparable<Character> {

  private static final DocumentFactory<Character> FACTORY = () -> new Character();
  private static final String PATH = "characters";

  private static final String FIELD_XP = "xp";
  private static final String FIELD_LEVEL = "level";
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
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
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
  }

  @Override
  @CallSuper
  protected Map<String, Object> write(Map<String, Object> data) {
    data = super.write(data);
    data.put(FIELD_XP, xp);
    data.put(FIELD_LEVEL, level);

    return data;
  }

  /*

  public void setCampaignId(String campaignId) {
    this.campaignId = campaignId;
  }

  public void setRace(String name) {
    race = Entries.get().getMonsters().get(name);
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public void setBattle(int initiative, int number) {
    this.initiative = initiative;
    this.initiativeRandom = RANDOM.nextInt(100_000);
    this.battleNumber = number;

    // Remove all round based conditions.
    initiatedConditions = initiatedConditions.stream()
        .filter(c -> c.getTimedCondition().hasEndDate() || c.getTimedCondition().isPermanent())
        .collect(Collectors.toList());
    affectedConditions = affectedConditions.stream()
        .filter(c -> c.hasEndDate() || c.isPermanent())
        .collect(Collectors.toList());

    store();
  }

  public void setXp(int xp) {
    this.xp = xp;
  }

  @Override
  public void addXp(int xp) {
    this.xp += xp;
    store();

    if (getCampaign().isPresent()) {
      context.histories().addedXp(xp, getCampaign().get().getDate(), getCharacterId(), campaignId);
    }
  }

  public void setLevel(int index, Level level) {
    if(levels.size() > index) {
      levels.set(index, level);
    } else {
      addLevel(level);
    }
  }

  public void addLevel(Character.Level level) {
    levels.add(level);
  }

  // TODO: remove this once we properly support level objets.
  public void setLevel(int level) {
    levels.clear();
    for (int i = 0; i < level; i++) {
      addLevel(new Character.Level("Barbarian"));
    }
  }

  @Override
  public void addInitiatedCondition(TargetedTimedCondition condition) {
    if (!condition.isPredefined()) {
      conditionsHistory.add(condition.getCondition());
      conditionsHistory =
          conditionsHistory.subList(0, Math.min(conditionsHistory.size(), MAX_HISTORY));
    }

    super.addInitiatedCondition(condition);
  }

   */

  public static class Level {
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

    @Override
    public Entry.CharacterProto.Level toProto() {
      return Entry.CharacterProto.Level.newBuilder()
          .setName(name)
          .setHp(hp)
          //.setAbilityIncrease(abilityIncrease.toProto())
          .build();
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
  }

  /*
  public ArrayList<String> levelSummaries() {
    ArrayList<String> summaries = new ArrayList<>();

    for (Character.Level level : levels) {
      summaries.add(level.summary());
    }

    return summaries;
  }

  public String summarizeLevels() {
    Multiset<String> countedNames = countedLevelNames();
    List<String> names = new ArrayList<>();
    for (String name : countedNames.elementSet()) {
      names.add(name + " " + countedNames.count(name));
    }

    return Strings.COMMA_JOINER.join(names);
  }

  private Multiset<String> countedLevelNames() {
    Multiset<String> names = HashMultiset.create();
    for (Level level : levels) {
      names.add(level.getName());
    }

    return names;
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

  @Override
  public boolean store() {
    if (super.store()) {
      if (context.characters().has(this)) {
        context.characters().update(this);
      } else {
        context.characters().add(this);
        if (getCampaign().isPresent()) {
          context.histories().created(getName(), getCampaign().get().getDate(),
              getCharacterId(), getCampaignId());
        }
      }
      return true;
    }

    return false;
  }

  @Override
  public int compareTo(Character that) {
    int name = this.name.compareTo(that.name);
    if (name != 0) {
      return name;
    }

    return this.getCharacterId().compareTo(that.getCharacterId());
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
