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

import java.util.Map;

import androidx.annotation.CallSuper;

/**
 * An encounter in a campaign
 */
public class Encounter extends Document<Encounter> {

  private static final String FIELD_ADVENTURE_ID = "adventure_id";
  private static final String FIELD_ENCOUNTER_ID = "encounter_id";

  private static final Document.DocumentFactory<Encounter> FACTORY = () -> new Encounter();

  private String adventureId;
  private String encounterId;

  @Override
  protected Map<String, Object> write(Map data) {
    data.put(FIELD_ADVENTURE_ID, adventureId);
    data.put(FIELD_ENCOUNTER_ID, encounterId);

    return data;
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();

    adventureId = data.get(FIELD_ADVENTURE_ID, "");
    encounterId = data.get(FIELD_ENCOUNTER_ID, "");
  }

  @Override
  public String toString() {
    return encounterId + " (" + adventureId + ")";
  }

  protected static Encounter fromData(CompanionContext context, DocumentSnapshot snapshot) {
    return Document.fromData(FACTORY, context, snapshot);
  }
}
