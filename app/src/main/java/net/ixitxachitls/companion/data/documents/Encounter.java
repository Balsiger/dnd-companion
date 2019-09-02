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

import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.templates.AdventureTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import androidx.annotation.CallSuper;

/**
 * An encounter in a campaign
 */
public class Encounter extends Document<Encounter> {

  private static final String FIELD_ADVENTURE_ID = "adventure_id";
  private static final String FIELD_ENCOUNTER_ID = "encounter_id";
  private static final String FIELD_MONSTERS = "monsters";

  private static final Document.DocumentFactory<Encounter> FACTORY = () -> new Encounter();

  private String adventureId;
  private String encounterId;
  private List<Monster> monsters;

  @Override
  protected Map<String, Object> write(Map data) {
    data.put(FIELD_ADVENTURE_ID, adventureId);
    data.put(FIELD_ENCOUNTER_ID, encounterId);
    data.put(FIELD_MONSTERS, monsters);

    return data;
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();

    adventureId = data.get(FIELD_ADVENTURE_ID, "");
    encounterId = data.get(FIELD_ENCOUNTER_ID, "");
    monsters = data.get(FIELD_MONSTERS, Collections.emptyList());
  }

  @Override
  public String toString() {
    return encounterId + " (" + adventureId + ")";
  }

  protected static Encounter fromData(CompanionContext context, DocumentSnapshot snapshot) {
    return Document.fromData(FACTORY, context, snapshot);
  }

  public static Encounter create(CompanionContext context, String campaignId, String adventureId,
                                 String encounterId) {
    Encounter encounter =
        Document.create(FACTORY, context, createId(campaignId, adventureId, encounterId));

    Optional<AdventureTemplate> adventure =
        Templates.get().getAdventureTemplates().get(adventureId);
    if (adventure.isPresent() && adventure.get().getEncounter(encounterId).isPresent()) {
      encounter.initialize(adventure.get().getEncounter(encounterId).get());
    }

    return encounter;
  }

  private void initialize(AdventureTemplate.EncounterTemplate encounter) {
    for (AdventureTemplate.EncounterTemplate.CreatureInitializer creature
        : encounter.getCreatures()) {
      // TODO(Merlin): For now we assume all creatures are monsters.

    }
  }

  private static String createId(String campaignId, String adventureId, String encounterId) {
    return campaignId + "/" + adventureId + "/" + encounterId;
  }
}
