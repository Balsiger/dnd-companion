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

package net.ixitxachitls.companion.rules;

/**
 * Rule information about levels.
 */
public class Levels {
  public static boolean allowsAbilityIncrease(int level) {
    // An ability increase happens every 4 levels.
    return (level % 4) == 0;
  }

  public static boolean allowsFeat(int level) {
    // You get a feat at first and every third level.
    return level == 1 || level % 3 == 0;
  }

  public static int saveModifier(int level, boolean good) {
    if (good) {
      return (level / 2) + 2;
    } else {
      return level / 3;
    }
  }
}
