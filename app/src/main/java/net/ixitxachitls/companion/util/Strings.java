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

package net.ixitxachitls.companion.util;

import com.google.common.base.Joiner;

/**
 * Utilities for handling strings.
 */
public class Strings {
  public static final Joiner COMMA_JOINER = Joiner.on(", ");
  private static final String SPACES =
      "                                                                      "
          + "                                                                     "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      "
          + "                                                                      ";
  private static final String ZEROES =
      "000000000000000000000000000000000000000000000000000000000000000000000000"
          + "0000000000000000000000000000000000000000000000000000000000000000000000"
          + "0000000000000000000000000000000000000000000000000000000000000000000000"
          + "0000000000000000000000000000000000000000000000000000000000000000000000";

  private Strings() {}

  public static String pad(String inText, int inLength, boolean inLeft)
  {
    if(inText.length() >= inLength)
      return inText;

    if(inLeft)
      return SPACES.substring(0, inLength - inText.length()) + inText;
    else
      return inText + SPACES.substring(0, inLength - inText.length());
  }

  public static String pad(long inNumber, int inLength, boolean inLeft)
  {
    String text = Long.toString(inNumber);

    if(text.length() >= inLength)
      return text;

    if(inLeft)
      return ZEROES.substring(0, inLength - text.length()) + text;
    else
      return text + SPACES.substring(0, inLength - text.length());
  }
}
