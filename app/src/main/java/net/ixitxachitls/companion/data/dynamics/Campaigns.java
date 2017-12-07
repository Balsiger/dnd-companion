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
import android.widget.Toast;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.util.Misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Access to and utilities for camapigns.
 */
public class Campaigns extends StoredEntries<Campaign> {
  private static final String TAG = "Campaigns";

  private static Campaigns local;
  private static Campaigns remote;

  public static final Campaign defaultCampaign = Campaign.createDefault();

  // Live data storages.
  private static Map<String, MutableLiveData<Optional<Campaign>>> campaignByCampaignId =
      new ConcurrentHashMap<>();

  private MutableLiveData<ImmutableList<Campaign>> allCampaigns = new MutableLiveData<>();

  // Data accessors.

  public static LiveData<Optional<Campaign>> getCampaign(String campaignId) {
    if (!campaignByCampaignId.containsKey(campaignId)) {
      MutableLiveData<Optional<Campaign>> campaign = new MutableLiveData<>();
      campaign.setValue(campaign(campaignId));
      campaignByCampaignId.put(campaignId, campaign);
    }

    return campaignByCampaignId.get(campaignId);
  }

  public static LiveData<ImmutableList<Campaign>> getLocalCampaigns() {
    return local.getCampaigns();
  }

  public static LiveData<ImmutableList<Campaign>> getRemoteCampaigns() {
    return remote.getCampaigns();
  }

  public static boolean hasAnyPublished() {
    for (Campaign campaign : local.getAll()) {
      if (campaign.isPublished()) {
        return true;
      }
    }

    return false;
  }

  // TODO(merlin): Try to get rid of this?
  public static Campaigns get(boolean local) {
    return local ? Campaigns.local : Campaigns.remote;
  }

  // This is not live data.
  public static List<Campaign> getCampaignsByServer(String serverId) {
    List<Campaign> filtered = new ArrayList<>();
    for (Campaign campaign : remote.getCampaigns().getValue()) {
      if (campaign.getCampaignId().startsWith(serverId)) {
        filtered.add(campaign);
      }
    }

    return filtered;
  }

  // This is not live data.
  public static List<Campaign> getAllCampaigns() {
    List<Campaign> campaigns = local.getCampaigns().getValue();
    campaigns.addAll(remote.getCampaigns().getValue());
    return campaigns;
  }

  public static long getLocalIdFor(String campaignId) {
    return local().getIdFor(campaignId);
  }

  public static long getRemoteIdFor(String campaignId) {
    return remote().getIdFor(campaignId);
  }

  // Data mutations.

  public static void updateCampaign(Campaign campaign) {
    if (campaignByCampaignId.containsKey(campaign.getCampaignId())) {
      campaignByCampaignId.get(campaign.getCampaignId()).setValue(Optional.of(campaign));
    }
  }

  public static void addCampaign(Campaign campaign) {
    // Might also need adding somewhere else?
    updateCampaign(campaign);

    List<Campaign> campaigns =
        new ArrayList<>(Campaigns.get(campaign.isLocal()).allCampaigns.getValue());
    campaigns.add(campaign);
    Campaigns.get(campaign.isLocal()).allCampaigns.setValue(ImmutableList.copyOf(campaigns));
  }

  public static void removeCampaign(String campaignId) {
    Campaign campaign = local.remove(campaignId);
    removeCampaign(campaign);
  }

  public static void removeCampaign(Campaign campaign) {
    local.remove(campaign);
    if (campaignByCampaignId.containsKey(campaign.getCampaignId())) {
      campaignByCampaignId.get(campaign.getCampaignId()).setValue(Optional.absent());
    }

    List<Campaign> campaigns =
        new ArrayList<>(Campaigns.get(campaign.isLocal()).allCampaigns.getValue());
    campaigns.remove(campaign);
    Campaigns.get(campaign.isLocal()).allCampaigns.setValue(ImmutableList.copyOf(campaigns));
  }

  // Publishing.

  public static void publish() {
    Log.d(TAG, "publishing all campaigns");
    for (Campaign campaign : local.getAll()) {
      if (!campaign.isDefault() && campaign.isPublished()) {
        campaign.publish();
      }
    }
  }

  // Member methods.

  private LiveData<ImmutableList<Campaign>> getCampaigns() {
    return allCampaigns;
  }

  // Private metbods.

  private Campaigns(Context context, boolean local) {
    super(context,
        local ? DataBaseContentProvider.CAMPAIGNS_LOCAL : DataBaseContentProvider.CAMPAIGNS_REMOTE,
        local);

    allCampaigns.setValue(ImmutableList.copyOf(getAll()));
  }

  // This should only be called from the main activity.
  public static void load(Context context) {
    loadLocal(context);
    loadRemote(context);
  }

  private static Optional<Campaign> campaign(String campaignId) {
    if (defaultCampaign.getCampaignId().equals(campaignId)) {
      return Optional.of(defaultCampaign);
    }

    if (Misc.onEmulator() && !Misc.emulatingLocal()) {
      return remote.get(campaignId);
    }

    return local.get(campaignId).or(remote.get(campaignId));
  }

  private static Campaigns local() {
    Preconditions.checkNotNull(local, "local campaigns have to be loaded!");
    return local;
  }

  private static Campaigns remote() {
    Preconditions.checkNotNull(remote, "remote campaigns have to be loaded!");
    return remote;
  }

  private static void loadLocal(Context context) {
    if (local != null) {
      Log.d(TAG, "local campaigns already loaded");
      return;
    }

    Log.d(TAG, "loading lcoal campaigns");
    local = new Campaigns(context, true);
  }

  private static void loadRemote(Context context) {
    if (remote != null) {
      Log.d(TAG, "remote campaigns already loaded");
      return;
    }

    Log.d(TAG, "loading lcoal campaigns");
    remote = new Campaigns(context, false);
  }

  protected Optional<Campaign> parseEntry(long id, byte[] blob) {
    try {
      return Optional.of(
          Campaign.fromProto(id, isLocal(),
              Data.CampaignProto.getDefaultInstance().getParserForType()
                  .parseFrom(blob)));
    } catch (InvalidProtocolBufferException e) {
      Log.e(TAG, "Cannot parse proto for campaign: " + e);
      Toast.makeText(context, "Cannot parse proto for campaign: " + e, Toast.LENGTH_LONG);
      return Optional.absent();
    }
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
