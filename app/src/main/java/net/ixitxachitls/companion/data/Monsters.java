/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * along with the Tabletop Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data;

import android.content.res.AssetManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * General information and storage for monsters.
 */
public class Monsters extends EntriesStore<Monster> {

  private ArrayList<String> primaryRaces = null;

  public Monsters() {
    super(Monster.class);
  }

  protected void read(AssetManager assetManager, String file)
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
    super.read(assetManager, file);
    primaryRaces = null;
  }

  public ArrayList<String> primaryRaces() {
    if (primaryRaces == null) {
      primaryRaces = new ArrayList<>();

      for (Monster monster : byName.values()) {
        if (monster.isPrimaryRace())
          primaryRaces.add(monster.getName());
      }

      Collections.sort(primaryRaces);
    }
    return primaryRaces;
  }
}
