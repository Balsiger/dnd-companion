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

package net.ixitxachitls.companion.data.statics;

import net.ixitxachitls.companion.proto.Entity;

/**
 * The representation of a monster template that can be used to create actual, real monsters in
 * the game.
 */
public class MonsterTemplate extends StaticEntry<Entity.MonsterProto> {

  public static final String TYPE = "monster";

  private boolean mPrimaryRace;

  protected MonsterTemplate(String name) {
    super(name);
  }

  public static Entity.MonsterProto defaultProto() {
    return Entity.MonsterProto.getDefaultInstance();
  }

  public static MonsterTemplate fromProto(Entity.MonsterProto proto) {
    MonsterTemplate template = new MonsterTemplate(proto.getEntity().getName());
    template.mPrimaryRace = proto.getMainRace();

    return template;
  }

  public boolean isPrimaryRace() {
    return mPrimaryRace;
  }

  @Override
  public String toString() {
    return getName();
  }
}
