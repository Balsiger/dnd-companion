/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Data storage for all creatures (that are not also characters).
 */
public class CreaturesData extends StoredEntries<Creature> {

  private final Map<String, MutableLiveData<Optional<Creature>>> creatureById =
      new ConcurrentHashMap<>();
  private final Map<String, MutableLiveData<ImmutableList<String>>> creaturesByCampaignId =
      new ConcurrentHashMap<>();

  public CreaturesData(CompanionApplication application) {
    super(application, DataBaseContentProvider.CREATURES_LOCAL, true);
  }

  public LiveData<Optional<Creature>> getCreature(String creatureId) {
    if (creatureById.containsKey(creatureId)) {
      return creatureById.get(creatureId);
    }

    MutableLiveData<Optional<Creature>> creature = new MutableLiveData<>();
    creatureById.put(creatureId, creature);
    creature.setValue(get(creatureId));

    return creature;
  }

  public boolean hasCreature(String creatureId) {
    return has(creatureId);
  }

  public List<Creature> getCreatures(String campaignId) {
    return getAll().stream()
        .filter(c -> c.getCampaignId().equals(campaignId))
        .collect(Collectors.toList());
  }

  public boolean hasCreatureForCampaign(String campaignId) {
    for (Creature creature : getAll()) {
      if (creature.getCampaignId().equals(campaignId)) {
        return true;
      }
    }

    return false;
  }

  void update(Creature creature) {
    if (creatureById.containsKey(creature.getCreatureId())) {
      creatureById.get(creature.getCreatureId()).setValue(Optional.of(creature));
    }
  }

  @Override
  public void add(Creature creature) {
    super.add(creature);

    // We need to check for null since ids will be setup only after the super constructor is run.
    if (creaturesByCampaignId != null) {
      MutableLiveData<ImmutableList<String>> ids =
          creaturesByCampaignId.get(creature.getCampaignId());
      if (ids == null) {
        ids = new MutableLiveData<>();
        creaturesByCampaignId.put(creature.getCampaignId(), ids);
      }

      LiveDataUtils.setValueIfChanged(ids, ImmutableList.copyOf(ids(creature.getCampaignId())));
    }
  }

  public List<String> ids(String campaignId) {
    return getCreatures(campaignId).stream()
        .map(Creature::getCreatureId)
        .collect(Collectors.toList());
  }

  public List<Creature> orphaned() {
    return getAll().stream()
        .filter(CreaturesData::isOrphaned)
        .collect(Collectors.toList());
  }

  private static boolean isOrphaned(Creature creature) {
    return creature.getCampaignId().equals(Campaigns.defaultCampaign.getCampaignId())
        || (!Campaigns.has(creature.getCampaignId(), true)
            && !Campaigns.has(creature.getCampaignId(), false));
  }

  @Override
  protected Optional<Creature> parseEntry(long id, byte[] blob) {
    try {
      return Optional.of(
          Creature.fromProto(id, Data.CreatureProto.getDefaultInstance().getParserForType()
              .parseFrom(blob)));
    } catch (InvalidProtocolBufferException e) {
      Status.toast("Cannot parse proto for campaign: " + e);
      return Optional.empty();
    }
  }
}
