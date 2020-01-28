/*
 * Copyright (c) 2017-2018 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.documents;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Monsters available in the games.
 */
public class Monsters extends Documents<Monsters> {
  private final Map<String, Monster> monstersByMonsterId = new HashMap<>();
  private final Multimap<String, Monster> monstersByCampaignId = HashMultimap.create();
  public Monsters(CompanionContext context) {
    super(context);
  }

  public Collection<Monster> getAll() {
    return monstersByMonsterId.values();
  }

  public static boolean isMonsterId(String id) {
    return id.contains("/monsters/");
  }

  public void addCampaign(String campaignId) {
    if (!monstersByCampaignId.containsKey(campaignId)) {
      CollectionReference reference = db.collection(campaignId + "/" + Monster.PATH);
      reference.addSnapshotListener((s, e) -> {
        if (e == null) {
          readMonsters(campaignId, s.getDocuments());
        } else {
          Status.exception("Cannot read monsters!", e);
        }
      });
    }
  }

  public void deleteAllInCampaign(String campaignId) {
    for (Monster monster : monstersByCampaignId.removeAll(campaignId)) {
      monstersByMonsterId.remove(monster.getId());
      delete(monster.getId());
    }
  }

  public Optional<Monster> get(String creatureId) {
    return Optional.ofNullable(monstersByMonsterId.get(creatureId));
  }

  public Collection<Monster> getCampaignMonsters(String campaignId) {
    return monstersByCampaignId.get(campaignId);
  }

  public Optional<? extends Creature<?>> getMonsterOrCharacter(String id) {
    Optional<? extends Creature<?>> creature = context.characters().get(id);
    if (!creature.isPresent()) {
      creature = get(id);
    }

    return creature;
  }

  public String nameFor(String id) {
    Optional<? extends Creature<?>> creature = getMonsterOrCharacter(id);
    if (creature.isPresent()) {
      return creature.get().getName();
    }

    return id;
  }

  private void readMonsters(String campaignId, List<DocumentSnapshot> snapshots) {
    Map<String, Monster> existing;
    if (monstersByCampaignId.containsKey(campaignId)) {
      existing = monstersByCampaignId.removeAll(campaignId).stream()
          .collect(Collectors.toMap(Monster::getId, Function.identity()));
      for (Monster monster : existing.values()) {
        monstersByMonsterId.remove(monster.getId());
      }
    } else {
      existing = Collections.emptyMap();
    }

    for (DocumentSnapshot snapshot : snapshots) {
      Monster monster = existing.get(snapshot.getId());
      if (monster == null) {
        monster = Monster.fromData(context, snapshot);
      } else {
        monster.snapshot = Optional.of(snapshot);
        monster.read();
      }

      monstersByCampaignId.put(campaignId, monster);
      monstersByMonsterId.put(monster.getId(), monster);
    }

    CompanionApplication.get().update("monsters loaded");
  }
}
