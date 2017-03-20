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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import net.ixitxachitls.companion.data.Campaign;
import net.ixitxachitls.companion.data.Character;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.util.Lazy;

/**
 * Helper to create an access the database.
 */
public class DataBase extends SQLiteOpenHelper {

  private static final int VERSION = 3;
  private static final String DATABASE_NAME = "PlayerCompanion";
  public static final String COLUMN_ID = "id";
  public static final String COLUMN_PROTO = "proto";

  public static final String CREATE_CAMPAIGNS = "CREATE TABLE " + Campaign.TABLE
      + " (" + COLUMN_ID + " INTEGER PRIMARY KEY,"
      + COLUMN_PROTO + " BLOB);";
  public static final String CREATE_CHARACTERS = "CREATE TABLE " + Character.TABLE
      + " (" + COLUMN_ID + " INTEGER PRIMARY KEY,"
      + COLUMN_PROTO + " BLOB);";
  public static final String CREATE_SETTINGS = "CREATE TABLE " + Settings.TABLE
      + " (" + COLUMN_ID + " INTEGER PRIMARY KEY,"
      + COLUMN_PROTO + " BLOB);";

  public static final String COLUMNS[] = {
      COLUMN_ID + " _id",
      COLUMN_PROTO,
  };

  private Lazy<SQLiteDatabase> readableDb = new Lazy<>(this::getReadableDatabase);
  private Lazy<SQLiteDatabase> writableDb = new Lazy<>(this::getWritableDatabase);


  public DataBase(Context context) {
    super(context, DATABASE_NAME, null, VERSION);
  }

  public Cursor query(String table, String[] projection, @Nullable String selection,
                      @Nullable String[] selectionArguments, @Nullable String sortOrder) {
    return readableDb.get().query(table, projection, selection, selectionArguments, null, null,
        sortOrder == null ? COLUMN_ID : sortOrder);
  }

  public long insert(String table, ContentValues values) {
    return writableDb.get().insert(table, null, values);
  }

  public int delete(String table, String selection, String[] selectionArguments) {
    return writableDb.get().delete(table, selection, selectionArguments);
  }

  public int update(String table, ContentValues values, String selection,
                    String[] selectionArguments) {
    return writableDb.get().update(table, values, selection, selectionArguments);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(CREATE_CAMPAIGNS);
    db.execSQL(CREATE_CHARACTERS);
    db.execSQL(CREATE_SETTINGS);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion == 1 && newVersion >= 2) {
      db.execSQL(CREATE_CHARACTERS);
    } if (oldVersion == 2 && newVersion >= 3) {
      db.execSQL(CREATE_SETTINGS);
    } else {
      throw new IllegalArgumentException("don't know how to convert from " + oldVersion + " to "
          + newVersion);
    }
  }
}
