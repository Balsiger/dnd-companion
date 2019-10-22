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
 * The alignment of a creature.
 */
public enum Alignment implements Enums.Named, Enums.Proto<Value.Alignment> {

  UNKNOWN("Unknown", "UNK", Value.Alignment.UNKNOWN_ALIGNMENT),
  LAWFUL_GOOD("Lawful Good", "LG", Value.Alignment.LAWFUL_GOOD),
  NEUTRAL_GOOD("Netural Good", "NG", Value.Alignment.NEUTRAL_GOOD),
  CHAOTIC_GOOD("Chaotic Good", "CG", Value.Alignment.CHAOTIC_GOOD),
  LAWFUL_NEUTRAL("Lawful Neutral", "LN", Value.Alignment.LAWFUL_NEUTRAL),
  TRUE_NEUTRAL("Neutral", "N", Value.Alignment.TRUE_NEUTRAL),
  CHOATIC_NETURAL("Chaotic Neutral", "CN", Value.Alignment.CHAOTIC_NEUTRAL),
  LAWFUL_EVIL("Lawful Evil", "LE", Value.Alignment.LAWFUL_EVIL),
  NEUTRAL_EVIL("Neutral Evil", "NE", Value.Alignment.NEUTRAL_EVIL),
  CHAOTIC_EVIL("Chaotic Evil", "CE", Value.Alignment.CHAOTIC_EVIL),
  ANY_CHAOTIC("Any Chaotic", "AC", Value.Alignment.ANY_CHAOTIC),
  ANY_EVIL("Any Evil", "AE", Value.Alignment.ANY_EVIL),
  ANY_GOOD("Any Good", "AG", Value.Alignment.ANY_GOOD),
  ANY_LAWFUL("Any Lawful", "AL", Value.Alignment.ANY_LAWFUL),
  ANY("Any", "A", Value.Alignment.ANY_ALIGNMENT);

  private final String name;
  private final String shortName;
  private final Value.Alignment proto;

  Alignment(String name, String shortName, Value.Alignment proto) {
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
  public Value.Alignment toProto() {
    return proto;
  }

  @Override
  public String toString() {
    return getName();
  }

  public static Alignment fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static Alignment fromProto(Value.Alignment proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
