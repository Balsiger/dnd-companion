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
import net.ixitxachitls.companion.data.enums.BattleStatus;
import net.ixitxachitls.companion.proto.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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

  private Battle(Campaign campaign, int number, BattleStatus status, int turn, int currentCombatantIndex,
                List<Combatant> combatants) {
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
    combatants.add(new Combatant("", name, RANDOM.nextInt(20) + initiativeModifier, true, false));
    campaign.store();
  }

  public void addCharacter(String characterId, String name, int initiativeModifier) {
    for (Iterator<Combatant> i = combatants.iterator(); i.hasNext(); ) {
      Combatant combatant = i.next();
      if (!combatant.isMonster() && combatant.getName().equals(name)) {
        i.remove();
        break;
      }
    }

    combatants.add(new Combatant(characterId, name, initiativeModifier, false, false));
    campaign.store();
  }

  public int getCurrentCombatantIndex() {
    return currentCombatantIndex;
  }

  public Combatant getCurrentCombatant() {
    return combatants.get(currentCombatantIndex);
  }

  public void battle() {
    turn = 0;
    status = BattleStatus.SURPRISED;
    currentCombatantIndex = 0;
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

  public void startSurpriseRound() {
    status = BattleStatus.SURPRISED;
    Collections.sort(combatants);
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

    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public int getInitiative() {
      return initiative;
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
