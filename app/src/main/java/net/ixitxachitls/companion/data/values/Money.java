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

package net.ixitxachitls.companion.data.values;

import net.ixitxachitls.companion.data.documents.Data;
import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A monetary value.
 */
public class Money {

  private static final String FIELD_PLATINUM = "platinum";
  private static final String FIELD_GOLD = "gold";
  private static final String FIELD_SILVER = "silver";
  private static final String FIELD_COPPER = "copper";
  private static final String FIELD_ARMOR = "armor";
  private static final String FIELD_WEAPON = "weapon";
  private static final ValueParser PARSER = new IntegerValueParser(
      new ValueParser.Unit("pp", "pp", "platinum", "platinums"),
      new ValueParser.Unit("gp", "gp", "gold", "golds"),
      new ValueParser.Unit("sp", "sp", "silver", "silvers"),
      new ValueParser.Unit("cp", "cp", "copper", "coppers"),
      new ValueParser.Unit("armor", "armors", "magic armor", "magic armors"),
      new ValueParser.Unit("weapon", "weapons", "magic weapon", "magic weapons"));
  public static Money ZERO = new Money(0, 0, 0, 0, 0, 0);
  private final int platinum;
  private final int gold;
  private final int silver;
  private final int copper;
  private final int armor;
  private final int weapon;

  private Money(int platinum, int gold, int silver, int copper, int armor, int weapon) {
    this.platinum = platinum;
    this.gold = gold;
    this.silver = silver;
    this.copper = copper;
    this.armor = armor;
    this.weapon = weapon;
  }

  public int getCopper() {
    return copper;
  }

  public int getGold() {
    return gold;
  }

  public int getPlatinum() {
    return platinum;
  }

  public int getSilver() {
    return silver;
  }

  public boolean isZero() {
    return platinum == 0 && gold == 0 && silver == 0 && copper == 0 && armor == 0 && weapon == 0;
  }

  public Money add(Money other) {
    return new Money(platinum + other.platinum, gold + other.gold, silver + other.silver,
        copper + other.copper, armor + other.armor, weapon + other.weapon);
  }

  public double asGold() {
    return platinum * 10 + gold + silver / 10.0 + copper / 100.0
        + armor * armor * 1000 + weapon * weapon * 2000;
  }

  public Money half() {
    int platinum = this.platinum;
    int gold = this.gold;
    int silver = this.silver;
    int copper = this.copper;

    if (platinum % 2 != 0) {
      gold += 5;
    }
    platinum /= 2;

    if (gold % 2 != 0) {
      silver += 5;
    }
    gold /= 2;

    if (silver %2 != 0) {
      copper += 5;
    }
    silver /= 2;

    copper /= 2;

    return new Money(platinum, gold, silver, copper, 0, 0);
  }

  @Override
  public int hashCode() {
    return Objects.hash(platinum, gold, silver, copper, armor, weapon);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Money money = (Money) o;
    return platinum == money.platinum &&
        gold == money.gold &&
        silver == money.silver &&
        copper == money.copper &&
        armor == money.armor &&
        weapon == money.weapon;
  }

  @Override
  public String toString() {
    List<String> parts = new ArrayList<>();

    if (platinum > 0) {
      parts.add(platinum + " pp");
    }
    if (gold > 0) {
      parts.add(gold + " gp");
    }
    if (silver > 0) {
      parts.add(silver + " sp");
    }
    if (copper > 0) {
      parts.add(copper + " cp");
    }
    if (armor > 0) {
      parts.add(armor + " armor");
    }
    if (weapon > 0) {
      parts.add(weapon + " weapon");
    }

    if (parts.isEmpty()) {
      return "0 gp";
    }

    return Strings.SPACE_JOINER.join(parts);
  }

  public Money multiply(int factor) {
    if (factor == 1) {
      return this;
    }
    if (factor == 0) {
      return ZERO;
    }

    if (armor != 0 || weapon != 0) {
      throw new IllegalStateException("Cannot multiply value with armor or weapon bonuses!");
    }

    return new Money(platinum * factor, gold * factor, silver * factor, copper * factor, 0, 0)
        .simplify();
  }

  public Money resolveMagic() {
    return new Money(platinum, gold + armor * armor * 1000 + weapon * weapon * 2000, silver, copper,
        0, 0);
  }

  public Money simplify() {
    int newCopper = copper;
    int newSilver = silver;
    int newGold = gold;

    if (newCopper > 10) {
      newSilver = newCopper / 10;
      newCopper = newCopper % 10;
    }
    if (newSilver > 10) {
      newGold = newSilver / 10;
      newSilver = newSilver % 10;
    }
    // We don't simplify platinums, as they are not widely used.

    if (copper == newCopper && silver == newSilver && gold == newGold) {
      return this;
    }

    return new Money(platinum, newGold, newSilver, newCopper, armor, weapon);
  }

  public Value.MoneyProto toProto() {
    return Value.MoneyProto.newBuilder()
        .setPlatinum(platinum)
        .setGold(gold)
        .setSilver(silver)
        .setCopper(copper)
        .setMagicArmor(armor)
        .setMagicWeapon(weapon)
        .build();
  }

  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_PLATINUM, platinum);
    data.put(FIELD_GOLD, gold);
    data.put(FIELD_SILVER, silver);
    data.put(FIELD_COPPER, copper);
    data.put(FIELD_ARMOR, armor);
    data.put(FIELD_WEAPON, weapon);

    return data;
  }

  public static Money armor(int amount) {
    return new Money(0, 0, 0, 0, amount, 0);
  }

  public static Money copper(int amount) {
    return new Money(0, 0, 0, amount, 0, 0);
  }

  public static Money fromProto(Value.MoneyProto proto) {
    return new Money(proto.getPlatinum(), proto.getGold(), proto.getSilver(), proto.getCopper(),
        proto.getMagicArmor(), proto.getMagicWeapon());
  }

  public static Money gold(int amount) {
    return new Money(0, amount, 0, 0, 0, 0);
  }

  public static Optional<Money> parse(String text) {
    try {
      List<Integer> values = PARSER.parse(text);
      return Optional.of(new Money(
          values.get(0), values.get(1), values.get(2), values.get(3), values.get(4), values.get(5)));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public static Money platinum(int amount) {
    return new Money(amount, 0, 0, 0, 0, 0);
  }

  public static Money read(Data data) {
    int platinum = data.get(FIELD_PLATINUM, 0);
    int gold = data.get(FIELD_GOLD, 0);
    int silver = data.get(FIELD_SILVER, 0);
    int copper = data.get(FIELD_COPPER, 0);
    int armor = data.get(FIELD_ARMOR, 0);
    int weapon = data.get(FIELD_WEAPON, 0);

    return new Money(platinum, gold, silver, copper, armor, weapon);
  }

  public static Money silver(int amount) {
    return new Money(0, 0, amount, 0, 0, 0);
  }

  public static List<String> validate(String text) {
    if (parse(text).isPresent()) {
      return Collections.emptyList();
    } else {
      return Collections.singletonList("Cannot parse text");
    }
  }

  public static Money weapon(int amount) {
    return new Money(0, 0, 0, 0, 0, amount);
  }
}
