/*
 * Copyright (c) 2017-2020 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
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
import net.ixitxachitls.companion.util.Die;

/**
 * A value represenation of a dice.
 */
public class Dice {

  private int number;
  private int dice;
  private int modifier;

  public Dice(int number, int dice, int modifier) {
    this.number = number;
    this.dice = dice;
    this.modifier = modifier;
  }

  public int getDice() {
    return dice;
  }

  public int getModifier() {
    return modifier;
  }

  public int getNumber() {
    return number;
  }

  public boolean isEmpty() {
    return (number == 0 || dice == 0) && modifier == 0;
  }

  public boolean isOne() {
    return number == 1 && dice == 1 && modifier == 0;
  }

  public int roll() {
    return Die.roll(number, dice, modifier);
  }

  public Value.DiceProto toProto() {
    return Value.DiceProto.newBuilder()
        .setNumber(number)
        .setDice(dice)
        .setModifier(modifier)
        .build();
  }

  public static Dice fromProto(Value.DiceProto proto) {
    return new Dice(proto.getNumber(), proto.getDice(), proto.getModifier());
  }
}
