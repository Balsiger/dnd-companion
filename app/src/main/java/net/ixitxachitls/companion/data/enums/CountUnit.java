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
 * An enumeration for the available weapon styles.
 */
public enum CountUnit implements Enums.Named, Enums.Proto<Template.CountedTemplateProto.Unit> {

  UNKNOWN("unknown", "unknowns", Template.CountedTemplateProto.Unit.UNKNOWN),
  DAY("day", "days", Template.CountedTemplateProto.Unit.DAY),
  PIECE("piece", "pieces", Template.CountedTemplateProto.Unit.PIECE),
  SHEET("sheet", "sheets", Template.CountedTemplateProto.Unit.SHEET),
  USE("use", "uses", Template.CountedTemplateProto.Unit.USE),
  PAGE("page", "pages", Template.CountedTemplateProto.Unit.PAGE),
  CHARGE("charge", "charges", Template.CountedTemplateProto.Unit.CHARGE),
  APPLICATION("application", "applications", Template.CountedTemplateProto.Unit.APPLICATION),
  DAMAGE("damage", "danages", Template.CountedTemplateProto.Unit.DAMAGE);

  private final String name;
  private final String plural;
  private final Template.CountedTemplateProto.Unit proto;

  CountUnit(String name, String plural, Template.CountedTemplateProto.Unit proto) {
    this.name = name;
    this.plural = plural;
    this.proto = proto;
  }

  @Override
  public String getName() {
    return name;
  }

  public String getPlural() {
    return plural;
  }

  @Override
  public String getShortName() {
    return name;
  }

  public String format(int value) {
    if (value != 1) {
      return "1 " + getName();
    }

    return value + " " + getPlural();
  }

  @Override
  public Template.CountedTemplateProto.Unit toProto() {
    return proto;
  }

  @Override
  public String toString() {
    return getName();
  }

  public static CountUnit fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static CountUnit fromProto(Template.CountedTemplateProto.Unit proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
