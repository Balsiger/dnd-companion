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

import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A monetary value.
 */
public class Money {

  public static Money ZERO = new Money(0, 0, 0, 0, 0, 0);
  private static final ValueParser PARSER = new IntegerValueParser(
      new ValueParser.Unit("pp", "pp", "platinum", "platinums"),
      new ValueParser.Unit("gp", "gp", "gold", "golds"),
      new ValueParser.Unit("sp", "sp", "silver", "silvers"),
      new ValueParser.Unit("cp", "cp", "copper", "coppers"),
      new ValueParser.Unit("armor", "armors", "magic armor", "magic armors"),
      new ValueParser.Unit("weapon", "weapons", "magic weapon", "magic weapons"));

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

  public boolean isZero() {
    return platinum == 0 && gold == 0 && silver == 0 && copper == 0 && armor == 0 && weapon == 0;
  }

  public Money add(Money other) {
    return new Money(platinum + other.platinum, gold + other.gold, silver + other.silver,
        copper + other.copper, armor + other.armor, weapon + other.weapon);
  }

  public static boolean validate(String text) {
    return parse(text).isPresent();
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

  public static Money gold(int amount) {
    return new Money(0, amount, 0, 0, 0, 0);
  }

  public static Money silver(int amount) {
    return new Money(0, 0, amount, 0, 0, 0);
  }

  public static Money copper(int amount) {
    return new Money(0, 0, 0, amount, 0, 0);
  }

  public static Money armor(int amount) {
    return new Money(0, 0, 0, 0, amount, 0);
  }

  public static Money weapon(int amount) {
    return new Money(0, 0, 0, 0, 0, amount);
  }

  public double asGold() {
    return platinum * 10 + gold + silver / 10.0 + copper / 100.0
        + armor * armor * 1000 + weapon * weapon * 2000;
  }

  public Money resolveMagic() {
    return new Money(platinum, gold + armor * armor * 1000 + weapon * weapon * 2000, silver, copper,
        0, 0);
  }

  public static Money fromProto(Value.MoneyProto proto) {
    return new Money(proto.getPlatinum(), proto.getGold(), proto.getSilver(), proto.getCopper(),
        proto.getMagicArmor(), proto.getMagicWeapon());
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
}
