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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Data storage for various characters.
 */
public class CharactersData extends StoredEntries<Character> {
  private final Map<String, MutableLiveData<Optional<Character>>> characterById =
      new ConcurrentHashMap<>();
  private final Map<String, MutableLiveData<ImmutableList<String>>> charactersByCampaignId =
      new ConcurrentHashMap<>();

  public CharactersData(Context context, boolean local) {
    super(context, local ?
            DataBaseContentProvider.CHARACTERS_LOCAL : DataBaseContentProvider.CHARACTERS_REMOTE,
        local);
  }

  public LiveData<Optional<Character>> getCharacter(String characterId) {
    if (characterById.containsKey(characterId)) {
      return characterById.get(characterId);
    }

    MutableLiveData<Optional<Character>> character = new MutableLiveData<>();
    characterById.put(characterId, character);
    character.setValue(get(characterId));

    return character;
  }

  public boolean hasCharacter(String characterId) {
    return has(characterId);
  }

  public List<Character> getCharacters(String campaignId) {
    return getAll().stream()
        .filter(c -> c.getCampaignId().equals(campaignId))
        .collect(Collectors.toList());
  }

  public boolean hasCharacterForCampaign(String campaignId) {
    for (Character character : getAll()) {
      if (character.getCampaignId().equals(campaignId)) {
        return true;
      }
    }

    return false;
  }

  void update(Character character) {
    if (characterById.containsKey(character.getCharacterId())) {
      characterById.get(character.getCharacterId()).setValue(Optional.of(character));
    }
  }

  @Override
  public void add(Character character) {
    super.add(character);

    // We need to check for null since ids will be setup only after the super constructor is run.
    if (charactersByCampaignId != null) {
      MutableLiveData<ImmutableList<String>> ids =
          charactersByCampaignId.get(character.getCampaignId());
      if (ids == null) {
        ids = new MutableLiveData<>();
        charactersByCampaignId.put(character.getCampaignId(), ids);
      }

      LiveDataUtils.setValueIfChanged(ids, ImmutableList.copyOf(ids(character.getCampaignId())));
    }
  }

  public List<String> ids(String campaignId) {
    return getCharacters(campaignId).stream()
        .map(Character::getCharacterId)
        .collect(Collectors.toList());
  }

  public List<Character> orphaned() {
    return getAll().stream()
        .filter(CharactersData::isOrphaned)
        .collect(Collectors.toList());
  }

  private static boolean isOrphaned(Character character) {
    return character.isLocal()
        && character.getCampaignId().equals(Campaigns.defaultCampaign.getCampaignId())
        || (!Campaigns.has(character.getCampaignId(), true)
        && !Campaigns.has(character.getCampaignId(), false));
  }

  @Override
  protected Optional<Character> parseEntry(long id, byte[] blob) {
    try {
      Data.CharacterProto proto = Data.CharacterProto.getDefaultInstance().getParserForType()
          .parseFrom(blob);
      return Optional.of(isLocal()
          ? LocalCharacter.fromProto(id, proto) : RemoteCharacter.fromProto(id, proto));
    } catch (InvalidProtocolBufferException e) {
      Status.toast("Cannot parse proto for campaign: " + e);
      return Optional.absent();
    }
  }
}
