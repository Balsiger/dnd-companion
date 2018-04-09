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
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Data;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A class encapsulating all data for a set of campaigns.
 */
public class CampaignsData extends StoredEntries<Campaign> {

  private final MutableLiveData<ImmutableList<String>> ids = new MutableLiveData<>();
  private final Map<String, MutableLiveData<Optional<Campaign>>> campaignById =
      new ConcurrentHashMap<>();

  CampaignsData(Data data, boolean local) {
    super(data,
        local ? DataBaseContentProvider.CAMPAIGNS_LOCAL : DataBaseContentProvider.CAMPAIGNS_REMOTE,
        local);

    ids.setValue(ImmutableList.copyOf(ids()));
  }

  List<Campaign> getCampaigns() {
    return new ArrayList<>(getAll());
  }

  LiveData<Optional<Campaign>> getCampaign(String campaignId) {
    if (campaignById.containsKey(campaignId)) {
      return campaignById.get(campaignId);
    }

    MutableLiveData<Optional<Campaign>> campaign = new MutableLiveData<>();
    campaignById.put(campaignId, campaign);
    campaign.setValue(get(campaignId));

    return campaign;
  }

  boolean hasCampaign(String campaignId) {
    return ids.getValue().contains(campaignId);
  }

  void update(Campaign campaign) {
    if (campaignById.containsKey(campaign.getCampaignId())) {
      campaignById.get(campaign.getCampaignId()).setValue(Optional.of(campaign));
    }
  }

  @Override
  public void add(Campaign campaign) {
    super.add(campaign);

    // We need to check for null since ids will be setup only after the super constructor is run.
    if (ids != null) {
      LiveDataUtils.setValueIfChanged(ids, ImmutableList.copyOf(ids()));
      if (campaignById.containsKey(campaign.getCampaignId())) {
        campaignById.get(campaign.getCampaignId()).setValue(Optional.of(campaign));
      }
    }
  }

  @Override
  public void remove(Campaign campaign) {
    super.remove(campaign);

    LiveDataUtils.setValueIfChanged(ids, ImmutableList.copyOf(ids()));
    if (campaignById.containsKey(campaign.getCampaignId())) {
      campaignById.get(campaign.getCampaignId()).setValue(Optional.empty());
    }
  }

  private List<String> ids() {
    return getAll().stream().map(Campaign::getCampaignId).collect(Collectors.toList());
  }

  protected Optional<Campaign> parseEntry(long id, byte[] blob) {
    try {
      Entry.CampaignProto proto = Entry.CampaignProto.getDefaultInstance().getParserForType()
          .parseFrom(blob);
      return Optional.of(isLocal()
          ? LocalCampaign.fromProto(data, id, proto)
          : RemoteCampaign.fromProto(data, id, proto));
    } catch (InvalidProtocolBufferException e) {
      Status.toast("Cannot parse proto for campaign: " + e);
      return Optional.empty();
    }
  }
}
