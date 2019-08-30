/*
 * Copyright (c) 2017-2019 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.enums;

import net.ixitxachitls.companion.proto.Value;

import java.util.ArrayList;

/**
 * The type of a monster.
 */
public enum MonsterType implements Enums.Named, Enums.Proto<Value.MonsterType> {

  UNKNOWN("Unknown", "UNK", Value.MonsterType.UNKNOWN_MONSTER_TYPE),
  ABERRATION("Aberration", "ABR", Value.MonsterType.ABERRATION),
  ANIMAL("Animal", "ANL", Value.MonsterType.ANIMAL),
  CONSTRUCT("Construct", "CON", Value.MonsterType.CONSTRUCT),
  DRAGON("Dragoin", "DRG", Value.MonsterType.DRAGON),
  ELEMENTAL("Elemental", "ELE", Value.MonsterType.ELEMENTAL),
  FEY("Fey", "FEY", Value.MonsterType.FEY),
  GIANT("Giant", "GNT", Value.MonsterType.GIANT),
  HUMANOID("Humanoid", "HUM", Value.MonsterType.HUMANOID),
  MAGICAL_BEAST("Magical Beast", "MBT", Value.MonsterType.MAGICAL_BEAST),
  MONSTROUS_HUMANOID("Monstrous Humanoid", "MHU", Value.MonsterType.MONSTROUS_HUMANOID),
  OOZE("Ooze", "OOZ", Value.MonsterType.OOZE),
  PLANT("Plant", "PLT", Value.MonsterType.PLANT),
  UNEAD("Undead", "UND", Value.MonsterType.UNDEAD),
  VERMIN("Vermin", "VER", Value.MonsterType.VERMIN);

  private final String name;
  private final String shortName;
  private final Value.MonsterType proto;

  MonsterType(String name, String shortName, Value.MonsterType proto) {
    this.name = name;
    this.shortName = shortName;
    this.proto = proto;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getShortName() {
    return shortName;
  }

  @Override
  public Value.MonsterType toProto() {
    return proto;
  }

  public static MonsterType fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static MonsterType fromProto(Value.MonsterType proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
