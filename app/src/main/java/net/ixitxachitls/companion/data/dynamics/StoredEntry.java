/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * along with the Tabletop Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.dynamics;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.google.protobuf.MessageLite;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.storage.DataBase;
import net.ixitxachitls.companion.util.Ids;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An entry that is stored in the database.
 */
public abstract class StoredEntry<P extends MessageLite> extends DynamicEntry<P> {
  private static final Map<String, MessageLite> PROTO_CACHE = new ConcurrentHashMap<>();

  protected long id;
  protected String type;
  protected String entryId;
  private final Uri dbUrl;
  private final boolean local;

  protected StoredEntry(long id, String type, String name, boolean local, Uri dbUrl) {
    this(id, type, null, name, local, dbUrl);
  }

  protected StoredEntry(long id, String type, @Nullable String entryId, String name, boolean local,
                        Uri dbUrl) {
    super(name);

    this.id = id;
    this.type = type;
    this.entryId = entryId == null ? createId() : entryId;
    this.local = local;
    this.dbUrl = dbUrl;
  }

  private static ContentValues toValues(MessageLite proto) {
    ContentValues values = new ContentValues();
    values.put(DataBase.COLUMN_PROTO, proto.toByteArray());

    return values;
  }

  public void setId(long id) {
    this.id = id;
  }

  @Override
  public void setName(String name) {
    super.setName(name);
  }

  protected long getId() {
    return id;
  }

  public boolean isLocal() {
    return local;
  }

  public String getEntryId() {
    return entryId;
  }

  public String getServerId() {
    return Ids.extractServerId(entryId);
  }

  public String getType() {
    return type;
  }

  public boolean store() {
    P proto = toProto();

    if (id == 0) {
      Uri row = Entries.getContext().getContentResolver().insert(dbUrl, toValues(proto));
      id = ContentUris.parseId(row);
      if (isLocal()) {
        entryId = createId();
      }
      proto = toProto();
    }

    String key = protoCacheKey();
    if (proto.equals(PROTO_CACHE.get(key))) {
      Status.log("no changes for " + getClass().getSimpleName() + "/" + getName());
      return false;
    }

    // Store it (again if id made us change the entry id above).
    Entries.getContext().getContentResolver().update(dbUrl, toValues(proto),
        "id = " + id, null);

    PROTO_CACHE.put(key, proto);
    Status.log("stored changes for " + getClass().getSimpleName() + "/" + getName());
    return true;
  }

  private String createId() {
    return type + "-" + Settings.get().getAppId() + "-" + id;
  }

  public static boolean hasType(String id, String type) {
    return id.startsWith(type);
  }

  public static String extractType(String id) {
    return id.replaceAll("-.*", "");
  }

  private String protoCacheKey() {
    return entryId + "-" + isLocal() + "-" + getClass().getSimpleName();
  }

  protected void remove() {
    PROTO_CACHE.remove(protoCacheKey());
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    StoredEntry<P> that = (StoredEntry<P>) other;

    if (local != that.local) {
      return false;
    }

    return entryId.equals(that.entryId);
  }

  @Override
  public int hashCode() {
    int result = entryId.hashCode();
    result = 31 * result + (local ? 1 : 0);

    return result;
  }
}
