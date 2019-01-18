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

package net.ixitxachitls.companion.data.templates;

import net.ixitxachitls.companion.data.values.Modifier;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.rules.Products;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A template for feats.
 */
public class FeatTemplate extends StoredTemplate<Template.FeatTemplateProto> {
  public static final String TYPE = "feat";
  private final Template.FeatTemplateProto proto;

  public FeatTemplate(Template.FeatTemplateProto proto, String name) {
    super(name);
    this.proto = proto;
  }

  public List<Modifier> getInitiativeAdjustment() {
    return proto.getInitiativeModifier().getModifierList().stream()
        .map(p -> Modifier.fromProto(p, name))
        .collect(Collectors.toList());
  }

  public boolean isFromPHB() {
    return Products.isFromPHB(proto.getTemplate());
  }

  public static Template.FeatTemplateProto defaultProto() {
    return Template.FeatTemplateProto.getDefaultInstance();
  }

  public static FeatTemplate fromProto(Template.FeatTemplateProto proto) {
    FeatTemplate feat = new FeatTemplate(proto, proto.getTemplate().getName());
    return feat;
  }
}
