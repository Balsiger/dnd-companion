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

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;

import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Monster;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.net.CompanionSubscriber;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Character represenatation.
 */
public class Character extends StoredEntry<Data.CharacterProto> implements Comparable<Character> {
  public static final String TYPE = "characters";
  public static final String TABLE_LOCAL = TYPE + "_local";
  public static final String TABLE_REMOTE = TYPE + "_remote";
  private static final String TAG = "Characters";
  public static final int NO_INITIATIVE = 200;
  private static final int MAX_HISTORY = 20;

  private String campaignId = "";
  private String playerName = "";
  private Optional<Monster> mRace = Optional.absent();
  private Gender mGender = Gender.UNKNOWN;
  private int strength;
  private int constitution;
  private int dexterity;
  private int intelligence;
  private int wisdom;
  private int charisma;
  private List<Level> mLevels = new ArrayList<>();
  private int initiative = NO_INITIATIVE;
  private int battleNumber = 0;
  private List<Character.TimedCondition> conditions = new ArrayList<>();
  private List<Character.TimedCondition> conditionsHistory = new ArrayList<>();

  public Character(long id, String name, String campaignId, boolean local) {
    super(id, Settings.get().getAppId() + "-" + id, name, local,
        local ? DataBaseContentProvider.CHARACTERS_LOCAL
            : DataBaseContentProvider.CHARACTERS_REMOTE);
    this.campaignId = campaignId;
  }

  public Optional<TimedCondition> getHistoryCondition(String text) {
    for (TimedCondition condition : conditionsHistory) {
      if (text.equalsIgnoreCase(condition.text)) {
        return Optional.of(condition);
      }
    }

    return Optional.absent();
  }

  public Optional<Character> refresh() {
    return Characters.get(isLocal()).get(entryId);
  }

