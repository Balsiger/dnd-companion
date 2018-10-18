/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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

package net.ixitxachitls.companion.data.values;

import android.support.annotation.CallSuper;

import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Creature;
import net.ixitxachitls.companion.data.documents.Document;
import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.enums.BattleStatus;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.rules.Conditions;
import net.ixitxachitls.companion.ui.dialogs.StartBattleDialog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Representation of a battle in a campaign.
 */
public class Encounter extends Document<Encounter> {
  public static final String DEFAULT_MONSTER_NAME = "Monsters";

  private static final Factory FACTORY = new Factory();
  private static final String PATH = "battles";
  private static final String FIELD_NUMBER = "number";
  private static final String FIELD_TURN = "turn";
  private static final String FIELD_STATUS = "status";
  private static final String FIELD_CREATURES = "creatures";
  private static final String FIELD_SURPRISED = "surprised";
  private static final String FIELD_CURRENT = "current";

  private Optional<Campaign> campaign;
  private int number;
  private int turn;
  private BattleStatus status = BattleStatus.ENDED;
  private Optional<String> lastMonsterName = Optional.empty();
  private List<String> creatureIds = new ArrayList<>();
  private List<String> surprisedCreatureIds = new ArrayList<>();
  private int currentCreatureIndex;

  public static Encounter getOrCreate(CompanionContext context, String campaignId) {
    Encounter encounter = Document.getOrCreate(FACTORY, context, campaignId + "/" + PATH);
    encounter.campaign = context.campaigns().get(campaignId);

    return encounter;
  }

  /*
  public Battle(FSCampaign campaign) {
    this(campaign, 0, BattleStatus.ENDED, 0, Collections.emptyList(),
        Collections.emptyList(), 0);
  }

  private Battle(FSCampaign campaign, int number, BattleStatus status, int turn,
                 List<String> creatureIds, List<String> surprisedCreatureIds,
                 int currentCreatureIndex) {
    this.campaign = campaign;
    this.number = number;
    this.status = status;
    this.turn = turn;
    this.currentCreatureIndex = currentCreatureIndex;
    this.creatureIds.addAll(creatureIds);
    this.surprisedCreatureIds.addAll(surprisedCreatureIds);
  }
  */

  public List<String> getCreatureIds() {
    return creatureIds;
  }

  public BattleStatus getStatus() {
    return status;
  }

  public boolean inBattle() {
    return status != BattleStatus.ENDED;
  }

  public boolean isEnded() {
    return status == BattleStatus.ENDED;
  }

  public boolean isStarting() {
    return status == BattleStatus.STARTING;
  }

  public boolean isSurprised() {
    return status == BattleStatus.SURPRISED;
  }

  public boolean isOngoing() {
    return status == BattleStatus.ONGOING;
  }

  public boolean isOngoingOrSurprised() {
    return isOngoing() || isSurprised();
  }

  public int getTurn() {
    return turn;
  }

  public Campaign getCampaign() {
    // TODO(merlin): needed? can it return an optional?
    assert campaign.isPresent();
    return campaign.get();
  }

  public int getNumber() {
    return number;
  }

  public String getCurrentCreatureId() {
    if (currentCreatureIndex < creatureIds.size()) {
      return creatureIds.get(currentCreatureIndex);
    }

    if (creatureIds.isEmpty()) {
      return "";
    }

    return creatureIds.get(0);
  }

  public boolean isCurrent(BaseCreature character) {
    return isEnded() || getCurrentCreatureId().equals(character.getCreatureId());
  }

  public boolean acted(String creatureId) {
    int index = creatureIds.indexOf(creatureId);
    return index >= 0 && index < currentCreatureIndex;
  }

  public boolean acting(String creatureId) {
    int index = creatureIds.indexOf(creatureId);
    return index >= 0 && index <= currentCreatureIndex;
  }

  public void setup() {
    number++;
    StartBattleDialog.newInstance(campaign.get().getId()).display();
    store();
  }

  public void starting(List<String> includedCreatureIds, List<String> surprisedCreatureIds) {
    status = BattleStatus.STARTING;
    this.creatureIds.clear();
    this.creatureIds.addAll(includedCreatureIds);
    this.surprisedCreatureIds.clear();
    this.surprisedCreatureIds.addAll(surprisedCreatureIds);

    store();
  }

  public void start() {
    currentCreatureIndex = -1;
    turn = 0;
    status = BattleStatus.SURPRISED;
    toNextUnsurprised();

    // Add flat-footed to all creatures.
    for (String creatureId : creatureIds) {
      Optional<? extends Creature> creature = campaign.get().creatures().get(creatureId);
      if (creature.isPresent()) {
        int rounds = surprisedCreatureIds.contains(creatureId) ? 1 : 0;
        creature.get().addAffectedCondition(
            new TimedCondition(Conditions.FLAT_FOOTED, creatureId, rounds));
      }
    }

    store();
  }

  public void addCreature(String creatureId) {
    creatureIds.add(creatureId);
  }

  public List<String> obtainCreatureIds() {
    // If the battle is ongoing, we don't sort ids anymore, as initiative cannot change
    // anymore (but the order can change by waiting).
    if (status != BattleStatus.STARTING && status != BattleStatus.ENDED) {
      return creatureIds;
    }

    List<Creature> creatures = new ArrayList<>();
    if (status == BattleStatus.STARTING) {
      for (String id : creatureIds) {
        Optional<? extends Creature> creature = campaign.get().creatures().get(id);
        if (creature.isPresent()) {
          creatures.add(creature.get());
        }
      }
    } else {
      creatures.addAll(campaign.get().creatures().getCampaignCreatures(campaign.get().getId()));
      creatures.addAll(campaign.get().characters().getCampaignCharacters(campaign.get().getId()));
    }

    creatures.sort(new InitiativeComparator());

    return creatures.stream().map(Creature::getId).collect(Collectors.toList());
  }

