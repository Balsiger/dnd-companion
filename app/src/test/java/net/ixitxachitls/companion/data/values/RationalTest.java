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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by balsiger on 7/31/18.
 */
public class RationalTest {

  @Test
  public void parse() {
    assertFalse(Rational.parse("").isPresent());
    assertFalse(Rational.parse("a").isPresent());
    assertFalse(Rational.parse("1 a/2").isPresent());
    assertFalse(Rational.parse("1 3 / a").isPresent());

    assertEquals("1", Rational.parse("1").get().toString());
    assertEquals("-1", Rational.parse(" -  1").get().toString());
    assertEquals("1 2/3", Rational.parse("1 2  / 3").get().toString());
    assertEquals("-1 2/3", Rational.parse(" -  1 2  / 3").get().toString());
    assertEquals("- 4/5", Rational.parse("- 4/  5 ").get().toString());
  }

  @Test
  public void simplify() {
    assertEquals("42", new Rational(42, 0, 0, false).simplify().toString());
    assertEquals("-42", new Rational(42, 0, 0, true).simplify().toString());

    assertEquals("4", new Rational(2, 4, 2, false).simplify().toString());
    assertEquals("-4", new Rational(2, 4, 2, true).simplify().toString());

    assertEquals("3 1/3", new Rational(1, 14, 6, false).simplify().toString());
    assertEquals("-3 1/3", new Rational(1, 14, 6, true).simplify().toString());

    assertEquals("1/2", new Rational(0, 4, 8, false).simplify().toString());
    assertEquals("-1/2", new Rational(0, 4, 8, true).simplify().toString());
  }

  @Test
  public void add() {
    assertEquals("42", new Rational(20, 0, 0, false).add(new Rational(22, 0, 0, false)).toString());
    assertEquals("-2", new Rational(20, 0, 0, false).add(new Rational(22, 0, 0, true)).toString());
    assertEquals("2", new Rational(20, 0, 0, true).add(new Rational(22, 0, 0, false)).toString());
    assertEquals("-42", new Rational(20, 0, 0, true).add(new Rational(22, 0, 0, true)).toString());

    assertEquals("1 1/6", new Rational(0, 1, 2, false).add(new Rational(0, 2, 3, false)).toString());
    assertEquals("-1/6", new Rational(0, 1, 2, false).add(new Rational(0, 2, 3, true)).toString());
    assertEquals("1/6", new Rational(0, 1, 2, true).add(new Rational(0, 2, 3, false)).toString());
    assertEquals("-1 1/6", new Rational(0, 1, 2, true).add(new Rational(0, 2, 3, true)).toString());

    assertEquals("4 1/6", new Rational(2, 1, 2, false).add(new Rational(1, 2, 3, false)).toString());
    assertEquals("5/6", new Rational(2, 1, 2, false).add(new Rational(1, 2, 3, true)).toString());
    assertEquals("-5/6", new Rational(2, 1, 2, true).add(new Rational(1, 2, 3, false)).toString());
    assertEquals("-4 1/6", new Rational(2, 1, 2, true).add(new Rational(1, 2, 3, true)).toString());
  }
}