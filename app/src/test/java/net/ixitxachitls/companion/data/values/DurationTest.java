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

/**
 * Created by balsiger on 6/11/18.
 */
public class DurationTest {

  @Test
  public void parse() {
    assertEquals("", Duration.parse("").toString());
    assertEquals("3 rounds", Duration.parse("   3    ").toString());
    assertEquals("1 round", Duration.parse("1 r").toString());
    assertEquals("42 rounds", Duration.parse("42 round").toString());
    assertEquals("", Duration.parse("0 r").toString());
    assertEquals("5 rounds", Duration.parse("5 rounds").toString());
    assertEquals("1 year 2 days 3 hours 5 minutes",
        Duration.parse("   5 m 3   h   2    d   1 y").toString());
    assertEquals("3 hours 12 minutes",
        Duration.parse("4 minutes 2 h 3 m 1 hour 5 min").toString());
  }
}