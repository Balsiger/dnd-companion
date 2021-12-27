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
 * The size enum.
 */
public enum Size implements Enums.Named, Enums.Proto<Value.SizeProto.Size> {

  UNKNOWN("Unknown", "Uk", Value.SizeProto.Size.UNKNOWN_SIZE, 0),
  UNRECOGNIZED("Unrecognized", "Ur", Value.SizeProto.Size.UNRECOGNIZED, 0),
  FINE("Fine", "F", Value.SizeProto.Size.FINE, -16),
  DIMINUTIVE("Diminutive", "D", Value.SizeProto.Size.DIMINUTIVE, -12),
  TINY("Tiny", "T", Value.SizeProto.Size.TINY, -8),
  SMALL("Small", "S", Value.SizeProto.Size.SMALL, -4),
  MEDIUM("Medium", "M", Value.SizeProto.Size.MEDIUM, 0),
  LARGE("Large", "L", Value.SizeProto.Size.LARGE, 4),
  HUGE("Huge", "H", Value.SizeProto.Size.HUGE, 8),
  GARGANTUAN("Gargantuan", "G", Value.SizeProto.Size.GARGANTUAN, 12),
  COLOSSAL("Colossal", "C", Value.SizeProto.Size.COLOSSAL, 16);

  private final String name;
  private final String shortName;
  private final Value.SizeProto.Size proto;
  private final int modifier;

  Size(String name, String shortName, Value.SizeProto.Size proto, int modifier) {
    this.name = name;
    this.shortName = shortName;
    this.proto = proto;
    this.modifier = modifier;
  }

  public int getModifier() {
    return modifier;
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
  public Value.SizeProto.Size toProto() {
    return proto;
  }

  @Override
  public String toString() {
    return getName();
  }

  public static Size fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static Size fromProto(Value.SizeProto.Size proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
