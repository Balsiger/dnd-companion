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

package net.ixitxachitls.companion.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for texts.
 */
public class TextsTest {

  @Test
  public void markBrackets() {
    assertEquals("simple", "\001<0>abc\002<0>",
        Texts.markBrackets("{abc}", '\\', '{', '}', '\001', '\002'));
    assertEquals("multiple", "\001<0>abc\002<0>  \001<0>\002<0>",
        Texts.markBrackets("{abc}  {}", '\\', '{', '}', '\001', '\002'));
    assertEquals("nested",
        "\001<0>a{b{}}{{}}c\002<0>\001<0>\002<0>",
        Texts.markBrackets("{a{b{}}{{}}c}{}", '\\', '{', '}', '\001',
            '\002'));
    assertEquals("escaped", "\001<0>a\\{b\\}c\002<0>",
        Texts.markBrackets("{a\\{b\\}c}", '\\', '{', '}', '\001',
            '\002'));
    assertEquals("incomplete", "{a\001<0>b\002<0>{",
        Texts.markBrackets("{a{b}{", '\\', '{', '}', '\001', '\002'));
  }
}