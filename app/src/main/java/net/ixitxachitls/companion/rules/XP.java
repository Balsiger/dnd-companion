/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Player Companion.
 *
 * The Player Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Player Companion is distributed in the hope that it will be useful,
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
 * A utility class dealing with rules about XP.
 */
public class XP {

  public static final int [][] xpByCLandEL = {
      // Character level 1.
      {    300,    600,    900,  1_350,  1_800,  2_700,  3_600,  5_400,  7_200,  10_800,
             0,      0,      0,      0,      0,      0,      0,      0,      0,       0 },
      // Character level 2.
      {    300,    600,    900,  1_350,  1_800,  2_700,  3_600,  5_400,  7_200,  10_800,
             0,      0,      0,      0,      0,      0,      0,      0,      0,       0 },
      // Character level 3.
      {    300,    600,    900,  1_350,  1_800,  2_700,  3_600,  5_400,  7_200,  10_800,
             0,      0,      0,      0,      0,      0,      0,      0,      0,       0 },
      // Character level 4.
      {    300,    600,    800,  1_200,  1_600,  2_400,  3_200,  4_800,  6_400,   9_600,
        12_800,      0,      0,      0,      0,      0,      0,      0,      0,       0 },
      // Character level 5.
      {    300,    500,    750,  1_000,  1_500,  2_250,  3_000,  4_500,  6_000,   9_000,
        12_000, 18_000,      0,      0,      0,      0,      0,      0,      0,       0 },
      // Character level 6.
      {    300,    450,    600,    900,  1_200,  1_800,  2_700,  3_600,  5_400,   7_200,
        10_800, 14_400, 21_600,      0,      0,      0,      0,      0,      0,       0 },
      // Character level 7.
      {    263,    350,    525,    700,  1_050,  1_400,  2_100,  3_150,  4_200,   6_300,
         8_400, 12_600, 16_800, 25_200,      0,      0,      0,      0,      0,       0 },
      // Character level 8.
      {    200,    300,    400,    600,    800,  1_200,  1_600,  2_400,  3_600,   4_800,
         7_200,  9_600, 14_400, 19_200, 28_800,      0,      0,      0,      0,       0 },
      // Character level 9.
      {      0,    225,    338,    450,    675,    900,  1_350,  1_800,  2_700,   4_050,
         5_400,  8_100, 10_800, 16_200, 21_600, 32_400,      0,      0,      0,       0 },
      // Character level 10.
      {      0,      0,    250,    375,    500,    750,  1_000,  1_500,  2_000,  3_000,
         4_500,  6_000,  9_000, 12_000, 18_000, 24_000, 36_000,      0,      0,      0 },
      // Character level 11.
      {      0,      0,      0,    275,    413,    550,    825,  1_100,  1_650,  2_200,
         3_300,  4_950,  6_600,  9_900, 13_200, 19_800, 26_400, 39_600,      0,      0 },
      // Character level 12.
      {      0,      0,      0,      0,    300,    450,    600,    900,  1_200,  1_800,
         2_400,  3_600,  5_400,  7_200, 10_800, 14_400, 21_600, 28_800, 43_200,      0 },
      // Character level 13.
      {      0,      0,      0,      0,      0,    325,    488,    650,    975,  1_300,
         1_950,  2_600,  3_900,  5_850,  7_800, 11_700, 15_600, 23_400, 31_200, 46_800 },
      // Character level 14.
      {      0,      0,      0,      0,      0,      0,    350,    525,    700,  1_050,
         1_400,  2_100,  2_800,  4_200,  6_300,  8_400, 12_600, 16_800, 25_200, 33_600 },
      // Character level 15.
      {      0,      0,      0,      0,      0,      0,      0,    375,    563,    750,
          1_125, 1_500,  2_250,  3_000,  4_500,  6_750,  9_000,  3_500, 18_000, 27_000 },
      // Character level 16.
      {      0,      0,      0,      0,      0,      0,      0,      0,    400,    600,
          800,   1_200,  1_600,  2_400,  3_200,  4_800,  7_200,  9_600, 14_400, 19_200 },
      // Character level 17.
      {      0,      0,      0,      0,      0,      0,      0,      0,      0,    425,
          638,     850,  1_275,  1_700,  2_550,  3_400,  5_100,  7_650, 10_200, 15_300 },
      // Character level 18.
      {      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,
          450,     675,    900,  1_350,  1_800,  2_700,  3_600,  5_400,  8_100, 10_800 },
      // Character level 19.
      {      0,      0,      0,      0,      0,      0,      0,      0,      0,      0,
             0,     475,   713,    950,  1_425,  1_900,  2_850,  3_800,  5_700,  8_550 },
      // Character level 20.
      {      0,       0,     0,      0,      0,      0,      0,      0,      0,      0,
             0,       0,   500,    750,  1_000,  1_500,  2_000,  3_000,  4_000,  6_000 },
  };

  public static int []xpByLevel = {
           0,  1_000,  3_000,  6_000,  10_000,  15_000,  21_000,  28_000,  36_000,  45_000,
      55_000, 66_000, 78_000,  91_000, 105_000, 120_000, 136_000, 153_000, 171_000, 190_000
  };


  public static int xpAward(int encounterLevel, int characterLevel, int partySize) {
    if (partySize <= 0 || encounterLevel <= 0 || characterLevel <= 0) {
      return 0;
    }

    if (encounterLevel > 20) {
      return 2 * xpAward(encounterLevel - 2, characterLevel, partySize);
    }

    return xpByCLandEL[characterLevel - 1][encounterLevel - 1] / partySize;
  }

  public static int xpForLevel(int level) {
    if (level <= 0) {
      return 0;
    }

    if (level > 20) {
      return xpForLevel(20);
    }

    return xpByLevel[level];
  }
}
