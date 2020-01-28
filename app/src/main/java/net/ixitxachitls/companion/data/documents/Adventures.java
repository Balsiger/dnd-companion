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

/**
 * Handling of adventures for a campaign.
 */
public class Adventures extends Documents<Adventures> {
  protected static final String PATH = "adventures";

  private final Map<String, Adventure> adventuresById = new HashMap<>();
  private final Map<String, List<Adventure>> adventuresByCampaignId = new HashMap<>();

  public Adventures(CompanionContext context) {
    super(context);
  }

  public static boolean isAdventureId(String id) {
    return id.contains("/" + PATH + "/");
  }

  @Override
  public void delete(String id) {
    super.delete(id);
  }

  public boolean exists(String campaignId, String name) {
    for (Adventure adventure
        : adventuresByCampaignId.getOrDefault(campaignId, Collections.emptyList())) {
      if (adventure.getName().equals(name)) {
        return true;
      }
    }

    return false;
  }

  public Optional<Adventure> get(String id) {
    return Optional.ofNullable(adventuresById.get(id));
  }

  public List<Adventure> getForCampaign(String campaignId) {
    return adventuresByCampaignId.getOrDefault(campaignId, Collections.emptyList());
  }

  public void readAdventures(String campaignId) {
    if (!adventuresByCampaignId.containsKey(campaignId)) {
      CollectionReference reference = db.collection(campaignId + "/" + PATH);
      reference.addSnapshotListener((s, e) -> {
        if (e == null) {
          readAdventures(campaignId, s.getDocuments());
        } else {
          Status.exception("Cannot read adventures!", e);
        }
      });
    }
  }

  private void readAdventures(String campaignId, List<DocumentSnapshot> snapshots) {
    List<Adventure> adventures = new ArrayList<>();
    for (DocumentSnapshot snapshot : snapshots) {
      Adventure adventure = Adventure.fromData(context, snapshot);
      adventures.add(adventure);
      adventuresById.put(adventure.getId(), adventure);
    }

    adventuresByCampaignId.put(campaignId, adventures);
    CompanionApplication.get().update("adventures loaded");
  }

  public static String createId(String campaignId, String adventureShortId) {
    return campaignId + "/" + PATH + "/" + adventureShortId;
  }

  public static String extractId(String id) {
    if (isAdventureId(id)) {
      return Strings.extractPattern(id, "(/" + PATH + "/.*?)(/|$)");
    }

    return "";
  }

  public static String extractShortId(String id) {
    if (isAdventureId(id)) {
      return Strings.extractPattern(id, PATH + "/(.*?)(/|$)");
    }

    return "";
  }
}
