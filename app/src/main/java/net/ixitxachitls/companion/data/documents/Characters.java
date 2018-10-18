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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.data.CompanionContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Model for all characters available to a user.
 */
public class Characters extends Documents<Characters> {
  protected static final String PATH = "characters";

  private final Map<String, Character> charactersByCharacterId = new HashMap<>();
  private final Multimap<String, Character> charactersByCampaignId = HashMultimap.create();
  private final Multimap<String, Character> charactersByPlayerId = HashMultimap.create();

  public Characters(CompanionContext context) {
    super(context);
  }

  public Character create(String campaignId) {
    Character character = Character.create(context, campaignId);
    character.whenReady(() -> {
      charactersByCharacterId.put(character.getId(), character);
      charactersByCampaignId.put(campaignId, character);
      charactersByPlayerId.put(character.getPlayer().getId(), character);
      updated();
    });

    return character;
  }

  public Collection<Character> getCampaignCharacters(String campaignId) {
    return charactersByCampaignId.get(campaignId);
  }

  public Optional<Character> get(String characterId) {
    return Optional.ofNullable(charactersByCharacterId.get(characterId));
  }

  public Collection<Character> getAll() {
    return charactersByCharacterId.values();
  }

  public int maxPartyLevel(String campaignId) {
    int max = 1;
    for (Character character : getCampaignCharacters(campaignId)) {
      max = Math.max(max, character.getLevel());
    }

    return max;
  }

  public int minPartyLevel(String campaignId) {
    int min = Integer.MAX_VALUE;
    for (Character character : getCampaignCharacters(campaignId)) {
      min = Math.min(min, character.getLevel());
    }

    if (min == Integer.MAX_VALUE) {
      return 1;
    }

    return min;
  }

  public static boolean isCloseECL(int level, int minPartyLevel, int maxPartyLevel) {
    return minPartyLevel <= level && maxPartyLevel >= level;
  }

  public void addPlayers(Campaign campaign) {
    addPlayer(campaign.getDm());

    for (String email : campaign.getInvites()) {
      context.invites().doWithUserId(email, (id) -> {
        User player = context.users().get(id);
        player.whenReady(() -> addPlayer(player));
      });
    }
  }

  public void addPlayer(User player) {
    if (!charactersByPlayerId.containsKey(player.getId())) {
      CollectionReference reference = db.collection(player.getId() + "/" + PATH);
      reference.get().addOnSuccessListener(task -> readCharacters(player, task.getDocuments()));
      reference.addSnapshotListener((s, e) -> readCharacters(player, s.getDocuments()));
    }
  }

  private void readCharacters(User player, List<DocumentSnapshot> snapshots) {
    clearPlayerData(player);

    for (DocumentSnapshot snapshot : snapshots) {
      Character character = Character.fromData(context, player, snapshot);
      charactersByPlayerId.put(player.getId(), character);
      charactersByCampaignId.put(character.getCampaignId(), character);
      charactersByCharacterId.put(character.getId(), character);
    }

    updated();
  }

  private void clearPlayerData(User player) {
    charactersByPlayerId.removeAll(player.getId());
    List<Character> toRemove = charactersByCampaignId.values().stream()
        .filter(c -> c.getPlayer() == player)
        .collect(Collectors.toList());
    for (Character character : toRemove) {
      charactersByCampaignId.remove(character.getCampaignId(), character);
    }
    toRemove = charactersByCharacterId.values().stream()
        .filter(c -> c.getPlayer() == player)
        .collect(Collectors.toList());
    for (Character character : toRemove) {
      charactersByCharacterId.remove(character.getId(), character);
    }

    updated();
  }

  /*
  public void update(Character character) {
    Status.log("updating character " + character);

    // We have to consider that the character changed the campaign id, but not the character id,
    // thus we have to update all character id by campaign id lists.
    for (Map.Entry<String, MutableLiveData<ImmutableList<String>>> entry
        : characterIdsByCampaignId.entrySet()) {
      // As Character is not immutable, we can't know whether Character has really changed or not.
      entry.getValue().setValue(ImmutableList.copyOf(characterIds(entry.getKey())));
    }

    if (character.isLocal()) {
      local.update(character);
    } else {
      remote.update(character);
    }
  }

  public void add(Character character) {
    Status.log("adding character " + character);

    if (character.isLocal()) {
      local.add(character);
    } else {
      remote.add(character);
    }

    if (characterIdsByCampaignId.containsKey(character.getCampaignId())) {
      LiveDataUtils.setValueIfChanged(characterIdsByCampaignId.get(character.getCampaignId()),
          ImmutableList.copyOf(characterIds(character.getCampaignId())));
    }
  }

  protected void remove(String characterId, boolean isLocal) {
    Optional<Character> character;
    if (isLocal) {
      character = local.getCharacter(characterId).getValue();
    } else {
      character = remote.getCharacter(characterId).getValue();
    }

    if (character.isPresent()) {
      remove(character.get());
    }
  }

  protected void remove(Character character) {
    Status.log("removing character " + character);

    if (character.isLocal()) {
      local.remove(character);
    } else {
      remote.remove(character);
    }

    // Update live data.
    if (characterIdsByCampaignId.containsKey(character.getCampaignId())) {
      LiveDataUtils.setValueIfChanged(characterIdsByCampaignId.get(character.getCampaignId()),
          ImmutableList.copyOf(characterIds(character.getCampaignId())));
    }

    context.histories().removed(character.getName(), character.getCampaign().get().getDate(),
        character.getCharacterId(), character.campaignId);
  }

  private List<String> orphaned() {
    List<String> ids = local.orphaned().stream()
        .map(Character::getCharacterId)
        .collect(Collectors.toList());
    ids.addAll(remote.orphaned().stream()
        .map(Character::getCharacterId)
        .collect(Collectors.toList()));

    return ids;
  }


   */
}
