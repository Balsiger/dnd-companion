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
import net.ixitxachitls.companion.data.templates.FeatTemplate;
import net.ixitxachitls.companion.data.values.Modifier;
import net.ixitxachitls.companion.data.values.Values;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.proto.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A feat a player has selected. Includes the feat name and possible parameters (like the weapon
 * selected for weapon feats).
 */
public class Feat extends NestedDocument {

  private static final String FIELD_NAME = "name";
  private static final String FIELD_WEAPON = "weapon";
  private static final String FIELD_SPELL_SCHOOL = "spell_school";

  private final FeatTemplate template;
  private final Optional<String> weapon;
  private final Optional<String> spellSchool;

  public Feat(String name) {
    this(name, Optional.empty(), Optional.empty());
  }

  public Feat(Value.FeatSelection selection) {
    this(selection.getName(), fromEmpty(selection.getWeapon()),
        fromEmpty(selection.getSpellSchool()));
  }

  public Feat(FeatTemplate template) {
    this.template = template;
    this.weapon = Optional.empty();
    this.spellSchool = Optional.empty();
  }

  private Feat(String name, Optional<String> weapon, Optional<String> spellSchool) {
    Optional<FeatTemplate> feat = Templates.get().getFeatTemplates().get(name);
    if (feat.isPresent()) {
      template = feat.get();
    } else {
      template = new FeatTemplate(Template.FeatTemplateProto.getDefaultInstance(), name);
    }
    this.weapon = weapon;
    this.spellSchool = spellSchool;
  }

  public List<Modifier> getInitiativeAdjustment() {
    return template.getInitiativeAdjustment();
  }

  public String getName() {
    return template.getName();
  }

  public FeatTemplate getTemplate() {
    return template;
  }

  @Override
  public int hashCode() {
    return Objects.hash(template.getName(), weapon.hashCode(), spellSchool.hashCode());
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null || getClass() != other.getClass()) return false;

    Feat feat = (Feat) other;
    return template.getName().equals(feat.template)
        && weapon.equals(feat.weapon)
        && spellSchool.equals(feat.spellSchool);
  }

  @Override
  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_NAME, template.getName());
    if (weapon.isPresent()) {
      data.put(FIELD_WEAPON, weapon.get());
    }
    if (spellSchool.isPresent()) {
      data.put(FIELD_SPELL_SCHOOL, spellSchool.get());
    }

    return data;
  }

  private static Optional<String> fromEmpty(String value) {
    if (value.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(value);
  }

  public static Feat read(Map<String, Object> data) {
    return new Feat(Values.get(data, FIELD_NAME, ""), fromEmpty(Values.get(data, FIELD_WEAPON, "")),
        fromEmpty(Values.get(data, FIELD_SPELL_SCHOOL, "")));
  }
}
