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

package net.ixitxachitls.companion.data.values;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.proto.Entity;

/**
 * A representation of a container from an item template.
 */
public class Container {
  public static final Container NONE = new Container(Volume.ZERO, State.unknown);

  private final Volume capacity;
  private final State state;

  public enum State { unknown, solid, granular, liquid, gaseous }

  protected Container(Volume capacity, State state) {
    this.capacity = capacity;
    this.state = state;
  }

  public boolean hasCapacity() {
    return state != State.unknown;
  }

  public static Container fromProto(Entity.ContainerProto proto) {
    return new Container(Volume.fromProto(proto.getCapacity()), convert(proto.getState()));
  }

  public Entity.ContainerProto toProto() {
    return Entity.ContainerProto.newBuilder()
        .setCapacity(capacity.toProto())
        .setState(convert(state))
        .build();
  }

  private static State convert(Entity.ContainerProto.State state) {
    switch (state) {
      default:
        Status.error("Cannot convert capacity state: " + state);

      case UNKNOWN:
        return State.unknown;

      case SOLID:
        return State.solid;

      case GRANULAR:
        return State.granular;

      case LIQUID:
        return State.liquid;

      case GASEOUS:
        return State.gaseous;
    }
  }

  private static Entity.ContainerProto.State convert(State state) {
    switch(state) {
      default:
        Status.error("Cannot convert capacity state: " + state);

      case unknown:
        return Entity.ContainerProto.State.UNKNOWN;

      case solid:
        return Entity.ContainerProto.State.SOLID;

      case granular:
        return Entity.ContainerProto.State.GRANULAR;

      case liquid:
        return Entity.ContainerProto.State.LIQUID;

      case gaseous:
        return Entity.ContainerProto.State.GASEOUS;
    }
  }
}
