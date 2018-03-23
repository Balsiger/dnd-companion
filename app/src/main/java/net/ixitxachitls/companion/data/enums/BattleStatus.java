/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * along with the Tabletop Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.enums;

import net.ixitxachitls.companion.proto.Value;

import java.util.ArrayList;

/**
 * Status of a battle.
 */
public enum BattleStatus implements Enums.Named, Enums.Proto<Value.BattleStatus> {
  ENDED("Ended", "E", Value.BattleStatus.ENDED),
  STARTING("Starting", "S", Value.BattleStatus.STARTING),
  SURPRISED("Surprised", "U", Value.BattleStatus.SURPRISED),
  ONGOING("Ongoing", "O", Value.BattleStatus.ONGOING);

  private final String name;
  private final String shortName;
  private final Value.BattleStatus proto;

  BattleStatus(String name, String shortName, Value.BattleStatus proto) {
    this.name = name;
    this.shortName = shortName;
    this.proto = proto;
  }

  public String getName() {
    return name;
  }

  public String getShortName() {
    return shortName;
  }

  public Value.BattleStatus toProto() {
    return proto;
  }

  public static BattleStatus fromProto(Value.BattleStatus proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }

  public static BattleStatus fromName(String name) {
    return Enums.fromName(name, values());
  }
}
