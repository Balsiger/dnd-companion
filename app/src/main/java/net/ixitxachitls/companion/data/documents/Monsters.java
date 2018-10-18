/*
 * Copyright (c) 2017-2018 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Tabletop Companion.
 *
 * The Tabletop Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Tabletop Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.documents;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.ixitxachitls.companion.data.CompanionContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Monsters available in the games.
 */
public class Monsters extends Documents<Monsters> {
  public Monsters(CompanionContext context) {
    super(context);
  }

  private final Map<String, Monster> creaturesByCreatureId = new HashMap<>();
  private final Multimap<String, Monster> creaturesByCampaignId = HashMultimap.create();

  public Collection<Monster> getCampaignCreatures(String campaignId) {
    return creaturesByCampaignId.get(campaignId);
  }

  public Optional<Monster> get(String creatureId) {
    return Optional.ofNullable(creaturesByCreatureId.get(creatureId));
  }

  public Collection<Monster> getAll() {
    return creaturesByCreatureId.values();
  }

  public Optional<? extends Creature<?>> getCreatureOrCharacter(String id) {
    Optional<? extends Creature<?>> creature = context.characters().get(id);
    if (!creature.isPresent()) {
      creature = get(id);
    }

    return creature;
  }

  public String nameFor(String id) {
    Optional<? extends Creature<?>> creature = getCreatureOrCharacter(id);
    if (creature.isPresent()) {
      return creature.get().getName();
    }

    return id;
  }

  /*

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


   */
}
