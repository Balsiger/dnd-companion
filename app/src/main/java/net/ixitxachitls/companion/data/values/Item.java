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

package net.ixitxachitls.companion.data.values;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Data;
import net.ixitxachitls.companion.data.documents.Monster;
import net.ixitxachitls.companion.data.documents.NestedDocument;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.data.enums.MagicEffectType;
import net.ixitxachitls.companion.data.enums.Probability;
import net.ixitxachitls.companion.data.enums.Size;
import net.ixitxachitls.companion.data.enums.Slot;
import net.ixitxachitls.companion.data.enums.WeaponProficiency;
import net.ixitxachitls.companion.data.enums.WeaponStyle;
import net.ixitxachitls.companion.data.enums.WeaponType;
import net.ixitxachitls.companion.data.templates.ItemTemplate;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.util.Lazy;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * An item in the game.
 */
public class Item extends NestedDocument {

  private static final String FIELD_ID = "id";
  private static final String FIELD_NAME = "name";
  private static final String FIELD_TEMPLATES = "templates";
  private static final String FIELD_HP = "hp";
  private static final String FIELD_VALUE = "value";
  private static final String FIELD_APPEARANCE = "appearance";
  private static final String FIELD_PLAYER_NAME = "player_name";
  private static final String FIELD_PLAYER_NOTES = "player_notes";
  private static final String FIELD_DM_NOTES = "dm_notes";
  private static final String FIELD_COUNT = "counted";
  private static final String FIELD_MULTIPLE = "multiple";
  private static final String FIELD_MULTIUSE = "multiuse";
  private static final String FIELD_TIME_LEFT = "time_left";
  private static final String FIELD_IDENTIFIED = "identified";
  private static final String FIELD_CONTENTS = "contents";
  private static final String FIELD_HISTORY = "history";

  private final String id;
  private String name;
  private List<ItemTemplate> templates;
  private int hp;
  private Money value;
  private String appearance;
  private String playerName;
  private String playerNotes;
  private String dmNotes;
  private int multiple;
  private int multiuse;
  private int count;
  private Duration timeLeft;
  private boolean identified;
  private List<Item> contents;
  private History history;

