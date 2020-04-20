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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.enums.CountUnit;
import net.ixitxachitls.companion.data.enums.MagicEffectType;
import net.ixitxachitls.companion.data.enums.Probability;
import net.ixitxachitls.companion.data.enums.Size;
import net.ixitxachitls.companion.data.enums.Slot;
import net.ixitxachitls.companion.data.values.Container;
import net.ixitxachitls.companion.data.values.Damage;
import net.ixitxachitls.companion.data.values.Distance;
import net.ixitxachitls.companion.data.values.Modifier;
import net.ixitxachitls.companion.data.values.Money;
import net.ixitxachitls.companion.data.values.RandomDuration;
import net.ixitxachitls.companion.data.values.Substance;
import net.ixitxachitls.companion.data.values.Weight;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A basic item.
 */
public class ItemTemplate extends StoredTemplate<Template.ItemTemplateProto> {

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

  public Count getAmount() {
    return new Count(proto.getMultiple().getCount(),
        CountUnit.fromProto(proto.getMultiple().getUnit()));
  }

  public List<Modifier> getArmorModifiers() {
    return proto.getArmor().getAcBonus().getModifierList().stream()
        .map(p -> Modifier.fromProto(p, getName()))
        .collect(Collectors.toList());
  }

  public int getBreakDC() {
    return proto.getBreakDc();
  }

  public List<String> getCategories() {
    return proto.getTemplate().getCategoryList();
  }

