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

import net.ixitxachitls.companion.proto.Template;

import java.util.ArrayList;

/**
 * An enumeration for the available weapon types.
 */
public enum WeaponType implements Enums.Named, Enums.Proto<Template.WeaponTemplateProto.Type> {

  UNKNOWN("Unknown", "UNK", Template.WeaponTemplateProto.Type.UNKNOWN),
  PIERCING_OR_SLASHING("piercing or slashing", "P/S",
      Template.WeaponTemplateProto.Type.PIERCING_OR_SLASHING),
  BLUDGEONING_OR_PIERING("bludgeoning or piercing", "B/P",
      Template.WeaponTemplateProto.Type.BLUDGEONING_OR_PIERCING),
  BLUDGEONING_AND_PIERCING("bludgeoning and piercing", "B&P",
      Template.WeaponTemplateProto.Type.BLUDGEONING_AND_PIERCING),
  SLASHING_OR_PIERCING("slashing or piercing", "S/P",
      Template.WeaponTemplateProto.Type.SLASHING_OR_PIERCING),
  SLASHING("slashing", "S", Template.WeaponTemplateProto.Type.SLASHING),
  BLUDGEONING("bludgeoning", "B", Template.WeaponTemplateProto.Type.BLUDGEONING),
  PIERCING("piercing", "P", Template.WeaponTemplateProto.Type.PIERCING),
  GRENADE("grenade", "G", Template.WeaponTemplateProto.Type.GRENADE),
  NONE("none", "-", Template.WeaponTemplateProto.Type.NONE);

  private final String name;
  private final String shortName;
  private final Template.WeaponTemplateProto.Type proto;

  WeaponType(String name, String shortName, Template.WeaponTemplateProto.Type proto) {
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
  public Template.WeaponTemplateProto.Type toProto() {
    return proto;
  }

  @Override
  public String toString() {
    return getName();
  }

  public static WeaponType fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static WeaponType fromProto(Template.WeaponTemplateProto.Type proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
