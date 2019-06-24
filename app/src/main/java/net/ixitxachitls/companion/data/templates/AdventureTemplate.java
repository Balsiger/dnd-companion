/*
 * Copyright (c) 2017-2019 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.templates;

import net.ixitxachitls.companion.proto.Template;

/**
 * A template for a complete Adventure
 */
public class AdventureTemplate extends StoredTemplate<Template.AdventureTemplateProto> {

  public static final String TYPE = "adventure";

  private final Template.AdventureTemplateProto proto;

  public AdventureTemplate(String name, Template.AdventureTemplateProto proto) {
    super(name);

    this.proto = proto;
  }

  public static Template.AdventureTemplateProto defaultProto() {
    return Template.AdventureTemplateProto.getDefaultInstance();
  }

  public static AdventureTemplate fromProto(Template.AdventureTemplateProto proto) {
    return new AdventureTemplate(proto.getTemplate().getName(), proto);
  }
}
