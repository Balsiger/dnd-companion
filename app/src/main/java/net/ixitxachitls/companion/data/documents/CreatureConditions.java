/*
 * Copyright (c) 2017-2018 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Tabletop Companion.
 *
 * The Tabletop Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Tabletop Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.documents;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.values.TimedCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import androidx.annotation.CallSuper;

/**
 * Managing of conditions on creatures.
 */
public class CreatureConditions extends Documents<CreatureConditions> {

  protected static final String PATH = "conditions";

  private final Map<String, List<CreatureCondition>> conditionsByCreatureId = new HashMap<>();

  public CreatureConditions(CompanionContext context) {
    super(context);
  }

  @Override
  @CallSuper
  public void delete(String id) {
    super.delete(id);

    removeFromList(id, conditionsByCreatureId.getOrDefault(id, Collections.emptyList()));
  }

  public void deleteAll(String name, String creatureId) {
    for (CreatureCondition condition
        : conditionsByCreatureId.getOrDefault(creatureId, Collections.emptyList())) {
      if (condition.getCondition().getName().equals(name)) {
        delete(condition.getId());
      }
    }
  }

  public void deleteCreatureConditions(String creatureId) {
    for (CreatureCondition condition : getCreatureConditions(creatureId)) {
      delete(condition.getId());
    }

    conditionsByCreatureId.remove(creatureId);
  }

  public void deleteRoundBasedCreatureConditions() {
    for (String id : conditionsByCreatureId.keySet()) {
      deleteRoundBasedCreatureConditions(id);
    }
  }

  public void deleteRoundBasedCreatureConditions(String creatureId) {
    for (CreatureCondition condition : getCreatureConditions(creatureId)) {
      if (!condition.getCondition().hasEndDate()) {
        delete(condition.getId());
      }
    }
  }

  public List<CreatureCondition> getCreatureConditions(String creatureId) {
    return conditionsByCreatureId.getOrDefault(creatureId, Collections.emptyList());
  }

  public List<TimedCondition> getCreatureTimedConditions(String creatureId) {
    return getCreatureConditions(creatureId).stream()
        .map(CreatureCondition::getCondition)
        .collect(Collectors.toList());
  }

  public boolean hasCondition(String creatureId, String name) {
    for (CreatureCondition condition
        : conditionsByCreatureId.getOrDefault(creatureId, Collections.emptyList())) {
      if (condition.getCondition().getName().equals(name)) {
        return true;
      }
    }

    return false;
  }

  public void readConditions(List<String> creatureIds) {
    for (String creatureId : creatureIds) {
      readConditions(creatureId);
    }
  }

  public void readConditions(String creatureId) {
    if (!conditionsByCreatureId.containsKey(creatureId)) {
      conditionsByCreatureId.put(creatureId, Collections.emptyList());
      CollectionReference reference = db.collection(creatureId + "/" + PATH);
      reference.addSnapshotListener((s, e) -> {
        if (e == null) {
          readConditions(creatureId, s.getDocuments());
        } else {
          Status.exception("Cannot read conditions!", e);
        }
      });
    }
  }

  private void readConditions(String creatureId, List<DocumentSnapshot> snapshots) {
    List<CreatureCondition> conditions = new ArrayList<>();
    for (DocumentSnapshot snapshot : snapshots) {
      conditions.add(CreatureCondition.fromData(context, snapshot));
    }

    conditionsByCreatureId.put(creatureId, conditions);
    updatedDocuments(snapshots);
  }

  private void removeFromList(String id, List<CreatureCondition> conditions) {
    for (Iterator<CreatureCondition> i = conditions.iterator(); i.hasNext(); ) {
      if (id.equals(i.next().getId())) {
        i.remove();
        return;
      }
    }
  }
}
