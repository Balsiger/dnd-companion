/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * along with the Roleplay Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.util;

import android.util.Log;

import java.util.Random;

/**
 * Small utility with dice related methods.
 */
public class Dice {

  private static final Random RANDOM = new Random();

  private Dice() {}

  public static int roll(int number, int dice, int modifier){
    int result = modifier;
    for (int i = 0; i < number; i++) {
      result = dX(dice);
    }

    if (result <= 0) {
      // Don't allow negative numbers.
      return 1;
    }

    return result;
  }

  public static int roll(int dice, int modifier) {
    int number = dX(dice) + modifier;
    if (number <= 0) {
      // Don't allow negative numbers.
      return 1;
    }

    return number;
  }

  public static int d100() {
    return dX(100);
  }

  public static int d20() {
    return dX(20);
  }

  public static int d12() {
    return dX(12);
  }

  public static int d10() {
    return dX(10);
  }

  public static int d8() {
    return dX(8);
  }

  public static int d6() {
    return dX(6);
  }

  public static int d4() {
    return dX(4);
  }

  private static int dX(int dice) {
    int number = RANDOM.nextInt(dice) + 1;
    Log.d("Dice", "d" + dice + " = " + number);
    return number;
  }
}
