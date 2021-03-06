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

package net.ixitxachitls.companion.ui;

import com.google.common.collect.ImmutableList;

import java.util.Random;

/**
 * Simple class to serve text hints for display.
 */
public class Hints {
  private static final Random RANDOM = new Random();
  private static final ImmutableList<String> hints =
      new ImmutableList.Builder<String>()
          .add("You can move characters from one campaign to another.")
          .add("Red lines below a value signal an invalid value. Most of such values can still be "
              + "used, but might not be according to the rules.")
          .add("Feedback is welcome at companion@ixitxachitls.net. Thanks!")
          .add("The Roleplay Companion is still in Beta. Thus, data might be lost at any time.")
          .add("The Roleplay Companion is still in Beta. Thus, many features are still missing.")
          .add("Features requests are welcome at companion@ixitxachitls.net. Thanks!")
          .add("The Roleplay Companion is currently aimed at D&D 3.5. Most of the things should "
              + "work for other editions too, though.")
          .add("You cannot directly enter your maximal hit points. Instead add the individual "
              + "hit points per level.")
          .add("If data changed by other players is not updated in your display, try leaving and "
              + "restarting the app (and yes, I am working on it).")
          .add("For many UI elements you can get additional information and help by long-pressing "
              + "them.")
          .add("You can click on the heart for your hit points to change them, even in the midst "
              + "of battle.")
          .add("A long press on a condition will tell you what it is.")
          .add("You can touch messages to receive them (which will remove them).")
          .add("A long press on a pending message will tell you what it is, without actually "
              + "reading it (they will not be removed).")
          .add("You can double tap an image to reload it. Images will automatically reload from "
              + "time to time, but that might be too slow sometimes.")
          .add("If you have decent artistic skills and want to help make the app more beautiful, "
              + "every help is welcome. Inquire at companion@ixitxachitls.net")
          .add("If you have good coding skills and would like to contribute some work to make the "
              + "app more useful, let me know at companion@ixitxachitls.net.")
          .add("If you find computations that are wrong or values that are missing, "
              + "let me know at companion@ixitxachitls.net")
          .add("If you find that some things are missing, like races, classes, qualities options "
              + "or items, let me know at companion@ixitxachitls.net. "
              + "Maybe I find some time to add them, maybe I ask for your help.")
          .add("If you are good at design and want to help out making this better, "
              + "contact me at companion@ixitxachitls.net")
          .add("If you want to contribute additional items, monsters or adventures, let me "
              + "know at companion@ixitxachitls.net.")
      .build();

  public static String nextHint() {
    return hints.get(RANDOM.nextInt(hints.size()));
  }
}
