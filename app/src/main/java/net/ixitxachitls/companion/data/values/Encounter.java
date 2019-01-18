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

import android.support.annotation.Nullable;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.Creature;
import net.ixitxachitls.companion.data.documents.CreatureCondition;
import net.ixitxachitls.companion.data.documents.CreatureConditions;
import net.ixitxachitls.companion.data.documents.Monster;
import net.ixitxachitls.companion.data.documents.Monsters;
import net.ixitxachitls.companion.data.enums.EncounterStatus;
import net.ixitxachitls.companion.rules.Conditions;
import net.ixitxachitls.companion.ui.dialogs.StartEncounterDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Representation of an encounter in a campaign.
 */
public class Encounter {
  public static final String DEFAULT_MONSTER_NAME = "Monsters";

  private static final String FIELD_NUMBER = "number";
  private static final String FIELD_TURN = "turn";
  private static final String FIELD_STATUS = "status";
  private static final String FIELD_CREATURES = "creatures";
  private static final String FIELD_CURRENT = "current";

  private Campaign campaign;

  private int number;
  private int turn;
  private EncounterStatus status = EncounterStatus.ENDED;
  private Optional<String> lastMonsterName = Optional.empty();
  private List<String> creatureIds = new ArrayList<>();
  private List<Creature> creatures = new ArrayList<>();
  private int currentCreatureIndex;

  public Encounter(Campaign campaign) {
    this.campaign = campaign;
  }

  public Campaign getCampaign() {
    return campaign;
  }

  public List<String> getCreatureIds() {
    return creatureIds;
  }

  public List<Creature> getCreatures() {
    return creatures;
  }

  public String getCurrentCreatureId() {
    if (currentCreatureIndex < 0 || currentCreatureIndex >= creatureIds.size()) {
      return "";
    }

    return creatureIds.get(currentCreatureIndex);
  }

  public int getCurrentCreatureIndex() {
    return currentCreatureIndex;
  }

  public Optional<String> getLastMonsterName() {
    return lastMonsterName;
  }

  public void setLastMonsterName(String name) {
    this.lastMonsterName = Optional.of(name);
  }

  public int getNumber() {
    return number;
  }

  public EncounterStatus getStatus() {
    return status;
  }

  public int getTurn() {
    return turn;
  }

  public boolean isEnded() {
    return status == EncounterStatus.ENDED;
  }

  public boolean isOngoing() {
    return status == EncounterStatus.ONGOING;
  }

  public boolean isOngoingOrSurprised() {
    return isOngoing() || isSurprised();
  }

  public boolean isStarting() {
    return status == EncounterStatus.STARTING;
  }

  public boolean isSurprised() {
    return status == EncounterStatus.SURPRISED;
  }

  public boolean acted(String creatureId) {
    int index = creatureIds.indexOf(creatureId);
    return index >= 0 && index < currentCreatureIndex;
  }

  public boolean acting(String creatureId) {
    int index = creatureIds.indexOf(creatureId);
    return index >= 0 && index <= currentCreatureIndex;
  }

  public boolean amCurrentPlayer() {
    return currentCreatureIndex >= 0
        && creatures.size() > currentCreatureIndex
        && creatures.get(currentCreatureIndex).amPlayer();
  }

  public void creatureDone() {
    toNextUnsurprised();
  }

  public void creatureWait() {
    if (currentCreatureIndex < creatureIds.size() - 1) {
      String id = creatureIds.remove(currentCreatureIndex);
      creatureIds.add(currentCreatureIndex + 1, id);

      // Remove the flat-footed condition, as the creature is acting now.
      campaign.getContext().conditions().deleteAll(Conditions.FLAT_FOOTED.getName(),
          creatureIds.get(currentCreatureIndex));
    }

    campaign.store();
  }

  public boolean currentIsLast() {
    return currentCreatureIndex >= creatureIds.size() - 1;
  }

