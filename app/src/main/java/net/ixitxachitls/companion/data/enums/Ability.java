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

package net.ixitxachitls.companion.data.enums;

import net.ixitxachitls.companion.proto.Value;

import java.util.ArrayList;

/**
 * Representation of an ability of an entity.
 */
public enum Ability implements Enums.Named, Enums.Proto<Value.Ability> {
  UNKNOWN("Unknown", "UNK", Value.Ability.UNKNOWN),
  STRENGTH("Strength", "STR", Value.Ability.STRENGTH),
  DEXTERITY("Dexterity", "DEX", Value.Ability.DEXTERITY),
  CONSTITUTION("Constitution", "CON", Value.Ability.CONSTITUTION),
  INTELLIGENCE("Intelligence", "INT", Value.Ability.INTELLIGENCE),
  WISDOM("Wisdom", "WIS", Value.Ability.WISDOM),
  CHARISMA("CHARISMA", "CHR", Value.Ability.CHARISMA),
  NONE("None", "-", Value.Ability.NONE);

  private final String mName;
  private final String mShortName;
  private final Value.Ability mProto;

  Ability(String name, String shortName, Value.Ability proto) {
    this.mName = name;
    this.mShortName = shortName;
    this.mProto = proto;
  }

  public String getName() {
    return mName;
  }

  public String getShortName() {
    return mShortName;
  }

  public Value.Ability toProto() {
    return mProto;
  }

  public static Ability fromProto(Value.Ability proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }

  public static Ability fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static int modifier(int value) {
    if(value < 0)
      return 0;

    return (int) (value / 2) - 5;
  }
}
