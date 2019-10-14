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

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import java.util.Map;

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

  @Test
  public void processExpressions() {
    Map<String, Texts.Value> values = ImmutableMap.<String, Texts.Value>builder()
        .put("life", new Texts.IntegerValue(42))
        .build();

    assertEquals("no expression", "just a text",
        Texts.processExpressions("just a text", values));

    assertEquals("simple number", "just -42 text",
        Texts.processExpressions("just [[  -42  ]] text", values));
    assertEquals("simple number", "just 42 text",
        Texts.processExpressions("just [[  +42  ]] text", values));
    assertEquals("simple number", "just 42 text",
        Texts.processExpressions("just [[  42  ]] text", values));

    assertEquals("simple variable", "just 42 text",
        Texts.processExpressions("just [[  $life ]] text", values));
    assertEquals("simple variable", "just <$life2> text",
        Texts.processExpressions("just [[$life2]] text", values));

    assertEquals("simple bracket", "just 42 text",
        Texts.processExpressions("just [[ (  $life ) ]] text", values));
    assertEquals("nested bracket", "just 42 text",
        Texts.processExpressions("just [[ ( ((  $life )  ) ) ]] text", values));

    assertEquals("simple operation", "42", Texts.processExpressions("[[ 6 * 7 ]]", values));
    assertEquals("simple operation", "252", Texts.processExpressions("[[ 6 * $life ]]", values));
    assertEquals("simple operation", "84", Texts.processExpressions("[[ 6 * 7 * 2]]", values));
    assertEquals("simple operation", "21", Texts.processExpressions("[[ 6 * 7 / 2]]", values));
    assertEquals("simple operation", "18", Texts.processExpressions("[[ 6 * (7 / 2)]]", values));

    assertEquals("simple operation", "11", Texts.processExpressions("[[ 6 + 7 - 2]]", values));
    assertEquals("simple operation", "11", Texts.processExpressions("[[ 6 + (7 - 2)]]", values));
    assertEquals("simple operation", "11", Texts.processExpressions("[[ (6 + 7) - 2]]", values));

    assertEquals("simple operation", "20", Texts.processExpressions("[[ 6 + 7 * 2]]", values));
    assertEquals("simple operation", "26", Texts.processExpressions("[[ (6 + 7) * 2]]", values));
  }
}