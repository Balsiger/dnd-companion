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
import net.ixitxachitls.companion.data.templates.FeatTemplate;
import net.ixitxachitls.companion.data.templates.ItemTemplate;
import net.ixitxachitls.companion.data.templates.LevelTemplate;
import net.ixitxachitls.companion.data.templates.MiniatureTemplate;
import net.ixitxachitls.companion.data.templates.MonsterTemplate;
import net.ixitxachitls.companion.data.templates.WorldTemplate;
import net.ixitxachitls.companion.storage.AssetAccessor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Storage place for all templates.
 */
@Singleton
public class Templates {

  private static final String PATH_ENTITIES = "entities";

  private static @Nullable Templates singleton = null;

  private final MonsterTemplates monsterTemplates = new MonsterTemplates();
  private final TemplatesStore<WorldTemplate> worlds = new TemplatesStore<>(WorldTemplate.class);
  private final ItemTemplates items = new ItemTemplates();
  private final TemplatesStore<LevelTemplate> levels = new TemplatesStore<>(LevelTemplate.class);
  private final TemplatesStore<FeatTemplate> feats = new TemplatesStore<>(FeatTemplate.class);
  private final MiniatureTemplates miniatures = new MiniatureTemplates();

  private final AssetAccessor assetAccessor;

  public Templates(AssetAccessor assetAccessor) {
    this.assetAccessor = assetAccessor;
  }

  public TemplatesStore<FeatTemplate> getFeatTemplates() {
    return feats;
  }

  public ItemTemplates getItemTemplates() {
    return items;
  }

  public TemplatesStore<LevelTemplate> getLevelTemplates() {
    return levels;
  }

  public MiniatureTemplates getMiniatureTemplates() {
    return miniatures;
  }

  public MonsterTemplates getMonsterTemplates() {
    return monsterTemplates;
  }

  public TemplatesStore<WorldTemplate> getWorldTemplates() {
    return worlds;
  }

  private void load() {
    try {
      for (String reference : assetAccessor.list(PATH_ENTITIES)) {
        for (String type : assetAccessor.list(PATH_ENTITIES + "/" + reference)) {
          String path = PATH_ENTITIES + "/" + reference + "/" + type;
          for (String file : assetAccessor.list(path)) {
            String name = path + "/" + file;
            switch (type) {
              case MonsterTemplate.TYPE:
                monsterTemplates.read(name, assetAccessor.open(name));
                break;
              case WorldTemplate.TYPE:
                worlds.read(name, assetAccessor.open(name));
                break;
              case ItemTemplate.TYPE:
                items.read(name, assetAccessor.open(name));
                break;
              case LevelTemplate.TYPE:
                levels.read(name, assetAccessor.open(name));
                break;
              case FeatTemplate.TYPE:
                feats.read(name, assetAccessor.open(name));
                break;
              case MiniatureTemplate.TYPE:
                miniatures.read(name, assetAccessor.open(name));
                break;
              default:
                Status.error("Unsupported type " + type + " found!");
                break;
            }
          }
        }
      }

      monsterTemplates.loaded();
      worlds.loaded();
      items.loaded();
      levels.loaded();
      feats.loaded();
      miniatures.loaded();
    } catch (IOException | NoSuchMethodException | IllegalAccessException
        | InvocationTargetException e) {
      Status.error("Loading of entries from internal storage failed: " + e);
    }
  }

  public static Templates get() {
    return singleton;
  }

  public static void init(AssetAccessor assetAccessor) {
    if (singleton == null) {
      singleton = new Templates(assetAccessor);
      singleton.load();
    }
  }
}
