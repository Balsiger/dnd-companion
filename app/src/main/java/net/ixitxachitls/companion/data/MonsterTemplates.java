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

import net.ixitxachitls.companion.data.statics.MonsterTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * General information and storage for monsters.
 */
public class MonsterTemplates extends EntriesStore<MonsterTemplate> {

  private ArrayList<String> primaryRaces = null;

  public MonsterTemplates() {
    super(MonsterTemplate.class);
  }

  protected void read(InputStream input)
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
    super.read(input);
    primaryRaces = null;
  }

  public ArrayList<String> primaryRaces() {
    if (primaryRaces == null) {
      primaryRaces = new ArrayList<>();

      for (MonsterTemplate template : byName.values()) {
        if (template.isPrimaryRace())
          primaryRaces.add(template.getName());
      }

      Collections.sort(primaryRaces);
    }
    return primaryRaces;
  }
}
