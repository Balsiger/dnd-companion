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

import android.content.Context;
import android.support.annotation.Nullable;

import net.ixitxachitls.companion.ui.Alert;

import java.io.IOException;

/**
 * Storage place for all entries.
 */
public class Entries {

  private static final String PATH_ENTITIES = "entities";

  private static @Nullable Entries sSingleton = null;

  private final Monsters mMonsters = new Monsters();
  private final Levels mLevels = new Levels();
  private final Context mContext;

  public Entries(Context context) {
    // nothing for now
    mContext = context;
  }

  public static void init(Context context) {
    if (sSingleton == null) {
      sSingleton = new Entries(context);
      sSingleton.load();
    }
  }

  public static Entries get() {
    return sSingleton;
  }

  public static Context getContext() {
    return get().mContext;
  }

  public Monsters getMonsters() {
    return mMonsters;
  }
  public Levels getLevels() {
    return mLevels;
  }

  private void load() {
    String message = null;
    try {
      for (String reference : mContext.getAssets().list(PATH_ENTITIES)) {
        for (String type : mContext.getAssets().list(PATH_ENTITIES + "/" + reference)) {
          String path = PATH_ENTITIES + "/" + reference + "/" + type;
          for (String file : mContext.getAssets().list(path)) {
            message = type + ": " + file;
            switch (type) {
              case Monster.TYPE:
                mMonsters.read(mContext.getAssets(), path + "/" + file);
                break;
              case Level.TYPE:
                mLevels.read(mContext.getAssets(), path + "/" + file);
                break;
              default:
                Alert.show(mContext, "Loading " + type + ": " + file,
                    "Unsupported type " + type + " found!");
                break;
            }
          }
        }
      }
    } catch (IOException e) {
      Alert.show(mContext, "Loading " + message,
          "Loading of entries from internal storage failed: " + e);
    }
  }
}
