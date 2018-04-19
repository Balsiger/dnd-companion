/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * along with the Roleplay Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.dynamics;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.storage.DataBaseAccessor;
import net.ixitxachitls.companion.util.Misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Access to and utilities for camapigns.
 */
public class Campaigns {
  private final CompanionContext companionContext;
  private final CampaignsData local;
  private final CampaignsData remote;

  private Campaign defaultCampaign;
  private final MutableLiveData<Optional<Campaign>> liveDefaultCampaign =
      new MutableLiveData<>();

  // Live data storages.
  private MutableLiveData<String> currentCampaignId = new MutableLiveData<>();
  private MutableLiveData<ImmutableList<String>> allCampaignIds = new MutableLiveData<>();

  public Campaigns(CompanionContext companionContext) {
    this.companionContext = companionContext;
    this.local = new CampaignsData(companionContext, true);
    this.remote = new CampaignsData(companionContext, false);

    this.defaultCampaign = LocalCampaign.createDefault(companionContext);
    this.currentCampaignId.setValue(defaultCampaign.getCampaignId());
    this.liveDefaultCampaign.setValue(Optional.of(defaultCampaign));

    // Check that we don't have local and remote campaigns with the same id.
    if (!Misc.onEmulator()) {
      List<String> localIds = local.getCampaigns().stream()
          .map(Campaign::getCampaignId)
          .collect(Collectors.toList());
      for (Campaign campaign : remote.getCampaigns()) {
        if (localIds.contains(campaign.getCampaignId())) {
          Status.toast("Removing duplicate remote campaign " + campaign.getName());
          remote.remove(campaign);
        }
      }
    }
  }

  public CompanionContext context() {
    return companionContext;
  }

  public Campaign getDefaultCampaign() {
    return defaultCampaign;
  }

  // Data accessors.
  public LiveData<Optional<Campaign>> getCampaign(String campaignId) {
    if (campaignId.equals(defaultCampaign.getCampaignId())) {
      return liveDefaultCampaign;
    }

    if ((!Misc.onEmulator() || !companionContext.settings().useRemoteCampaigns())
        && local.hasCampaign(campaignId)) {
      return local.getCampaign(campaignId);
    }

    return remote.getCampaign(campaignId);
  }

  public LiveData<String> getCurrentCampaignId() {
    return currentCampaignId;
  }

  public List<Campaign> getLocalCampaigns() {
    return local.getCampaigns();
  }

  public List<Campaign> getRemoteCampaigns() {
    return remote.getCampaigns();
  }

  public List<Campaign> getAllCampaigns() {
    List<Campaign> campaigns = new ArrayList<>();
    campaigns.add(defaultCampaign);
    if (Misc.onEmulator()) {
      if (companionContext.settings().useRemoteCampaigns()) {
        campaigns.addAll(remote.getCampaigns());
      } else {
        campaigns.addAll(local.getCampaigns());
      }
    } else {
      campaigns.addAll(local.getCampaigns());
      campaigns.addAll(remote.getCampaigns());
    }


    return campaigns;
  }

  public boolean has(Campaign campaign) {
    return has(campaign.getCampaignId(), campaign.isLocal());
  }

  public boolean has(String campaignId, boolean isLocal) {
    if (isLocal) {
      return local.has(campaignId);
    }

    return remote.has(campaignId);
  }

  public boolean hasAnyPublished() {
    for (Campaign campaign : local.getAll()) {
      if (campaign.isPublished()) {
        return true;
      }
    }

    return false;
  }

  // This is not live data.
  public List<Campaign> getCampaignsByServer(String serverId) {
    List<Campaign> filtered = new ArrayList<>();
    for (Campaign campaign : remote.getCampaigns()) {
      if (campaign.getServerId().equals(serverId)) {
        filtered.add(campaign);
      }
    }

    return filtered;
  }

  public LiveData<ImmutableList<String>> getAllCampaignIds() {
    if (allCampaignIds.getValue() == null) {
      LiveDataUtils.setValueIfChanged(allCampaignIds, ImmutableList.copyOf(campaignIds()));
    }

    return allCampaignIds;
  }

