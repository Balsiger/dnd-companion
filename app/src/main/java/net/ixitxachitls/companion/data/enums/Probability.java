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
 * The representation of a probability value.
 */
public enum Probability
    implements Enums.Named, Enums.Proto<Template.ItemTemplateProto.Probability> {

  UNKNOWN("Unknown", "UNK", Template.ItemTemplateProto.Probability.UNKNOWN),
  COMMON("Common", "CMN", Template.ItemTemplateProto.Probability.COMMON),
  UNCOMMON("Uncommon", "UNC", Template.ItemTemplateProto.Probability.UNCOMMON),
  RARE("Rare", "RAR", Template.ItemTemplateProto.Probability.RARE),
  VERY_RARE("Very Rare", "VRR", Template.ItemTemplateProto.Probability.VERY_RARE),
  UNIQUE("Unique", "UNQ", Template.ItemTemplateProto.Probability.UNIQUE);

  private final String name;
  private final String shortName;
  private final Template.ItemTemplateProto.Probability proto;

  Probability(String name, String shortName, Template.ItemTemplateProto.Probability proto) {
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
  public Template.ItemTemplateProto.Probability toProto() {
    return proto;
  }

  @Override
  public String toString() {
    return getName();
  }

  public static Probability fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static Probability fromProto(Template.ItemTemplateProto.Probability proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
