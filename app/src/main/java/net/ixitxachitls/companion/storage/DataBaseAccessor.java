/*
 * Copyright (c) 2017-2018 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Interface for accessing database data.
 */
public interface DataBaseAccessor {
  Cursor queryAll(Uri table);
  Cursor query(Uri table, long id);
  void update(Uri table, long id, ContentValues values);
  void delete(Uri table, long id);
  long insert(Uri table, ContentValues values);
}
