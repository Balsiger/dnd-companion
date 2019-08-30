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
 * The alignment status of a creature.
 */
public enum AlignmentStatus implements Enums.Named, Enums.Proto<Value.AlignmentStatus> {

  UNKNOWN("Unknown", "UNK", Value.AlignmentStatus.UNKNOWN_ALIGNMENT_STATUS),
  ALWAYS("Always", "AW", Value.AlignmentStatus.ALWAYS),
  USUALLY("Usually", "US", Value.AlignmentStatus.USUALLY),
  OFTEN("Often", "OF", Value.AlignmentStatus.OFTEN);

  private final String name;
  private final String shortName;
  private final Value.AlignmentStatus proto;

  AlignmentStatus(String name, String shortName, Value.AlignmentStatus proto) {
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
  public Value.AlignmentStatus toProto() {
    return proto;
  }

  @Override
  public String toString() {
    return getName();
  }

  public static AlignmentStatus fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static AlignmentStatus fromProto(Value.AlignmentStatus proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