  public String getCampaignId() {
    return campaignId;
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

  public void setCampaignId(String campaignId) {
    this.campaignId = campaignId;
    store();
  }

  public void setRace(String name) {
    mRace = Entries.get().getMonsters().get(name);
    store();
  }

  public void setGender(Gender gender) {
    mGender = gender;
    store();
  }

  public int getStrength() {
    return strength;
  }

  public void setStrength(int strength) {
    if (this.strength != strength) {
      this.strength = strength;
      store();
    }
  }

  public int getConstitution() {
    return constitution;
  }

  public void setConstitution(int constitution) {
    if (this.constitution != constitution) {
      this.constitution = constitution;
      store();
    }
  }

  public int getDexterity() {
    return dexterity;
  }

  public void setDexterity(int dexterity) {
    if (this.dexterity != dexterity) {
      this.dexterity = dexterity;
      store();
    }
  }

  public int getIntelligence() {
    return intelligence;
  }

  public void setIntelligence(int intelligence) {
    if (this.intelligence != intelligence) {
      this.intelligence = intelligence;
      store();
    }
  }

  public int getWisdom() {
    return wisdom;
  }

  public void setWisdom(int wisdom) {
    if (this.wisdom != wisdom) {
      this.wisdom = wisdom;
      store();
    }
  }

  public int getCharisma() {
    return charisma;
  }

  public void setCharisma(int charisma) {
    if (this.charisma != charisma) {
      this.charisma = charisma;
      store();
    }
  }

  public int getBattleNumber() {
    return battleNumber;
  }

  public boolean hasInitiative() {
    Optional<Campaign> campaign = Campaigns.get(!isLocal()).getCampaign(getCampaignId());
    return initiative != NO_INITIATIVE
        && campaign.isPresent()
        && battleNumber == campaign.get().getBattle().getNumber();
  }

  public int getInitiative() {
    return initiative;
  }

  public int initiativeModifier() {
    // TODO: this needs treatment of things like feats and items.
    return Ability.modifier(dexterity);
  }

  public void setBattle(int initiative, int number) {
    this.initiative = initiative;
    this.battleNumber = number;
    this.conditions.clear();
    store();
  }

  public static Character createNew(String campaignId) {
    return new Character(0, "", campaignId, true);
  }

  public Optional<Level> getLevel(int number) {
    if (mLevels.size() > number) {
      return Optional.of(mLevels.get(number));
    }

    return Optional.absent();
  }

  public Collection<? extends TimedCondition> conditionsFor(String characterId) {
    List<TimedCondition> matchingConditions = new ArrayList<>();
    for (TimedCondition condition : conditions) {
      if (condition.characterIds.contains(characterId)) {
        matchingConditions.add(condition);
      }
    }

    return matchingConditions;
  }

  @Override
  public Data.CharacterProto toProto() {
    Data.CharacterProto.Builder proto = Data.CharacterProto.newBuilder()
        .setId(entryId)
        .setName(name)
        .setCampaignId(campaignId)
        .setPlayer(playerName)
        .setGender(mGender.toProto())
        .setAbilities(Data.CharacterProto.Abilities.newBuilder()
            .setStrength(strength)
            .setDexterity(dexterity)
            .setConstitution(constitution)
            .setIntelligence(intelligence)
            .setWisdom(wisdom)
            .setCharisma(charisma)
            .build())
        .setBattle(Data.CharacterProto.Battle.newBuilder()
            .setInitiative(initiative)
            .setNumber(battleNumber)
            .addAllTimedCondition(convertConditions(conditions))
            .build())
        .addAllTimedConditionHistory(convertConditions(conditionsHistory));

    if (mRace.isPresent()) {
      proto.setRace(mRace.get().getName());
    }

    for (Level level : mLevels) {
      proto.addLevel(level.toProto());
    }

    return proto.build();
  }

  private List<Data.CharacterProto.TimedCondition>
  convertConditions(List<Character.TimedCondition> conditions) {
    List<Data.CharacterProto.TimedCondition> protos = new ArrayList<>();

    for (Character.TimedCondition condition : conditions) {
      protos.add(condition.toProto());
    }

    return protos;
  }

  public static Character fromProto(long id, boolean local, Data.CharacterProto proto) {
    Character character = new Character(id, proto.getName(), proto.getCampaignId(), local);
    character.campaignId = proto.getCampaignId();
    character.entryId =
        proto.getId().isEmpty() ? Settings.get().getAppId() + "-" + id : proto.getId();
    character.mRace = Entries.get().getMonsters().get(proto.getRace());
    character.mGender = Gender.fromProto(proto.getGender());
    character.playerName = proto.getPlayer();
    character.strength = proto.getAbilities().getStrength();
    character.dexterity = proto.getAbilities().getDexterity();
    character.constitution = proto.getAbilities().getConstitution();
    character.intelligence = proto.getAbilities().getIntelligence();
    character.wisdom = proto.getAbilities().getWisdom();
    character.charisma = proto.getAbilities().getCharisma();
    character.initiative = proto.hasBattle() ? proto.getBattle().getInitiative() : NO_INITIATIVE;
    character.battleNumber = proto.getBattle().getNumber();
    character.conditions = convert(proto.getBattle().getTimedConditionList());
    character.conditionsHistory = convert(proto.getTimedConditionHistoryList());

    for (Data.CharacterProto.Level level : proto.getLevelList()) {
      character.mLevels.add(Character.fromProto(level));
    }

    if (character.playerName.isEmpty()) {
      character.playerName = Settings.get().getNickname();
    }

    return character;
  }

  private static List<TimedCondition> convert(List<Data.CharacterProto.TimedCondition> protos) {
    List<TimedCondition> conditions = new ArrayList<>();

    for (Data.CharacterProto.TimedCondition proto : protos) {
      conditions.add(TimedCondition.fromProto(proto));
    }

    return conditions;
  }

  public Gender getGender() {
    return mGender;
  }

  public void setLevel(int index, Level level) {
    if(mLevels.size() > index) {
      mLevels.set(index, level);
      store();
    } else {
      addLevel(level);
    }
  }

  public void addLevel(Character.Level level) {
    mLevels.add(level);
    store();
  }

  public ArrayList<String> levelSummaries() {
    ArrayList<String> summaries = new ArrayList<>();

    for (Character.Level level : mLevels) {
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
    for (Level level : mLevels) {
      names.add(level.getName());
    }

    return names;
  }

  public void addTimedCondition(Character.TimedCondition condition) {
    conditions.add(condition);
    conditionsHistory.add(condition);
    conditionsHistory =
        conditionsHistory.subList(0, Math.min(conditionsHistory.size(), MAX_HISTORY));
    store();
  }

  public static class TimedCondition extends DynamicEntry<Data.CharacterProto.TimedCondition> {

    private final int rounds;
    private final int endRound;
    private final ImmutableList<String> characterIds;
    private final String text;

    public TimedCondition(int rounds, int endRound, List<String> characterIds, String text) {
      super(text);

      this.rounds = rounds;
      this.endRound = endRound;
      this.characterIds = ImmutableList.copyOf(characterIds);
      this.text = text;
    }

    public int getRounds() {
      return rounds;
    }

    public ImmutableList<String> getCharacterIds() {
      return characterIds;
    }

    public String getText() {
      return text;
    }

    public int getEndRound() {
      return endRound;
    }

    @Override
    public Data.CharacterProto.TimedCondition toProto() {
      return Data.CharacterProto.TimedCondition.newBuilder()
          .setRounds(rounds)
          .setEndRound(endRound)
          .addAllCharacterId(characterIds)
          .setText(text)
          .build();
    }

    public static TimedCondition fromProto(Data.CharacterProto.TimedCondition proto) {
      return new TimedCondition(proto.getRounds(), proto.getEndRound(), proto.getCharacterIdList(),
          proto.getText());
    }
  }

  public static class Level extends DynamicEntry<Data.CharacterProto.Level> {

    private int mHp;
    private Ability mAbilityIncrease;

    public Level(String name) {
      super(name);
    }

    public int getHp() {
      return mHp;
    }

    public void setHp(int hp) {
      mHp = hp;
    }

    @Override
    public Data.CharacterProto.Level toProto() {
      return Data.CharacterProto.Level.newBuilder()
          .setName(name)
          .setHp(mHp)
          .setAbilityIncrease(mAbilityIncrease.toProto())
          .build();
    }

    public String summary() {
      List<String> parts = new ArrayList<>();
      if(mHp > 0) {
        parts.add(mHp + " hp");
      }

      if(hasAbilityIncrease()) {
        parts.add("+1 " + mAbilityIncrease.getShortName());
      }

      return name + (parts.isEmpty() ? "" : " (" + Strings.COMMA_JOINER.join(parts) + ")");
    }

    public boolean hasAbilityIncrease() {
      return mAbilityIncrease != Ability.NONE && mAbilityIncrease != Ability.UNKNOWN;
    }

    public Ability getAbilityIncrease() {
      return mAbilityIncrease;
    }

    public void setAbilityIncrease(Ability ability) {
      mAbilityIncrease = ability;
    }
  }

  public static Level fromProto(Data.CharacterProto.Level proto) {
    Level level = new Level(proto.getName());
    level.mHp = proto.getHp();
    level.mAbilityIncrease = Ability.fromProto(proto.getAbilityIncrease());

    return level;
  }

  public Optional<Bitmap> loadImage() {
    return Images.get(isLocal()).load(Character.TYPE, getCharacterId());
  };

  @Override
  public boolean store() {
    if (playerName.isEmpty()) {
      playerName = Settings.get().getNickname();
    }

    boolean changed = super.store();
    if (changed) {
      Characters.get(isLocal()).add(this);
      if (isLocal()) {
        CompanionSubscriber.get().publish(this);
      }
    }

    return changed;
  }

  public void publish() {
    Log.d(TAG, "publishing character " + getName());
    CompanionSubscriber.get().publish(this);
  }

  public List<String> conditionHistoryNames() {
    List<String> names = new ArrayList<>();

    for (TimedCondition condition : conditionsHistory) {
      names.add(condition.text);
    }

    return names;
  }

  @Override
  public int compareTo(@NonNull Character that) {
    int name = this.name.compareTo(that.name);
    if (name != 0) {
      return name;
    }

    return this.getCharacterId().compareTo(that.getCharacterId());
  }
}
