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

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Information and storage for all creatures. Creatures that are also characters are not
 * stored here and should be obtained from Characters.
 */
public class Creatures {

  private final CreaturesData local;
  private final CompanionContext companionContext;

  // Live data storages.
  private final Map<String, MutableLiveData<ImmutableList<String>>> creatureIdsByCampaignId =
      new ConcurrentHashMap<>();

  public Creatures(CompanionContext companionContext) {
    this.companionContext = companionContext;
    this.local = new CreaturesData(companionContext);
  }

  public Optional<? extends BaseCreature> getCreatureOrCharacter(String creatureId) {
    switch (StoredEntry.extractType(creatureId)) {
      case Character.TYPE:
        return companionContext.characters().getCharacter(creatureId).getValue();

      case Creature.TYPE:
        return getCreature(creatureId).getValue();

      default:
        return Optional.empty();
    }
  }

  // Data accessors.

  public LiveData<Optional<Creature>> getCreature(String creatureId) {
    return local.getCreature(creatureId);
  }

  public boolean has(Creature creature){
    return has(creature.getCreatureId());
  }

  public boolean has(String creatureId) {
    return local.has(creatureId);
  }

  public LiveData<ImmutableList<String>> getCampaignCreatureIds(String campaignId) {
    if (creatureIdsByCampaignId.containsKey(campaignId)) {
      return creatureIdsByCampaignId.get(campaignId);
    }

    MutableLiveData<ImmutableList<String>> ids = new MutableLiveData<>();
    LiveDataUtils.setValueIfChanged(ids, ImmutableList.copyOf(creatureIds(campaignId)));
    creatureIdsByCampaignId.put(campaignId, ids);

    return ids;
  }

  public List<Creature> getCampaignCreatures(String campaignId) {
    return local.getCreatures(campaignId);
  }

  public String nameFor(String id) {
    Optional<? extends BaseCreature> creature = getCreatureOrCharacter(id);
    if (creature.isPresent()) {
      return creature.get().getName();
    }

    return id;
  }

  // Data mutations.

  public void update(Creature creature) {
    Status.log("updating creature " + creature);

    // We cannot move a creature to a different campaign, so id lists cannot change.
    local.update(creature);
  }

  public void add(Creature creature) {
    Status.log("adding creature " + creature);

    local.add(creature);

    if (creatureIdsByCampaignId.containsKey(creature.getCampaignId())) {
      LiveDataUtils.setValueIfChanged(creatureIdsByCampaignId.get(creature.getCampaignId()),
          ImmutableList.copyOf(creatureIds(creature.getCampaignId())));
    }
  }

  public void remove(String creatureId) {
    Optional<Creature> creature = local.getCreature(creatureId).getValue();
    if (creature.isPresent()) {
      remove(creature.get());
    } else {
      Status.error("Cannot remove unknown creature " + creatureId);
    }
  }

  public void remove(Creature creature) {
    Status.log("removing creature " + creature);
    local.remove(creature);

    // Update live data.
    if (creatureIdsByCampaignId.containsKey(creature.getCampaignId())) {
      LiveDataUtils.setValueIfChanged(creatureIdsByCampaignId.get(creature.getCampaignId()),
            ImmutableList.copyOf(local.ids(creature.getCampaignId())));
    }

    companionContext.images(creature.isLocal()).remove(Creature.TABLE, creature.getCreatureId());
  }

  private List<String> orphaned() {
    return local.orphaned().stream()
        .map(Creature::getCreatureId)
        .collect(Collectors.toList());
  }

  private List<String> creatureIds(String campaignId) {
    if (campaignId.equals(companionContext.campaigns().getDefaultCampaign().getCampaignId())) {
      return orphaned();
    }

    return local.ids(campaignId);
  }
}
