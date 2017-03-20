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

package net.ixitxachitls.companion.data.enums;

import net.ixitxachitls.companion.proto.Value;

import java.util.ArrayList;

/**
 * Representation of a gender of an entity (character, npc, monster, ...).
 */
public enum Gender implements Enums.Named, Enums.Proto<Value.Gender>
{
  UNKNOWN("Unknown", "U", Value.Gender.UNKNOWN_GENDER),
  FEMALE("Female", "F", Value.Gender.FEMALE),
  MALE("Male", "M", Value.Gender.MALE),
  OTHER("Other", "O", Value.Gender.NONE_GENDER);

  private final String mName;
  private final String mShortName;
  private final Value.Gender mProto;

  Gender(String name, String shortName, Value.Gender proto) {
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

  public Value.Gender toProto() {
    return mProto;
  }

  public static Gender fromProto(Value.Gender proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }

  public static Gender fromName(String name) {
    return Enums.fromName(name, values());
  }
}
