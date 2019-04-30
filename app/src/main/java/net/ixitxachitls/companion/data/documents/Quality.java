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

import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.templates.QualityTemplate;
import net.ixitxachitls.companion.data.values.Values;
import net.ixitxachitls.companion.proto.Template;

import java.util.HashMap;
import java.util.Map;

/**
 * A quality of a creature or item.
 */
public class Quality extends NestedDocument {

  private static final String FIELD_NAME = "name";

  private final Template.ParametrizedTemplateProto proto;
  private final QualityTemplate template;
  private final String entity;

  public Quality(Template.ParametrizedTemplateProto proto, String entity) {
    this(proto, template(proto.getName()), entity);
  }

  public Quality(String name, String entity) {
    this(Template.ParametrizedTemplateProto.newBuilder().setName(name).build(),
        template(name), entity);
  }

  public Quality(QualityTemplate template, String entity) {
    this(Template.ParametrizedTemplateProto.newBuilder().setName(template.getName()).build(),
        template, entity);
  }

  private Quality(Template.ParametrizedTemplateProto proto, QualityTemplate template,
                  String entity) {
    this.proto = proto;
    this.template = template;
    this.entity = entity;
  }

  public String getEntity() {
    return entity;
  }

  public String getName() {
    return template.getName();
  }

  public QualityTemplate getTemplate() {
    return template;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_NAME, getName());

    return data;
  }

  public static Quality read(Map<String, Object> data, String entity) {
    return new Quality(Values.get(data, FIELD_NAME, ""), entity);
  }

  private static QualityTemplate template(String name) {
    return Templates.get().getQualityTemplates().get(name)
        .orElse(new QualityTemplate(Template.QualityTemplateProto.getDefaultInstance(),
            name));
  }
}
