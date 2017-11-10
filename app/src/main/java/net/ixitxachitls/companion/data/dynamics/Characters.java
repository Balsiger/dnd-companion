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
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.net.CompanionPublisher;
import net.ixitxachitls.companion.net.CompanionSubscriber;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Information and storage for all characters.
 */
public class Characters extends StoredEntries<Character> {
  private static final String TAG = "Characters";

  private static Characters local;
  private static Characters remote;

  private Characters(Context context, boolean local) {
    super(context, local ?
        DataBaseContentProvider.CHARACTERS_LOCAL : DataBaseContentProvider.CHARACTERS_REMOTE,
        local);
  }

  public static Optional<Character> getCharacter(String characterId) {
    return local.get(characterId).or(remote.get(characterId));
  }

  public static Optional<Character> getCharacter(String characterId, String campaignId) {
    if (characterId.isEmpty()) {
      return Optional.of(Character.createNew(campaignId));
    }

    return getCharacter(characterId);
  }

  public static List<Character> getLocalCharacters() {
    List<Character> characters = new ArrayList<>(local.getAll());
    Collections.sort(characters, new CharacterComparator());
    return characters;
  }

  public static List<Character> getAllCharacters(String campaignId) {
    Optional<Campaign> campaign = Campaigns.getCampaign(campaignId);
    if (campaign.isPresent() && campaign.get().isDefault()) {
      return local.getOrphanedCharacters();
    }

    List<Character> characters = local.getCharacters(campaignId);
    Set<String> ids = new HashSet<>();
    for (Character character : characters) {
      ids.add(character.getCharacterId());
    }
    for (Character character : remote.getCharacters(campaignId)) {
      if (!ids.contains(character.getCharacterId())) {
        characters.add(character);
      }
    }

    Collections.sort(characters, new CharacterComparator());
    return characters;
  }

  public static List<Character> getLocalCharacters(String campaignId) {
    return local.getCharacters(campaignId);
  }

  public static boolean hasLocalCharacters(String campaignId) {
    return !local.getCharacters(campaignId).isEmpty();
  }

  public static void publish() {
    Log.d(TAG, "publishing all local characters");
    for (Character character : Characters.getLocalCharacters()) {
      character.publish();
      Images.get(character.isLocal()).publishImageFor(character);
    }
  }

  public static void addCharacter(boolean local, Character character) {
    Characters.get(local).add(character);
  }

  public static void removeCharacter(Character character) {
    local.remove(character);
    CompanionPublisher.get().delete(character);
    CompanionSubscriber.get().delete(character);
  }

  public static void publish(String campaignId) {
    Log.d(TAG, "publishing characters of campaign " + campaignId);
    for (Character character : Characters.getLocalCharacters(campaignId)) {
      character.publish();
      Images.get(character.isLocal()).publishImageFor(character);
    }
  }

  public static long getLocalIdFor(String campaignId) {
    return local().getIdFor(campaignId);
  }

  public static long getRemoteIdFor(String campaignId) {
    return remote().getIdFor(campaignId);
  }

  public static void removeRemote(String characterId) {
    remote().remove(characterId);
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

  public static void load(Context context) {
    loadLocal(context);
    loadRemote(context);
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

  private List<Character> getCharacters(String campaignId) {
    Optional<Campaign> campaign = Campaigns.getCampaign(campaignId);
    if (campaign.isPresent() && campaign.get().isDefault()) {
      return getOrphanedCharacters();
    }

    List<Character> characters = new ArrayList<>();
    for (Character character : getAll()) {
      if (character.getCampaignId().equals(campaignId)) {
        characters.add(character);
      }
    }
    Collections.sort(characters, new CharacterComparator());
    return characters;
  }

  private List<Character> getOrphanedCharacters() {
    List<Character> characters = new ArrayList<>();
    for (Character character : getAll()) {
      if (!Campaigns.get(isLocal()).has(character.getCampaignId())) {
        characters.add(character);
      }
    }
    Collections.sort(characters, new CharacterComparator());

    return characters;
  }

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
