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

import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.enums.BattleStatus;
import net.ixitxachitls.companion.proto.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Representation of a battle in a campaign.
 */
public class Battle {
  private static final Random RANDOM = new Random();

  private final Campaign campaign;
  private int number;
  private final List<Combatant> combatants = new ArrayList<>();
  private BattleStatus status;
  private int turn;
  private int currentCombatantIndex = 0;
  private Optional<String> lastMonsterName = Optional.absent();

  public Battle(Campaign campaign) {
    this(campaign, 0, BattleStatus.ENDED, 0, 0, Collections.emptyList());
  }

  private Battle(Campaign campaign, int number, BattleStatus status, int turn,
                 int currentCombatantIndex, List<Combatant> combatants) {
    this.campaign = campaign;
    this.number = number;
    this.status = status;
    this.turn = turn;
    this.currentCombatantIndex = currentCombatantIndex;
    this.combatants.addAll(combatants);
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

  public int getTurn() {
    return turn;
  }

  public int getNumber() {
    return number;
  }

  public void addMonster(String name, int initiativeModifier) {
    lastMonsterName = Optional.of(name);
    combatants.add(new Combatant(name, name, RANDOM.nextInt(20) + initiativeModifier, true, false));
    campaign.store();
  }

  public void refreshCombatant(String characterId, String name, int initiativeModifier) {
    for (int i = 0; i < combatants.size(); i++) {
      Combatant existing = combatants.get(i);
      if (existing.id.equals(characterId) && existing.name.equals(name)) {
        combatants.set(i, new Combatant(characterId, name, initiativeModifier, existing.monster,
            existing.waiting));
        return;
      }
    }

    // No combatant to refresh was found.
    combatants.add(new Combatant(characterId, name, initiativeModifier, false, false));
    campaign.store();
  }

  public void refreshCombatants() {
    for (int i = 0; i < combatants.size(); i++) {
      Combatant existing = combatants.get(i);
      if (!existing.isMonster()) {
        Optional<Character> character = Characters.getCharacter(existing.id).getValue();
        if (character.isPresent()) {
          combatants.set(i,
              new Combatant(existing.id, existing.name,
                  character.get().getBattleNumber() == number ?
                      character.get().getInitiative() : Character.NO_INITIATIVE,
                  false, existing.waiting));
        }
      }
    }

    campaign.store();
  }



  public int getCurrentCombatantIndex() {
    return currentCombatantIndex;
  }

  public Combatant getCurrentCombatant() {
    if (currentCombatantIndex < combatants.size()) {
      return combatants.get(currentCombatantIndex);
    }

    if (!combatants.isEmpty()) {
      return combatants.get(0);
    }

    return new Combatant("", "Invalid", Character.NO_INITIATIVE, true, false);
  }

  public void battle() {
    turn = 0;
    status = BattleStatus.SURPRISED;
    currentCombatantIndex = 0;
    Collections.sort(combatants);
    campaign.store();
  }

  public void combatantDone() {
    combatants.get(currentCombatantIndex).setWaiting(false);
    currentCombatantIndex++;
    if (currentCombatantIndex >= combatants.size()) {
      currentCombatantIndex = 0;
      status = BattleStatus.ONGOING;
      turn++;
    }

    campaign.store();
  }

  public void combatantLater() {
    if (currentCombatantIndex + 1 >= combatants.size()) {
      // Cannot be later.
      return;
    }

    Combatant combatant = combatants.remove(currentCombatantIndex);
    combatants.add(currentCombatantIndex + 1, combatant);
    combatant.setWaiting(true);

    campaign.store();
  }

  public List<Combatant> combatants() {
    return Collections.unmodifiableList(combatants);
  }

  public List<Combatant> combatantsByInitiative() {
    Collections.sort(combatants);
    return Collections.unmodifiableList(combatants);
  }

  public void end() {
    status = BattleStatus.ENDED;
    for (Combatant combatant : combatants) {
      if (!combatant.isMonster()) {
        Optional<Character> character = Characters.getCharacter(combatant.getId()).getValue();
        if (character.isPresent()) {
          character.get().clearInitiative();
        }
      }
    }
    combatants.clear();
    lastMonsterName = Optional.absent();
    campaign.store();
  }

  public Optional<String> getLastMonsterName() {
    return lastMonsterName;
  }

  public void removeCombatant() {
    combatants.remove(currentCombatantIndex);
    campaign.store();
  }

  public void start() {
    status = BattleStatus.STARTING;
    number++;
    combatants.clear();
    campaign.store();
  }

  public Data.CampaignProto.Battle toProto() {
    Data.CampaignProto.Battle.Builder proto = Data.CampaignProto.Battle.newBuilder()
        .setStatus(status.toProto())
        .setTurn(turn)
        .setNumber(number)
        .setCurrentCombatantIndex(currentCombatantIndex);

    for (Combatant combatant : combatants()) {
      proto.addCombatant(combatant.toProto());
    }

    return proto.build();
  }

  public static Battle fromProto(Campaign campaign, Data.CampaignProto.Battle proto) {
    List<Combatant> combatants = new ArrayList<>();
    for (Data.CampaignProto.Battle.Combatant combatant : proto.getCombatantList()) {
      combatants.add(Combatant.fromProto(combatant));
    }
    return new Battle(campaign, proto.getNumber(), BattleStatus.fromProto(proto.getStatus()),
        proto.getTurn(), proto.getCurrentCombatantIndex(), combatants);
  }

  public boolean currentIsLast() {
    return currentCombatantIndex >= combatants.size() - 1;
  }

  public boolean currentIsWaiting() {
    return combatants.get(currentCombatantIndex).isWaiting();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Battle battle = (Battle) o;

    if (number != battle.number) return false;
    if (turn != battle.turn) return false;
    if (currentCombatantIndex != battle.currentCombatantIndex) return false;
    if (!combatants.equals(battle.combatants)) return false;
    if (status != battle.status) return false;
    return lastMonsterName.equals(battle.lastMonsterName);
  }

  @Override
  public int hashCode() {
    int result = number;
    result = 31 * result + combatants.hashCode();
    result = 31 * result + status.hashCode();
    result = 31 * result + turn;
    result = 31 * result + currentCombatantIndex;
    result = 31 * result + lastMonsterName.hashCode();
    return result;
  }

  public static class Combatant implements Comparable<Combatant> {
    private final String id;
    private final String name;
    private final int initiative;
    private final boolean monster;
    private boolean waiting;

    private Combatant(String id, String name, int initiative, boolean monster, boolean waiting) {
      this.id = id;
      this.name = name;
      this.initiative = initiative;
      this.monster = monster;
      this.waiting = waiting;
    }

    private Combatant(Character character, boolean isWaiting) {
      this.id = character.getCharacterId();
      this.name = character.getName();
      this.initiative = character.getInitiative();
      this.monster = false;
      this.waiting = isWaiting;
    }

    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public int getInitiative() {
      return initiative;
    }

    public boolean hasInitiative() {
      return initiative != Character.NO_INITIATIVE;
    }

    public boolean isMonster() {
      return monster;
    }

    public boolean isWaiting() {
      return waiting;
    }

    public void setWaiting(boolean waiting) {
      this.waiting = waiting;
    }

    @Override
    public int compareTo(Combatant other) {
      return Integer.compare(other.initiative, this.initiative);
    }

    public Data.CampaignProto.Battle.Combatant toProto() {
      return Data.CampaignProto.Battle.Combatant.newBuilder()
          .setId(id)
          .setName(name)
          .setInitiativeModifier(initiative)
          .setMonster(monster)
          .setWaiting(waiting)
          .build();
    }

    public static Combatant fromProto(Data.CampaignProto.Battle.Combatant proto) {
      return new Combatant(proto.getId(), proto.getName(), proto.getInitiativeModifier(),
          proto.getMonster(), proto.getWaiting());
    }
  }
}
