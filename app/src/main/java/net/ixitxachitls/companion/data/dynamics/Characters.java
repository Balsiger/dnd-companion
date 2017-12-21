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
import android.widget.Toast;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.net.CompanionPublisher;
import net.ixitxachitls.companion.net.CompanionSubscriber;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Information and storage for all characters.
 */
public class Characters extends StoredEntries<Character> {
  private static final String TAG = "Characters";

  private static Characters local;
  private static Characters remote;

  // Data.
  private static Map<String, String> campaignIdsByCharacterId = new ConcurrentHashMap<>();

  // Live data storages.
  private static Map<String, MutableLiveData<ImmutableList<Character>>> charactersByCampaignId
      = new ConcurrentHashMap<>();
  private static Map<String, MutableLiveData<Optional<Character>>> charactersByCharacterId
      = new ConcurrentHashMap<>();

  private MutableLiveData<ImmutableList<Character>> allCharacters = new MutableLiveData<>();

  // Data accessors.

  public static LiveData<Optional<Character>> getCharacter(String characterId) {
    if (!charactersByCharacterId.containsKey(characterId)) {
      MutableLiveData<Optional<Character>> character = new MutableLiveData<>();
      character.setValue(local.get(characterId).or(remote.get(characterId)));
      charactersByCharacterId.put(characterId, character);
    }

    return charactersByCharacterId.get(characterId);
  }

  public static LiveData<Optional<Character>> createCharacter(String campaignId) {
    MutableLiveData<Optional<Character>> live = new MutableLiveData<>();
    Character character = Character.createNew(campaignId);
    live.setValue(Optional.of(character));
    charactersByCharacterId.put(character.getCharacterId(), live);

    return live;
  }

  public static LiveData<ImmutableList<Character>> getLocalCharacters() {
    return local.getCharacters();
  }

  public static boolean hasLocalCampaignCharacters(String campaignId) {
    return !local.getCharacters(campaignId).isEmpty();
  }

  public static LiveData<ImmutableList<Character>> getRemoteCharacters() {
    return remote.getCharacters();
  }

  public static boolean hasRemoteCampaignCharacters(String campaignId) {
    return !remote.getCharacters(campaignId).isEmpty();
  }

  public static LiveData<ImmutableList<Character>> getCampaignCharacters(String campaignId) {
    synchronized (charactersByCampaignId) {
      if (!charactersByCampaignId.containsKey(campaignId)) {
        MutableLiveData<ImmutableList<Character>> data = new MutableLiveData<>();
        data.setValue(ImmutableList.copyOf(campaignCharacters(campaignId)));
        charactersByCampaignId.put(campaignId, data);
      }
    }

    return charactersByCampaignId.get(campaignId);
  }

  public static long getLocalIdFor(String characterId) {
    return local().getIdFor(characterId);
  }

  public static long getRemoteIdFor(String characterId) {
    return remote().getIdFor(characterId);
  }

  // Data mutations.

  public static void addCharacter(boolean local, Character character) {
    Characters.get(local).add(character);
    updateCharacter(character);
  }

  public static void updateCharacter(Character character) {
    String campaignId = campaignIdsByCharacterId.get(character.getCharacterId());
    if (character.getCampaignId().equals(campaignId)) {
      if (!character.equals(charactersByCharacterId.get(character.getCharacterId()))) {
        // Update characters by campaign to singla that once of it's characters changed
        // (if you need more fine grained updates, get live data for individual characters.)
        updateCharacters(character.getCampaignId());
      }
    } else if (campaignId == null) {
      // Update campaign and all characters for this newly added character.
      campaignIdsByCharacterId.put(character.getCharacterId(), character.getCampaignId());
      updateCharacters(character.getCampaignId());
      MutableLiveData<ImmutableList<Character>> all = get(character.isLocal()).allCharacters;
      ImmutableList.Builder<Character> characters = new ImmutableList.Builder<>();
      characters.addAll(all.getValue());
      characters.add(character);
      all.setValue(characters.build());
    } else {
      // Update characters by campaign if the character was moved to a different campaign.
      updateCharacters(campaignId);
      updateCharacters(character.getCampaignId());
    }

    // Update all live data for the individual character.
    if (charactersByCharacterId.containsKey(character.getCharacterId())) {
      charactersByCharacterId.get(character.getCharacterId()).setValue(Optional.of(character));
    }
  }

  public static void removeCharacter(Character character) {
    local.remove(character);

    // Unpublish the character.
    CompanionPublisher.get().delete(character);
    CompanionSubscriber.get().delete(character);

    // Update live data.
    campaignIdsByCharacterId.remove(character.getCharacterId());
    updateCharacters(character.getCampaignId());
    if (charactersByCharacterId.containsKey(character.getCharacterId())) {
      charactersByCharacterId.get(character.getCharacterId()).setValue(Optional.absent());
    }
  }

