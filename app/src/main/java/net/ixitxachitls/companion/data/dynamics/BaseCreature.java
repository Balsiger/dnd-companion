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

import android.net.Uri;
import android.support.annotation.VisibleForTesting;

import com.google.protobuf.MessageLite;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Monster;
import net.ixitxachitls.companion.data.enums.Gender;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.data.values.Condition;
import net.ixitxachitls.companion.data.values.TargetedTimedCondition;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.rules.Conditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A creature representation, as the basis for creatures (monsters without levels) and
 * characters (playable monsters with levels).
 */
public abstract class BaseCreature<P extends MessageLite> extends StoredEntry<P> {
  private static final int NO_INITIATIVE = 200;

  protected String campaignId = "";
  protected Optional<Monster> race = Optional.empty();
  protected Gender gender = Gender.UNKNOWN;
  protected int strength;
  protected int constitution;
  protected int dexterity;
  protected int intelligence;
  protected int wisdom;
  protected int charisma;
  protected int hp;
  protected int maxHp;
  protected int nonlethalDamage;
  protected int initiative = NO_INITIATIVE;
  protected int initiativeRandom = 0;
  protected int battleNumber = 0;
  protected List<TargetedTimedCondition> initiatedConditions = new ArrayList<>();
  protected List<TimedCondition> affectedConditions = new ArrayList<>();

  protected BaseCreature(CompanionContext context, long id, String type, String name, boolean local, Uri dbUrl,
                         String campaignId) {
    super(context, id, type, name, local, dbUrl);
    this.campaignId = campaignId;
  }

  public String getCampaignId() {
    return campaignId;
  }

  public Optional<Campaign> getCampaign() {
    return context.campaigns().getCampaign(campaignId).getValue();
  }

