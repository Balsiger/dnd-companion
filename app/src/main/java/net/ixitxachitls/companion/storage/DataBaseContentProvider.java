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

package net.ixitxachitls.companion.storage;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.google.common.base.Strings;

import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Character;

import java.util.List;

/**
 * Provider for data from the database.
 */
public class DataBaseContentProvider extends ContentProvider {

  public static final String CONTENT = "content://";
  public static final String AUTHORITY = "net.ixitxachitls.companion";
  public static final Uri CAMPAIGNS_LOCAL =
      Uri.parse(CONTENT + AUTHORITY + "/" + Campaign.TABLE_LOCAL);
  public static final Uri CAMPAIGNS_REMOTE =
      Uri.parse(CONTENT + AUTHORITY + "/" + Campaign.TABLE_REMOTE);
  public static final Uri CHARACTERS_LOCAL =
      Uri.parse(CONTENT + AUTHORITY + "/" + Character.TABLE_LOCAL);
  public static final Uri CHARACTERS_REMOTE =
      Uri.parse(CONTENT + AUTHORITY + "/" + Character.TABLE_REMOTE);
  public static final Uri SETTINGS = Uri.parse(CONTENT + AUTHORITY + "/" + Settings.TABLE);

  private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  private static final String TABLE_MIME = "vnd.android.cursor.dir/vnd." + AUTHORITY + ".";


  static {
    sUriMatcher.addURI(AUTHORITY, "*", 0);
  }

  private DataBase db;

  @Override
  public boolean onCreate() {
    db = new DataBase(getContext());

    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArguments,
                      String sortOrder) {

    String table = uri.getLastPathSegment();

    try {
      long id = ContentUris.parseId(uri);
      if (Strings.isNullOrEmpty(selection))
        selection = DataBase.COLUMN_ID + "= " + id;
      else
        selection += " AND " + DataBase.COLUMN_ID + "= " + id;

      List<String> segments = uri.getPathSegments();
      table = segments.get(segments.size() - 2);
    } catch (NumberFormatException e) {
      // no id, ignore.
    }

    return db.query(table, projection, selection, selectionArguments, sortOrder);
  }

  @Override
  public String getType(Uri uri) {
    return TABLE_MIME + uri.getLastPathSegment();
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    long id = db.insert(uri.getLastPathSegment(), values);

    return Uri.withAppendedPath(uri, String.valueOf(id));
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArguments) {
    return db.delete(uri.getLastPathSegment(), selection, selectionArguments);
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArguments) {
    return db.update(uri.getLastPathSegment(), values, selection, selectionArguments);
  }

  public static void reset(ContentResolver contentResolver) {
    contentResolver.delete(SETTINGS, null, null);
    contentResolver.delete(CAMPAIGNS_LOCAL, null, null);
    contentResolver.delete(CAMPAIGNS_REMOTE, null, null);
    contentResolver.delete(CHARACTERS_LOCAL, null, null);
    contentResolver.delete(CHARACTERS_REMOTE, null, null);
  }
}
