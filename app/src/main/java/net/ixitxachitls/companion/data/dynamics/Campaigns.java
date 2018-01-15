/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Player Companion.
 *
 * The Player Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Player Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.dynamics;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.util.Misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Access to and utilities for camapigns.
 */
public class Campaigns {
  private static final String TAG = "Campaigns";

  private static CampaignsData local;
  private static CampaignsData remote;

  public static final Campaign defaultCampaign = Campaign.createDefault();
  private static final MutableLiveData<Optional<Campaign>> liveDefaultCampaign =
      new MutableLiveData<>();

  // Live data storages.
  private static MutableLiveData<String> currentCampaignId = new MutableLiveData<>();
  private static MutableLiveData<ImmutableList<String>> allCampaignIds = new MutableLiveData<>();

  static {
    currentCampaignId.setValue(defaultCampaign.getCampaignId());
    liveDefaultCampaign.setValue(Optional.of(defaultCampaign));
  }

  // Data accessors.
  public static LiveData<Optional<Campaign>> getCampaign(String campaignId) {
    if (campaignId.equals(defaultCampaign.getCampaignId())) {
      return liveDefaultCampaign;
    }

    if (local.hasCampaign(campaignId)) {
      return local.getCampaign(campaignId);
    }

    return remote.getCampaign(campaignId);
  }

  public static LiveData<Optional<Campaign>> getCampaign(String campaignId, boolean isLocal) {
    if (isLocal) {
      return local.getCampaign(campaignId);
    }

    return remote.getCampaign(campaignId);
  }

  public static LiveData<String> getCurrentCampaignId() {
    return currentCampaignId;
  }

  public static List<Campaign> getLocalCampaigns() {
    return local.getCampaigns();
  }

  public static List<Campaign> getAllCampaigns() {
    List<Campaign> campaigns = new ArrayList<>();
    campaigns.add(defaultCampaign);
    campaigns.addAll(local.getCampaigns());
    campaigns.addAll(remote.getCampaigns());

    return campaigns;
  }

  public static boolean has(String campaignId, boolean isLocal) {
    if (isLocal) {
      return local.has(campaignId);
    }

    return remote.has(campaignId);
  }

  public static boolean hasAnyPublished() {
    for (Campaign campaign : local.getAll()) {
      if (campaign.isPublished()) {
        return true;
      }
    }

    return false;
  }

  // This is not live data.
  public static List<Campaign> getCampaignsByServer(String serverId) {
    List<Campaign> filtered = new ArrayList<>();
    for (Campaign campaign : remote.getCampaigns()) {
      if (campaign.getCampaignId().startsWith(serverId)) {
        filtered.add(campaign);
      }
    }

    return filtered;
  }

  public static LiveData<ImmutableList<String>> getAllCampaignIds() {
    if (allCampaignIds.getValue() == null) {
      LiveDataUtils.setValueIfChanged(allCampaignIds, ImmutableList.copyOf(campaignIds()));
    }

    return allCampaignIds;
  }

  public static long getLocalIdFor(String campaignId) {
    return local.getIdFor(campaignId);
  }

  public static long getRemoteIdFor(String campaignId) {
    return remote.getIdFor(campaignId);
  }

  // Data mutations.

  public static void changeCurrent(String campaignId) {
    // Don't check for equality with the current campaign here, as we might have changed
    // the object and need to update UI now, as the campaign actually changed, although
    // the object is already updated.
    Log.d(TAG, "setting current campaign to " + campaignId);
    LiveDataUtils.setValueIfChanged(currentCampaignId, campaignId);
  }

  public static void update(Campaign campaign) {
    // We assume that the campaign id did not change.
    Log.d(TAG, "updating campaign " + campaign);

    if (campaign.isLocal()) {
      local.update(campaign);
    } else {
      remote.update(campaign);
    }
  }

  public static void add(Campaign campaign) {
    Log.d(TAG, "adding campaign " + campaign);

    if (campaign.isLocal()) {
      local.add(campaign);
    } else {
      remote.add(campaign);
    }

    LiveDataUtils.setValueIfChanged(allCampaignIds, ImmutableList.copyOf(campaignIds()));
  }

  public static void remove(String campaignId, boolean isLocal) {
    Log.d(TAG, "removing campaign " + campaignId + " / " + isLocal);
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

  public static void remove(Campaign campaign) {
    remove(campaign.getCampaignId(), campaign.isLocal());
  }

  // Publishing.
  // TODO(merlin): This should be moved out of here into the publisher.
  public static void publish() {
    Log.d(TAG, "publishing all campaigns");
    for (Campaign campaign : local.getAll()) {
      if (!campaign.isDefault() && campaign.isPublished()) {
        campaign.publish();
      }
    }
  }

  // Private metbods.

  // This should only be called from the main activity.
  public static void load(Context context) {
    loadLocal(context);
    loadRemote(context);
  }

  private static List<String> campaignIds() {
    List<String> ids = new ArrayList<>();
    ids.add(defaultCampaign.getCampaignId());
    ids.addAll(local.getCampaigns()
        .stream()
        .map(Campaign::getCampaignId)
        .collect(Collectors.toList()));
    if (Misc.onEmulator()) {
      ids.addAll(remote.getCampaigns()
          .stream()
          .map(Campaign::getCampaignId)
          .collect(Collectors.toList()));
    } else {
      ids.addAll(remote.getCampaigns()
          .stream()
          .map(Campaign::getCampaignId)
          .collect(Collectors.toList()));
    }

    return ids;
  }

  private static Optional<Campaign> campaign(String campaignId) {
    if (defaultCampaign.getCampaignId().equals(campaignId)) {
      return Optional.of(defaultCampaign);
    }

    return local.get(campaignId).or(remote.get(campaignId));
  }

  private static void loadLocal(Context context) {
    if (local != null) {
      Log.d(TAG, "local campaigns already loaded");
      return;
    }

    Log.d(TAG, "loading lcoal campaigns");
    local = new CampaignsData(context, true);
  }

  private static void loadRemote(Context context) {
    if (remote != null) {
      Log.d(TAG, "remote campaigns already loaded");
      return;
    }

    Log.d(TAG, "loading lcoal campaigns");
    remote = new CampaignsData(context, false);
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
