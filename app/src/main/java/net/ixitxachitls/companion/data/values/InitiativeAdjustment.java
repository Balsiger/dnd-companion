/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

import android.text.Spanned;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An adjustment to initiative.
 */
public class InitiativeAdjustment extends Adjustment {

  protected static final Pattern PARSE_PATTERN =
      Pattern.compile("(?i:\\s*(?:init|initiative)\\s+((?:\\+|-)\\d+))");
  private static final String FIELD_ADJUSTMENT = "adjustment";
  private final int adjustment;

  public InitiativeAdjustment(int adjustment) {
    super(Type.initiative);

    this.adjustment = adjustment;
  }

  public int getAdjustment() {
    return adjustment;
  }

  @Override
  public Spanned toSpanned() {
    return toSupportedSpanned();
  }

  @Override
  public Map<String, Object> write() {
    Map<String, Object> data = super.write();
    data.put(FIELD_ADJUSTMENT, adjustment);

    return data;
  }

  @Override
  public String toString() {
    return "Initiative " + (adjustment >= 0 ? "+" : "") + adjustment;
  }

  public static Optional<InitiativeAdjustment> parseAbility(String text, String source) {
    Matcher matcher = PARSE_PATTERN.matcher(text);
    if (matcher.matches()) {
      return Optional.of(new InitiativeAdjustment(Integer.valueOf(matcher.group(1))));
    }

    return Optional.empty();
  }

  public static InitiativeAdjustment readInitiative(Map<String, Object> data) {
    int adjustment = (int) Values.get(data, FIELD_ADJUSTMENT, 0);

    return new InitiativeAdjustment(adjustment);
  }
}