  public Count getCounted() {
    return new Count(proto.getCounted().getCount(),
        CountUnit.fromProto(proto.getCounted().getUnit()));
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

  public RandomDuration getDonDuration() {
    return RandomDuration.fromProto(proto.getWearable().getWear());
  }

  public RandomDuration getDonHastilyDuration() {
    return RandomDuration.fromProto(proto.getWearable().getWearHastily());
  }

  public int getHardness() {
    return proto.getHardness();
  }

  public String getIncomplete() {
    if (proto.hasIncomplete() && !proto.getTemplate().getIncomplete().isEmpty()) {
      return proto.getIncomplete().getText() + " " + proto.getIncomplete().getText();
    }

    if (proto.hasIncomplete()) {
      return proto.getIncomplete().getText();
    }

    return proto.getTemplate().getIncomplete();
  }

  public List<Modifier> getMagicAttackModifiers() {
    return getMagicModifiers(Template.MagicTemplateProto.Type.ATTACK);
  }

  public Multimap<MagicEffectType, Modifier> getMagicModifiers() {
    Multimap<MagicEffectType, Modifier> modifiers = ArrayListMultimap.create();
    for (Template.MagicTemplateProto.Modifier modifier : proto.getMagic().getModifierList()) {
      modifiers.putAll(MagicEffectType.fromProto(modifier.getType()),
          Modifier.fromProto(modifier.getModifier(), getName()));
    }

    return modifiers;
  }

  public Template.ItemTemplateProto.Substance.Material getMaterialProto() {
    return proto.getSubstance().getMaterial();
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

  public Probability getProbability() {
    return Probability.fromProto(proto.getProbability());
  }

  @Override
  public Set<String> getProductIds() {
    return extractProductIds(proto.getTemplate());
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

  public List<String> getReferences() {
    return proto.getTemplate().getReferenceList().stream()
        .map(r -> formatReference(r))
        .collect(Collectors.toList());
  }

  public RandomDuration getRemoveDuration() {
    return RandomDuration.fromProto(proto.getWearable().getRemove());
  }

  public Damage getSecondaryDamage() {
    if (!isWeapon()) {
      return new Damage();
    }

    Damage damage = Damage.from(proto.getWeapon().getSecondaryDamage(), getName());
    for (Template.MagicTemplateProto.Modifier modifier : proto.getMagic().getModifierList()) {
      if (modifier.getType() == Template.MagicTemplateProto.Type.DAMAGE) {
        damage.addModifiers(Modifier.fromProto(modifier.getModifier(), getName()));
      }
    }

    return damage;
  }

  public String getShortDescription() {
    return proto.getTemplate().getShortDescription();
  }

  public Size getSize() {
    return Size.fromProto(proto.getSize().getSize());
  }

  public Value.SizeProto.Size getSizeProto() {
    return proto.getSize().getSize();
  }

  public Slot getSlot() {
    return Slot.fromProto(proto.getWearable().getSlot());
  }

  public Damage getSplashDamage() {
    if (!isWeapon()) {
      return new Damage();
    }

    return Damage.from(proto.getWeapon().getSplash(), getName());
  }

  public Substance getSubstance() {
    return substance;
  }

  public List<String> getSynonyms() {
    return proto.getTemplate().getSynonymList();
  }

  public RandomDuration getTimed() {
    return RandomDuration.fromProto(proto.getTimed().getDuration());
  }

  public int getUses() {
    return proto.getMultiuse().getCount();
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

  public Value.Proficiency getWeaponProficiency() {
    return proto.getWeapon().getProficiency();
  }

  public Value.WeaponStyle getWeaponStyle() {
    return proto.getWeapon().getStyle();
  }

  public Template.WeaponTemplateProto.Type getWeaponType() {
    return proto.getWeapon().getType();
  }

  public Weight getWeight() {
    return weight;
  }

  public Size getWielderSize() {
    return Size.fromProto(proto.getWeapon().getWielderSize());
  }

  public List<String> getWorlds() {
    return proto.getTemplate().getWorldList();
  }

  public boolean isAmmunition() {
    return proto.getWeapon().getAmmunition();
  }

  public boolean isArmor() {
    return proto.hasArmor();
  }

  public boolean isBaseOnly() {
    return proto.getTemplate().getBaseOnly();
  }

  public boolean isContainer() {
    return container.hasCapacity();
  }

  public boolean isCounted() {
    return proto.hasCounted();
  }

  public boolean isMonetary() {
    return monetary;
  }

  public boolean isMultiple() {
    return proto.hasMultiple();
  }

  public boolean isMultiuse() {
    return proto.hasMultiuse();
  }

  public boolean isReal() {
    return !isTemplate();
  }

  public boolean isTemplate() {
    return weight.isZero() || value.isZero();
  }

  public boolean isTimed() {
    return proto.hasTimed();
  }

  public boolean isWeapon() {
    return proto.hasWeapon();
  }

  public boolean isWearable() {
    return proto.hasWearable();
  }

  public List<ItemTemplate> collectTemplates() {
    List<ItemTemplate> templates = new ArrayList<>();
    templates.add(this);

    for (String base : proto.getTemplate().getBaseList()) {
      Optional<ItemTemplate> template = Templates.get().getItemTemplates().get(base);
      if (template.isPresent()) {
        templates.add(template.get());
      } else {
        Status.error("Cannot find base template " + base + " for item template " + getName());
      }
    }

    return templates;
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

  public boolean hasWeaponFiness() {
    return proto.getWeapon().getFinesse();
  }

  @Override
  public String toString() {
    return getName();
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

  private String formatRange(Value.RangeProto range) {
    if (range.getHigh() == range.getLow()) {
      return String.valueOf(range.getLow());
    }

    return range.getLow() + "_" + range.getHigh();
  }

  private String formatReference(Value.ReferenceProto reference) {
    Optional<ProductTemplate> product =
        Templates.get().getProductTemplates().get(reference.getName());
    String name;
    if (product.isPresent()) {
      name = product.get().getTitle() + " (" + reference.getName() + ")";
    } else {
      name = reference.getName();
    }

    String pages = "";
    if (reference.getPagesCount() > 1) {
      pages = " pages " + Strings.SPACE_JOINER.join(reference.getPagesList().stream()
          .map(p -> formatRange(p))
          .collect(Collectors.toList()));
    } else if (reference.getPagesCount() > 0) {
      pages = " page " + formatRange(reference.getPages(0));
    }

    return name + pages;
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

  public static class Count {
    private final int count;
    private final CountUnit unit;

    public Count(int count, CountUnit unit) {
      this.count = count;
      this.unit = unit;
    }

    public int getCount() {
      return count;
    }

    public CountUnit getUnit() {
      return unit;
    }

    public String format() {
      return unit.format(count);
    }
  }
}
