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
 * The sub type of a monster.
 */
public enum MonsterSubType implements Enums.Named, Enums.Proto<Value.MonsterSubtype> {

  UNKNOWN("Unknown", "UNK", Value.MonsterSubtype.UNKNOWN_MONSTER_SUBTYPE),
  NONE("None", "NON", Value.MonsterSubtype.NONE_SUBTYPE),
  AIR("Air", "AIR", Value.MonsterSubtype.AIR),
  AQUATIC("Aquatic", "AQU", Value.MonsterSubtype.AQUATIC),
  ARCHON("Archon", "ARC", Value.MonsterSubtype.ARCHON),
  AUGMENTED("Augmented", "AUG", Value.MonsterSubtype.AUGMENTED),
  BAATEZU("Baatezu", "BAT", Value.MonsterSubtype.BAATEZU),
  CHAOTIC("Chaotic", "CHA", Value.MonsterSubtype.CHAOTIC),
  COLD("COLD", "CLD", Value.MonsterSubtype.COLD),
  DWARF("Dwarf", "DWF", Value.MonsterSubtype.DWARF),
  EARTH("Earth", "EAR", Value.MonsterSubtype.EARTH),
  ELADRIN("Eladrin", "ELA", Value.MonsterSubtype.ELADRIN),
  ELF("Elf", "ELF", Value.MonsterSubtype.ELF),
  EVIL("Evil", "EVL", Value.MonsterSubtype.EVIL),
  EXTRAPLANAR("Extraplanar", "EXT", Value.MonsterSubtype.EXTRAPLANAR),
  FIRE("FIRE", "FIR", Value.MonsterSubtype.FIRE),
  GOBLINOID("Goblinoid", "GOB", Value.MonsterSubtype.GOBLINOID),
  GOOD("Good", "GOD", Value.MonsterSubtype.GOOD),
  GNOME("Gnome", "GNM", Value.MonsterSubtype.GNOME),
  GNOLL("Gnoll", "GNL", Value.MonsterSubtype.GNOLL),
  GUARIDNAL("Guardinal", "GDL", Value.MonsterSubtype.GUARDINAL),
  HALFLING("Halfling", "HLN", Value.MonsterSubtype.HALFLING),
  HUMAN("Human", "HUM", Value.MonsterSubtype.HUMAN),
  INCORPOREAL("Incorporeal", "INC", Value.MonsterSubtype.INCORPOREAL),
  LAWFUL("Lawful", "LAW", Value.MonsterSubtype.LAWFUL),
  NATIVE("Native", "NAT", Value.MonsterSubtype.NATIVE),
  ORC("Orc", "ORC", Value.MonsterSubtype.ORC),
  REPTILIAN("Reptilian", "RTL", Value.MonsterSubtype.REPTILIAN),
  SHAPECHANGER("Shapechanger", "SHC", Value.MonsterSubtype.SHAPECHANGER),
  SWARM("Swarm", "SWM", Value.MonsterSubtype.SWARM),
  WATER("Water", "WTR", Value.MonsterSubtype.WATER);

  private final String name;
  private final String shortName;
  private final Value.MonsterSubtype proto;

  MonsterSubType(String name, String shortName, Value.MonsterSubtype proto) {
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
  public Value.MonsterSubtype toProto() {
    return proto;
  }

  public static MonsterSubType fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static MonsterSubType fromProto(Value.MonsterSubtype proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
