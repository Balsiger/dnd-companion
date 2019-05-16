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

import net.ixitxachitls.companion.data.enums.Size;

/**
 * Rule definitions around armor.
 */
public class Armor {
  public static int sizeModifier(Size size) {
    switch (size) {
      default:
      case UNRECOGNIZED:
      case UNKNOWN:
        return 0;

      case FINE:
        return +8;

      case DIMINUTIVE:
        return +4;

      case TINY:
        return +2;

      case SMALL:
        return +1;

      case MEDIUM:
        return 0;

      case LARGE:
        return -1;

      case HUGE:
        return -2;

      case GARGANTUAN:
        return -4;

      case COLOSSAL:
        return -8;
    }
  }
}
