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

package net.ixitxachitls.companion.data.documents;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;

import com.google.common.collect.ImmutableMap;

import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.templates.QualityTemplate;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.util.Texts;

import java.util.Map;

/**
 * A quality of a creature or item.
 */
public class Quality extends NestedDocument implements Parcelable {

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
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeString(template.getName());
    parcel.writeString(entity);

    // TODO(merlin): Need to write the parametrized template proto here as well!
  }

  public Spannable formatName(Context context, int count, Character character) {
    return Texts.processCommands(context, template.getNameFormat(),
        collectFormatValues(count, character));
  }

  public Map<String, Texts.Value> collectFormatValues(int count, Character character) {
    return ImmutableMap.<String, Texts.Value>builder()
        .putAll(character.collectFormatValues())
        .put("count", new Texts.IntegerValue(count))
        .put("source", new Texts.StringValue(entity))
        .build();
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public Data write() {
    return Data.empty()
        .set(FIELD_NAME, getName());
  }

  public static Quality read(Data data, String entity) {
    return new Quality(data.get(FIELD_NAME, ""), entity);
  }

  private static QualityTemplate template(String name) {
    return Templates.get().getQualityTemplates().get(name)
        .orElse(new QualityTemplate(Template.QualityTemplateProto.getDefaultInstance(),
            name));
  }
}
