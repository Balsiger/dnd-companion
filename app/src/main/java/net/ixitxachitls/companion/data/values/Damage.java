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

package net.ixitxachitls.companion.data.values;

import net.ixitxachitls.companion.data.enums.DamageType;
import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A damage value that can be modified.
 */
public class Damage {

  private final List<DamageValue> simplified = new ArrayList<>();
  private final List<DamageValue> values = new ArrayList<>();

  public Damage() {
  }

  public void add(int number, int dice, int modifier, DamageType type, Optional<String> effect,
                    String source) {
    add(new DamageValue(number, dice, modifier, type, effect, source));
  }

  public void add(Damage damage) {
    add(damage.values);
  }

  public void add(Modifier modifier) {
    add(0, 0, modifier.getValue(), DamageType.NONE, Optional.empty(), modifier.getSource());
  }

  public void addModifiers(List<Modifier> modifiers) {
    for (Modifier modifier : modifiers) {
      add(modifier);
    }
  }

  public List<String> details() {
    return values.stream()
        .map(v -> v.toString() + " (" + v.source + ")")
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return Strings.SPACE_JOINER.join(simplified);
  }

  private void add(List<DamageValue> values) {
    for (DamageValue value : values) {
      add(value);
    }
  }

  private void add(DamageValue value) {
    values.add(value);
    addSimplified(value);
  }

  private void addSimplified(DamageValue value) {
    for (DamageValue existing : simplified) {
      if (existing.compatible(value)) {
        simplified.remove(existing);
        simplified.add(existing.add(value));
        return;
      }
    }

    simplified.add(value);
  }

  public static Damage from(List<Damage> damages) {
    Damage result = new Damage();
    for (Damage damage: damages) {
      result.add(damage);
    }

    return result;
  }

  public static Damage from(Value.DamageProto proto, String source) {
    Damage result = new Damage();
    for (Value.DamageProto.Damage damage : proto.getDamageList()) {
      result.add(damage.getBase().getNumber(), damage.getBase().getDice(),
          damage.getBase().getModifier(), DamageType.from(damage.getType()),
          Strings.optionalIfEmpty(damage.getEffect()), source);
    }

    return result;
  }

  private class DamageValue {
    final int number;
    final int dice;
    final int modifier;
    final DamageType type;
    final Optional<String> effect;
    final String source;

    private DamageValue(int number, int dice, int modifier, DamageType type,
                        Optional<String> effect, String source) {
      this.number = number;
      this.dice = dice;
      this.modifier = modifier;
      this.type = type;
      this.effect = effect;
      this.source = source;
    }

    public DamageValue add(DamageValue other) {
      return new DamageValue(number + other.number, dice, modifier + other.modifier, type, effect,
          "");
    }

    public boolean compatible(DamageValue other) {
      if (this.type != other.type || !this.effect.equals(other.effect)) {
        // Types or effect are different.
        return false;
      }

      if (this.dice == other.dice) {
        // Dice are the same.
        return true;
      }


      if (other.dice == 1 || other.dice == 0 || other.number == 0) {
        // Modifiers can be added to any dice.
        return true;
      }

      return false;
    }

    @Override
    public String toString() {
      String result = "";
      if (number > 0 && dice > 0 && (number != 1 || dice != 1)) {
        result = number + "d" + dice;
      }

      if (modifier != 0) {
        result += " " + Strings.signed(modifier);
      }

      if (type != DamageType.NONE) {
        result += " " + type.getName();
      }

      if (effect.isPresent()) {
        result += " (" + effect.get() + ")";
      }

      return result;
    }
  }
}