  // Lazy values.
  private Lazy.State state = new Lazy.State();
  private Lazy.Resettable<Money> totalValue = new Lazy.Resettable<>(state, () -> {
    Money totalValue = multiple > 0 ? value.multiply(multiple) : value;
    totalValue = count > 0 ? totalValue.multiply(count) : totalValue;
    for (Item content : contents) {
      totalValue = totalValue.add(content.getValue());
    }

    return totalValue;
  });
  private Lazy.Resettable<List<Modifier>> armorModifiers = new Lazy.Resettable<>(state, () ->
      Templates.collectModifiers(templates, ItemTemplate::getArmorModifiers,
          t -> t.getMagicModifiers(Template.MagicTemplateProto.Type.ARMOR_CLASS)));
  private Lazy.Resettable<List<Modifier>> armorDeflectionModifiers =
      new Lazy.Resettable<>(state, () ->
          getArmorModifiers().stream()
              .filter(m -> m.getType() == Modifier.Type.DEFLECTION)
              .collect(Collectors.toList()));
  private Lazy.Resettable<Integer> breakDC = new Lazy.Resettable<>(state, () ->
      templates.stream().mapToInt(t -> t.getBreakDC()).max().orElse(0));
  private Lazy.Resettable<Set<String>> categories = new Lazy.Resettable<>(state, () ->
      templates.stream().flatMap(t -> t.getCategories().stream()).collect(Collectors.toSet()));
  private Lazy.Resettable<Damage> damage = new Lazy.Resettable<>(state, () ->
      Damage.from(templates.stream()
          .map(ItemTemplate::getDamage)
          .collect(Collectors.toList())));
  private Lazy.Resettable<RandomDuration> donDuration = new Lazy.Resettable<>(state, () ->
      templates.stream()
          .map(t -> t.getDonDuration())
          .max(RandomDuration::compareTo)
          .orElse(RandomDuration.NULL));
  private Lazy.Resettable<RandomDuration> donHastilyDuration = new Lazy.Resettable<>(state, () ->
      templates.stream()
          .map(t -> t.getDonHastilyDuration())
          .max(RandomDuration::compareTo)
          .orElse(RandomDuration.NULL));
  private Lazy.Resettable<Integer> hardness = new Lazy.Resettable<>(state, () ->
      templates.stream().mapToInt(t -> t.getHardness()).max().orElse(0));
  private Lazy.Resettable<String> incomplete = new Lazy.Resettable<>(state, () ->
      Strings.NEWLINE_JOINER.join(templates.stream()
          .map(ItemTemplate::getIncomplete)
          .collect(Collectors.toList())));
  private Lazy.Resettable<List<Modifier>> magicAttackModifiers = new Lazy.Resettable<>(state, () ->
      Templates.collectModifiers(templates, ItemTemplate::getMagicAttackModifiers));
  private Lazy.Resettable<Multimap<MagicEffectType, Modifier>> magicModifiers =
      new Lazy.Resettable<>(state, () -> {
        Multimap<MagicEffectType, Modifier> modifiers = ArrayListMultimap.create();
        for (ItemTemplate template : templates) {
          modifiers.putAll(template.getMagicModifiers());
        }

        return modifiers;
      });
  private Lazy.Resettable<Integer> maxMultiple = new Lazy.Resettable<>(state, () ->
      maxAmount(templates));
  private Lazy.Resettable<Integer> maxAttacks = new Lazy.Resettable<>(state, () ->
      templates.stream()
          .filter(t -> t.isWeapon())
          .mapToInt(t -> t.getMaxAttacks())
          .min().orElse(Integer.MAX_VALUE));
  private Lazy.Resettable<Integer> maxDexterityModifier = new Lazy.Resettable<>(state, () ->
      templates.stream()
          .mapToInt(t -> t.getMaxDexterityModifier())
          .min()
          .orElse(Integer.MAX_VALUE));
  private Lazy.Resettable<Integer> maxUses = new Lazy.Resettable<Integer>(state, () ->
      maxUses(templates));
  private Lazy.Resettable<Probability> probability = new Lazy.Resettable<>(state, () -> {
    Probability result = Probability.UNKNOWN;
    for (ItemTemplate template : templates) {
      Probability probability = template.getProbability();
      if (probability.ordinal() > result.ordinal()) {
        result = probability;
      }
    }

    return result;
  });
  private Lazy.Resettable<Weight> rawWeight =
      new Lazy.Resettable<>(state, () -> weight(templates));
  private Lazy.Resettable<List<String>> referenes = new Lazy.Resettable<>(state, () ->
      templates.stream()
          .flatMap(t -> t.getReferences().stream())
          .collect(Collectors.toList()));
  private Lazy.Resettable<RandomDuration> removeDuration = new Lazy.Resettable<>(state, () ->
      templates.stream()
          .map(t -> t.getRemoveDuration())
          .max(RandomDuration::compareTo)
          .orElse(RandomDuration.NULL));
  private Lazy.Resettable<Damage> secondaryDamage = new Lazy.Resettable<>(state, () ->
      Damage.from(templates.stream().map(ItemTemplate::getSecondaryDamage)
          .collect(Collectors.toList())));
  private Lazy.Resettable<Size> size = new Lazy.Resettable<>(state, () -> {
    Optional<ItemTemplate> base = getBaseTemplate();
    if (base.isPresent()) {
      return base.get().getSize();
    }

    return Size.UNKNOWN;
  });
  private Lazy.Resettable<Slot> slot = new Lazy.Resettable<>(state, () -> templates.stream()
      .map(ItemTemplate::getSlot)
      .filter(s -> s != Slot.UNKNOWN)
      .findFirst()
      .orElse(Slot.UNKNOWN));
  private Lazy.Resettable<Damage> splashDamage = new Lazy.Resettable<>(state, () ->
      Damage.from(templates.stream().map(ItemTemplate::getSplashDamage)
          .collect(Collectors.toList())));
  private Lazy.Resettable<Substance> substance = new Lazy.Resettable<>(state, () -> {
    Optional<ItemTemplate> base = getBaseTemplate();
    if (base.isPresent()) {
      return base.get().getSubstance();
    }

    return Substance.ZERO;
  });
  private Lazy.Resettable<List<String>> synonyms = new Lazy.Resettable<>(state, () -> {
    Optional<ItemTemplate> base = getBaseTemplate();
    if (base.isPresent()) {
      return base.get().getSynonyms();
    } else {
      return Collections.emptyList();
    }
  });
  private Lazy.Resettable<List<String>> templateNames = new Lazy.Resettable<>(state, () ->
      templates.stream().map(t -> t.getName()).collect(Collectors.toList()));
  private Lazy.Resettable<WeaponProficiency> weaponProficiency =
      new Lazy.Resettable<>(state, () -> {
        Optional<Value.Proficiency> type = templates.stream()
            .map(t -> t.getWeaponProficiency())
            .filter(s -> s != Value.Proficiency.UNKNOWN_PROFICIENCY
                && s != Value.Proficiency.UNRECOGNIZED)
            .findFirst();

        if (type.isPresent()) {
          return WeaponProficiency.fromProto(type.get());
        }

        return WeaponProficiency.UNKNOWN;
      });
  private Lazy.Resettable<Distance> weaponRange = new Lazy.Resettable<>(state, () -> {
    Optional<Distance> range = templates.stream()
        .map(ItemTemplate::getRange)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();

    if (range.isPresent()) {
      return range.get();
    }

    return Distance.ZERO;
  });
  private Lazy.Resettable<Distance> weaponReach = new Lazy.Resettable<>(state, () -> {
    Optional<Distance> reach = templates.stream()
        .map(ItemTemplate::getReach)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();

    if (reach.isPresent()) {
      return reach.get();
    }

    return Distance.ZERO;
  });
  private Lazy.Resettable<WeaponStyle> weaponStyle = new Lazy.Resettable<>(state, () -> {
    Optional<Value.WeaponStyle> style = templates.stream()
        .map(t -> t.getWeaponStyle())
        .filter(s -> s != Value.WeaponStyle.UNRECOGNIZED && s != Value.WeaponStyle.UNKNOWN_STYLE)
        .findFirst();

    if (style.isPresent()) {
      return WeaponStyle.fromProto(style.get());
    }

    return WeaponStyle.UNKNOWN;
  });
  private Lazy.Resettable<WeaponType> weaponType = new Lazy.Resettable<>(state, () -> {
    Optional<Template.WeaponTemplateProto.Type> type = templates.stream()
        .map(t -> t.getWeaponType())
        .filter(s -> s != Template.WeaponTemplateProto.Type.UNKNOWN
            && s != Template.WeaponTemplateProto.Type.UNRECOGNIZED)
        .findFirst();

    if (type.isPresent()) {
      return WeaponType.fromProto(type.get());
    }

    return WeaponType.UNKNOWN;
  });
  private Lazy.Resettable<Weight> weight = new Lazy.Resettable<>(state, () -> {
    Weight weight = count > 0 ? getRawWeight().multiply(multiple) : getRawWeight();
    for (Item content : contents) {
      weight = weight.add(content.getWeight());
    }

    return weight;
  });
  private Lazy.Resettable<List<String>> worlds = new Lazy.Resettable<>(state, () -> {
    Optional<ItemTemplate> base = getBaseTemplate();
    if (base.isPresent()) {
      return base.get().getWorlds();
    }

    return Collections.emptyList();
  });

