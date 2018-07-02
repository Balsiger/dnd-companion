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

package net.ixitxachitls.companion.data.dynamics;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.values.CampaignDate;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

/**
 * Access to history entries.
 */
public class Histories extends StoredEntries<HistoryEntry> {

  private final MutableLiveData<ImmutableList<HistoryEntry>> entries = new MutableLiveData<>();
  private final Map<String, MutableLiveData<ImmutableList<HistoryEntry>>> entriesById =
      new ConcurrentHashMap<>();

  public Histories(CompanionContext context) {
    super(context, DataBaseContentProvider.HISTORY, true);

    entries.setValue(entries(null));
  }

  public LiveData<ImmutableList<HistoryEntry>> getEntries() {
    return entries;
  }

  public LiveData<ImmutableList<HistoryEntry>> getEntries(@Nullable String id) {
    if (id == null) {
      return getEntries();
    }

    if (!entriesById.containsKey(id)) {
      MutableLiveData<ImmutableList<HistoryEntry>> data = new MutableLiveData<>();
      data.setValue(entries(id).stream()
          .filter(e -> !e.isViewed())
          .collect(ImmutableList.toImmutableList()));
      entriesById.put(id, data);
    }

    return entriesById.get(id);
  }

  public boolean has(HistoryEntry entry) {
    return getEntries().getValue().contains(entry);
  }

  private ImmutableList<HistoryEntry> entries(@Nullable String id) {
    return getAll().stream()
        .filter(e -> !e.isViewed() && e.hasId(id))
        .sorted()
        .collect(ImmutableList.toImmutableList());
  }

  protected void update(HistoryEntry entry) {
    LiveDataUtils.setValueIfChanged(entries, entries(null));
    for (String id : entry.getIds()) {
      if (entriesById.containsKey(id)) {
        entriesById.get(id).setValue(ImmutableList.copyOf(entries(id)));
      }
    }
  }

  public void created(CampaignDate gameDate, String ... ids) {
    created("", gameDate, ids);
  }

  public void created(String description, CampaignDate gameDate, String ... ids) {
    HistoryEntry entry = new HistoryEntry(companionContext, gameDate, HistoryEntry.Type.create,
        Arrays.asList(ids), description, true);
    entry.store();
    add(entry);
  }

  public void removed(CampaignDate gameDate, String ... ids) {
    removed("", gameDate, ids);
  }

  public void removed(String description, CampaignDate gameDate, String ... ids) {
    HistoryEntry entry = new HistoryEntry(companionContext, gameDate, HistoryEntry.Type.remove,
        Arrays.asList(ids), description, true);
    entry.store();
    add(entry);
  }

  public void expired(String description, CampaignDate gameDate, String ... ids) {
    HistoryEntry entry = new HistoryEntry(companionContext, gameDate, HistoryEntry.Type.expired,
        Arrays.asList(ids), description, false);
    entry.store();
    add(entry);
  }

  public void addedXp(int xp, CampaignDate gameDate, String characterId, String campaignId) {
    HistoryEntry entry = new HistoryEntry(companionContext, gameDate, HistoryEntry.Type.xp,
        ImmutableList.of(characterId, campaignId), String.valueOf(xp), false);
    entry.store();
    add(entry);
  }


  @Override
  public void add(HistoryEntry entry) {
    super.add(entry);

    // We need to check for null since entries will be setup only after the super constructor is
    // run.
    if (entries != null) {
      update(entry);
    }
  }

  @Override
  public void remove(HistoryEntry entry) {
    super.remove(entry);

    update(entry);
  }

  @Override
  protected Optional<HistoryEntry> parseEntry(long id, byte[] blob) {
    try {
      Entry.HistoryProto proto = Entry.HistoryProto.getDefaultInstance().getParserForType()
          .parseFrom(blob);
      return Optional.of(HistoryEntry.fromProto(companionContext, id, proto));
    } catch (InvalidProtocolBufferException e){
      Status.toast("Cannot parse proto for history entry: " + e);
      return Optional.empty();
    }
  }
}