  public long getLocalIdFor(String campaignId) {
    return local.getIdFor(campaignId);
  }

  public long getRemoteIdFor(String campaignId) {
    return remote.getIdFor(campaignId);
  }

  // Data mutations.

  public void changeCurrent(String campaignId) {
    // Don't check for equality with the current campaign here, as we might have changed
    // the object and need to update UI now, as the campaign actually changed, although
    // the object is already updated.
    Status.log("setting current campaign to " + campaignId);
    LiveDataUtils.setValueIfChanged(currentCampaignId, campaignId);
  }

  public void update(Campaign campaign) {
    // We assume that the campaign id did not change.
    Status.log("updating campaign " + campaign);

    if (campaign.isLocal()) {
      local.update(campaign);
    } else {
      remote.update(campaign);
    }
  }

  public void add(Campaign campaign) {
    Status.log("adding campaign " + campaign);

    if (campaign.isLocal()) {
      local.add(campaign);
    } else {
      remote.add(campaign);
    }

    LiveDataUtils.setValueIfChanged(allCampaignIds, ImmutableList.copyOf(campaignIds()));
  }

  public void remove(String campaignId, boolean isLocal) {
    Status.log("removing campaign " + campaignId + " / " + isLocal);
    if (isLocal) {
      local.remove(campaignId);
    } else {
      remote.remove(campaignId);
    }

    LiveDataUtils.setValueIfChanged(allCampaignIds, ImmutableList.copyOf(campaignIds()));
    if (currentCampaignId.getValue().equals(campaignId)) {
      currentCampaignId.setValue(defaultCampaign.getCampaignId());
    }
  }

  public void remove(Campaign campaign) {
    remove(campaign.getCampaignId(), campaign.isLocal());
  }

  // Publishing.
  public void publish() {
    Status.log("publishing all campaigns");
    for (Campaign campaign : local.getAll()) {
      LocalCampaign localCampaign = campaign.asLocal();
      if (!localCampaign.isDefault() && localCampaign.isPublished()) {
        localCampaign.publish();
      }
    }
  }

  // Private methods.

  // This should only be called from the main activity.
  public void load(DataBaseAccessor dataBaseAccessor) {
  }

  private Set<String> campaignIds() {
    Set<String> ids = new HashSet<>();
    ids.add(defaultCampaign.getCampaignId());
    if (Misc.onEmulator()) {
      if (companionContext.settings().useRemoteCampaigns()) {
        ids.addAll(remote.getCampaigns()
            .stream()
            .map(Campaign::getCampaignId)
            .collect(Collectors.toList()));
      } else {
        ids.addAll(local.getCampaigns()
            .stream()
            .map(Campaign::getCampaignId)
            .collect(Collectors.toList()));
      }
    } else {
      ids.addAll(local.getCampaigns()
          .stream()
          .map(Campaign::getCampaignId)
          .collect(Collectors.toList()));
      ids.addAll(remote.getCampaigns()
          .stream()
          .map(Campaign::getCampaignId)
          .collect(Collectors.toList()));
    }

    return ids;
  }

  private Optional<Campaign> campaign(String campaignId) {
    if (defaultCampaign.getCampaignId().equals(campaignId)) {
      return Optional.of(defaultCampaign);
    }

    Optional<Campaign> campaign = local.get(campaignId);
    if (campaign.isPresent()) {
      return campaign;
    }

    return remote.get(campaignId);
  }

  private static class CampaignComparator implements Comparator<Campaign> {
    @Override
    public int compare(Campaign first, Campaign second) {
      if (first.getId() == second.getId())
        return 0;

      if (first.isDefault() && !second.isDefault()) {
        return -1;
      }
      if (!first.isDefault() && second.isDefault()) {
        return +1;
      }

      if (first.isLocal() && !second.isLocal()) {
        return -1;
      }
      if (!first.isLocal() && second.isLocal()) {
        return +1;
      }

      return first.getName().compareTo(second.getName());
    }

    @Override
    public boolean equals(Object obj) {
      return false;
    }
  }
}