  private Lazy.Resettable<Size> wielderSize = new Lazy.Resettable<>(state, () -> templates.stream()
      .map(ItemTemplate::getWielderSize)
      .filter(s -> s != Size.UNKNOWN)
      .max((a, b) -> a.ordinal() - b.ordinal())
      .orElse(Size.UNKNOWN));

  public Item(String id, String name, List<ItemTemplate> templates, int hp, Money value,
              String appearance, String playerName, String playerNotes, String dmNotes,
              int multiple, int multiuse, int count, Duration timeLeft, boolean identified,
              List<Item> contents, History history) {
    this.id = id;
    this.name = name;
    this.templates = templates;
    this.hp = hp;
    this.value = value;
    this.appearance = appearance;
    this.playerName = playerName.isEmpty() && !templates.isEmpty()
        ? templates.get(0).getName() : playerName;
    this.playerNotes = playerNotes;
    this.dmNotes = dmNotes;
    this.multiple = multiple;
    this.multiuse = multiuse;
    this.count = count;
    this.timeLeft = timeLeft;
    this.identified = identified;
    this.contents = new ArrayList<>(contents);
    this.history = history;

    // In case we don't have any templates, try to get the base template derived from the name.
    if (this.templates.isEmpty()) {
      Optional<ItemTemplate> template = Templates.get().getItemTemplates().get(name);
      if (template.isPresent()) {
        this.templates.add(template.get());
      }
    }
  }

  public interface Owner {
    public String getId();

    public boolean isCharacter();

    public boolean isWearing(Item item);

    public void add(Item item);

    public boolean amDM();

    public boolean canEdit();

    public void combine(Item item, Item other);

    //public void dragEnded(boolean result);

    public Optional<Item> getItem(String id);

    public boolean moveItemAfter(Item item, Item move);

    public boolean moveItemBefore(Item item, Item move);

    public void moveItemInto(Item container, Item item);

    public void updated(Item item);
  }

  public String getAppearance() {
    return appearance;
  }

  public void setAppearance(String appearance) {
    this.appearance = appearance;
  }

