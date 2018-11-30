/*
 * Copyright (c) 2017-2018 Peter Balsiger
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

package net.ixitxachitls.companion.data.statics;

import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.rules.Products;

/**
 * All the base information about a level.
 */
public class LevelTemplate extends StaticEntry<Template.LevelTemplateProto> {
  public static final String TYPE = "level";

  private final Template.LevelTemplateProto proto;
  private final int maxHp;

  public LevelTemplate() {
    this(defaultProto(), "", 0);
  }

  public LevelTemplate(Template.LevelTemplateProto proto, String name, int maxHp) {
    super(name);
    this.proto = proto;
    this.maxHp = maxHp;
  }

  public int getMaxHp() {
    return maxHp;
  }

  public boolean isFromPHB() {
    return Products.isFromPHB(proto.getTemplate());
  }

  public static Template.LevelTemplateProto defaultProto() {
    return Template.LevelTemplateProto.getDefaultInstance();
  }

  public static LevelTemplate fromProto(Template.LevelTemplateProto proto) {
    LevelTemplate level = new LevelTemplate(proto, proto.getTemplate().getName(),
        proto.getHitDice().getDice());
    return level;
  }
}
