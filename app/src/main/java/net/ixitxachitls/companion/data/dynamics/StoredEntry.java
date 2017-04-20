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

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;

import com.google.protobuf.MessageLite;

import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.storage.DataBase;

/**
 * An entry that is stored in the database.
 */
public abstract class StoredEntry<P extends MessageLite> extends DynamicEntry<P> {
  private long id;
  private final Uri dbUrl;

  protected StoredEntry(long id, String name, Uri dbUrl) {
    super(name);

    this.id = id;
    this.dbUrl = dbUrl;
  }

  public ContentValues toValues() {
    ContentValues values = new ContentValues();
    values.put(DataBase.COLUMN_PROTO, toProto().toByteArray());

    return values;
  }

  @Override
  public void setName(String name) {
    super.setName(name);
  }

  protected long getId() {
    return id;
  }

  protected void setId(long id) {
    this.id = id;
  }

  public void store() {
    if (id == 0) {
      Uri row = Entries.getContext().getContentResolver().insert(dbUrl, toValues());
      id = ContentUris.parseId(row);
    } else {
      Entries.getContext().getContentResolver().update(dbUrl, toValues(),
          "id = " + id, null);
    }
  }
}
