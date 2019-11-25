/*
 * Copyright (c) 2017-2018 Peter Balsiger
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

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.enums.Size;
import net.ixitxachitls.companion.data.values.Container;
import net.ixitxachitls.companion.data.values.Damage;
import net.ixitxachitls.companion.data.values.Distance;
import net.ixitxachitls.companion.data.values.Modifier;
import net.ixitxachitls.companion.data.values.Money;
import net.ixitxachitls.companion.data.values.Substance;
import net.ixitxachitls.companion.data.values.Weight;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.proto.Value;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A basic item.
 */
public class ItemTemplate extends StoredTemplate<Template.ItemTemplateProto> {

  public enum Probability {
    unknown, common, uncommon, rare, veryRare, unique,
  }

  public static final String TYPE = "item";

  private final List<String> synonyms;
  private final String description;
  private final Money value;
  private final Weight weight;
  private final int hp;
  private final Substance substance;
  private final Container container;
  private final Appearances appearances;
  private final boolean monetary;
  private final Template.ItemTemplateProto proto;

  protected ItemTemplate(String name, List<String> synonyms, String description, Money value,
                         Weight weight, int hp, Substance substance, Container container,
                         Appearances appearances, boolean monetary,
                         Template.ItemTemplateProto proto) {
    super(name);

    this.synonyms = synonyms;
    this.description = description;
    this.value = value;
    this.weight = weight;
    this.hp = hp;
    this.substance = substance;
    this.container = container;
    this.appearances = appearances;
    this.monetary = monetary;
    this.proto = proto;
  }

  public List<Modifier> getArmorModifiers() {
    return proto.getArmor().getAcBonus().getModifierList().stream()
        .map(p -> Modifier.fromProto(p, getName()))
        .collect(Collectors.toList());
  }

  public Damage getDamage() {
    if (!isWeapon()) {
      return new Damage();
    }

    Damage damage = Damage.from(proto.getWeapon().getDamage(), getName());
    for (Template.MagicTemplateProto.Modifier modifier : proto.getMagic().getModifierList()) {
      if (modifier.getType() == Template.MagicTemplateProto.Type.DAMAGE) {
        damage.addModifiers(Modifier.fromProto(modifier.getModifier(), getName()));
      }
    }

    return damage;
  }

  public String getDescription() {
    return description;
  }

  public List<Modifier> getMagicAttackModifiers() {
    return getMagicModifiers(Template.MagicTemplateProto.Type.ATTACK);
  }

  public boolean isBaseOnly() {
    return proto.getTemplate().getBaseOnly();
  }

  public int weightedProbability() {
    switch (proto.getProbability()) {
      case UNIQUE:
        return 1;
      case VERY_RARE:
        return 10;
      case RARE:
        return 100;
      case UNCOMMON:
        return 500;
      case COMMON:
        return 1000;

      default:
      case UNRECOGNIZED:
      case UNKNOWN:
        return 0;
    }
  }

  public List<String> getCategories() {
    return proto.getTemplate().getCategoryList();
  }

  public int getMaxAttacks() {
    return proto.getWeapon().getMaxAttacks() > 0 ?
        proto.getWeapon().getMaxAttacks() : Integer.MAX_VALUE;
  }

  public int getMaxDexterityModifier() {
    if (proto.hasArmor() && proto.getArmor().getMaxDexterity() > 0) {
      return proto.getArmor().getMaxDexterity();
    }

    return Integer.MAX_VALUE;
  }

  public String getNamePart() {
    // TODO(merlind): Handle this whole things better somehow...
    if (synonyms.isEmpty() || isReal()) {
      return name;
    }

    if (synonyms.get(0).contains(",")) {
      return name;
    }

    return synonyms.get(0);
  }

  public Optional<Distance> getRange() {
    if (!isWeapon() && proto.getWeapon().hasRange()) {
      return Optional.empty();
    }

    return Optional.of(Distance.fromProto(proto.getWeapon().getRange()));
  }

  public Optional<Distance> getReach() {
    if (!isWeapon() && proto.getWeapon().hasReach()) {
      return Optional.empty();
    }

    return Optional.of(Distance.fromProto(proto.getWeapon().getReach()));
  }

  public Money getValue() {
    return value;
  }

  public int getWeaponCriticalLow() {
    int low = (int) proto.getWeapon().getCritical().getThreat().getLow();
    return low > 0 ? low : 20;
  }

  public int getWeaponCriticalMultiplier() {
    int multiplier = proto.getWeapon().getCritical().getMultiplier();
    return multiplier == 0 ? 2 : multiplier;
  }

