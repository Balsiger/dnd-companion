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

package net.ixitxachitls.companion.data.templates;

import net.ixitxachitls.companion.data.enums.Rarity;
import net.ixitxachitls.companion.data.enums.Size;
import net.ixitxachitls.companion.proto.Template;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A representation of a miniature template that can be used to create actual miniatures for the
 * game.
 */
public class MiniatureTemplate extends StoredTemplate<Template .MiniatureTemplateProto> {

  public static final String TYPE = "miniature";
  private final Template.MiniatureTemplateProto proto;

  public MiniatureTemplate(String name, Template.MiniatureTemplateProto proto) {
    super(proto.getTemplate(), name);
    this.proto = proto;
  }

  public List<String> getClasses() {
    return proto.getClass_List();
  }

  public int getNumber() {
    return proto.getNumber();
  }

  public String getNumberAffix() {
    return proto.getNumberAffix();
  }

  @Override
  public Set<String> getProductIds() {
    return Collections.emptySet();
  }

  public String getRace() {
    return proto.getRace();
  }

  public Rarity getRarity() {
    return Rarity.fromProto(proto.getRarity());
  }

  public String getSet() {
    return proto.getSet();
  }

  public Size getSize() {
    return Size.fromProto(proto.getSize());
  }

  public List<String> getSubtypes() {
    return proto.getSubtypeList();
  }

  public String getType() {
    return proto.getType();
  }

  @Override
  public String toString() {
    return getName();
  }

  public static Template.MiniatureTemplateProto defaultProto() {
    return net.ixitxachitls.companion.proto.Template.MiniatureTemplateProto.getDefaultInstance();
  }

  private static String extractProductId(Template.TemplateProto template) {
    if (template.getReferenceCount() > 0) {
      return template.getReference(0).getName();
    }

    return "";
  }

  public static MiniatureTemplate fromProto(Template.MiniatureTemplateProto proto) {
    MiniatureTemplate template = new MiniatureTemplate(proto.getTemplate().getName(), proto);

    return template;
  }
}
