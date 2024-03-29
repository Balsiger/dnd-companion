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
import net.ixitxachitls.companion.data.values.Modifier;
import net.ixitxachitls.companion.data.values.Speed;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.proto.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A template for a quality.
 */
public class QualityTemplate extends StoredTemplate<Template.QualityTemplateProto> {

  public static final String TYPE = "quality";

  private final Template.QualityTemplateProto proto;

  public QualityTemplate(Template.QualityTemplateProto proto, String name) {
    super(proto.getTemplate(), name);
    this.proto = proto;
  }

  public List<Modifier> getAcModifiers() {
    return proto.getAcModifier().getModifierList().stream()
        .map(p -> Modifier.fromProto(p, getName()))
        .collect(Collectors.toList());
  }

  public List<Modifier> getAttackModifiers() {
    return proto.getAttackModifier().getModifierList().stream()
        .map(p -> Modifier.fromProto(p, getName()))
        .collect(Collectors.toList());
  }

  public List<Modifier> getDamageModifiers() {
    return Modifier.fromProto(proto.getDamageModifier(), getName());
  }

  public String getDescription() {
    return proto.getTemplate().getDescription();
  }

  public List<Modifier> getFortitudeModifiers() {
    List<Modifier> modifiers = new ArrayList<>();
    for (Value.ModifierProto.Modifier modifier : proto.getFortitudeModifier().getModifierList()) {
      modifiers.add(Modifier.fromProto(modifier, getName()));
    }

    return modifiers;
  }

  public String getNameFormat() {
    if (proto.getNameFormat().isEmpty()) {
      return name;
    }

    return proto.getNameFormat();
  }

  @Override
  public Set<String> getProductIds() {
    return extractProductIds(proto.getTemplate());
  }

  public List<Modifier> getReflexModifiers() {
    List<Modifier> modifiers = new ArrayList<>();
    for (Value.ModifierProto.Modifier modifier : proto.getReflexModifier().getModifierList()) {
      modifiers.add(Modifier.fromProto(modifier, getName()));
    }

    return modifiers;
  }

  public String getShortDescription() {
    if (proto.getTemplate().getShortDescription().isEmpty()) {
      return "(no short description)";
    }

    return proto.getTemplate().getShortDescription();
  }

  public Template.QualityTemplateProto.Type getType() {
    return proto.getType();
  }

  public String getTypeFormatted() {
    switch (proto.getType()) {
      case EXTRAORDINARY:
        return "Extraordinary";
      case SPELL_LIKE:
        return "Spell Like";
      case SUPERNATURAL:
        return "Supernatural";
      case UNRECOGNIZED:
        return "Unrecognized";
      default:
      case UNKNOWN:
        return "Uknown";
    }
  }

  public List<Modifier> getWillModifiers() {
    List<Modifier> modifiers = new ArrayList<>();
    for (Value.ModifierProto.Modifier modifier : proto.getWillModifier().getModifierList()) {
      modifiers.add(Modifier.fromProto(modifier, getName()));
    }

    return modifiers;
  }

  public List<Modifier> getAbilityModifiers(Ability ability) {
    List<Modifier> modifiers = new ArrayList<>();

    for (Template.QualityTemplateProto.AbilityModifier abilityModifier
        : proto.getAbilityModifierList()) {
      if (Ability.fromProto(abilityModifier.getAbility()) == ability) {
        for (Value.ModifierProto.Modifier modifier
            : abilityModifier.getModifier().getModifierList()) {
          modifiers.add(Modifier.fromProto(modifier, getName()));
        }
      }
    }

    return modifiers;
  }

  public int getSpeedSquares(Speed.Mode mode) {
    if (Speed.convert(proto.getSpeed().getMode()) == mode) {
      return proto.getSpeed().getSquares();
    }

    return 0;
  }

  public static Template.QualityTemplateProto defaultProto() {
    return Template.QualityTemplateProto.getDefaultInstance();
  }

  public static QualityTemplate fromProto(Template.QualityTemplateProto proto) {
    QualityTemplate quality = new QualityTemplate(proto, proto.getTemplate().getName());
    return quality;
  }
}
