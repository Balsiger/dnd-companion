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

import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.enums.BattleStatus;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.rules.Conditions;
import net.ixitxachitls.companion.ui.dialogs.StartBattleDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
  private Optional<String> lastMonsterName = Optional.empty();
  private List<String> creatureIds = new ArrayList<>();
  private List<String> surprisedCreatureIds = new ArrayList<>();
  private int currentCreatureIndex;

  public Battle(Campaign campaign) {
    this(campaign, 0, BattleStatus.ENDED, 0, Collections.emptyList(),
        Collections.emptyList(), 0);
  }

  private Battle(Campaign campaign, int number, BattleStatus status, int turn,
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

  public List<String> getCreatureIds() {
    return creatureIds;
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

  public Campaign getCampaign() {
    return campaign;
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
    StartBattleDialog.newInstance(campaign.getCampaignId()).display();
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
      Optional<? extends BaseCreature> creature =
          campaign.creatures().getCreatureOrCharacter(creatureId);
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
    // If the battle is ongoing, we don't sort ids anymore, as initative cannot change
    // anymore (but the order can change by waiting).
    if (status != BattleStatus.STARTING && status != BattleStatus.ENDED) {
      return creatureIds;
    }

    List<BaseCreature> creatures = new ArrayList<>();
    if (status == BattleStatus.STARTING) {
      for (String id : creatureIds) {
        Optional<? extends BaseCreature> creature = campaign.creatures().getCreatureOrCharacter(id);
        if (creature.isPresent()) {
          creatures.add(creature.get());
        }
      }
    } else {
      for (String id : campaign.getCreatureIds().getValue()) {
        if (campaign.creatures().getCreature(id).getValue().isPresent()) {
          creatures.add(campaign.creatures().getCreature(id).getValue().get());
        }
      }
      for (String id : campaign.getCharacterIds().getValue()) {
        if (campaign.data().characters().getCharacter(id).getValue().isPresent()) {
          creatures.add(campaign.data().characters().getCharacter(id).getValue().get());
        }
      }
    }

    creatures.sort(new InitiativeComparator());

    return creatures.stream().map(BaseCreature::getCreatureId).collect(Collectors.toList());
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

  private void store() {
    campaign.store();
  }

  public void end() {
    status = BattleStatus.ENDED;

    // Remove all monsters used in the battle.
    for (String creatureId : campaign.getCreatureIds().getValue()) {
      campaign.creatures().remove(creatureId);
    }

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

  public static Battle fromProto(Campaign campaign, Entry.CampaignProto.Battle proto) {
    return new Battle(campaign, proto.getNumber(),
        BattleStatus.fromProto(proto.getStatus()), proto.getTurn(), proto.getCreatureIdList(),
        proto.getSurprisedCreatureIdList(), proto.getCurrentCreatureIndex());
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
