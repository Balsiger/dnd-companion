/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.proto.Entry;
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

  public CreaturesData(CompanionContext companionContext) {
    super(companionContext, DataBaseContentProvider.CREATURES_LOCAL, true);
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

    if (creatureById != null) {
      if (creatureById.containsKey(creature.getCreatureId())) {
        creatureById.get(creature.getCreatureId()).setValue(Optional.of(creature));
      }
    }
  }

  @Override
  public void remove(Creature creature) {
    super.remove(creature);

    if (creatureById.containsKey(creature.getCreatureId())) {
      creatureById.get(creature.getCreatureId()).setValue(Optional.empty());
    }
  }

  public List<String> ids(String campaignId) {
    return getCreatures(campaignId).stream()
        .map(Creature::getCreatureId)
        .collect(Collectors.toList());
  }

  public List<Creature> orphaned() {
    return getAll().stream()
        .filter(this::isOrphaned)
        .collect(Collectors.toList());
  }

  private boolean isOrphaned(Creature creature) {
    return creature.getCampaignId().equals(companionContext.campaigns().getDefaultCampaign().getCampaignId())
        || (!companionContext.campaigns().has(creature.getCampaignId(), true)
            && !companionContext.campaigns().has(creature.getCampaignId(), false));
  }

  @Override
  protected Optional<Creature> parseEntry(long id, byte[] blob) {
    try {
      return Optional.of(Creature.fromProto(companionContext, id,
          Entry.CreatureProto.getDefaultInstance().getParserForType().parseFrom(blob)));
    } catch (InvalidProtocolBufferException e) {
      Status.toast("Cannot parse proto for campaign: " + e);
      return Optional.empty();
    }
  }
}
