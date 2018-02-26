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

import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.storage.DataBase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Base for all stored entry collections.
 */
public abstract class StoredEntries<E extends StoredEntry<?>> extends ViewModel {

  protected final Context context;
  protected final Uri table;
  protected final boolean local;
  protected final Map<String, E> entriesById = new HashMap<>();

  protected StoredEntries(Context context, Uri table, boolean local) {
    this.context = context;
    this.table = table;
    this.local = local;

    Cursor cursor = context.getContentResolver().query(table, DataBase.COLUMNS, null, null, null);
    while(cursor.moveToNext()) {
      Optional<E> entry = parseEntry(cursor.getLong(cursor.getColumnIndex("_id")),
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

  public static boolean isLocalId(String characterId) {
    return characterId.startsWith(Settings.get().getAppId());
  }

  public boolean isLocal() {
    return local;
  }

  public Optional<E> get(String id) {
    return Optional.fromNullable(entriesById.get(id));
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
    context.getContentResolver().delete(table, "id = " + entry.getId(), null);
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
