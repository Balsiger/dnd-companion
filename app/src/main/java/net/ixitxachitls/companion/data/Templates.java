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

import com.google.inject.Singleton;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.templates.AdventureTemplate;
import net.ixitxachitls.companion.data.templates.FeatTemplate;
import net.ixitxachitls.companion.data.templates.ItemTemplate;
import net.ixitxachitls.companion.data.templates.LevelTemplate;
import net.ixitxachitls.companion.data.templates.MiniatureTemplate;
import net.ixitxachitls.companion.data.templates.MonsterTemplate;
import net.ixitxachitls.companion.data.templates.ProductTemplate;
import net.ixitxachitls.companion.data.templates.QualityTemplate;
import net.ixitxachitls.companion.data.templates.SkillTemplate;
import net.ixitxachitls.companion.data.templates.SpellTemplate;
import net.ixitxachitls.companion.data.templates.WorldTemplate;
import net.ixitxachitls.companion.storage.AssetAccessor;
import net.ixitxachitls.companion.ui.activities.MainActivity;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import androidx.annotation.Nullable;

/**
 * Storage place for all templates.
 */
@Singleton
public class Templates {

  private static final String PATH_ENTITIES = "entities";
  private static final String LOADING_TEMPLATES = "templates";

  private static @Nullable Templates singleton = null;

  private final MonsterTemplates monsterTemplates = new MonsterTemplates();
  private final TemplatesStore<WorldTemplate> worlds = new TemplatesStore<>(WorldTemplate.class);
  private final ItemTemplates items = new ItemTemplates();
  private final TemplatesStore<LevelTemplate> levels = new TemplatesStore<>(LevelTemplate.class);
  private final TemplatesStore<FeatTemplate> feats = new TemplatesStore<>(FeatTemplate.class);
  private final TemplatesStore<QualityTemplate> qualities =
      new TemplatesStore<>(QualityTemplate.class);
  private final MiniatureTemplates miniatures = new MiniatureTemplates();
  private final TemplatesStore<SkillTemplate> skills = new TemplatesStore<>(SkillTemplate.class);
  private final TemplatesStore<SpellTemplate> spells = new TemplatesStore<>(SpellTemplate.class);
  private final TemplatesStore<AdventureTemplate> adventures =
      new TemplatesStore<>(AdventureTemplate.class);
  private final ProductTemplates products = new ProductTemplates();
  private final Set<String> productsWithData = new HashSet<>();

  private final AssetAccessor assetAccessor;
  private boolean loaded = false;
  private List<Callback> loadedCallbacks = new ArrayList<>();

  public Templates(AssetAccessor assetAccessor) {
    this.assetAccessor = assetAccessor;
  }

  @FunctionalInterface
  public interface Callback {
    public void call();
  }

  public TemplatesStore<AdventureTemplate> getAdventureTemplates() {
    return adventures;
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

  public ProductTemplates getProductTemplates() {
    return products;
  }

  public TemplatesStore<QualityTemplate> getQualityTemplates() {
    return qualities;
  }

  public TemplatesStore<SkillTemplate> getSkillTemplates() {
    return skills;
  }

  public TemplatesStore<SpellTemplate> getSpellTemplates() {
    return spells;
  }

  public TemplatesStore<WorldTemplate> getWorldTemplates() {
    return worlds;
  }

  public void executeAfterLoading(Callback callback) {
    if (loaded) {
      callback.call();
    } else {
      loadedCallbacks.add(callback);
    }
  }

  public LevelTemplate getOrCreateLevel(String name) {
    Optional<LevelTemplate> template = getLevelTemplates().get(name);
    if (template.isPresent()) {
      return template.get();
    }

    return new LevelTemplate(LevelTemplate.defaultProto(), name, 0);
  }

  public boolean hasData(String id) {
    return productsWithData.contains(id);
  }

  private void load(MainActivity main) {
    main.startLoading(LOADING_TEMPLATES);
    worlds.ensure(WorldTemplate.DEFAULT);

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
              case SkillTemplate.TYPE:
                skills.read(name, assetAccessor.open(name));
                break;
              case SpellTemplate.TYPE:
                spells.read(name, assetAccessor.open(name));
                break;
              case QualityTemplate.TYPE:
                qualities.read(name, assetAccessor.open(name));
                break;
              case AdventureTemplate.TYPE:
                adventures.read(name, assetAccessor.open(name));
                break;
              case ProductTemplate.TYPE:
                products.read(name, assetAccessor.open(name));
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
      skills.loaded();
      spells.loaded();
      qualities.loaded();
      adventures.loaded();
      products.loaded();

      productsWithData.addAll(monsterTemplates.getProductIds());
      productsWithData.addAll(items.getProductIds());
      productsWithData.addAll(levels.getProductIds());
      productsWithData.addAll(feats.getProductIds());
      productsWithData.addAll(qualities.getProductIds());
      productsWithData.addAll(skills.getProductIds());
      productsWithData.addAll(spells.getProductIds());
      productsWithData.addAll(adventures.getProductIds());

      main.finishLoading(LOADING_TEMPLATES);

      loaded();
    } catch (IOException | NoSuchMethodException | IllegalAccessException
        | InvocationTargetException e) {
      Status.error("Loading of entries from internal storage failed: " + e);
    }
  }

  private void loaded() {
    loaded = true;
    for (Callback callback : loadedCallbacks) {
      callback.call();
    }
  }

  public static Templates get() {
    return singleton;
  }

  public static void init(AssetAccessor assetAccessor, MainActivity main) {
    if (singleton == null) {
      singleton = new Templates(assetAccessor);
      singleton.load(main);
    }
  }
}
