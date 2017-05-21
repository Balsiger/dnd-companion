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
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBase;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Access to and utilities for camapigns.
 */
public class Campaigns {

  private static final String TAG = "Campaigns";

  private static Campaigns local;
  private static Campaigns remote;

  private final Context context;
  private final Uri table;
  private Campaign defaultCampaign;
  private final Map<Long, Campaign> campaignsByStorageId = new HashMap<>();
  private final Map<String, Campaign> campaignsByCampaignId = new HashMap<>();
  private final List<Campaign> campaigns = new ArrayList<>();

  private Campaigns(Context context, Uri table) {
    this.context = context;
    this.table = table;

    Cursor cursor = context.getContentResolver().query(table, DataBase.COLUMNS, null, null, null);
    while(cursor.moveToNext()) {
      try {
        Campaign campaign =
            Campaign.fromProto(cursor.getLong(cursor.getColumnIndex("_id")),
                Data.CampaignProto.getDefaultInstance().getParserForType()
                    .parseFrom(cursor.getBlob(cursor.getColumnIndex(DataBase.COLUMN_PROTO))));
        add(campaign);
      } catch (InvalidProtocolBufferException e) {
        Log.e(TAG, "Cannot parse proto for campaign: " + e);
        Toast.makeText(context, "Cannot parse proto for campaign: " + e, Toast.LENGTH_LONG);
      }
    }
  }

  public static Campaigns local() {
    Preconditions.checkNotNull(local, "local campaigns have to be loaded!");
    return local;
  }

  public static Campaigns remote() {
    Preconditions.checkNotNull(remote, "remote campaigns have to be loaded!");
    return remote;
  }

  public static Campaigns loadLocal(Context context) {
    if (local != null) {
      Log.d(TAG, "local campaigns already loaded");
      return local;
    }

    Log.d(TAG, "loading lcoal campaigns");
    local = new Campaigns(context, DataBaseContentProvider.CAMPAIGNS_LOCAL);

    // Add the default campaign.
    local.defaultCampaign = Campaign.createDefault();
    local.add(local.defaultCampaign);

    return local;
  }

  public static Campaigns loadRemote(Context context) {
    if (remote != null) {
      Log.d(TAG, "remote campaigns already loaded");
      return remote;
    }

    Log.d(TAG, "loading lcoal campaigns");
    remote = new Campaigns(context, DataBaseContentProvider.CAMPAIGNS_REMOTE);

    return remote;
  }

  public Campaign getCampaign(String id) {
    if (id.isEmpty()) {
      return defaultCampaign;
    }

    Preconditions.checkArgument(campaignsByCampaignId.containsKey(id));
    return campaignsByCampaignId.get(id);
  }

  public boolean hasCampaign(String id) {
    return campaignsByCampaignId.containsKey(id);
  }

  private void add(Campaign campaign) {
    campaigns.add(campaign);
    campaignsByStorageId.put(campaign.getId(), campaign);
    campaignsByCampaignId.put(campaign.getCampaignId(), campaign);
  }

  public void ensureAdded(Campaign campaign) {
    if (!campaignsByCampaignId.containsKey(campaign.getCampaignId())) {
      add(campaign);
    }
  }

  public void addOrUpdate(Campaign campaign) {
    if (campaignsByCampaignId.containsKey(campaign.getCampaignId())) {
      Campaign existingCampaign = campaignsByCampaignId.get(campaign.getCampaignId());
      campaign.mergeFrom(existingCampaign);
      campaigns.remove(existingCampaign);
    }

    add(campaign);
    campaign.store();
  }

  public void remove(Campaign campaign) {
    campaigns.remove(campaign);
    campaignsByStorageId.remove(campaign.getId());
    campaignsByCampaignId.remove(campaign.getCampaignId());

    context.getContentResolver().delete(table, "id = " + campaign.getId(), null);
  }

  public List<Campaign> getCampaigns() {
    Collections.sort(campaigns, new CampaignComparator());
    return campaigns;
  }

  public List<Campaign> getCampaigns(String serverId) {
    List<Campaign> filtered = new ArrayList<>();
    for (Campaign campaign : campaigns) {
      if (campaign.getCampaignId().startsWith(serverId)) {
        filtered.add(campaign);
      }
    }

    return filtered;
  }

  public void publish() {
    Log.d(TAG, "publishing all campaigns");
    for (Campaign campaign : campaigns) {
      if (campaign.isPublished()) {
        campaign.publish();
      }
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
