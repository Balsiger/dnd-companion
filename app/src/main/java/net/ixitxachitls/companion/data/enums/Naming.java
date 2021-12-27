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

import net.ixitxachitls.companion.proto.Template;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 */
public enum Naming implements Enums.Named, Enums.Proto<Template.TemplateProto.Naming> {

  INFIX("INFIX", "IN", Template.TemplateProto.Naming.INFIX),
  POSTFIX("Postfix", "POST", Template.TemplateProto.Naming.POSTFIX),
  PREFIX("Prefix", "PRE", Template.TemplateProto.Naming.PREFIX),
  IGNORE("Ignore", "IGN", Template.TemplateProto.Naming.IGNORE);

  public static String PATTERN = Arrays.asList(values()).stream()
      .map(Naming::getName).reduce((a, b) -> a + "|" + b).get()
      + "|" + Arrays.asList(values()).stream()
      .map(Naming::getShortName).reduce((a, b) -> a + "|" + b).get();

  private final String name;
  private final String shortName;
  private final Template.TemplateProto.Naming proto;

  Naming(String name, String shortName, Template.TemplateProto.Naming proto) {
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
  public Template.TemplateProto.Naming toProto() {
    return proto;
  }

  public static Naming fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static Naming fromProto(Template.TemplateProto.Naming proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