  public Value.WeaponStyle getWeaponStyle() {
    return proto.getWeapon().getStyle();
  }

  public Size getSize() {
    return Size.fromProto(proto.getSize().getSize());
  }

  public Value.SizeProto.Size getSizeProto() {
    return proto.getSize().getSize();
  }

  public Weight getWeight() {
    return weight;
  }

  public Template.ItemTemplateProto.Substance.Material getMaterialProto() {
    return proto.getSubstance().getMaterial();
  }

  public boolean isArmor() {
    return proto.hasArmor();
  }

  public boolean isContainer() {
    return container.hasCapacity();
  }

  public boolean isMonetary() {
    return monetary;
  }

  public boolean isReal() {
    return !isTemplate();
  }

  public boolean isTemplate() {
    return weight.isZero() || value.isZero();
  }

  public boolean isWeapon() {
    return proto.hasWeapon();
  }

  public String computeAppearance() {
    return appearances.random();
  }

  public int computeHp() {
    if (hp > 0) {
      return hp;
    }

    return substance.computeHp();
  }

  public List<Modifier> getMagicModifiers(Template.MagicTemplateProto.Type type) {
    return proto.getMagic().getModifierList().stream()
        .filter(m -> m.getType() == type)
        .map(m -> m.getModifier().getModifierList())
        .flatMap(List::stream)
        .map(m -> Modifier.fromProto(m, getName()))
        .collect(Collectors.toList());
  }

  public int getMaxSpeedSquares(boolean isFast) {
    if (proto.hasArmor()) {
      if (isFast && proto.getArmor().getSpeedFast() > 0) {
        return proto.getArmor().getSpeedFast();
      } else if (!isFast && proto.getArmor().getSpeedSlow() > 0) {
        return proto.getArmor().getSpeedSlow();
      }
    }

    return Integer.MAX_VALUE;
  }

  @Override
  public String toString() {
    return getName();
  }

  public static Probability convert(net.ixitxachitls.companion.proto.Template.ItemTemplateProto.Probability probability) {
    switch (probability) {
      default:
        Status.error("Cannot convert unknown probability: " + probability);

      case UNKNOWN:
        return Probability.unknown;

      case COMMON:
        return Probability.common;

      case UNCOMMON:
        return Probability.uncommon;

      case RARE:
        return Probability.rare;

      case VERY_RARE:
        return Probability.veryRare;

      case UNIQUE:
        return Probability.unique;
    }
  }

  public static net.ixitxachitls.companion.proto.Template.ItemTemplateProto.Probability convert(Probability probability) {
    switch (probability) {
      default:
        Status.error("Cannot convert unknown probabilyt " + probability);

      case unknown:
        return net.ixitxachitls.companion.proto.Template.ItemTemplateProto.Probability.UNKNOWN;

      case common:
        return net.ixitxachitls.companion.proto.Template.ItemTemplateProto.Probability.COMMON;

      case uncommon:
        return net.ixitxachitls.companion.proto.Template.ItemTemplateProto.Probability.UNCOMMON;

      case rare:
        return net.ixitxachitls.companion.proto.Template.ItemTemplateProto.Probability.RARE;

      case veryRare:
        return net.ixitxachitls.companion.proto.Template.ItemTemplateProto.Probability.VERY_RARE;

      case unique:
        return net.ixitxachitls.companion.proto.Template.ItemTemplateProto.Probability.UNIQUE;
    }
  }

  public static ItemTemplate createEmpty(String name) {
    return new ItemTemplate(name, Collections.emptyList(), "", Money.ZERO, Weight.ZERO, 0,
        Substance.ZERO, Container.NONE, Appearances.EMPTY, false, defaultProto());
  }

  public static net.ixitxachitls.companion.proto.Template.ItemTemplateProto defaultProto() {
    return net.ixitxachitls.companion.proto.Template.ItemTemplateProto.getDefaultInstance();
  }

  public static ItemTemplate fromProto(
      net.ixitxachitls.companion.proto.Template.ItemTemplateProto proto) {
    ItemTemplate item = new ItemTemplate(proto.getTemplate().getName(),
        proto.getTemplate().getSynonymList(), proto.getTemplate().getDescription(),
        Money.fromProto(proto.getValue()), Weight.fromProto(proto.getWeight()),
        proto.getHitPoints(), Substance.fromProto(proto.getSubstance()),
        Container.fromProto(proto.getContainer()),
        Appearances.fromProto(proto.getAppearanceList()),
        proto.getMonetary(), proto);

    return item;
  }
}
