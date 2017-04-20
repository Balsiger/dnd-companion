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

import net.ixitxachitls.companion.data.dynamics.DynamicEntry;
import net.ixitxachitls.companion.proto.Entity;

/**
 * The representation of a monster.
 */
public class Level extends DynamicEntry<Entity.LevelProto> {

  public static final String TYPE = "level";

  protected Level(String name) {
    super(name);
  }

  public static Entity.LevelProto defaultProto() {
    return Entity.LevelProto.getDefaultInstance();
  }

  @Override
  public Entity.LevelProto toProto() {
    return Entity.LevelProto.newBuilder()
        .setEntity(Entity.EntityProto.newBuilder()
            .setName(name)
            .build())
        .build();
  }

  public static Level fromProto(Entity.LevelProto proto) {
    Level level = new Level(proto.getEntity().getName());

    return level;
  }

  public static boolean hasAbilityIncrease(int levelNumber) {
    return (levelNumber % 4) == 0;
  }
}
