/*
 * Copyright (c) 2017-{2018} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
 *
 * The Roleplay Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Roleplay Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Roleplay Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.dynamics;

import android.support.annotation.CallSuper;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.data.values.Condition;
import net.ixitxachitls.companion.data.values.TargetedTimedCondition;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.stream.Collectors;

/**
 * A local instanace of a character.
 */
public class LocalCharacter extends Character {

  public static final String TABLE = Character.TABLE + "_local";

  public LocalCharacter(CompanionContext companionContext, long id, String name,
                        String campaignId) {
    super(companionContext, id, name, campaignId, true,
        DataBaseContentProvider.CHARACTERS_LOCAL);
  }

  public static LocalCharacter createNew(CompanionContext companionContext, String campaignId) {
    return new LocalCharacter(companionContext, 0, "", campaignId);
  }

  public LocalCharacter asLocal() {
    return this;
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

  public void setBattle(int initiative, int number) {
    this.initiative = initiative;
    this.initiativeRandom = RANDOM.nextInt(100_000);
    this.battleNumber = number;

    // TODO(merlin): If we want to support long running conditions outside of battle, this has to
    // change.
    this.initiatedConditions.clear();
    this.affectedConditions.clear();

    store();
  }

  public void setXp(int xp) {
    this.xp = xp;
  }

  @Override
  public void addXp(int xp) {
    this.xp += xp;
    store();
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

  public void publish() {
    Status.log("publishing character " + this);
    context.messenger().send(this);
  }

  @Override @CallSuper
  public void delete() {
    super.delete();
    context.messenger().sendDeletion(this);
  }

  @Override
  public boolean store() {
    if (playerName.isEmpty()) {
      playerName = context.settings().getNickname();
    }

    if (super.store()) {
      context.messenger().send(this);

      return true;
    }

    return false;
  }

  @Override
  public String toString() {
    return super.toString() + "/local";
  }

  public static LocalCharacter fromProto(CompanionContext companionContext, long id,
                                         Entry.CharacterProto proto) {
    LocalCharacter character = new LocalCharacter(companionContext, id,
        proto.getCreature().getName(), proto.getCreature().getCampaignId());
    character.fromProto(proto.getCreature());
    character.playerName = proto.getPlayer();
    character.conditionsHistory = proto.getConditionHistoryList().stream()
        .map(Condition::fromProto)
        .collect(Collectors.toList());
    character.xp = proto.getXp();

    for (Entry.CharacterProto.Level level : proto.getLevelList()) {
      character.levels.add(Character.fromProto(level));
    }

    if (character.playerName.isEmpty()) {
      character.playerName = companionContext.settings().getNickname();
    }

    return character;
  }
}
