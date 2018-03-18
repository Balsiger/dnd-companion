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
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.data.values.Condition;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Character represenatation.
 */
public abstract class Character extends BaseCreature<Data.CharacterProto>
    implements Comparable<Character> {

  public static final String TYPE = "character";
  public static final String TABLE = "characters";
  public static final String TABLE_LOCAL = TABLE + "_local";
  public static final String TABLE_REMOTE = TABLE + "_remote";
  public static final int NO_INITIATIVE = 200;
  protected static final int MAX_HISTORY = 20;
  protected static final Random RANDOM = new Random();

  protected String playerName = "";
  protected List<Level> levels = new ArrayList<>();
  protected int xp = 0;
  protected List<Condition> conditionsHistory = new ArrayList<>();

  public Character(long id, String name, String campaignId, boolean local) {
    super(id, TYPE, name, local,
        local ? DataBaseContentProvider.CHARACTERS_LOCAL
            : DataBaseContentProvider.CHARACTERS_REMOTE, campaignId);
  }

  protected Character(long id, String name, String campaignId, boolean local, Uri dbUrl) {
    super(id, TYPE, name, local, dbUrl, campaignId);
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
    Optional<Campaign> campaign = getCampaign();
    if (campaign.isPresent() && campaign.get().getBattle().getNumber() != battleNumber) {
      initiative = NO_INITIATIVE;
    }

    return super.getInitiative();
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

  public int getXp() {
    return xp;
  }

  public Optional<Level> getLevel(int number) {
    if (levels.size() > number) {
      return Optional.of(levels.get(number));
    }

    return Optional.empty();
  }

  public Gender getGender() {
    return gender;
  }

  public int getLevel() {
    return levels.size();
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

  public List<Condition> getConditionsHistory() {
    return ImmutableList.copyOf(conditionsHistory);
  }

  public LocalCharacter asLocal() {
    IllegalStateException e = new IllegalStateException("Cannot access remote character as local!");
    Status.exception("Invalid local conversion", e);
    throw e;
  }

  public abstract void addXp(int xp);

  public static Optional<LocalCharacter> asLocal(Optional<Character> character) {
    if (!character.isPresent() || !character.get().isLocal()) {
      return Optional.empty();
    }

    return Optional.of((LocalCharacter) character.get());
  }

  public LiveData<Optional<Image>> loadImage() {
    return Images.get(isLocal()).getImage(Character.TABLE, getCharacterId());
  };

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

  private Multiset<String> countedLevelNames() {
    Multiset<String> names = HashMultiset.create();
    for (Level level : levels) {
      names.add(level.getName());
    }

    return names;
  }

  @Override
  public boolean store() {
    if (super.store()) {
      if (Characters.has(this)) {
        Characters.update(this);
      } else {
        Characters.add(this);
      }

      return true;
    }

    return false;
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
    return getName() + " (" + getCharacterId() + ")";
  }
}
