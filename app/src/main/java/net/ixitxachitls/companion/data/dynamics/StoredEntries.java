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

import android.arch.lifecycle.ViewModel;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import net.ixitxachitls.companion.data.Data;
import net.ixitxachitls.companion.storage.DataBase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Base for all stored entry collections.
 */
public abstract class StoredEntries<E extends StoredEntry<?>> extends ViewModel {

  protected final Data data;
  protected final Uri table;
  protected final boolean local;
  protected final Map<String, E> entriesById = new HashMap<>();

  protected StoredEntries(Data data, Uri table, boolean local) {
    this.data = data;
    this.table = table;
    this.local = local;

    Cursor cursor = data.getDataBaseAccessor().queryAll(table);
    while(cursor.moveToNext()) {
      Optional<E> entry = parseEntry(cursor.getLong(0),
          cursor.getBlob(cursor.getColumnIndex(DataBase.COLUMN_PROTO)));
      if (entry.isPresent()) {
        add(entry.get());
      }
    }
  }

  public boolean has(String id) {
    return entriesById.containsKey(id);
  }

  public long getIdFor(String id) {
    Optional<E> entry = get(id);
    if (entry.isPresent()) {
      return entry.get().getId();
    } else {
      return 0;
    }
  }

  public boolean isLocal() {
    return local;
  }

  public Optional<E> get(String id) {
    return Optional.ofNullable(entriesById.get(id));
  }

  public Collection<E> getAll() {
    return entriesById.values();
  }

  protected void add(E entry) {
    if (entriesById.containsKey(entry.getEntryId())) {
      // Update the id of the new entry to the old.
      entry.setId(entriesById.get(entry.getEntryId()).getId());
    }

    entriesById.put(entry.getEntryId(), entry);
  }

  protected void remove(E entry) {
    entry.remove();
    entriesById.remove(entry.getEntryId());
    data.getDataBaseAccessor().delete(table, entry.getId());
  }

  @Nullable
  protected E remove(String entryId) {
    E entry = entriesById.get(entryId);
    if (entry != null) {
      remove(entry);
    }

    return entry;
  }

  protected abstract Optional<E> parseEntry(long id, byte[] blob);
}
