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

package net.ixitxachitls.companion.data.documents;

import android.support.annotation.Nullable;

import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.templates.FeatTemplate;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.proto.Value;

import java.util.Map;
import java.util.Optional;

/**
 * A feat a player has selected. Includes the feat name and possible parameters (like the weapon
 * selected for weapon feats).
 */
public class Feat extends NestedDocument {

  private final FeatTemplate template;
  private final Optional<Value.ParametersProto> parameters;

  public Feat(String name) {
    this(name, null);
  }

  public Feat(String name, @Nullable Value.ParametersProto parameters) {
    Optional<FeatTemplate> feat = Templates.get().getFeatTemplates().get(name);
    if (feat.isPresent()) {
      template = feat.get();
    } else {
      template = new FeatTemplate(Template.FeatTemplateProto.getDefaultInstance(), name);
    }

    this.parameters = Optional.ofNullable(parameters);
  }

  @Override
  public Map<String, Object> write() {
    return null;
  }

  public static Feat read(Map<String, Object> data) {
    return null;
  }
}
