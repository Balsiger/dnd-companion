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

package net.ixitxachitls.companion.rules;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XPTest {
  @Test
  public void xpAward() throws Exception {
    assertEquals(0, XP.xpAward(-1, -1, -1));
    assertEquals(0, XP.xpAward(0, 0, 0));
    assertEquals(0, XP.xpAward(5, 5, 0));
    assertEquals(300, XP.xpAward(1, 1, 1));
    assertEquals(60, XP.xpAward(1, 1, 5));
    assertEquals(400, XP.xpAward(5, 4, 4));
    assertEquals(2_400, XP.xpAward(10, 8, 2));
    assertEquals(0, XP.xpAward(20, 10, 4));
    assertEquals(1_500, XP.xpAward(20, 20, 4));
    assertEquals(0, XP.xpAward(30, 10, 4));
    assertEquals(48_000, XP.xpAward(30, 20, 4));
    assertEquals(0, XP.xpAward(30, 30, 4));
  }

  @Test
  public void xpForLevel() throws Exception {
    assertEquals(0, XP.xpForLevel(-10));
    assertEquals(0, XP.xpForLevel(-1));
    assertEquals(0, XP.xpForLevel(0));
    assertEquals(0, XP.xpForLevel(1));
    assertEquals(1_000, XP.xpForLevel(2));
    assertEquals(3_000, XP.xpForLevel(3));
    assertEquals(6_000, XP.xpForLevel(4));
    assertEquals(10_000, XP.xpForLevel(5));
    assertEquals(15_000, XP.xpForLevel(6));
    assertEquals(21_000, XP.xpForLevel(7));
    assertEquals(28_000, XP.xpForLevel(8));
    assertEquals(36_000, XP.xpForLevel(9));
    assertEquals(45_000, XP.xpForLevel(10));
    assertEquals(55_000, XP.xpForLevel(11));
    assertEquals(66_000, XP.xpForLevel(12));
    assertEquals(78_000, XP.xpForLevel(13));
    assertEquals(91_000, XP.xpForLevel(14));
    assertEquals(105_000, XP.xpForLevel(15));
    assertEquals(120_000, XP.xpForLevel(16));
    assertEquals(136_000, XP.xpForLevel(17));
    assertEquals(153_000, XP.xpForLevel(18));
    assertEquals(171_000, XP.xpForLevel(19));
    assertEquals(190_000, XP.xpForLevel(20));
    assertEquals(190_000, XP.xpForLevel(21));
    assertEquals(190_000, XP.xpForLevel(22));
    assertEquals(190_000, XP.xpForLevel(23));
    assertEquals(190_000, XP.xpForLevel(24));
    assertEquals(190_000, XP.xpForLevel(25));
  }

}