  public void end() {
    lastMonsterName = Optional.empty();

    campaign.getContext().conditions().deleteRoundBasedCreatureConditions();
    campaign.getContext().monsters().deleteAllInCampaign(campaign.getId());
    status = EncounterStatus.ENDED;
    campaign.store();
  }

  public Optional<Character> firstPlayerCharacterNeedingInitiative() {
    for (Creature creature : creatures) {
      if (Characters.isCharacterId(creature.getId())) {
        Character character = (Character) creature;
        if (character.amPlayer() && !character.hasInitiative(number)) {
          return Optional.of(character);
        }
      }
    }

    return Optional.empty();
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

  public boolean inBattle() {
    return status != EncounterStatus.ENDED;
  }

  public boolean includes(String creatureId) {
    return creatureIds.contains(creatureId);
  }

  public String numberedMonsterName() {
    if (lastMonsterName.isPresent()) {
      lastMonsterName = Optional.of(numberName(lastMonsterName.get()));
    } else {
      lastMonsterName = Optional.of(DEFAULT_MONSTER_NAME);
    }

    return lastMonsterName.get();
  }

  public void read(Map<String, Object> data) {
    number = (int) Values.get(data, FIELD_NUMBER, 0);
    turn = (int) Values.get(data, FIELD_TURN, 0);
    status = Values.get(data, FIELD_STATUS, EncounterStatus.ENDED);
    creatureIds = Values.get(data, FIELD_CREATURES, Collections.emptyList());
    currentCreatureIndex = (int) Values.get(data, FIELD_CURRENT, 0);

    syncCreaturesWithIds(status == EncounterStatus.ENDED || status == EncounterStatus.STARTING);
  }

  public void setup() {
    if (!isEnded()) {
      Status.error("The encounter is already running!");
      return;
    }

    number++;
    turn = 0;
    currentCreatureIndex = -1;
    campaign.store();
    StartEncounterDialog.newInstance(campaign.getId()).display();
  }

  public void start() {
    turn = 0;
    status = EncounterStatus.SURPRISED;

    toNextUnsurprised();
    campaign.store();
  }

  public void starting(List<String> includedCreatureIds, List<String> surprisedCreatureIds) {
    if (campaign.amDM()) {
      status = EncounterStatus.STARTING;
      creatureIds = includedCreatureIds;

      // Setup initial conditions for characters.
      for (String creatureId : includedCreatureIds) {
        Optional<? extends Creature> creature = campaign.characters().getCreature(creatureId);
        if (creature.isPresent()) {
          creature.get().addCondition(new TimedCondition(
              Conditions.FLAT_FOOTED, getCampaign().getDm().getId()));
        }
      }

      for (String creatureId : surprisedCreatureIds) {
        Optional<? extends Creature> creature = campaign.characters().getCreature(creatureId);
        if (creature.isPresent()) {
          creature.get().addCondition(new TimedCondition(
              Conditions.SURPRISED, getCampaign().getDm().getId()));
        }
      }

      campaign.store();
    }
  }

  public void update(CreatureConditions conditions) {
    updateConditions();
  }

  public void update(Characters characters) {
    if (status == EncounterStatus.STARTING) {
      syncCreaturesWithIds(true);

      // Don't start the encounter if we don't yet have all characters, as we might
      // be missing some data.
      if (campaign.amDM() && creatureIds.size() == creatures.size()) {
        boolean done = true;
        for (Creature creature : creatures) {
          if (!creature.hasInitiative(number)) {
            done = false;
          }
        }

        if (done) {
          start();
          campaign.store();
        }
      }
    } else {
      syncCreaturesWithIds(false);
    }
  }

  public void update(Monsters monsters) {
    for (Monster monster : monsters.getCampaignMonsters(campaign.getId())) {
      if (!creatureIds.contains(monster.getId())) {
        // A new monster has appeared. Add if after the current creature.
        creatureIds.add(currentCreatureIndex + 1, monster.getId());
        creatures.add(currentCreatureIndex + 1, monster);
      }
    }

    update(campaign.getContext().characters());
  }

  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_NUMBER, number);
    data.put(FIELD_TURN, turn);
    data.put(FIELD_STATUS, status.toString());
    data.put(FIELD_CREATURES, creatureIds);
    data.put(FIELD_CURRENT, currentCreatureIndex);

