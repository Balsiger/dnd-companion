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

import com.google.common.collect.ImmutableList;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Creatures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Model for all the campaigns available to a user.
 */
public class FSCampaigns extends Documents<FSCampaigns> {

  protected static final String PATH = "campaigns";

  private final Users users;
  private final Characters characters;
  private final Creatures creatures;
  private final CollectionReference myCampaigns;

  private ImmutableList<String> ids = ImmutableList.of();
  private ImmutableList<FSCampaign> campaigns = ImmutableList.of();
  private Map<String, FSCampaign> campaignsById = new HashMap<>();

  public FSCampaigns(Users users, Characters characters, Creatures creatures) {
    this.users = users;
    this.characters = characters;
    this.creatures = creatures;
    this.myCampaigns = db.collection(users.getMe().getId() + "/" + PATH);

    readCampaigns();
  }

  public ImmutableList<String> getIds() {
    return ids;
  }

  public Optional<FSCampaign> getCampaign(String id) {
    return Optional.ofNullable(campaignsById.get(id));
  }

  public List<FSCampaign> getCampaigns() {
    return campaigns;
  }

  public FSCampaign create() {
    return new FSCampaign(users.getMe(), users, characters, creatures);
  }

  public void add(FSCampaign campaign) {
    if (campaignsById.containsKey(campaign.getId())) {
      throw new IllegalStateException("Campaign already added: " + campaign);
    }

    campaignsById.put(campaign.getId(), campaign);
    update();
  }

  public void remove(FSCampaign campaign) {
    if (!campaignsById.containsKey(campaign.getId())) {
      throw new IllegalStateException("Campaign to be removed does not exist: " + campaign);
    }

    campaignsById.remove(campaign.getId());
    delete(campaign.getId());
    update();
  }

  private void readCampaigns() {
    myCampaigns.get().addOnSuccessListener(task -> readCampaigns(task.getDocuments()));
    myCampaigns.addSnapshotListener((s, e) -> readCampaigns(s.getDocuments()));
  }

  private void readCampaigns(List<DocumentSnapshot> documents) {
    campaignsById.clear();
    for (DocumentSnapshot snapshot : documents) {
      FSCampaign campaign = new FSCampaign(snapshot, users.getMe(), users, characters, creatures);
      campaignsById.put(campaign.getId(), campaign);
    }

    update();
  }

  // TODO(merlin): Once we add another query, things get more complicated, as we might get an update
  // in one but not both. Although think about moving all that handling into Documents.
  private void update() {
    List<FSCampaign> all = new ArrayList<>(campaignsById.values());
    Collections.sort(all);
    campaigns = ImmutableList.copyOf(all);
    this.ids = campaigns.stream().map(FSCampaign::getId).collect(ImmutableList.toImmutableList());

    updated();
  }
}
