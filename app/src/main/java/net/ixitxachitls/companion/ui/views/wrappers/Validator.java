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

package net.ixitxachitls.companion.ui.views.wrappers;

import java.util.Collections;
import java.util.List;

/**
 * A validator for displayed values
 */
@FunctionalInterface
public interface Validator {
  public List<String> validate(String value);

  public static class RangeValidator implements Validator {

    private final int min;
    private final int max;

    public RangeValidator(int min, int max) {
      this.min = min;
      this.max = max;
    }

    @Override
    public List<String> validate(String input) {
      try {
        int value = Integer.parseInt(input);
        if (value < min) {
          return Collections.singletonList("Value too small");
        }
        if (value > max) {
          return Collections.singletonList("Value to high");
        }

        return Collections.emptyList();
      } catch (NumberFormatException e) {
        return Collections.emptyList();
      }
    }
  }
}
