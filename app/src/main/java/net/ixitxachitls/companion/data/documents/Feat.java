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

import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.templates.FeatTemplate;
import net.ixitxachitls.companion.data.values.Modifier;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.util.Strings;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A feat a player has selected. Includes the feat name and possible parameters (like the weapon
 * selected for weapon feats).
 */
public class Feat extends NestedDocument {

  private static final String FIELD_NAME = "name";
  private static final String FIELD_QUALIFIERS = "qualifier";

  private final FeatTemplate template;
  private final List<String> qualifiers;
  private final String source;

  public Feat(String name, String source) {
    this(name, Collections.emptyList(), source);
  }

  public Feat(Value.FeatSelection selection, String source) {
    this(selection.getName(), selection.getQualifierList(), source);
  }

  public Feat(FeatTemplate template, String source) {
    this(template, Collections.emptyList(), source);
  }

  public Feat(FeatTemplate template, List<String> qualifiers, String source) {
    this.template = template;
    this.qualifiers = qualifiers;
    this.source = source;
  }

  private Feat(String name, List<String> qualifiers, String source) {
    Optional<FeatTemplate> feat = Templates.get().getFeatTemplates().get(name);
    if (feat.isPresent()) {
      template = feat.get();
    } else {
      template = new FeatTemplate(Template.FeatTemplateProto.getDefaultInstance(), name);
    }
    this.qualifiers = qualifiers;
    this.source = source;
  }

  public List<Modifier> getAttackModifiers() {
    return template.getAttackModifiers();
  }

  public List<Modifier> getDamageModifiers() {
    return template.getDamageModifiers();
  }

  public List<Modifier> getInitiativeAdjustment() {
    return template.getInitiativeAdjustment();
  }

  public String getName() {
    return template.getName();
  }

  public List<String> getQualifiers() {
    return qualifiers;
  }

  public String getSource() {
    return source;
  }

  public FeatTemplate getTemplate() {
    return template;
  }

  public String getTitle() {
    if (!qualifiers.isEmpty()) {
      return getName() + " (" + Strings.COMMA_JOINER.join(qualifiers) + ")";
    }

    return getName();
  }

  @Override
  public int hashCode() {
    return Objects.hash(template.getName(), qualifiers.hashCode());
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null || getClass() != other.getClass()) return false;

    Feat feat = (Feat) other;
    return template.getName().equals(feat.template.getName())
        && equalQualifiers(qualifiers, feat.qualifiers);
  }

  @Override
  public String toString() {
    return getTitle();
  }

  public Feat withQualifiers(List<String> qualifiers) {
    return new Feat(template, qualifiers, source);
  }

  @Override
  public Data write() {
    return Data.empty()
        .set(FIELD_NAME, template.getName())
        .set(FIELD_QUALIFIERS, qualifiers);
  }

  private static boolean equalQualifiers(List<String> first, List<String> second) {
    if (first.size() != second.size()) {
      return false;
    }

    for (String value : first) {
      if (second.indexOf(value) < 0) {
        return false;
      }
    }

    return true;
  }

  private static Optional<String> fromEmpty(String value) {
    if (value.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(value);
  }

  public static Feat read(Data data, String source) {
    return new Feat(data.get(FIELD_NAME, ""),
        data.getList(FIELD_QUALIFIERS, Collections.emptyList()), source);
  }
}
