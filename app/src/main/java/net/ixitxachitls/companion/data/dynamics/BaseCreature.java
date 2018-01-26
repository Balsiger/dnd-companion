/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

import android.net.Uri;

import com.google.common.base.Optional;
import com.google.protobuf.MessageLite;

import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Monster;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.proto.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A creature representation, as the basis for creatures (monsters without levels) and
 * characters (playable monsters with levels).
 */
public abstract class BaseCreature<P extends MessageLite> extends StoredEntry<P> {
  public static final int NO_INITIATIVE = 200;

  protected String campaignId = "";
  protected Optional<Monster> mRace = Optional.absent();
  protected Gender gender = Gender.UNKNOWN;
  protected int strength;
  protected int constitution;
  protected int dexterity;
  protected int intelligence;
  protected int wisdom;
  protected int charisma;
  protected int initiative = NO_INITIATIVE;
  protected int initiativeRandom = 0;
  protected int battleNumber = 0;
  protected List<Character.TimedCondition> conditions = new ArrayList<>();

  protected BaseCreature(long id, String type, String name, boolean local, Uri dbUrl,
                         String campaignId) {
    super(id, type, name, local, dbUrl);

    this.campaignId = campaignId;
  }

  public String getCampaignId() {
    return campaignId;
  }

  public String getCreatureId() {
    return entryId;
  }

  public int getInitiative() {
    return initiative;
  }

  public int getInitiativeRandom() {
    return initiativeRandom;
  }

  public int getStrength() {
    return strength;
  }

  public int getConstitution() {
    return constitution;
  }

  public int getDexterity() {
    return dexterity;
  }

  public int getIntelligence() {
    return intelligence;
  }

  public int getWisdom() {
    return wisdom;
  }

  public int getCharisma() {
    return charisma;
  }

  public Data.CreatureProto toCreatureProto() {
    Data.CreatureProto.Builder proto = Data.CreatureProto.newBuilder()
        .setId(entryId)
        .setName(name)
        .setCampaignId(campaignId)
        .setGender(gender.toProto())
        .setAbilities(Data.CreatureProto.Abilities.newBuilder()
            .setStrength(strength)
            .setDexterity(dexterity)
            .setConstitution(constitution)
            .setIntelligence(intelligence)
            .setWisdom(wisdom)
            .setCharisma(charisma)
            .build())
        .setBattle(Data.CreatureProto.Battle.newBuilder()
            .setInitiative(initiative)
            .setNumber(battleNumber)
            .addAllTimedCondition(
                conditions.stream().map(tc -> tc.toProto()).collect(Collectors.toList()))
            .build());

    if (mRace.isPresent()) {
      proto.setRace(mRace.get().getName());
    }

    return proto.build();
  }

  protected void fromProto(Data.CreatureProto proto) {
    campaignId = proto.getCampaignId();
    entryId = proto.getId().isEmpty() ? Settings.get().getAppId() + "-" + id : proto.getId();
    mRace = Entries.get().getMonsters().get(proto.getRace());
    gender = Gender.fromProto(proto.getGender());
    strength = proto.getAbilities().getStrength();
    dexterity = proto.getAbilities().getDexterity();
    constitution = proto.getAbilities().getConstitution();
    intelligence = proto.getAbilities().getIntelligence();
    wisdom = proto.getAbilities().getWisdom();
    charisma = proto.getAbilities().getCharisma();
    initiative = proto.hasBattle() ? proto.getBattle().getInitiative() : NO_INITIATIVE;
    battleNumber = proto.getBattle().getNumber();
    conditions = proto.getBattle().getTimedConditionList()
        .stream()
        .map(Character.TimedCondition::fromProto)
        .collect(Collectors.toList());
  }
}
