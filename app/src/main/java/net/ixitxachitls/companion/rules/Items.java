/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

package net.ixitxachitls.companion.rules;

import net.ixitxachitls.companion.data.enums.Size;

/**
 * Rules for items
 */
public class Items {

  public enum Slot {
    head, eyes, neck, shoulders, torso, body, wrists, hands, fingers, feet,
  }

  public enum Encumbrance { light, medium, heavy, overloaded}

  /** The light carrying capacities per pound per strength score. */
  private static final int []LIGHT_LOAD =
      {
          0,   3,   6,  10,  13,  16,  20,  23,  26,  30,
          33,  38,  43,  50,  58,  66,  76,  86, 100, 116,
          133, 153, 173, 200, 233, 266, 306, 346, 400, 466,
      };

  /** The medium carrying capacities per pound per strength score. */
  private static final int []MEDIUM_LOAD =
      {
          0,   6,  13,  20,  26,  33,  40,  46,  53,  60,
          66,  76,  86, 100, 116, 133, 153, 173, 200, 233,
          266, 306, 346, 400, 466, 533, 613, 693, 800, 933,
      };

  /** The heavy carrying capacities per strength score. */
  private static final int []HEAVY_LOAD =
      {
          0,  10,  20,  30,  40,  50,  60,   70,   80,   90,
          100, 115, 130, 150, 175, 200, 230,  260,  300,  350,
          400, 460, 520, 600, 700, 800, 900, 1040, 1200, 1400,
      };

  private static double bipedalSizeFactor(Size size)
  {
    switch(size)
    {
      default:
      case UNKNOWN:
      case MEDIUM:
        return 1.0;

      case FINE:
        return 1.0/8;

      case DIMINUTIVE:
        return 1.0/4;

      case TINY:
        return 1.0/2;

      case SMALL:
        return 3.0/4;

      case LARGE:
        return 2.0;

      case HUGE:
        return 4.0;

      case GARGANTUAN:
        return 8.0;

      case COLOSSAL:
        return 16.0;
    }
  }

  public static int dragLoad(int strength, Size size, boolean isBipedal)
  {
    return heavyLoad(strength, size, isBipedal) * 5;
  }

  public static Encumbrance encumbrance(int loadPounds, int strength) {
    if (loadPounds < lightLoad(strength)) {
      return Encumbrance.light;
    }

    if (loadPounds < mediumLoad(strength)) {
      return Encumbrance.medium;
    }

    if (loadPounds < heavyLoad(strength)) {
      return Encumbrance.heavy;
    }

    return Encumbrance.overloaded;
  }

  public static int heavyLoad(int strength, Size size, boolean isBipdeal)
  {
    return (int) (heavyLoad(strength) * sizeFactor(size, isBipdeal));
  }

  private static int heavyLoad(int strength)
  {
    if(strength < HEAVY_LOAD.length)
      return HEAVY_LOAD[strength];

    return heavyLoad(strength - 10) * 4;
  }

  public static int liftLoad(int strength, Size size, boolean isBipedal)
  {
    return heavyLoad(strength, size, isBipedal);
  }

  public static int lightLoad(int strength, Size size, boolean isBipedal)
  {
    return (int) (lightLoad(strength) * sizeFactor(size, isBipedal));
  }

  private static int lightLoad(int strength)
  {
    if(strength < LIGHT_LOAD.length)
      return LIGHT_LOAD[strength];

    return lightLoad(strength - 10) * 4;
  }

  public static int mediumLoad(int strength, Size size, boolean isBipedal)
  {
    return (int) (mediumLoad(strength) * sizeFactor(size, isBipedal));
  }

  private static int mediumLoad(int strength)
  {
    if(strength < MEDIUM_LOAD.length)
      return MEDIUM_LOAD[strength];

    return mediumLoad(strength - 10) * 4;
  }

  private static double quadrupedalSizeFactor(Size size)
  {
    switch(size)
    {
      default:
      case UNKNOWN:
      case MEDIUM:
        return 3.0/2;

      case FINE:
        return 1.0/4;

      case DIMINUTIVE:
        return 1.0/2;

      case TINY:
        return 3.0/4;

      case SMALL:
        return 1.0;

      case LARGE:
        return 3.0;

      case HUGE:
        return 6.0;

      case GARGANTUAN:
        return 12.0;

      case COLOSSAL:
        return 24.0;
    }
  }

  private static double sizeFactor(Size size, boolean isBipedal)
  {
    return isBipedal ? bipedalSizeFactor(size) : quadrupedalSizeFactor(size);
  }
}
