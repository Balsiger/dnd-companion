/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Access to multiple encounters of a campaign.
 */
public class Encounters extends Documents<Encounters> {

  protected static final String PATH = "encounters";

  private final Map<String, Encounter> encountersById = new HashMap<>();
  private final Map<String, List<Encounter>> encountersByAdventureId = new HashMap<>();

  public Encounters(CompanionContext context) {
    super(context);
  }

  private void setEncounter(Encounter encounter) {
    encountersById.put(encounter.getId(), encounter);
    String adventureId = Adventures.extractId(encounter.getId());
    List<Encounter> encounters = encountersByAdventureId.get(adventureId);
    if (encounters == null) {
      encounters = new ArrayList<>();
      encountersByAdventureId.put(adventureId, encounters);
    }

    for (int i = 0; i < encounters.size(); i++) {
      if (encounters.get(i).getId().equals(encounter.getId())) {
        encounters.set(i, encounter);
        return;
      }
    }

    encounters.add(encounter);
  }

  public static boolean isEncounterId(String id) {
    return id.contains("/" + PATH + "/");
  }

  public Optional<Encounter> get(String id) {
    return Optional.ofNullable(encountersById.get(id));
  }

  public Encounter getOrInitialize(String encounterId) {
    if (has(encounterId)) {
      return get(encounterId).get();
    } else {
      return reset(encounterId);
    }
  }

  public boolean has(String encounterId) {
    return encountersById.containsKey(encounterId);
  }

  public boolean hasLoaded(String adventureId) {
    return encountersByAdventureId.containsKey(adventureId);
  }

  public void loadEncounters(String adventureId) {
    if (!encountersByAdventureId.containsKey(adventureId)) {
      encountersByAdventureId.put(adventureId, Collections.emptyList());
      CollectionReference reference = db.collection(adventureId + "/" + PATH);
      reference.addSnapshotListener((s, e) -> {
        if (e == null) {
          readEncounters(s.getDocuments());
        } else {
          Status.exception("Could not read encounters!", e);
        }
      });
    }
  }

  public Encounter reset(String encounterId) {
    // Creating a new encounter and store it.
    Encounter encounter = Encounter.create(context, encounterId);
    encounter.store();
    setEncounter(encounter);

    return encounter;
  }

  private void readEncounters(List<DocumentSnapshot> snapshots) {
    if (!snapshots.isEmpty()) {
      String path = snapshots.get(0).getReference().getParent().getParent().getPath();
      List<Encounter> encounters = snapshots.stream()
          .map(s -> Encounter.fromData(context, s))
          .collect(Collectors.toList());

      encountersByAdventureId.put(path, encounters);
      for (Encounter encounter : encounters) {
        encountersById.put(encounter.getId(), encounter);
      }

      CompanionApplication.get().update("encounters read");
    }
  }

  public static String createId(String adventureId, String encounterShortId) {
    return adventureId + "/" + PATH + "/" + encounterShortId;
  }

  public static String extractShortId(String id) {
    if (isEncounterId(id)) {
      return Strings.extractPattern(id, PATH + "/(.*?)(/|$)");
    }

    return "";
  }
}