  public void creatureDone() {
    toNextUnsurprised();
  }

  private void toNextUnsurprised() {
    if (status == BattleStatus.SURPRISED) {
      while (currentCreatureIndex < creatureIds.size()) {
        currentCreatureIndex++;
        if (!surprisedCreatureIds.contains(getCurrentCreatureId())) {
          break;
        }
      }
    } else {
      currentCreatureIndex++;
    }

    if (currentCreatureIndex >= creatureIds.size()) {
      nextTurn();
    }

    store();
  }

  private void nextTurn() {
    currentCreatureIndex = 0;
    status = BattleStatus.ONGOING;
    turn++;
  }

  public void creatureWait() {
    if (currentCreatureIndex < creatureIds.size() - 1) {
      String id = creatureIds.remove(currentCreatureIndex);
      creatureIds.add(currentCreatureIndex + 1, id);
    }

    store();
  }

  public void end() {
    status = BattleStatus.ENDED;

    // Remove all monsters used in the battle.
    //for (String creatureId : campaign.getCreatureIds()) {
      //campaign.creatures().remove(creatureId);
    //}

    lastMonsterName = Optional.empty();
    store();
  }

  public void setLastMonsterName(String name) {
    this.lastMonsterName = Optional.of(name);
  }

  public String numberedMonsterName() {
    if (lastMonsterName.isPresent()) {
      lastMonsterName = Optional.of(numberName(lastMonsterName.get()));
    } else {
      lastMonsterName = Optional.of(DEFAULT_MONSTER_NAME);
    }

    return lastMonsterName.get();
  }

  private String numberName(String name) {
    String []parts = name.split(" ");
    if (parts.length == 1) {
      return name + " 2";
    }

    try {
      int number = Integer.parseInt(parts[parts.length - 1]) + 1;
      String result = "";
      for (int i = 0; i < parts.length - 1; i++) {
        result += parts[i] + " ";
      }

      result += number;
      return result;
    } catch (NumberFormatException e) {
      return name + " 2";
    }
  }

  public Optional<String> getLastMonsterName() {
    return lastMonsterName;
  }

  public Entry.CampaignProto.Battle toProto() {
    Entry.CampaignProto.Battle.Builder proto = Entry.CampaignProto.Battle.newBuilder()
        .setStatus(status.toProto())
        .setTurn(turn)
        .setNumber(number)
        .addAllCreatureId(creatureIds)
        .addAllSurprisedCreatureId(surprisedCreatureIds)
        .setCurrentCreatureIndex(currentCreatureIndex);

    return proto.build();
  }

  /*
  public static Battle fromProto(FSCampaign campaign, Entry.CampaignProto.Battle proto) {
    return new Battle(campaign, proto.getNumber(),
        BattleStatus.fromProto(proto.getStatus()), proto.getTurn(), proto.getCreatureIdList(),
        proto.getSurprisedCreatureIdList(), proto.getCurrentCreatureIndex());
  }
  */

  public boolean currentIsLast() {
    return currentCreatureIndex >= creatureIds.size() - 1;
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();
    number = (int) get(FIELD_NUMBER, 0);
    turn = (int) get(FIELD_TURN, 0);
    status = get(FIELD_STATUS, BattleStatus.ENDED);
    creatureIds = get(FIELD_CREATURES, creatureIds);
    surprisedCreatureIds = get(FIELD_SURPRISED, surprisedCreatureIds);
    currentCreatureIndex = (int) get(FIELD_CURRENT, currentCreatureIndex);
  }

  @Override
  protected Map<String, Object> write(Map<String, Object> data) {
    data.put(FIELD_NUMBER, number);
    data.put(FIELD_TURN, turn);
    data.put(FIELD_STATUS, status.toString());
    data.put(FIELD_CREATURES, creatureIds);
    data.put(FIELD_SURPRISED, surprisedCreatureIds);
    data.put(FIELD_CURRENT, currentCreatureIndex);

    return data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Encounter encounter = (Encounter) o;

    if (number != encounter.number) return false;
    if (turn != encounter.turn) return false;
    if (status != encounter.status) return false;
    if (currentCreatureIndex != encounter.currentCreatureIndex) return false;
    if (creatureIds.equals(encounter.creatureIds)) return false;
    return lastMonsterName.equals(encounter.lastMonsterName);
  }

  @Override
  public int hashCode() {
    int result = number;
    result = 31 * result + creatureIds.hashCode();
    result = 31 * result + status.hashCode();
    result = 31 * result + turn;
    result = 31 * result + currentCreatureIndex;
    result = 31 * result + lastMonsterName.hashCode();
    return result;
  }

  private static class InitiativeComparator implements Comparator<Creature> {
    @Override
    public int compare(Creature first, Creature second) {
      int compare = Integer.compare(second.getInitiative(), first.getInitiative());
      if (compare != 0) {
        return compare;
      }

      compare = Integer.compare(second.getDexterity(), first.getDexterity());
      if (compare != 0) {
        return compare;
      }

      /*
      compare = Integer.compare(second.getInitiativeRandom(), first.getInitiativeRandom());
      if (compare != 0) {
        return compare;
      }
      */

      return first.getName().compareTo(second.getName());
    }
  }

  private static class Factory implements DocumentFactory<Encounter> {
    @Override
    public Encounter create() {
      return new Encounter();
    }
  }
}
