/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * Data storage for various characters.
 */
public class CharactersData extends StoredEntries<Character> {

  private final Map<String, MutableLiveData<Optional<Character>>> characterById =
      new ConcurrentHashMap<>();

  public CharactersData(CompanionContext companionContext, boolean local) {
    super(companionContext, local ?
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

    if (characterById != null) {
      if (characterById.containsKey(character.getCharacterId())) {
        characterById.get(character.getCharacterId()).setValue(Optional.of(character));
      }
    }
  }

  @Override
  public void remove(Character character) {
    super.remove(character);


    if (characterById.containsKey(character.getCharacterId())) {
      characterById.get(character.getCharacterId()).setValue(Optional.empty());
    }

    /*
    if (charactersByCampaignId.containsKey(character.getCampaignId())) {
      charactersByCampaignId.get(character.getCampaignId())
          .setValue(ImmutableList.copyOf(ids(character.getCampaignId())));
    }
    */
  }

  public List<String> ids(String campaignId) {
    return getCharacters(campaignId).stream()
        .map(Character::getCharacterId)
        .collect(Collectors.toList());
  }

  public List<Character> orphaned() {
    return getAll().stream()
        .filter(this::isOrphaned)
        .collect(Collectors.toList());
  }

  private boolean isOrphaned(Character character) {
    return character.isLocal()
        && character.getCampaignId().equals(companionContext.campaigns().getDefaultCampaign().getCampaignId())
        || (!companionContext.campaigns().has(character.getCampaignId(), true)
        && !companionContext.campaigns().has(character.getCampaignId(), false));
  }

  @Override
  protected Optional<Character> parseEntry(long id, byte[] blob) {
    try {
      Entry.CharacterProto proto = Entry.CharacterProto.getDefaultInstance().getParserForType()
          .parseFrom(blob);
      return Optional.of(isLocal()
          ? LocalCharacter.fromProto(companionContext, id, proto)
          : RemoteCharacter.fromProto(companionContext, id, proto));
    } catch (InvalidProtocolBufferException e) {
      Status.toast("Cannot parse proto for campaign: " + e);
      return Optional.empty();
    }
  }
}
