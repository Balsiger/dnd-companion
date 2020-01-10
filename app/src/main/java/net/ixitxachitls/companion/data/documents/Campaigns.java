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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Model for all the campaigns available to a user.
 */
public class Campaigns extends Documents<Campaigns> {

  public static final String PATH = "campaigns";

  private CollectionReference dmCampaigns;

  private ImmutableList<String> ids = ImmutableList.of();
  private ImmutableList<Campaign> campaigns = ImmutableList.of();
  private Map<String, Campaign> dmCampaignsById = new HashMap<>();
  private Map<String, Campaign> playerCampaignsById = new HashMap<>();

  public Campaigns(CompanionContext context) {
    super(context);
  }

  public List<Campaign> getCampaigns() {
    return campaigns;
  }

  public List<Campaign> getDMCampaigns() {
    return new ArrayList<>(dmCampaignsById.values());
  }

  public ImmutableList<String> getIds() {
    return ids;
  }

  public static boolean isCampaignId(String id) {
    return id.contains("/" + PATH + "/");
  }

  public void add(Campaign campaign) {
    if (campaign.amDM()) {
      if (dmCampaignsById.containsKey(campaign.getId())) {
        throw new IllegalStateException("Campaign already added: " + campaign);
      }

      dmCampaignsById.put(campaign.getId(), campaign);
    } else {
      if (playerCampaignsById.containsKey(campaign.getId())) {
        throw new IllegalStateException("Campaign already added: " + campaign);
      }

      playerCampaignsById.put(campaign.getId(), campaign);
    }

    update(Collections.singletonList(campaign.getId()));
  }

  public Campaign create() {
    return Campaign.create(context, context.me());
  }

  public void delete(Campaign campaign) {
    if (campaign.amDM()) {
      if (!dmCampaignsById.containsKey(campaign.getId())
          && !playerCampaignsById.containsKey(campaign.getId())) {
        throw new IllegalStateException("Campaign to be removed does not exist: " + campaign);
      }

      campaign.uninviteAll();
      dmCampaignsById.remove(campaign.getId());
      playerCampaignsById.remove(campaign.getId());
      delete(campaign.getId());
      update(Collections.singletonList(campaign.getId()));
    }
  }

  public Campaign get(String id) {
    if (Strings.isNullOrEmpty(id)) {
      return Campaign.DEFAULT;
    }

    if (id.startsWith(context.me().getId())) {
      return Optional.ofNullable(dmCampaignsById.get(id)).orElse(Campaign.DEFAULT);
    } else {
      Campaign campaign = playerCampaignsById.get(id);
      if (campaign == null) {
        campaign = Campaign.getOrCreate(context, id);
        playerCampaignsById.put(id, campaign);
      }
      return campaign;
    }
  }

  public Optional<Campaign> getOptional(String id) {
    if (Strings.isNullOrEmpty(id)) {
      return Optional.empty();
    }

    if (id.startsWith(context.me().getId())) {
      return Optional.ofNullable(dmCampaignsById.get(id));
    } else {
      Campaign campaign = playerCampaignsById.get(id);
      if (campaign == null) {
        campaign = Campaign.getOrCreate(context, id);
        playerCampaignsById.put(id, campaign);
      }
      return Optional.of(campaign);
    }
  }

  public void loggedIn(User me) {
    this.dmCampaigns = db.collection(me.getId() + "/" + PATH);
    me.observeForever(u -> processPlayerCampaigns(me));
    Templates.get().executeAfterLoading(() -> {
      processDMCampaigns();
      processPlayerCampaigns(me);
    });
  }

  private boolean changedCampaigns(User me) {
    return me.getCampaigns().size() != playerCampaignsById.size()
        || me.getCampaigns().stream()
           .anyMatch(c -> !playerCampaignsById.containsKey(c))
        || playerCampaignsById.keySet().stream()
           .anyMatch(c -> !me.getCampaigns().contains(c));
  }

  private void processDMCampaigns(List<DocumentSnapshot> documents) {
    Map<String, Campaign> existing = new HashMap<>(dmCampaignsById);
    dmCampaignsById.clear();
    for (DocumentSnapshot snapshot : documents) {
      Campaign campaign = existing.get(snapshot.getReference().getPath());
      if (campaign == null) {
        campaign = Campaign.fromData(context, snapshot);
      } else {
        campaign.snapshot = Optional.of(snapshot);
        campaign.read();
      }

      dmCampaignsById.put(campaign.getId(), campaign);
    }

    update(documents.stream().map(d -> d.getReference().getPath()).collect(Collectors.toList()));
  }

  private void processDMCampaigns() {
    // DM campaigns, from the sub documents of the user.
    dmCampaigns.addSnapshotListener((s, e) -> {
      if (e == null)  {
        processDMCampaigns(s.getDocuments());
      } else {
        Status.exception("Cannot read campaigns!", e);
      }
    });
  }

  private void processPlayerCampaigns(User me) {
    if (changedCampaigns(me)) {
      // Player campaigns, invitations from other users.
      playerCampaignsById.clear();
      for (String campaignId : me.getCampaigns()) {
        Optional<Campaign> campaign = getOptional(campaignId);
        if (campaign.isPresent()) {
          playerCampaignsById.put(campaignId, campaign.get());
        }
      }

      update(new ArrayList<>(playerCampaignsById.keySet()));
    }
  }

  private void update(List<String> ids) {
    List<Campaign> all = new ArrayList<>(dmCampaignsById.values());
    all.addAll(playerCampaignsById.values());
    Collections.sort(all);
    this.campaigns = ImmutableList.copyOf(all);
    this.ids =
        ImmutableList.copyOf(campaigns.stream().map(Campaign::getId).collect(Collectors.toList()));

    updated(ids);
  }
}
