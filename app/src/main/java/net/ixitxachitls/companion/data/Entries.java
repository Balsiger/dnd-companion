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

package net.ixitxachitls.companion.data;

import android.support.annotation.Nullable;

import com.google.inject.Singleton;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.statics.World;
import net.ixitxachitls.companion.storage.AssetAccessor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Storage place for all entries.
 */
@Singleton
public class Entries {

  private static final String PATH_ENTITIES = "entities";

  private static @Nullable Entries singleton = null;

  private final Monsters monsters = new Monsters();
  private final EntriesStore<Level> levels = new EntriesStore<>(Level.class);
  private final EntriesStore<World> worlds = new EntriesStore<>(World.class);
  private final AssetAccessor assetAccessor;

  public Entries(AssetAccessor assetAccessor) {
    this.assetAccessor = assetAccessor;
  }

  public static void init(AssetAccessor assetAccessor) {
    if (singleton == null) {
      singleton = new Entries(assetAccessor);
      singleton.load();
    }
  }

  public static Entries get() {
    return singleton;
  }

  public static AssetAccessor getAssetAccessor() {
    return get().assetAccessor;
  }

  public Monsters getMonsters() {
    return monsters;
  }

  public EntriesStore<World> getWorlds() {
    return worlds;
  }

  public EntriesStore<Level> getLevels() {
    return levels;
  }

  private void load() {
    try {
      for (String reference : assetAccessor.list(PATH_ENTITIES)) {
        for (String type : assetAccessor.list(PATH_ENTITIES + "/" + reference)) {
          String path = PATH_ENTITIES + "/" + reference + "/" + type;
          for (String file : assetAccessor.list(path)) {
            switch (type) {
              case Monster.TYPE:
                monsters.read(assetAccessor.open(path + "/" + file));
                break;
              case Level.TYPE:
                levels.read(assetAccessor.open(path + "/" + file));
                break;
              case World.TYPE:
                worlds.read(assetAccessor.open(path + "/" + file));
                break;
              default:
                Status.error("Unsupported type " + type + " found!");
                break;
            }
          }
        }
      }
    } catch (IOException | NoSuchMethodException | IllegalAccessException
        | InvocationTargetException e) {
      Status.error("Loading of entries from internal storage failed: " + e);
    }
  }
}
