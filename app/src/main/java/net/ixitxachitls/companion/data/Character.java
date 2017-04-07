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

package net.ixitxachitls.companion.data;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Character represenatation.
 */
public class Character extends StoredEntry<Data.CharacterProto> {
  public static final String TABLE = "characters";

  private String campaignId;
  private Optional<Monster> mRace = Optional.absent();
  private Gender mGender = Gender.UNKNOWN;
  private List<Level> mLevels = new ArrayList<>();

  public Character(long id, String name, String campaignId) {
    super(id, name, DataBaseContentProvider.CHARACTERS);
    this.campaignId = campaignId;
  }

  public String getCampaignId() {
    return campaignId;
  }

  public String getRace() {
    if(mRace.isPresent())
      return mRace.get().getName();

    return "";
  }

  public void setRace(String name) {
    mRace = Entries.get().getMonsters().get(name);
    store();
  }

  public void setGender(Gender gender) {
    mGender = gender;
    store();
  }

  public static Character createNew(String campaignId) {
    return new Character(0, "", campaignId);
  }

  public Optional<Level> getLevel(int number) {
    if (mLevels.size() > number) {
      return Optional.of(mLevels.get(number));
    }

    return Optional.absent();
  }

  @Override
  public Data.CharacterProto toProto() {
    Data.CharacterProto.Builder proto = Data.CharacterProto.newBuilder()
        .setName(name)
        .setCampaignId(campaignId)
        .setGender(mGender.toProto());

    if (mRace.isPresent()) {
      proto.setRace(mRace.get().getName());
    }

    for (Level level : mLevels) {
      proto.addLevel(level.toProto());
    }

    return proto.build();
  }

  public static Character fromProto(long id, Data.CharacterProto proto) {
    Character character = new Character(id, proto.getName(), proto.getCampaignId());
    character.campaignId = proto.getCampaignId();
    character.mRace = Entries.get().getMonsters().get(proto.getRace());
    character.mGender = Gender.fromProto(proto.getGender());
    for (Data.CharacterProto.Level level : proto.getLevelList()) {
      character.mLevels.add(Character.fromProto(level));
    }

    return character;
  }

  public static Optional<Character> load(long id) {
    if (id == 0) {
      return Optional.absent();
    }

    try {
      return Optional.of(fromProto(id, Data.CharacterProto.getDefaultInstance().getParserForType()
          .parseFrom(loadBytes(Entries.getContext(), id, DataBaseContentProvider.CHARACTERS))));
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
      return Optional.absent();
    }
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

  public static class Level extends Entry<Data.CharacterProto.Level> {

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
}
