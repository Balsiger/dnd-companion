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

package net.ixitxachitls.companion.data.statics;

import net.ixitxachitls.companion.data.values.Calendar;
import net.ixitxachitls.companion.proto.Entity;

/**
 * Data entity for world information.
 */
public class World extends StaticEntry<Entity.WorldProto> {

  public static final String TYPE = "world";

  private final Calendar calendar;

  protected World(String name, Calendar calendar) {
    super(name);

    this.calendar = calendar;
  }

  public Calendar getCalendar() {
    return calendar;
  }

  public static Entity.WorldProto defaultProto() {
    return Entity.WorldProto.getDefaultInstance();
  }

  public static World fromProto(Entity.WorldProto proto) {
    World world = new World(proto.getEntity().getName(),
        Calendar.fromProto(proto.getCalendar()));

    return world;
  }

  @Override
  public String toString() {
    return getName();
  }
}