    return data;
  }

  private boolean isSurprised(int index) {
    return isSurprised(creatures.get(index));
  }

  private boolean isSurprised(Creature creature) {
    return creature.hasCondition(Conditions.SURPRISED.getName());
  }

  private void nextTurn() {
    if (status == EncounterStatus.SURPRISED) {
      // Remove all surprised conditions.
      for (String id : creatureIds) {
        campaign.getContext().conditions().deleteAll(Conditions.SURPRISED.getName(), id);
      }
    }

    currentCreatureIndex = 0;
    status = EncounterStatus.ONGOING;
    turn++;
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

  private void syncCreaturesWithIds(boolean sort) {
    creatures.clear();
    List<String> sortedIds = new ArrayList<>();
    for (String id : creatureIds) {
      Optional<? extends Creature> creature;
      if (Characters.isCharacterId(id)) {
        creature = campaign.characters().get(id);
      } else {
        creature = campaign.monsters().get(id);
      }
      if (creature.isPresent()) {
        creatures.add(creature.get());
      } else {
        sortedIds.add(id);
      }
    }

    if (sort) {
      creatures.sort(new Creature.InitiativeComparator(campaign.getEncounter().getNumber()));
      sortedIds.addAll(creatures.stream().map(Creature::getId).collect(Collectors.toList()));
      creatureIds = sortedIds;
    }

    campaign.getContext().conditions().readConditions(creatureIds);
  }

  private void toNextUnsurprised() {
    if (status == EncounterStatus.SURPRISED) {
      while (currentCreatureIndex < creatureIds.size()) {
        currentCreatureIndex++;
        if (currentCreatureIndex < creatures.size() && !isSurprised(currentCreatureIndex)) {
          break;
        }
      }
    } else {
      currentCreatureIndex++;
    }

    if (currentCreatureIndex >= creatureIds.size()) {
      nextTurn();
    }

    // Remove the flat-footed condition, as the creature is acting now.
    campaign.getContext().conditions().deleteAll(Conditions.FLAT_FOOTED.getName(),
        creatureIds.get(currentCreatureIndex));

    updateConditions();

    campaign.store();
  }

  private void updateConditions() {
    int lastCreatureIndex = currentCreatureIndex - 1;
    if (turn > 0 && lastCreatureIndex < 0) {
      lastCreatureIndex = creatures.size() - 1;
    }

    if (lastCreatureIndex >= 0 && creatures.size() > lastCreatureIndex) {
      // Remove conditions that have expired for the last creature.
      Creature creature = creatures.get(lastCreatureIndex);
      updateConditions(creature.getId(), false);
    }

    if (currentCreatureIndex >= 0 && creatures.size() > currentCreatureIndex) {
      // Remove expired conditions for the current creature.
      Creature creature = creatures.get(currentCreatureIndex);
      updateConditions(creature.getId(), true);
    }
  }

  private void updateConditions(String creatureId, boolean before) {
    for (CreatureCondition condition
        : campaign.getContext().conditions().getCreatureConditions(creatureId)) {
      if (!condition.getCondition().hasEndDate()
          && (condition.getCondition().getEndRound() < turn
              || (before && condition.getCondition().getEndRound() == turn))) {
        campaign.getContext().conditions().delete(condition.getId());
      }
    }
  }

  public static Encounter read(Campaign campaign, @Nullable Map<String, Object> data) {
    Encounter encounter = new Encounter(campaign);
    if (data != null) {
      encounter.read(data);
    }

    return encounter;
  }
}
