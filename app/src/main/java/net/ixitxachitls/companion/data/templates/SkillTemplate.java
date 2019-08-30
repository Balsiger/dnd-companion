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

import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.proto.Template;

/**
 * A template for a skill.
 */
public class SkillTemplate extends StoredTemplate<Template.SkillTemplateProto> {
  public static final String TYPE = "skill";

  private final Template.SkillTemplateProto proto;

  public SkillTemplate(Template.SkillTemplateProto proto, String name) {
    super(name);
    this.proto = proto;
  }

  public Ability getAbility() {
    return Ability.fromProto(proto.getAbility());
  }

  public static Template.SkillTemplateProto defaultProto() {
    return Template.SkillTemplateProto.getDefaultInstance();
  }

  public static SkillTemplate fromProto(Template.SkillTemplateProto proto) {
    SkillTemplate skill = new SkillTemplate(proto, proto.getTemplate().getName());
    return skill;
  }
}
