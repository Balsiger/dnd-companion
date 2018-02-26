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

package net.ixitxachitls.companion.data.values;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Creatures;
import net.ixitxachitls.companion.data.enums.BattleStatus;
import net.ixitxachitls.companion.proto.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Representation of a battle in a campaign.
 */
public class Battle {
  public static final String DEFAULT_MONSTER_NAME = "Monsters";

  private final Campaign campaign;
  private int number;
  private int turn;
  private BattleStatus status;
  private Optional<String> lastMonsterName = Optional.absent();
  private List<String> creatureIds = new ArrayList<>();
  private int currentCreatureIndex;

  public Battle(Campaign campaign) {
    this(campaign, 0, BattleStatus.ENDED, 0, Collections.emptyList(), 0);
  }

  private Battle(Campaign campaign, int number, BattleStatus status, int turn,
                 List<String> creatureIds, int currentCreatureIndex) {
    this.campaign = campaign;
    this.number = number;
    this.status = status;
    this.turn = turn;
    this.currentCreatureIndex = currentCreatureIndex;
    this.creatureIds.addAll(creatureIds);
  }

  public BattleStatus getStatus() {
    return status;
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

  public void setup() {
    status = BattleStatus.STARTING;
    number++;
    store();
  }

  public void start() {
    currentCreatureIndex = 0;
    turn = 0;
    creatureIds = obtainCreatureIds();
    status = BattleStatus.SURPRISED;
    store();
  }

  public List<String> obtainCreatureIds() {
    // If the battle is ongoing, we don't sort ids anymore, as initative cannot change
    // anymore (but the order can change by waiting).
    if (status != BattleStatus.STARTING && status != BattleStatus.ENDED) {
      return creatureIds;
    }

    List<BaseCreature> creatures = new ArrayList<>();
    for (String id : campaign.getCreatureIds().getValue()) {
      creatures.add(Creatures.getCreature(id).getValue().get());
    }
    for (String id : campaign.getCharacterIds().getValue()) {
      creatures.add(Characters.getCharacter(id).getValue().get());
    }

    creatures.sort(new InitiativeComparator());

    return creatures.stream().map(BaseCreature::getCreatureId).collect(Collectors.toList());
  }

  public void creatureDone() {
    currentCreatureIndex++;
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

  private void store() {
    campaign.store();
  }

  public void end() {
    status = BattleStatus.ENDED;

    for (String creatureId : campaign.getCreatureIds().getValue()) {
      Creatures.remove(creatureId);
    }

    lastMonsterName = Optional.absent();
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

  public Data.CampaignProto.Battle toProto() {
    Data.CampaignProto.Battle.Builder proto = Data.CampaignProto.Battle.newBuilder()
        .setStatus(status.toProto())
        .setTurn(turn)
        .setNumber(number)
        .addAllCreatureId(creatureIds)
        .setCurrentCreatureIndex(currentCreatureIndex);

    return proto.build();
  }

  public static Battle fromProto(Campaign campaign, Data.CampaignProto.Battle proto) {
    return new Battle(campaign, proto.getNumber(), BattleStatus.fromProto(proto.getStatus()),
        proto.getTurn(), proto.getCreatureIdList(), proto.getCurrentCreatureIndex());
  }

  public boolean currentIsLast() {
    return currentCreatureIndex >= creatureIds.size() - 1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Battle battle = (Battle) o;

    if (number != battle.number) return false;
    if (turn != battle.turn) return false;
    if (status != battle.status) return false;
    if (currentCreatureIndex != battle.currentCreatureIndex) return false;
    if (creatureIds.equals(battle.creatureIds)) return false;
    return lastMonsterName.equals(battle.lastMonsterName);
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

  private static class InitiativeComparator implements Comparator<BaseCreature> {
    @Override
    public int compare(BaseCreature first, BaseCreature second) {
      int compare = Integer.compare(second.getInitiative(), first.getInitiative());
      if (compare != 0) {
        return compare;
      }

      compare = Integer.compare(second.getDexterity(), first.getDexterity());
      if (compare != 0) {
        return compare;
      }

      compare = Integer.compare(second.getInitiativeRandom(), first.getInitiativeRandom());
      if (compare != 0) {
        return compare;
      }

      return first.getName().compareTo(second.getName());
    }
  }
}
