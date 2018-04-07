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

package net.ixitxachitls.companion.storage;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import net.ixitxachitls.companion.CompanionApplication;

/**
 * Context to handle database interaction.
 */
public class ApplicationDataBaseAccessor implements DataBaseAccessor {
  private CompanionApplication application;

  public ApplicationDataBaseAccessor(CompanionApplication application) {
    this.application = application;
  }

  public Cursor queryAll(Uri table) {
    return application.getContentResolver().query(table, DataBase.COLUMNS, null, null, null);
  }

  @Override
  public Cursor query(Uri table, long id) {
    return application.getContentResolver().query(ContentUris.withAppendedId(table, id),
        DataBase.COLUMNS, null, null, null);
  }

  @Override
  public void update(Uri table, long id, ContentValues values) {
    application.getContentResolver().update(table, values, "id = " + id, null);
  }

  public void delete(Uri table, long id) {
    application.getContentResolver().delete(table, "id = " + id, null);
  }

  @Override
  public long insert(Uri table, ContentValues contentValues) {
    Uri row = application.getContentResolver().insert(table, contentValues);
    return ContentUris.parseId(row);
  }
}
