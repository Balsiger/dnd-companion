/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Player Companion.
 *
 * The Player Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Player Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.dynamics;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.data.values.Condition;
import net.ixitxachitls.companion.data.values.TargetedTimedCondition;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.net.CompanionMessenger;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.rules.Conditions;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Character represenatation.
 */
public class Character extends BaseCreature<Data.CharacterProto> implements Comparable<Character> {

  public static final String TYPE = "character";
  public static final String TABLE = "characters";
  public static final String TABLE_LOCAL = TABLE + "_local";
  public static final String TABLE_REMOTE = TABLE + "_remote";
  public static final int NO_INITIATIVE = 200;
  private static final int MAX_HISTORY = 20;
  private static final Random RANDOM = new Random();

  private String playerName = "";
  private List<Level> levels = new ArrayList<>();
  private int xp = 0;
  private List<Condition> conditionsHistory = new ArrayList<>();

  public Character(long id, String name, String campaignId, boolean local) {
    super(id, TYPE, name, local,
        local ? DataBaseContentProvider.CHARACTERS_LOCAL
            : DataBaseContentProvider.CHARACTERS_REMOTE, campaignId);
  }

  public String getRace() {
    if(mRace.isPresent())
      return mRace.get().getName();

    return "";
  }

  public String getPlayerName() {
    return playerName;
  }

  public String getCharacterId() {
    return entryId;
  }

  @Override
  public int getInitiative() {
    Optional<Campaign> campaign = Campaigns.getCampaign(campaignId).getValue();
    if (campaign.isPresent() && campaign.get().getBattle().getNumber() != battleNumber) {
      initiative = NO_INITIATIVE;
    }

    return super.getInitiative();
  }

  public void setCampaignId(String campaignId) {
    this.campaignId = campaignId;
  }

  public void setRace(String name) {
    mRace = Entries.get().getMonsters().get(name);
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public void setStrength(int strength) {
    if (this.strength != strength) {
      this.strength = strength;
    }
  }

  public void setConstitution(int constitution) {
    if (this.constitution != constitution) {
      this.constitution = constitution;
    }
  }

  public void setDexterity(int dexterity) {
    if (this.dexterity != dexterity) {
      this.dexterity = dexterity;
    }
  }

  public void setIntelligence(int intelligence) {
    if (this.intelligence != intelligence) {
      this.intelligence = intelligence;
    }
  }

  public void setWisdom(int wisdom) {
    if (this.wisdom != wisdom) {
      this.wisdom = wisdom;
    }
  }

  public void setCharisma(int charisma) {
    if (this.charisma != charisma) {
      this.charisma = charisma;
    }
  }

  public int getBattleNumber() {
    return battleNumber;
  }

  public boolean hasInitiative() {
    Optional<Campaign> campaign = Campaigns.getCampaign(getCampaignId()).getValue();
    return initiative != NO_INITIATIVE
        && campaign.isPresent()
        && battleNumber == campaign.get().getBattle().getNumber();
  }

  public int initiativeModifier() {
    // TODO: this needs treatment of things like feats and items.
    return Ability.modifier(dexterity);
  }

  public void setBattle(int initiative, int number) {
    this.initiative = initiative;
    this.initiativeRandom = RANDOM.nextInt(100_000);
    this.battleNumber = number;

    // TODO(merlin): If we want to support long running conditions outside of battle, this has to
    // change.
    this.initiatedConditions.clear();

    // Clear all conditions exception surprised, as we only just added it.
    for (Iterator<TimedCondition> i = affectedConditions.iterator(); i.hasNext(); ) {
      if (!i.next().getName().equals(Conditions.SURPRISED.getName())) {
        i.remove();
      }
    }
    store();
  }

  public void clearInitiative() {
    this.initiative = NO_INITIATIVE;
    store();
  }

  public void setXp(int xp) {
    this.xp = xp;
  }

  public void addXp(int xp) {
    this.xp += xp;
    store();
  }

  public int getXp() {
    return xp;
  }

  public static Character createNew(String campaignId) {
    return new Character(0, "", campaignId, true);
  }

  public Optional<Level> getLevel(int number) {
    if (levels.size() > number) {
      return Optional.of(levels.get(number));
    }

    return Optional.absent();
  }

  @Override
  public Data.CharacterProto toProto() {
    Data.CharacterProto.Builder proto = Data.CharacterProto.newBuilder()
        .setCreature(toCreatureProto())
        .addAllConditionHistory(conditionsHistory.stream()
            .map(Condition::toProto).collect(Collectors.toList()))
        .setXp(xp);

    for (Level level : levels) {
      proto.addLevel(level.toProto());
    }

    return proto.build();
  }

  public static Character fromProto(long id, boolean local, Data.CharacterProto proto) {
    Character character = new Character(id, proto.getCreature().getName(),
        proto.getCreature().getCampaignId(), local);
    character.fromProto(proto.getCreature());
    character.playerName = proto.getPlayer();
    character.conditionsHistory = proto.getConditionHistoryList().stream()
        .map(Condition::fromProto)
        .collect(Collectors.toList());
    character.xp = proto.getXp();

    for (Data.CharacterProto.Level level : proto.getLevelList()) {
      character.levels.add(Character.fromProto(level));
    }

    if (character.playerName.isEmpty()) {
      character.playerName = Settings.get().getNickname();
    }

    return character;
  }

  public Gender getGender() {
    return gender;
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

  public int getLevel() {
    return levels.size();
  }

  // TODO: remove this once we properly support level objets.
  public void setLevel(int level) {
    levels.clear();
    for (int i = 0; i < level; i++) {
      addLevel(new Character.Level("Barbarian"));
    }
  }

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

  @Override
  public void addInitiatedCondition(TargetedTimedCondition condition) {
    if (!condition.isPredefined()) {
      conditionsHistory.add(condition.getCondition());
      conditionsHistory =
          conditionsHistory.subList(0, Math.min(conditionsHistory.size(), MAX_HISTORY));
    }

    super.addInitiatedCondition(condition);
  }

  public static class Level extends DynamicEntry<Data.CharacterProto.Level> {

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
    public Data.CharacterProto.Level toProto() {
      return Data.CharacterProto.Level.newBuilder()
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
  }

  public static Level fromProto(Data.CharacterProto.Level proto) {
    Level level = new Level(proto.getName());
    level.hp = proto.getHp();
    level.abilityIncrease = Ability.fromProto(proto.getAbilityIncrease());

    return level;
  }

  public LiveData<Optional<Image>> loadImage() {
    return Images.get(isLocal()).getImage(Character.TABLE, getCharacterId());
  };

  @Override
  public boolean store() {
    if (playerName.isEmpty()) {
      playerName = Settings.get().getNickname();
    }

    boolean changed = super.store();
    if (changed) {
      if (Characters.has(this)) {
        Characters.update(this);
      } else {
        Characters.add(this);
      }
      if (isLocal()) {
        CompanionMessenger.get().send(this);
      }
    }

    return changed;
  }

  public void publish() {
    Status.log("publishing character " + this);
    CompanionMessenger.get().send(this);
  }

  public List<Condition> getConditionsHistory() {
    return ImmutableList.copyOf(conditionsHistory);
  }

  @Override
  public int compareTo(@NonNull Character that) {
    int name = this.name.compareTo(that.name);
    if (name != 0) {
      return name;
    }

    return this.getCharacterId().compareTo(that.getCharacterId());
  }

  @Override
  public String toString() {
    return getName() + " (" + getCharacterId() + "/" + (isLocal() ? "local" : "remote") + ")";
  }
}
