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
public enum MagicEffectType implements Enums.Named, Enums.Proto<Template.MagicTemplateProto.Type> {

  UNKNOWN("Unknown", "UNK", Template.MagicTemplateProto.Type.UNKNOWN),
  STRENGTH("Strength", "STR", Template.MagicTemplateProto.Type.STRENGTH),
  DEXTERITY("Dexterity", "DEX", Template.MagicTemplateProto.Type.DEXTERITY),
  CONSTITUTION("Constitution", "CON", Template.MagicTemplateProto.Type.CONSTITUTION),
  INTELLIGENCE("Intelligence", "INT", Template.MagicTemplateProto.Type.INTELLIGENCE),
  WISDOM("Wisdom", "WIS", Template.MagicTemplateProto.Type.WISDOM),
  CHARISMA("Charisma", "CHA", Template.MagicTemplateProto.Type.CHARISMA),
  ATTACK("Attack", "ATT", Template.MagicTemplateProto.Type.ATTACK),
  DAMAGE("Damage", "DMG", Template.MagicTemplateProto.Type.DAMAGE),
  ARMOR_CLASS("Armor Class", "AC", Template.MagicTemplateProto.Type.ARMOR_CLASS),
  HIDE("Hide", "HD", Template.MagicTemplateProto.Type.HIDE),
  REFLEX("Reflex", "Ref", Template.MagicTemplateProto.Type.REFLEX),
  WILL("Will", "Will", Template.MagicTemplateProto.Type.WILL),
  FORTITUDE("Fortitude", "For", Template.MagicTemplateProto.Type.FORTITUDE),
  MOVE_SILENTLY("Move Silently", "MS", Template.MagicTemplateProto.Type.MOVE_SILENTLY);

  private final String name;
  private final String shortName;
  private final Template.MagicTemplateProto.Type proto;

  MagicEffectType(String name, String shortName, Template.MagicTemplateProto.Type proto) {
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
  public Template.MagicTemplateProto.Type toProto() {
    return proto;
  }

  @Override
  public String toString() {
    return getName();
  }

  public static MagicEffectType fromName(String name) {
    return Enums.fromName(name, values());
  }

  public static MagicEffectType fromProto(Template.MagicTemplateProto.Type proto) {
    return Enums.fromProto(proto, values());
  }

  public static ArrayList<String> names() {
    return Enums.names(values());
  }
}
