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

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.util.Misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Information and storage for all characters.
 */
public class Characters {
  private final CompanionContext context;
  private final CharactersData local;
  private final CharactersData remote;

  // Live data storages.
  private final Map<String, MutableLiveData<ImmutableList<String>>> characterIdsByCampaignId =
      new ConcurrentHashMap<>();

  public Characters(CompanionContext context) {
    this.context = context;
    this.local = new CharactersData(context, true);
    this.remote = new CharactersData(context, false);
  }

  // Data accessors.

  public LiveData<Optional<Character>> getCharacter(String characterId) {
    if (!context.settings().useRemoteCharacters() && local.hasCharacter(characterId)) {
      return local.getCharacter(characterId);
    }

    return remote.getCharacter(characterId);
  }

  public boolean has(Character character) {
    return has(character.getCharacterId(), character.isLocal());
  }

  public boolean has(String characterId, boolean isLocal) {
    if (isLocal) {
      return local.has(characterId);
    }

    return remote.has(characterId);
  }

  public boolean hasLocalCharacterForCampaign(String campaignId) {
    return local.hasCharacterForCampaign(campaignId);
  }

  public LiveData<ImmutableList<String>> getCampaignCharacterIds(String campaignId) {
    if (characterIdsByCampaignId.containsKey(campaignId)) {
      return characterIdsByCampaignId.get(campaignId);
    }

    MutableLiveData<ImmutableList<String>> ids = new MutableLiveData<>();
    LiveDataUtils.setValueIfChanged(ids, ImmutableList.copyOf(characterIds(campaignId)));
    characterIdsByCampaignId.put(campaignId, ids);

    return ids;
  }

  public List<Character> getCampaignCharacters(String campaignId) {
    List<Character> characters = new ArrayList<>();

    for (String id : characterIds(campaignId)) {
      characters.add(getCharacter(id).getValue().get());
    }

    return characters;
  }

  public Collection<Character> getLocalCharacters() {
    return local.getAll();
  }

  public List<Character> getAllCharacters() {
    List<Character> characters = new ArrayList<>();
    if (Misc.onEmulator()) {
      if (context.settings().useRemoteCharacters()) {
        characters.addAll(remote.getAll());
      } else {
        characters.addAll(local.getAll());
      }
    } else {
      characters.addAll(local.getAll());
      characters.addAll(remote.getAll());
    }

    return characters;
  }

  public long getLocalIdFor(String characterId) {
    return local.getIdFor(characterId);
  }

  public long getRemoteIdFor(String characterId) {
    return remote.getIdFor(characterId);
  }

  // Data mutations.

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
  }

  // Publishing characters.
  // TODO(merlin): Move this over to the publishers.

  public void publish() {
    Status.log("publishing all local characters");
    for (Character character : local.getAll()) {
      character.asLocal().publish();
      context.images(character.isLocal()).publishImageFor(character);
    }
  }

  public void publish(String campaignId) {
    Status.log("publishing characters of campaign " + campaignId);
    for (Character character : local.getCharacters(campaignId)) {
      character.asLocal().publish();
      context.images(character.isLocal()).publishImageFor(character);
    }
  }

  // Private methods.

  private List<String> orphaned() {
    List<String> ids = local.orphaned().stream()
        .map(Character::getCharacterId)
        .collect(Collectors.toList());
    ids.addAll(remote.orphaned().stream()
        .map(Character::getCharacterId)
        .collect(Collectors.toList()));

    return ids;
  }

  private List<String> characterIds(String campaignId) {
    if (campaignId.equals(context.campaigns().getDefaultCampaign().getCampaignId())) {
      return orphaned();
    }

    List<String> ids = local.ids(campaignId);
    // Only show each charcter once, even if they are locallay and remotely available
    // (mostly for the emulator).
    for (String id : remote.ids(campaignId)) {
      if (!ids.contains(id)) {
        ids.add(id);
      }
    }

    return ids;
  }
}
