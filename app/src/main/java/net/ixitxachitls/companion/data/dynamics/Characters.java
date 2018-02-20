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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.net.CompanionMessenger;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Information and storage for all characters.
 */
public class Characters {
  private static final String TAG = "Characters";
  private static final String REMOTE = "REMOTE:";

  private static CharactersData local;
  private static CharactersData remote;

  // Live data storages.
  private static final Map<String, MutableLiveData<ImmutableList<String>>>
      characterIdsByCampaignId = new ConcurrentHashMap<>();

  // Data accessors.

  public static LiveData<Optional<Character>> getCharacter(String characterId) {
    if (characterId.startsWith(REMOTE)) {
      return remote.getCharacter(characterId.substring(REMOTE.length()));
    }

    if (local.hasCharacter(characterId)) {
      return local.getCharacter(characterId);
    }

    return remote.getCharacter(characterId);
  }

  public static boolean has(Character character) {
    return has(character.getCharacterId(), character.isLocal());
  }

  public static boolean has(String characterId, boolean isLocal) {
    if (isLocal) {
      return local.has(characterId);
    }

    return remote.has(characterId);
  }

  public static boolean hasLocalCharacterForCampaign(String campaignId) {
    return local.hasCharacterForCampaign(campaignId);
  }

  public static LiveData<ImmutableList<String>> getCampaignCharacterIds(String campaignId) {
    if (characterIdsByCampaignId.containsKey(campaignId)) {
      return characterIdsByCampaignId.get(campaignId);
    }

    MutableLiveData<ImmutableList<String>> ids = new MutableLiveData<>();
    LiveDataUtils.setValueIfChanged(ids, ImmutableList.copyOf(characterIds(campaignId)));
    characterIdsByCampaignId.put(campaignId, ids);

    return ids;
  }

  public static Collection<Character> getLocalCharacters() {
    return local.getAll();
  }

  public static long getLocalIdFor(String characterId) {
    return local.getIdFor(characterId);
  }

  public static long getRemoteIdFor(String characterId) {
    return remote.getIdFor(characterId);
  }

  // Data mutations.

  public static void update(Character character) {
    Log.d(TAG, "updating character " + character);

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

  public static void add(Character character) {
    Log.d(TAG, "adding character " + character);

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

  public static void remove(String characterId, boolean isLocal) {
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

  public static void remove(Character character) {
    Log.d(TAG, "removing character " + character);

    if (character.isLocal()) {
      local.remove(character);
    } else {
      remote.remove(character);
    }
    local.remove(character);

    // Unpublish the character.
    CompanionMessenger.get().sendDeletion(character);

    // Update live data.
    if (characterIdsByCampaignId.containsKey(character.getCampaignId())) {
      if (character.isLocal()) {
        LiveDataUtils.setValueIfChanged(characterIdsByCampaignId.get(character.getCampaignId()),
            ImmutableList.copyOf(local.ids(character.getCampaignId())));
      } else {
        LiveDataUtils.setValueIfChanged(characterIdsByCampaignId.get(character.getCampaignId()),
            ImmutableList.copyOf(remote.ids(character.getCampaignId())));
      }
    }

    Images.get(character.isLocal()).remove(Character.TABLE, character.getCharacterId());
  }

  // Publishing characters.
  // TODO(merlin): Move this over to the publishers.

  public static void publish() {
    Log.d(TAG, "publishing all local characters");
    for (Character character : local.getAll()) {
      character.publish();
      Images.get(character.isLocal()).publishImageFor(character);
    }
  }

  public static void publish(String campaignId) {
    Log.d(TAG, "publishing characters of campaign " + campaignId);
    for (Character character : local.getCharacters(campaignId)) {
      character.publish();
      Images.get(character.isLocal()).publishImageFor(character);
    }
  }

  // Private methods.

  // While this method is public, it should only be called in the main application.
  public static void load(Context context) {
    loadLocal(context);
    loadRemote(context);
  }

  private static void loadLocal(Context context) {
    if (local != null) {
      Log.d(TAG, "local characters already loaded");
      return;
    }

    Log.d(TAG, "loading local characters");
    local = new CharactersData(context, true);
  }

  private static void loadRemote(Context context) {
    if (remote != null) {
      Log.d(TAG, "remote characters already loaded");
      return;
    }

    Log.d(TAG, "loading remote characters");
    remote = new CharactersData(context, false);
  }

  private static List<String> orphaned() {
    List<String> ids = local.orphaned().stream()
        .map(Character::getCharacterId)
        .collect(Collectors.toList());
    ids.addAll(remote.orphaned().stream()
        .map(Character::getCharacterId)
        .collect(Collectors.toList()));

    return ids;
  }

  private static List<String> characterIds(String campaignId) {
    if (campaignId.equals(Campaigns.defaultCampaign.getCampaignId())) {
      return orphaned();
    }

    List<String> ids = local.ids(campaignId);
    // Only show each charcter once, even if they are locallay and remotely available
    // (mostly for the emulator).
    for (String id : remote.ids(campaignId)) {
      if (id.startsWith(StoredEntries.REMOTE)) {
        if (!ids.contains(id.substring(StoredEntries.REMOTE.length()))) {
          ids.add(id);
        }
      } else if (!ids.contains(id)) {
        ids.add(id);
      }
    }

    return ids;
  }

  private static class CharacterComparator implements Comparator<Character> {
    @Override
    public int compare(Character first, Character second) {
      if (first.getId() == second.getId())
        return 0;

      int compare = first.getName().compareTo(second.getName());
      if (compare != 0) {
        return compare;
      }

      return Long.compare(first.getId(), second.getId());
    }

    @Override
    public boolean equals(Object obj) {
      return false;
    }
  }
}
