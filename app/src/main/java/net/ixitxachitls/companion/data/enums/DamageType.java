/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

package net.ixitxachitls.companion.data.enums;

import net.ixitxachitls.companion.proto.Value;

/**
 * Representation of a damage type.
 */
public enum DamageType implements Enums.Named, Enums.Proto<Value.DamageProto.Damage.Type> {
  UNKNOWN("unknown", Value.DamageProto.Damage.Type.UNKNOWN),
  FIRE("fire", Value.DamageProto.Damage.Type.FIRE),
  ELECTRICAL("electrical", Value.DamageProto.Damage.Type.ELECTRICAL),
  SONIC("sonic", Value.DamageProto.Damage.Type.SONIC),
  WATER("water", Value.DamageProto.Damage.Type.WATER),
  ACID("acid", Value.DamageProto.Damage.Type.ACID),
  HOLY("holy", Value.DamageProto.Damage.Type.HOLY),
  NEGATIVE_ENERGY("negative energy", Value.DamageProto.Damage.Type.NEGATIVE_ENERGY),
  NONLETHAL("non-lethal", Value.DamageProto.Damage.Type.NONLETHAL),
  COLD("cold", Value.DamageProto.Damage.Type.COLD),
  STR("Str", Value.DamageProto.Damage.Type.STR),
  DEX("Dex", Value.DamageProto.Damage.Type.DEX),
  CON("Con", Value.DamageProto.Damage.Type.CON),
  INT("Int", Value.DamageProto.Damage.Type.INT),
  WIS("Wis", Value.DamageProto.Damage.Type.WIS),
  CHA("Cha", Value.DamageProto.Damage.Type.CHA),
  NONE("", Value.DamageProto.Damage.Type.NONE);

  private final String name;
  private final Value.DamageProto.Damage.Type proto;

  DamageType(String name, Value.DamageProto.Damage.Type proto) {
    this.name = name;
    this.proto = proto;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getShortName() {
    return name;
  }

  @Override
  public Value.DamageProto.Damage.Type toProto() {
    return proto;
  }

  public static DamageType from(Value.DamageProto.Damage.Type fromProto) {
    for (DamageType type : DamageType.values()) {
      if (type.proto == fromProto) {
        return type;
      }
    }

    throw new IllegalArgumentException("Cannot get damage type from " + fromProto);
  }

  public static DamageType fromName(String name) {
    return Enums.fromName(name, values());
  }
}
