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

package net.ixitxachitls.companion.data;

import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Creatures;
import net.ixitxachitls.companion.storage.DataBaseAccessor;

/**
 * Reference to all global data.
 */
public abstract class Data {

  private final DataBaseAccessor dataBaseAccessor;

  protected Settings settings;
  protected Campaigns campaigns;
  protected Creatures creatures;
  protected Characters characters;

  protected Data(DataBaseAccessor dataBaseAccessor) {
    this.dataBaseAccessor = dataBaseAccessor;
  }

  public DataBaseAccessor getDataBaseAccessor() {
    return dataBaseAccessor;
  }

  public Settings settings() {
    return settings;
  }

  public Campaigns campaigns() {
    return campaigns;
  }

  public Creatures creatures() {
    return creatures;
  }

  public Characters characters() {
    return characters;
  }
}
