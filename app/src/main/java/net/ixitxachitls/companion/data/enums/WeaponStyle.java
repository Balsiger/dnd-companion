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
 * An enumeration for the available weapon styles.
 */
public enum WeaponStyle implements Enums.Named, Enums.Proto<Value.WeaponStyle> {

  UNKNOWN("Unknown", "UNK", Value.WeaponStyle.UNKNOWN_STYLE),
  TWOHANDED_MELEE("two-handed melee", "THM", Value.WeaponStyle.TWOHANDED_MELEE),
  ONEHANDED_MELEE("one-handed melee", "OHM", Value.WeaponStyle.ONEHANDED_MELEE),
  LIGHT_MELEE("light melee", "LM", Value.WeaponStyle.LIGHT_MELEE),
  UNARMED("unarmed", "UA", Value.WeaponStyle.UNARMED),
  RANGED_TOUCH("ranged touch", "RT", Value.WeaponStyle.RANGED_TOUCH),
  RANGED("ranged", "RA", Value.WeaponStyle.RANGED),
  THROWN_TOUCH("thrown touch", "TT", Value.WeaponStyle.THROWN_TOUCH),
  THROWN("thrown", "TH", Value.WeaponStyle.THROWN),
  TOUCH("touch", "TO", Value.WeaponStyle.TOUCH),
  THROWN_TWO_HANDED("thrown two-handed", "TTH", Value.WeaponStyle.THROWN_TWO_HANDED);

  private final String name;
  private final String shortName;
  private final Value.WeaponStyle proto;

  WeaponStyle(String name, String shortName, Value.WeaponStyle proto) {
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
  public Value.WeaponStyle toProto() {
    return proto;
  }

  @Override
  public String toString() {
    return getName();
  }

  public static WeaponStyle fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static WeaponStyle fromProto(Value.WeaponStyle proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
