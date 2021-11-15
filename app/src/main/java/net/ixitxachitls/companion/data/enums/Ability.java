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
import net.ixitxachitls.companion.proto.Value;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Representation of an ability of an entity.
 */
public enum Ability implements Enums.Named, Enums.Proto<Value.Ability> {

  UNKNOWN("Unknown", "UNK", Value.Ability.UNKNOWN, Template.MagicTemplateProto.Type.UNKNOWN),
  STRENGTH("Strength", "STR", Value.Ability.STRENGTH, Template.MagicTemplateProto.Type.STRENGTH),
  DEXTERITY("Dexterity", "DEX", Value.Ability.DEXTERITY,
      Template.MagicTemplateProto.Type.DEXTERITY),
  CONSTITUTION("Constitution", "CON", Value.Ability.CONSTITUTION,
      Template.MagicTemplateProto.Type.CONSTITUTION),
  INTELLIGENCE("Intelligence", "INT", Value.Ability.INTELLIGENCE,
      Template.MagicTemplateProto.Type.INTELLIGENCE),
  WISDOM("Wisdom", "WIS", Value.Ability.WISDOM, Template.MagicTemplateProto.Type.WISDOM),
  CHARISMA("Charisma", "CHR", Value.Ability.CHARISMA, Template.MagicTemplateProto.Type.CHARISMA),
  NONE("None", "-", Value.Ability.NONE, Template.MagicTemplateProto.Type.UNKNOWN);

  public static String PATTERN = Arrays.asList(values()).stream()
      .map(Ability::getName).reduce((a, b) -> a + "|" + b).get()
      + "|" + Arrays.asList(values()).stream()
      .map(Ability::getShortName).reduce((a, b) -> a + "|" + b).get();

  private final String name;
  private final String shortName;
  private final Value.Ability proto;
  private Template.MagicTemplateProto.Type magicType;

  Ability(String name, String shortName, Value.Ability proto,
          Template.MagicTemplateProto.Type magicType) {
    this.name = name;
    this.shortName = shortName;
    this.proto = proto;
    this.magicType = magicType;
  }

  public Template.MagicTemplateProto.Type getMagicType() {
    return magicType;
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
  public Value.Ability toProto() {
    return proto;
  }

  public static Ability fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static Ability fromProto(Value.Ability proto) {
    return Enums.fromProto(proto, values());
  }

  public static int modifier(int value) {
    if (value < 0)
      return 0;

    return (int) (value / 2) - 5;
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
