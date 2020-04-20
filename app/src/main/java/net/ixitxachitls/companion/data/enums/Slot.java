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
 * An enumeration for the available weapon proficiencies.
 */
public enum Slot implements Enums.Named, Enums.Proto<Template.WearableTemplateProto.Slot> {

  UNKNOWN("Unknown", "UNK", Template.WearableTemplateProto.Slot.UNKNOWN),
  HEAD("head", "HD", Template.WearableTemplateProto.Slot.HAND),
  NECK("neck", "NE", Template.WearableTemplateProto.Slot.NECK),
  TORSO("torso", "TO", Template.WearableTemplateProto.Slot.TORSO),
  BODY("body", "BD", Template.WearableTemplateProto.Slot.BODY),
  WAIST("waist", "WT", Template.WearableTemplateProto.Slot.WAIST),
  SHOULDERS("shoulders", "SH", Template.WearableTemplateProto.Slot.SHOULDERS),
  HANDS("hands", "HS", Template.WearableTemplateProto.Slot.HANDS),
  HAND("hand", "HA", Template.WearableTemplateProto.Slot.HAND),
  FINGER("finger", "FI", Template.WearableTemplateProto.Slot.FINGER),
  WRISTS("wrists", "WR", Template.WearableTemplateProto.Slot.WRISTS),
  FEET("feet", "FT", Template.WearableTemplateProto.Slot.FEET),
  EYES("eyes", "EY", Template.WearableTemplateProto.Slot.EYES);

  private final String name;
  private final String shortName;
  private final Template.WearableTemplateProto.Slot proto;

  Slot(String name, String shortName, Template.WearableTemplateProto.Slot proto) {
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
  public Template.WearableTemplateProto.Slot toProto() {
    return proto;
  }

  @Override
  public String toString() {
    return getName();
  }

  public static Slot fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static Slot fromProto(Template.WearableTemplateProto.Slot proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
