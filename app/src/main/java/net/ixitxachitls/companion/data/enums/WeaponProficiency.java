/*
 * Copyright (c) 2017-2020 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
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

import java.util.ArrayList;

/**
 * An enumeration for the available weapon proficiencies.
 */
public enum WeaponProficiency implements Enums.Named, Enums.Proto<Value.Proficiency> {

  UNKNOWN("Unknown", "UNK", Value.Proficiency.UNKNOWN_PROFICIENCY),
  SIMPLE("simple", "SMP", Value.Proficiency.SIMPLE),
  MARTIAL("martial", "MAR", Value.Proficiency.MARTIAL),
  EXOTIC("exotic", "EXT", Value.Proficiency.EXOCTIC),
  IMPROVISED("improvised", "IMP", Value.Proficiency.IMPROVISED),
  NONE("none", "NON", Value.Proficiency.NONE_PROFICIENCY);

  private final String name;
  private final String shortName;
  private final Value.Proficiency proto;

  WeaponProficiency(String name, String shortName, Value.Proficiency proto) {
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
  public Value.Proficiency toProto() {
    return proto;
  }

  @Override
  public String toString() {
    return getName();
  }

  public static WeaponProficiency fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static WeaponProficiency fromProto(Value.Proficiency proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
