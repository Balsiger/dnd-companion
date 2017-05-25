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

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Access to and utilities for camapigns.
 */
public class Campaigns extends StoredEntries<Campaign> {

  private static final String TAG = "Campaigns";

  private static Campaigns local;
  private static Campaigns remote;

  private Campaign defaultCampaign;

  private Campaigns(Context context, boolean local) {
    super(context,
        local ? DataBaseContentProvider.CAMPAIGNS_LOCAL : DataBaseContentProvider.CAMPAIGNS_REMOTE,
        local);
  }

  public static Campaigns local() {
    Preconditions.checkNotNull(local, "local campaigns have to be loaded!");
    return local;
  }

  public static Campaigns remote() {
    Preconditions.checkNotNull(remote, "remote campaigns have to be loaded!");
    return remote;
  }

  public static Campaigns get(boolean local) {
    return local ? Campaigns.local : Campaigns.remote;
  }

  public static void load(Context context) {
    loadLocal(context);
    loadRemote(context);
  }

  private static void loadLocal(Context context) {
    if (local != null) {
      Log.d(TAG, "local campaigns already loaded");
      return;
    }

    Log.d(TAG, "loading lcoal campaigns");
    local = new Campaigns(context, true);

    // Add the default campaign.
    local.defaultCampaign = Campaign.createDefault();
    local.add(local.defaultCampaign);
  }

  private static void loadRemote(Context context) {
    if (remote != null) {
      Log.d(TAG, "remote campaigns already loaded");
      return;
    }

    Log.d(TAG, "loading lcoal campaigns");
    remote = new Campaigns(context, false);
  }

  public Campaign getCampaign(String id) {
    if (id.isEmpty()) {
      return defaultCampaign;
    }

    return get(id);
  }

  public boolean hasAnyPublished() {
    if (!isLocal()) {
      return false;
    }

    for (Campaign campaign : entriesById.values()) {
      if (campaign.isPublished()) {
        return true;
      }
    }

    return false;
  }

  /*
  private void add(Campaign campaign) {
    campaigns.add(campaign);
    campaignsByStorageId.put(campaign.getId(), campaign);
    campaignsByCampaignId.put(campaign.getCampaignId(), campaign);
  }
  */

  /*
  public void remove(Campaign campaign) {
    campaigns.remove(campaign);
    campaignsByStorageId.remove(campaign.getId());
    campaignsByCampaignId.remove(campaign.getCampaignId());

    context.getContentResolver().delete(table, "id = " + campaign.getId(), null);
  }
  */

  public List<Campaign> getCampaigns() {
    List<Campaign> campaigns = new ArrayList<>(getAll());
    Collections.sort(campaigns, new CampaignComparator());
    return campaigns;
  }

  public List<Campaign> getCampaigns(String serverId) {
    List<Campaign> filtered = new ArrayList<>();
    for (Campaign campaign : getAll()) {
      if (campaign.getCampaignId().startsWith(serverId)) {
        filtered.add(campaign);
      }
    }

    return filtered;
  }

  public void publish() {
    Log.d(TAG, "publishing all campaigns");
    for (Campaign campaign : getAll()) {
      if (!campaign.isDefault() && campaign.isPublished()) {
        campaign.publish();
      }
    }
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

  private class CampaignComparator implements Comparator<Campaign> {
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
