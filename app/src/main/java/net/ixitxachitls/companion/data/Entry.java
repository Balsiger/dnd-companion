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

package net.ixitxachitls.companion.data;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.protobuf.MessageLite;

import net.ixitxachitls.companion.storage.DataBase;

/**
 * Base class for all entries.
 */
public abstract class Entry<P extends MessageLite> {

  protected String name;

  protected Entry() {
    this("");
  }

  protected Entry(String name) {
    this.name = name;
  }

  public boolean isDefined() {
    return !name.isEmpty();
  }

  public String getName() {
    return name;
  }

  protected static byte[] loadBytes(Context context, long id, Uri table) {
    Cursor cursor = context.getContentResolver().query(ContentUris.withAppendedId(table, id),
        DataBase.COLUMNS, null, null, null);
    cursor.moveToFirst();
    return cursor.getBlob(cursor.getColumnIndex(DataBase.COLUMN_PROTO));
  }

  public void setName(String name) {
    this.name = name;
  }
}