  public List<Modifier> getArmorDeflectionModifiers() {
    return armorDeflectionModifiers.get();
  }

  public List<Modifier> getArmorModifiers() {
    return armorModifiers.get();
  }

  public Optional<ItemTemplate> getBaseTemplate() {
    if (templates.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(templates.get(0));
  }

  public int getBreakDC() {
    return breakDC.get();
  }

  public Set<String> getCategories() {
    return categories.get();
  }

  public List<Item> getContents() {
    return Collections.unmodifiableList(this.contents);
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
    state.reset();
  }

  public String getDMNotes() {
    return dmNotes;
  }

  public void setDMNotes(String notes) {
    dmNotes = notes;
  }

  public Damage getDamage() {
    return damage.get();
  }

  private RandomDuration getDonDuration() {
    return donDuration.get();
  }

  private RandomDuration getDonHastilyDuration() {
    return donHastilyDuration.get();
  }

  public int getHardness() {
    return hardness.get();
  }

  public History getHistory() {
    return history;
  }

  public int getHp() {
    return hp;
  }

  public void setHp(int hp) {
    this.hp = hp;
    state.reset();
  }

  public String getId() {
    return id;
  }

  public String getIncomplete() {
    return incomplete.get();
  }

  public List<Modifier> getMagicAttackModifiers() {
    return magicAttackModifiers.get();
  }

  public Multimap<MagicEffectType, Modifier> getMagicModifiers() {
    return magicModifiers.get();
  }

  public int getMaxAmount() {
    return maxMultiple.get();
  }

  public int getMaxAttacks() {
    return maxAttacks.get();
  }

  public int getMaxDexterityModifier() {
    return maxDexterityModifier.get();
  }

  public int getMaxUses() {
    return maxUses.get();
  }

  public int getMultiple() {
    return multiple;
  }

  public void setMultiple(int multiple) {
    this.multiple = multiple;
    state.reset();
  }

  public int getMultiuse() {
    return multiuse;
  }

  public void setMultiuse(int multiuse) {
    this.multiuse = multiuse;
    state.reset();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    state.reset();
  }

  public String getPlayerName() {
    if (playerName.isEmpty()) {
      return name;
    }

    return playerName;
  }

  public void setPlayerName(String name) {
    this.playerName = name;
  }

  public String getPlayerNotes() {
    return playerNotes;
  }

  public void setPlayerNotes(String notes) {
    playerNotes = notes;
  }

  public Probability getProbability() {
    return probability.get();
  }

  public Money getRawValue() {
    return value;
  }

  public Weight getRawWeight() {
    return rawWeight.get();
  }

  public List<String> getReferences() {
    return referenes.get();
  }

  private RandomDuration getRemoveDuration() {
    return removeDuration.get();
  }

  public Damage getSecondaryDamage() {
    return secondaryDamage.get();
  }

  public Size getSize() {
    return size.get();
  }

  public Slot getSlot() {
    return slot.get();
  }

  public Damage getSplashDamage() {
    return splashDamage.get();
  }

  public Substance getSubstance() {
    return substance.get();
  }

  public List<String> getSynonyms() {
    return synonyms.get();
  }

  public List<String> getTemplateNames() {
    return templateNames.get();
  }

  public List<ItemTemplate> getTemplates() {
    return Collections.unmodifiableList(templates);
  }

  public void setTemplates(List<ItemTemplate> templates) {
    this.templates = new ArrayList<>(templates);
    state.reset();
  }

  public Duration getTimeLeft() {
    return timeLeft;
  }

  public void setTimeLeft(Duration duration) {
    this.timeLeft = duration;
  }

  public Money getValue() {
    return totalValue.get();
  }

  public void setValue(Money value) {
    this.value = value;
    state.reset();
  }

  public WeaponProficiency getWeaponProficiency() {
    return weaponProficiency.get();
  }

  public Distance getWeaponRange() {
    return weaponRange.get();
  }

  public Distance getWeaponReach() {
    return weaponReach.get();
  }

  public WeaponStyle getWeaponStyle() {
    return weaponStyle.get();
  }

  public WeaponType getWeaponType() {
    return weaponType.get();
  }

  public Weight getWeight() {
    return weight.get();
  }

  public Size getWielderSize() {
    return wielderSize.get();
  }

  public List<String> getWorlds() {
    return worlds.get();
  }

  public boolean isAmmunition() {
    for (ItemTemplate template : templates) {
      if (template.isAmmunition()) {
        return true;
      }
    }

    return false;
  }

  public boolean isArmor() {
    return templates.stream().anyMatch(ItemTemplate::isArmor);
  }

  public boolean isContainer() {
    for (ItemTemplate template : templates) {
      if (template.isContainer()) {
        return true;
      }
    }

    return false;
  }

  public boolean isIdentified() {
    return identified;
  }

  public boolean isMagic() {
    return templates.stream().anyMatch(ItemTemplate::isMagic);
  }

  public boolean isMonetary() {
    return templates.stream().anyMatch(ItemTemplate::isMonetary);
  }

  public boolean isWeapon() {
    return templates.stream().anyMatch(ItemTemplate::isWeapon);
  }

  public boolean isWearable() {
    return templates.stream().anyMatch(ItemTemplate::isWearable);
  }

  public void add(Item item) {
    contents.add(item);
  }

  public boolean addItemAfter(Item item, Item move) {
    if (contents.contains(item)) {
      contents.add(contents.indexOf(item) + 1, move);
      return true;
    } else {
      for (Item container : contents) {
        if (container.addItemAfter(item, move)) {
          return true;
        }
      }
    }

    return false;
  }

  public boolean addItemBefore(Item item, Item move) {
    if (contents.contains(item)) {
      contents.add(contents.indexOf(item), move);
      return true;
    } else {
      for (Item container : contents) {
        if (container.addItemBefore(item, move)) {
          return true;
        }
      }
    }

    return false;
  }

  public List<Item> collectDeepContents() {
    List<Item> deep = new ArrayList<>(contents);
    for (Item item : contents) {
      deep.addAll(item.collectDeepContents());
    }

    return deep;
  }

  public List<Modifier> computeAbilityModifers(Ability ability) {
    List<Modifier> modifiers = new ArrayList<>();
    for (ItemTemplate template : templates) {
      if (template.isMagic()) {
        for (Modifier modifier : template.getMagicModifiers(ability.getMagicType())) {
          modifiers.add(modifier);
        }
      }
    }

    return modifiers;
  }

  public int computeMaxHp() {
    return templates.stream().mapToInt(t -> t.computeHp()).sum();
  }

  public String formatAmount() {
    Optional<ItemTemplate> template =
        templates.stream().filter(ItemTemplate::isMultiple).findFirst();
    if (template.isPresent()) {
      ItemTemplate.Count count = template.get().getAmount();
      return multiple + "/" + count.format();
    } else {
      return "";
    }
  }

  public String formatCounted() {
    Optional<ItemTemplate> template =
        templates.stream().filter(ItemTemplate::isCounted).findFirst();
    if (template.isPresent()) {
      ItemTemplate.Count count = template.get().getCounted();
      return count + " / " + count.format();
    } else {
      return "";
    }
  }

  public String formatMagic() {
    List<String> parts = new ArrayList<>();
    for (Map.Entry<MagicEffectType, Modifier> modifier : getMagicModifiers().entries()) {
      parts.add(modifier.getKey() + " - " + modifier.getValue());
    }

    return Strings.SEMICOLON_JOINER.join(parts);
  }

  public String formatTime() {
    Optional<ItemTemplate> template = templates.stream().filter(t -> t.isTimed()).findFirst();
    if (template.isPresent()) {
      return getTimeLeft() + " of " + template.get().getTimed();
    }

    return "";
  }

  public String formatUses() {
    Optional<ItemTemplate> template =
        templates.stream().filter(ItemTemplate::isMultiuse).findFirst();
    if (template.isPresent()) {
      return multiuse + " / " + template.get().getUses();
    } else {
      return "";
    }
  }

  public String formatWeapon() {
    if (!isWeapon()) {
      return "";
    }

    List<String> parts = new ArrayList<>();

    // Damage.
    String damage = getDamage().format();
    Damage secondaryDamage = getSecondaryDamage();
    Damage splash = getSplashDamage();

    if (!secondaryDamage.isEmpty()) {
      damage += "/" + secondaryDamage.format();
    }

    if (!splash.isEmpty()) {
      damage += ", splash " + splash.format();
    }

    if (weaponCriticalLow() < 20 || weaponCriticalMultiplier() != 2) {
      damage += " (" + weaponCriticalLow() + "-20 x" + weaponCriticalMultiplier() + ")";
    }

    parts.add(damage);

    // Style & type & proficiency.
    parts.add(getWeaponStyle().getName());
    parts.add(getWeaponType().getName());
    parts.add(getWeaponProficiency().getName());

    // Range.
    Distance range = getWeaponRange();
    if (!range.isZero()) {
      parts.add("range " + range.toString());
    }

    // Reach.
    Distance reach = getWeaponReach();
    if (!reach.isZero() && reach.asFeet() != 5.0) {
      parts.add("reach " + reach.toString());
    }

    // Max attacks.
    int attacks = getMaxAttacks();
    if (attacks > 0 && attacks != Integer.MAX_VALUE) {
      if (attacks == 1) {
        parts.add("maximally 1 attack per round");
      } else {
        parts.add("maximally " + attacks + " attacks per round");
      }
    }

    return Strings.SEMICOLON_JOINER.join(parts);
  }

  public String formatWearable() {
    if (!isWearable()) {
      return "";
    }

    List<String> parts = new ArrayList<>();

    // Slot.
    Slot slot = getSlot();
    if (slot != Slot.UNKNOWN) {
      parts.add(slot.getName());
    }

    // Donning and removing.
    RandomDuration don = getDonDuration();
    RandomDuration donHastily = getDonHastilyDuration();
    RandomDuration remove = getRemoveDuration();
    if (!don.isNone()) {
      parts.add("don " + don);
    }
    if (!donHastily.isNone()) {
      parts.add("don hastily " + donHastily);
    }
    if (!remove.isNone()) {
      parts.add("remove " + remove);
    }

    return Strings.SEMICOLON_JOINER.join(parts);
  }

  public String getDescription(boolean dm) {
    if (templates.isEmpty()) {
      return "";
    }

    if (dm) {
      return Strings.NEWLINE_JOINER.join(templates.stream()
          .map(t -> Strings.ensureSentence(t.getDescription()))
          .filter(s -> !s.isEmpty())
          .collect(Collectors.toList()));
    } else {
      return templates.get(0).getDescription();
    }
  }

  public Optional<Item> getItem(String id) {
    if (getId().equals(id)) {
      return Optional.of(this);
    }

    for (Item item : getContents()) {
      Optional<Item> found = item.getItem(id);
      if (found.isPresent()) {
        return found;
      }
    }

    return Optional.empty();
  }

  public List<Modifier> getMagicModifiers(MagicEffectType type) {
    List<Modifier> modifiers = new ArrayList<>();
    for (ItemTemplate template : templates) {
      modifiers.addAll(template.getMagicModifiers(type.toProto()));
    }

    return modifiers;
  }

  public int getMaxSpeedSquares(boolean isFast) {
    return templates.stream()
        .mapToInt(t -> t.getMaxSpeedSquares(isFast))
        .min()
        .orElse(Integer.MAX_VALUE);
  }

  public Optional<Item> getNestedItem(String itemId) {
    for (Item item : contents) {
      if (item.getId().equals(itemId)) {
        return Optional.of(item);
      }

      Optional<Item> nested = item.getNestedItem(itemId);
      if (nested.isPresent()) {
        return nested;
      }
    }

    return Optional.empty();
  }

  public String getShortDescription(boolean dm) {
    if (templates.isEmpty()) {
      return "";
    }

    if (dm) {
      return Strings.SPACE_JOINER.join(templates.stream()
          .map(t -> Strings.ensureSentence(t.getShortDescription()))
          .filter(t -> !t.isEmpty())
          .collect(Collectors.toList()));
    } else {
      return templates.get(0).getShortDescription();
    }
  }

  public boolean hasContents() {
    return !contents.isEmpty();
  }

  public boolean hasWeaponFiness() {
    for (ItemTemplate template : templates) {
      if (template.hasWeaponFiness()) {
        return true;
      }
    }

    return false;
  }

  public Optional<Distance> range() {
    Optional<Distance> result = Optional.empty();
    for (ItemTemplate template : templates) {
      Optional<Distance> reach = template.getRange();
      result = Distance.larger(result, reach);
    }

    return result;
  }

  public Optional<Distance> reach() {
    Optional<Distance> result = Optional.empty();
    for (ItemTemplate template : templates) {
      Optional<Distance> reach = template.getReach();
      result = Distance.larger(result, reach);
    }

    return result;
  }

  public boolean remove(Item item) {
    if (contents.remove(item)) {
      return true;
    }

    for (Item container : contents) {
      if (container.remove(item)) {
        return true;
      }
    }

    return false;
  }

  public boolean similar(Item other) {
    return name.equals(other.name)
        && hp == other.hp
        && value.equals(other.value)
        && appearance.equals(other.appearance)
        && playerName.equals(other.playerName)
        && playerNotes.equals(other.playerNotes)
        && dmNotes.equals(other.dmNotes)
        && timeLeft.equals(other.timeLeft)
        && identified == other.identified
        && contents.isEmpty() && other.contents.isEmpty()
        && other.multiuse == 0 && multiuse == 0
        && other.multiple == 0 && multiple == 0
        && similar(templates, other.templates);
  }

  public String summary() {
    return getValue().toString() + " / " + getWeight().toString();
  }

  @Override
  public String toString() {
    return name + " (" + value + ")";
  }

  public int weaponCriticalLow() {
    return templates.stream().mapToInt(ItemTemplate::getWeaponCriticalLow).min().orElse(20);
  }

  public int weaponCriticalMultiplier() {
    return templates.stream().mapToInt(ItemTemplate::getWeaponCriticalMultiplier).min().orElse(2);
  }

  @Override
  public Data write() {
    return Data.empty()
        .set(FIELD_ID, id)
        .set(FIELD_NAME, name)
        .set(FIELD_TEMPLATES,
            templates.stream().map(ItemTemplate::getName).collect(Collectors.toList()))
        .set(FIELD_HP, hp)
        .set(FIELD_VALUE, value.write())
        .set(FIELD_APPEARANCE, appearance)
        .set(FIELD_PLAYER_NAME, playerName)
        .set(FIELD_PLAYER_NOTES, playerNotes)
        .set(FIELD_DM_NOTES, dmNotes)
        .set(FIELD_MULTIPLE, multiple)
        .set(FIELD_COUNT, count)
        .set(FIELD_MULTIUSE, multiuse)
        .set(FIELD_TIME_LEFT, timeLeft.write())
        .set(FIELD_IDENTIFIED, identified)
        .setNested(FIELD_CONTENTS, contents)
        .setNested(FIELD_HISTORY, history);
  }

  private boolean similar(List<ItemTemplate> first, List<ItemTemplate> second) {
    List<String> firstNames =
        first.stream().map(ItemTemplate::getName).collect(Collectors.toList());
    List<String> secondNames =
        first.stream().map(ItemTemplate::getName).collect(Collectors.toList());

    for (String name : firstNames) {
      if (!secondNames.remove(name)) {
        return false;
      }
    }

    return secondNames.isEmpty();
  }

  public static String appearance(List<ItemTemplate> templates) {
    return Strings.SPACE_JOINER.join(templates.stream()
        .map(ItemTemplate::computeAppearance)
        .collect(Collectors.toList()));
  }

  public static Item create(CompanionContext context, String creatorId, CampaignDate date,
                            String... names) {
    List<ItemTemplate> templates = expandBaseTemplates(
        Arrays.asList(names).stream().map(Item::template).collect(Collectors.toList()));
    String name = name(templates);
    return new Item(generateId(context), name, templates, hp(templates), value(templates),
        appearance(templates), names[0], "", "", Item.maxAmount(templates), Item.maxUses(templates),
        Item.maxCount(templates), Item.maxTime(templates), false,
        Collections.emptyList(), History.create(creatorId, date));
  }

  public static Item createLookupItem(CompanionContext context, ItemTemplate template,
                                      Template.ItemLookupProto proto, String creatorId,
                                      CampaignDate date) {
    List<ItemTemplate> templates = new ArrayList<>();
    templates.add(template);
    templates.addAll(proto.getTemplatesList().stream()
        .map(Item::template)
        .collect(Collectors.toList()));
    templates = expandBaseTemplates(templates);

    return new Item(generateId(context), name(templates), templates,
        proto.getHp() > 0 ? proto.getHp() : hp(templates),
        proto.hasValue() ? Money.fromProto(proto.getValue()) : value(templates),
        proto.getAppearance().isEmpty() ? appearance(templates) : proto.getAppearance(),
        "", "", proto.getDmNotes(),
        proto.getMultiple() > 0 ? proto.getMultiple() : Item.maxAmount(templates),
        proto.getMultiuse() > 0 ? proto.getMultiuse() : Item.maxUses(templates),
        proto.getCount() > 0 ? proto.getCount() : Item.maxCount(templates),
        proto.hasTimeLeft() ? Duration.fromProto(proto.getTimeLeft()) : Item.maxTime(templates),
        false, Item.createLookupItems(context, proto.getContentList(), creatorId, date),
        History.create(creatorId, date));
  }

  public static List<Item> createLookupItems(CompanionContext context,
                                             List<Template.ItemLookupProto> protos,
                                             String creatorId, CampaignDate date) {
    List<Item> items = new ArrayList<>();
    for (Template.ItemLookupProto proto : protos) {
      Optional<Item> item =
          Templates.get().getItemTemplates().lookup(context, proto, creatorId, date);
      if (item.isPresent()) {
        items.add(item.get());
      }
    }

    return items;
  }

  public static List<ItemTemplate> expandBaseTemplates(List<ItemTemplate> templates) {
    return templates.stream()
        .flatMap(t -> t.collectTemplates().stream())
        .distinct()
        .collect(Collectors.toList());
  }

  public static Optional<? extends Item.Owner> findOwner(String id) {
    Optional<Character> character = CompanionApplication.get().characters().get(id);
    if (character.isPresent()) {
      return character;
    } else {
      Optional<Monster> monster = CompanionApplication.get().monsters().get(id);
      if (monster.isPresent()) {
        return monster;
      } else {
        return CompanionApplication.get().encounters().get(id);
      }
    }
  }

  public static String generateId(CompanionContext context) {
    return context.me().getId() + "-" + UUID.randomUUID();
  }

  public static int hp(List<ItemTemplate> templates) {
    return templates.stream().mapToInt(ItemTemplate::computeHp).sum();
  }

  public static int maxAmount(List<ItemTemplate> templates) {
    Optional<ItemTemplate> template =
        templates.stream().filter(ItemTemplate::isMultiple).findFirst();
    if (template.isPresent()) {
      return template.get().getAmount().getCount();
    }

    return 0;
  }

  public static int maxCount(List<ItemTemplate> templates) {
    Optional<ItemTemplate> template =
        templates.stream().filter(ItemTemplate::isCounted).findFirst();
    if (template.isPresent()) {
      return template.get().getCounted().getCount();
    }

    return 1;
  }

  public static Duration maxTime(List<ItemTemplate> templates) {
    Duration result = Duration.ZERO;

    for (ItemTemplate template : templates) {
      if (template.isTimed()) {
        result = result.add(template.getTimed().roll());
      }
    }

    return result;
  }

  public static int maxUses(List<ItemTemplate> templates) {
    Optional<ItemTemplate> template =
        templates.stream().filter(ItemTemplate::isMultiuse).findFirst();
    if (template.isPresent()) {
      return template.get().getUses();
    }

    return 0;
  }

  public static String name(List<ItemTemplate> templates) {
    return Strings.SPACE_JOINER.join(templates.stream()
        .map(ItemTemplate::getNamePart)
        .collect(Collectors.toList()));
  }

  public static Item read(Data data) {
    String id = data.get(FIELD_ID, "(no id)");
    String name = data.get(FIELD_NAME, "(no name)");
    List<ItemTemplate> templates = new ArrayList<>();
    for (String templateName : data.getList(FIELD_TEMPLATES, Collections.<String>emptyList())) {
      if (!templateName.isEmpty()) {
        Optional<ItemTemplate> template = Templates.get().getItemTemplates().get(templateName);
        if (template.isPresent()) {
          templates.add(template.get());
        } else {
          Status.error("Cannot find item template '" + templateName + "'");
        }
      }
    }
    int hp = data.get(FIELD_HP, 0);
    Money value = Money.read(data.getNested(FIELD_VALUE));
    String appearance = data.get(FIELD_APPEARANCE, "");
    String playerName = data.get(FIELD_PLAYER_NAME, "");
    String playerNotes = data.get(FIELD_PLAYER_NOTES, "");
    String dmNotes = data.get(FIELD_DM_NOTES, "");
    int multiple = data.get(FIELD_MULTIPLE, 0);
    int multiuse = data.get(FIELD_MULTIUSE, 0);
    int count = data.get(FIELD_COUNT, 0);
    Duration timeLeft = Duration.read(data.getNested(FIELD_TIME_LEFT));
    boolean identified = data.get(FIELD_IDENTIFIED, false);
    List<Item> contents = data.getNestedList(FIELD_CONTENTS).stream()
        .map(Item::read)
        .collect(Collectors.toList());
    History history = History.read(data.getNested(FIELD_HISTORY));

    return new Item(id, name, templates, hp, value, appearance, playerName, playerNotes, dmNotes,
        multiple, multiuse, count, timeLeft, identified, contents, history);
  }

  private static ItemTemplate template(String name) {
    Optional<ItemTemplate> template = Templates.get().getItemTemplates().get(name);
    if (template.isPresent()) {
      return template.get();
    }

    return ItemTemplate.createEmpty(name);
  }

  public static Money value(List<ItemTemplate> templates) {
    Money total = Money.ZERO;
    for (ItemTemplate template : templates) {
      total = total.add(template.getValue());
    }

    return total.resolveMagic();
  }

  public static Weight weight(List<ItemTemplate> templates) {
    Weight total = Weight.ZERO;
    for (ItemTemplate template : templates) {
      total = total.add(template.getWeight());
    }

    return total;
  }
}
