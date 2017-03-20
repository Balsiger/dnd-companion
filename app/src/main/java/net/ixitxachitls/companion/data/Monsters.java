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

import android.content.res.AssetManager;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.data.Monster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * General information and storage for monsters.
 */
public class Monsters {

  private final Map<String, Monster> mMonstersByName = new HashMap<>();
  private ArrayList<String> mPrimaryRaces;

  protected Monsters() {
  }

  protected void read(AssetManager assetManager, String file) throws IOException {
    Monster monster = Monster.fromProto(
        Monster.defaultProto().getParserForType().parseFrom(assetManager.open(file)));
    mMonstersByName.put(monster.getName(), monster);
  }

  public ArrayList<String> primaryRaces() {
    if (mPrimaryRaces == null) {
      mPrimaryRaces = new ArrayList<>();

      for (Monster monster : mMonstersByName.values()) {
        if (monster.isPrimaryRace())
          mPrimaryRaces.add(monster.getName());
      }
    }
    return mPrimaryRaces;
  }

  public Optional<Monster> get(String name) {
    return Optional.fromNullable(mMonstersByName.get(name));
  }
}