  public Optional<Battle> getBattle() {
    return getCampaign().map(Campaign::getBattle);
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

  public void addInitiatedCondition(TargetedTimedCondition condition) {
    if (!condition.getTimedCondition().getSourceId().equals(getCreatureId())) {
      throw new IllegalArgumentException("source id does not match creature id!");
    }

    if (isLocal()) {
      initiatedConditions.add(condition);
      store();

      // Send the condition to all affected characters/creatures.
      for (String id : condition.getTargetIds()) {
        Optional<? extends BaseCreature> creature = context.creatures().getCreatureOrCharacter(id);
        if (creature.isPresent()) {
          creature.get().addAffectedCondition(condition.getTimedCondition());
        } else {
          Status.error("Cannot affect " + id + " with condition " + condition);
        }
      }

    } else {
      Status.error("Cannot add initiated condition to remote character or creature.");
    }
  }

  public void addAffectedCondition(TimedCondition condition) {
    if (isLocal()) {
      affectedConditions.add(condition);
      store();
    } else {
      context.messenger().send(this, condition);
    }
  }

  protected void maybeAddAffectedCondition(TimedCondition condition) {
    if (!hasAffectedCondition(condition.getName())) {
      addAffectedCondition(condition);
    }
  }

  public void removeInitiatedCondition(String name) {
    for (Iterator<TargetedTimedCondition> i = initiatedConditions.iterator(); i.hasNext(); ) {
      TargetedTimedCondition condition = i.next();
      if (condition.getName().equals(name)) {
        i.remove();
        store();

        for (String targetId : condition.getTargetIds()) {
          Optional<? extends BaseCreature> creature =
              context.creatures().getCreatureOrCharacter(targetId);
          if (creature.isPresent()) {
            creature.get().removeAffectedCondition(name, getCreatureId());
          } else {
            Status.error("Cannot get creature to remove condition " + name + " from: "
                + Status.nameFor(targetId));
          }
        }

        break;
      }
    }
  }

  public boolean hasAffectedCondition(String name) {
    for (Iterator<TimedCondition> i = affectedConditions.iterator(); i.hasNext(); ) {
      if (i.next().getName().equals(name)) {
        return true;
      }
    }

    return false;
  }

  public void removeAffectedCondition(String name, String sourceId) {
    if (isLocal()) {
      for (Iterator<TimedCondition> i = affectedConditions.iterator(); i.hasNext(); ) {
        TimedCondition condition = i.next();
        if (condition.getName().equals(name) && condition.getSourceId().equals(sourceId)) {
          i.remove();
          store();
          break;
        }
      }
    } else {
      context.messenger().sendDeletion(name, sourceId, this);
    }
  }

  public void removeAllAffectedConditions(String name) {
    affectedConditions.removeIf(timedCondition -> timedCondition.getName().equals(name));
    store();
  }

  public List<TargetedTimedCondition> getInitiatedConditions() {
    return Collections.unmodifiableList(initiatedConditions);
  }

  public List<TimedCondition> getAffectedConditions() {
    return Collections.unmodifiableList(affectedConditions);
  }

  public List<Condition> getActiveConditions(Battle battle) {
    return affectedConditions.stream()
        .filter(c -> c.active(battle))
        .map(TimedCondition::getCondition)
        .collect(Collectors.toList());
  }

  public int getHp() {
    return hp;
  }

  public void setHp(int hp) {
    if (isLocal()) {
      this.hp = hp;
      if (this.hp > maxHp) {
        this.hp = maxHp;
      }

      if (hp <= -10) {
        maybeAddAffectedCondition(new TimedCondition(Conditions.DEAD, getCreatureId()));
        removeAffectedCondition(Conditions.DISABLED.getName(), getCreatureId());
        removeAffectedCondition(Conditions.DYING.getName(), getCreatureId());
        removeAffectedCondition(Conditions.UNCONSCIOUS.getName(), getCreatureId());
      } else if (hp < 0) {
        maybeAddAffectedCondition(new TimedCondition(Conditions.DYING, getCreatureId()));
        maybeAddAffectedCondition(new TimedCondition(Conditions.UNCONSCIOUS, getCreatureId()));
        removeAffectedCondition(Conditions.DISABLED.getName(), getCreatureId());
        removeAffectedCondition(Conditions.DEAD.getName(), getCreatureId());
      } else if (hp == 0) {
        removeAffectedCondition(Conditions.DEAD.getName(), getCreatureId());
        removeAffectedCondition(Conditions.DYING.getName(), getCreatureId());
        removeAffectedCondition(Conditions.UNCONSCIOUS.getName(), getCreatureId());
        maybeAddAffectedCondition(new TimedCondition(Conditions.DISABLED, getCreatureId()));
      } else {
        removeAffectedCondition(Conditions.DEAD.getName(), getCreatureId());
        removeAffectedCondition(Conditions.DISABLED.getName(), getCreatureId());
        removeAffectedCondition(Conditions.DYING.getName(), getCreatureId());
        removeAffectedCondition(Conditions.UNCONSCIOUS.getName(), getCreatureId());
      }

      store();
    }
  }

  public void addHp(int hp) {
    setHp(this.hp + hp);
  }

  public int getMaxHp() {
    return maxHp;
  }

  public void setMaxHp(int maxHp) {
    this.maxHp = maxHp;
  }

  public int getNonlethalDamage() {
    return nonlethalDamage;
  }

  public void setNonlethalDamage(int nonlethalDamage) {
    if (nonlethalDamage >= 0) {
      this.nonlethalDamage = nonlethalDamage;
    } else {
      this.nonlethalDamage = 0;
    }

    if (this.nonlethalDamage < hp) {
      removeAffectedCondition(Conditions.STAGGERED.getName(), getCreatureId());
      removeAffectedCondition(Conditions.UNCONSCIOUS.getName(), getCreatureId());
    } else if (this.nonlethalDamage == hp && hp > 0) {
      maybeAddAffectedCondition(new TimedCondition(Conditions.STAGGERED, getCreatureId()));
      removeAffectedCondition(Conditions.UNCONSCIOUS.getName(), getCreatureId());
    } else {
      maybeAddAffectedCondition(new TimedCondition(Conditions.UNCONSCIOUS, getCreatureId()));
      removeAffectedCondition(Conditions.STAGGERED.getName(), getCreatureId());
    }

    store();
  }

  public void addNonlethalDamage(int damage) {
    setNonlethalDamage(nonlethalDamage + damage);
  }

  public Entry.CreatureProto toCreatureProto() {
    Entry.CreatureProto.Builder proto = Entry.CreatureProto.newBuilder()
        .setId(entryId)
        .setName(name)
        .setCampaignId(campaignId)
        .setGender(gender.toProto())
        .setAbilities(Entry.CreatureProto.Abilities.newBuilder()
            .setStrength(strength)
            .setDexterity(dexterity)
            .setConstitution(constitution)
            .setIntelligence(intelligence)
            .setWisdom(wisdom)
            .setCharisma(charisma)
            .build())
        .setBattle(Entry.CreatureProto.Battle.newBuilder()
            .setInitiative(initiative)
            .setNumber(battleNumber)
            .build())
        .addAllInitiatedCondition(initiatedConditions.stream()
            .map(TargetedTimedCondition::toProto)
            .collect(Collectors.toList()))
        .addAllAffectedCondition(affectedConditions.stream()
            .map(TimedCondition::toProto)
            .collect(Collectors.toList()))
        .setHp(hp)
        .setHpMax(maxHp)
        .setNonlethalDamage(nonlethalDamage);

    if (race.isPresent()) {
      proto.setRace(race.get().getName());
    }

    return proto.build();
  }

  protected void fromProto(Entry.CreatureProto proto) {
    campaignId = proto.getCampaignId();
    entryId = proto.getId().isEmpty() ? context.settings().getAppId() + "-" + id : proto.getId();
    race = Entries.get().getMonsters().get(proto.getRace());
    gender = Gender.fromProto(proto.getGender());
    strength = proto.getAbilities().getStrength();
    dexterity = proto.getAbilities().getDexterity();
    constitution = proto.getAbilities().getConstitution();
    intelligence = proto.getAbilities().getIntelligence();
    wisdom = proto.getAbilities().getWisdom();
    charisma = proto.getAbilities().getCharisma();
    initiative = proto.hasBattle() ? proto.getBattle().getInitiative() : NO_INITIATIVE;
    battleNumber = proto.getBattle().getNumber();
    initiatedConditions = proto.getInitiatedConditionList().stream()
        .map(TargetedTimedCondition::fromProto)
        .collect(Collectors.toList());
    affectedConditions = proto.getAffectedConditionList().stream()
        .map(TimedCondition::fromProto)
        .collect(Collectors.toList());
    hp = proto.getHp();
    maxHp = proto.getHpMax();
    nonlethalDamage = proto.getNonlethalDamage();
  }

  @VisibleForTesting
  public List<String> affectedConditions() {
    return affectedConditions.stream().map(TimedCondition::getName).collect(Collectors.toList());
  }
}
