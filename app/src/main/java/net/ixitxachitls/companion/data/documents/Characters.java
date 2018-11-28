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

import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Model for all characters available to a user.
 */
public class Characters extends Documents<Characters> {
  protected static final String PATH = "characters";

  private final Map<String, Character> charactersByCharacterId = new HashMap<>();
  private final SortedSetMultimap<String, Character> charactersByCampaignId = TreeMultimap.create();
  private final SortedSetMultimap<String, Character> charactersByPlayerId = TreeMultimap.create();
  private final Set<String> userIdsLoading = new HashSet<>();

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
    if (charactersByCampaignId.containsKey(campaignId)) {
      return charactersByCampaignId.get(campaignId);
    }

    return Collections.emptyList();
  }

  public Collection<Character> getPlayerCharacters(String userId) {
    if (charactersByPlayerId.containsKey(userId)) {
      return charactersByPlayerId.get(userId);
    }

    return Collections.emptyList();
  }

  public Optional<Character> get(String characterId) {
    return Optional.ofNullable(charactersByCharacterId.get(characterId));
  }

  public Optional<? extends Creature> getCreature(String creatureId) {
    if (isCharacterId(creatureId)) {
      return get(creatureId);
    } else {
      return context.monsters().get(creatureId);
    }
  }

  public List<Character> getAll() {
    return charactersByCharacterId.values().stream()
        .sorted()
        .collect(Collectors.toList());
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

  public void addPlayers(Optional<Campaign> campaign) {
    if (campaign.isPresent()) {
      addPlayers(campaign.get());
    }
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

  public static boolean isCharacterId(String id) {
    return id.contains("/characters/");
  }

  public void addPlayer(User player) {
    if (!userIdsLoading.contains(player.getId())) {
      userIdsLoading.add(player.getId());
      CollectionReference reference = db.collection(player.getId() + "/" + PATH);
      reference.addSnapshotListener((s, e) -> {
        if (e == null) {
          readCharacters(player, s.getDocuments());
        } else {
          Status.exception("Cannot read characters!", e);
        }
      });
    }
  }

  private void readCharacters(User player, List<DocumentSnapshot> snapshots) {
    Map<String, Character> existing = clearPlayerData(player);

    for (DocumentSnapshot snapshot : snapshots) {
      // need to use full id here!
      Character character = existing.get(snapshot.getReference().getPath());
      if (character == null) {
        character = Character.fromData(context, player, snapshot);
      } else {
        character.snapshot = Optional.of(snapshot);
        character.read();
      }

      charactersByPlayerId.put(player.getId(), character);
      charactersByCampaignId.put(character.getCampaignId(), character);
      charactersByCharacterId.put(character.getId(), character);
    }

    updated();
  }

  private Map<String, Character> clearPlayerData(User player) {
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

    return charactersByPlayerId.removeAll(player.getId()).stream()
        .collect(Collectors.toMap(Character::getId, Function.identity()));
  }

  public void delete(Character character) {
    if (character.amPlayer() || character.amDM()) {
      delete(character.getId());
      charactersByCharacterId.remove(character.getId());
      charactersByCampaignId.remove(character.getCampaignId(), character);
      charactersByPlayerId.remove(character.getPlayer().getId(), character);

      updated();
    }
  }
}
