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

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;

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

  protected static final String PATH_ADVENTURES = "adventures";
  protected static final String PATH_ENCOUNTERS = "encounters";

  private final Map<String, Encounter> encountersById = new HashMap<>();
  private final Map<String, List<Encounter>> encountersByCampaignAndAdventureId = new HashMap<>();

  public Encounters(CompanionContext context) {
    super(context);
  }

  public void loadEncounters(String campaignId, String adventureId) {
    String key = createKey(campaignId, adventureId);
    if (!encountersByCampaignAndAdventureId.containsKey(key)) {
      encountersByCampaignAndAdventureId.put(key, null);
      CollectionReference reference =
          db.collection(createKey(campaignId, adventureId) + "/" + PATH_ENCOUNTERS);
      reference.addSnapshotListener((s, e) -> {
        if (e == null) {
          readEncounters(s.getDocuments());
        } else {
          Status.exception("Could not read encounters!", e);
        }
      });
    }
  }

  public Optional<Encounter> get(String id) {
    return Optional.ofNullable(encountersById.get(id));
  }

  public Optional<Encounter> get(String campaignId, String adventureId, String encounterId) {
    return encountersByCampaignAndAdventureId.getOrDefault(createKey(campaignId, adventureId),
        Collections.emptyList()).stream()
        .filter(e -> e.getEncounterId().equals(encounterId))
        .findFirst();
  }

  private void readEncounters(List<DocumentSnapshot> snapshots) {
    if (!snapshots.isEmpty()) {
      String path = snapshots.get(0).getReference().getParent().getParent().getPath();
      List<Encounter> encounters = snapshots.stream()
          .map(s -> Encounter.fromData(context, s))
          .collect(Collectors.toList());

      encountersByCampaignAndAdventureId.put(path, encounters);
      for (Encounter encounter : encounters) {
        encountersById.put(encounter.getId(), encounter);
      }

      updatedDocuments(snapshots);
    }
  }

  public static String createKey(String campaignId, String adventureId) {
    return campaignId + "/" + PATH_ADVENTURES + "/" + adventureId;
  }

  public boolean has(String campaignId, String adventureId, String encounterId) {
    return encountersByCampaignAndAdventureId.getOrDefault(createKey(campaignId, adventureId),
        Collections.emptyList()).stream().anyMatch(e -> e.getEncounterId().equals(encounterId));
  }

  public boolean hasLoaded(String campaignId, String adventureId) {
    return encountersByCampaignAndAdventureId.get(createKey(campaignId, adventureId)) != null;
  }

  public Encounter getOrInitialize(String campaignId, String adventureId, String encounterId) {
    if (has(campaignId, adventureId, encounterId)) {
      return get(campaignId, adventureId, encounterId).get();
    } else {
      return reset(campaignId, adventureId, encounterId);
    }
  }

  public Encounter reset(String campaignId, String adventureId, String encounterId) {
    // Creating a new encounter and store it.
    Encounter encounter = Encounter.create(context, campaignId, adventureId, encounterId);
    encounter.store();

    return encounter;
  }
}
