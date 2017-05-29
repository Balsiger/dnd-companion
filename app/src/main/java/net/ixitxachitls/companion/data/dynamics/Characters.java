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

import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

  public static Characters local() {
    Preconditions.checkNotNull(local, "local characters have to be loaded!");
    return local;
  }

  public static Characters remote() {
    Preconditions.checkNotNull(local, "remote characters have to be loaded!");
    return remote;
  }

  public static Characters get(boolean local) {
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

  public Optional<Character> getCharacter(String characterId, String campaignId) {
    if (characterId.isEmpty()) {
      return Optional.of(Character.createNew(campaignId));
    }

    return get(characterId);
  }

  public List<Character> getCharacters() {
    List<Character> characters = new ArrayList<>(getAll());
    Collections.sort(characters, new CharacterComparator());
    return characters;
  }

  public List<Character> getCharacters(String campaignId) {
    Optional<Campaign> campaign = Campaigns.get(!isLocal()).getCampaign(campaignId);
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

  public List<Character> getOrphanedCharacters() {
    List<Character> characters = new ArrayList<>();
    for (Character character : getAll()) {
      if (!Campaigns.get(isLocal()).has(character.getCampaignId())) {
        characters.add(character);
      }
    }
    Collections.sort(characters, new CharacterComparator());

    return characters;
  }

  public void publish() {
    Log.d(TAG, "publishing all characters");
    for (Character character : getCharacters()) {
      character.publish();
      Images.get(character.isLocal()).publish(character.getCampaignId(), Character.TYPE, character.getCharacterId());
    }
  }

  public void publish(String campaignId) {
    Log.d(TAG, "publishing characters of campaign " + campaignId);
    for (Character character : getCharacters(campaignId)) {
      character.publish();
      Images.local().publish(campaignId, Character.TYPE, character.getCharacterId());
    }
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
  public void remove(Character character){
    super.remove(character);
    Images.get(isLocal()).remove(Character.TYPE, character.getCharacterId());
  }

  private class CharacterComparator implements Comparator<Character> {
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