  public static void removeRemote(String characterId) {
    remote().remove(characterId);

    // Update live data.
    String campaignId = campaignIdsByCharacterId.get(characterId);
    if (campaignId != null) {
      campaignIdsByCharacterId.remove(characterId);
      updateCharacters(campaignId);
    }
    if (charactersByCharacterId.containsKey(characterId)) {
      charactersByCharacterId.get(characterId).setValue(Optional.absent());
    }
  }

  // Publishing characters.

  public static void publish() {
    Log.d(TAG, "publishing all local characters");
    for (Character character : Characters.getLocalCharacters().getValue()) {
      character.publish();
      Images.get(character.isLocal()).publishImageFor(character);
    }
  }

  public static void publish(String campaignId) {
    Log.d(TAG, "publishing characters of campaign " + campaignId);
    for (Character character : Characters.getLocalCampaignCharacters(campaignId)) {
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

  private Characters(Context context, boolean local) {
    super(context, local ?
            DataBaseContentProvider.CHARACTERS_LOCAL : DataBaseContentProvider.CHARACTERS_REMOTE,
        local);

    // Fill live data storages (the remaining storages will be filled on a per use bases, to allow
    // to serve live data for future characters as well.
    Collection<Character> all = getAll();
    allCharacters.setValue(ImmutableList.copyOf(all));

    for (Character character : all) {
      campaignIdsByCharacterId.put(character.getCharacterId(), character.getCampaignId());
    }
  }

  private static void loadLocal(Context context) {
    if (local != null) {
      Log.d("Characters", "local characters already loaded");
      return;
    }

    Log.d("Characters", "loading local characters");
    local = new Characters(context, true);
  }

  private static void loadRemote(Context context) {
    if (remote != null) {
      Log.d("Characters", "remote characters already loaded");
      return;
    }

    Log.d("Characters", "loading remote characters");
    remote = new Characters(context, false);
  }

  private static Characters local() {
    Preconditions.checkNotNull(local, "local characters have to be loaded!");
    return local;
  }

  private static Characters remote() {
    Preconditions.checkNotNull(local, "remote characters have to be loaded!");
    return remote;
  }

  private static Characters get(boolean local) {
    return local ? Characters.local : Characters.remote;
  }

  private static ImmutableList<Character> campaignCharacters(String campaignId) {
    Optional<Campaign> campaign = Campaigns.getCampaign(campaignId).getValue();
    if (campaign.isPresent() && campaign.get().isDefault()) {
      return local.getOrphanedCharacters();
    }

    List<Character> characters = local.getCharacters(campaignId);
    Set<String> ids = new HashSet<>();
    for (Character character : characters) {
      ids.add(character.getCharacterId());
    }

    // Add the remote characters we don't already have.
    for (Character character : remote.getCharacters(campaignId)) {
      if (!ids.contains(character.getCharacterId())) {
        characters.add(character);
      }
    }

    return ImmutableList.copyOf(characters);
  }

  private static List<Character> getLocalCampaignCharacters(String campaignId) {
    return local.getCharacters(campaignId);
  }

  private static List<Character> remoteCampaignCharacters(String campaignId) {
    return remote.getCharacters(campaignId);
  }

  private static void updateCharacters(String campaignId) {
    if (charactersByCampaignId.containsKey(campaignId)) {
      charactersByCampaignId.get(campaignId).setValue(campaignCharacters(campaignId));
    }
  }

  // Member methods.

  private LiveData<ImmutableList<Character>> getCharacters() {
    return allCharacters;
  }

  private List<Character> getCharacters(String campaignId) {
    List<Character> characters = new ArrayList<>();
    for (Character character : getAll()) {
      if (character.getCampaignId().equals(campaignId)) {
        characters.add(character);
      }
    }
    return characters;
  }

  private ImmutableList<Character> getOrphanedCharacters() {
    ImmutableList.Builder<Character> characters = new ImmutableList.Builder<>();
    for (Character character : getAll()) {
      if (!Campaigns.has(character.getCampaignId(), isLocal())) {
        characters.add(character);
      }
    }

    return characters.build();
  }

  @Override
  protected Optional<Character> parseEntry(long id, byte[] blob) {
    try {
      return Optional.of(
          Character.fromProto(id, isLocal(), Data.CharacterProto.getDefaultInstance().getParserForType()
              .parseFrom(blob)));
    } catch (InvalidProtocolBufferException e) {
      Log.e(TAG, "Cannot parse proto for campaign: " + e);
      Toast.makeText(context, "Cannot parse proto for campaign: " + e, Toast.LENGTH_LONG);
      return Optional.absent();
    }
  }

  @Override
  public void remove(Character character) {
    super.remove(character);
    Images.get(isLocal()).remove(Character.TYPE, character.getCharacterId());
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
