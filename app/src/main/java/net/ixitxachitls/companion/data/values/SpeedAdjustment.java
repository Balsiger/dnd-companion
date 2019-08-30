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

package net.ixitxachitls.companion.data.values;

import android.text.Spanned;

import net.ixitxachitls.companion.data.documents.Data;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An adjustment to a creatures speed.
 */
public class SpeedAdjustment extends Adjustment {
  private static final Pattern PARSE_PATTERN = Pattern.compile("(?i:\\s*(half)\\s+speed\\s*)");
  private static final String FIELD_HALF = "half";

  private boolean half;

  public SpeedAdjustment(boolean half) {
    super(Type.speed);

    this.half = half;
  }

  public boolean isHalf() {
    return half;
  }

  @Override
  public Spanned toSpanned() {
    return toSupportedSpanned();
  }

  @Override
  public Map<String, Object> write() {
    Map<String, Object> data = super.write();
    data.put(FIELD_HALF, half);

    return data;
  }

  @Override
  public String toString() {
    if (half) {
      return "half speed";
    }

    return "(unknown speed adjustment)";
  }

  public static SpeedAdjustment half() {
    return new SpeedAdjustment(true);
  }

  public static Optional<SpeedAdjustment> parseSpeed(String text, String source) {
    Matcher matcher = PARSE_PATTERN.matcher(text);
    if (matcher.matches()) {
      return Optional.of(new SpeedAdjustment(true));
    }

    return Optional.empty();
  }

  public static SpeedAdjustment readSpeed(Data data) {
    return new SpeedAdjustment(data.get(FIELD_HALF, false));
  }
}
