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

import net.ixitxachitls.companion.proto.Value;

/**
 * A speed value of a monster or character.
 */
public class Speed {

  public enum Mode { unknown, burrow, climb, fly, swim, run };
  public enum Maneuverability { unknown, perfect, good, average, poor, clumsy, none };

  private final int squares;
  private final Mode mode;
  private final Maneuverability maneuverability;

  protected Speed(int squares, Mode mode, Maneuverability maneuverability) {
    this.squares = squares;
    this.mode = mode;
    this.maneuverability = maneuverability;
  }

  private static Maneuverability convert(Value.SpeedProto.Maneuverability maneuverability) {
    switch (maneuverability) {
      case PERFECT:
        return Maneuverability.perfect;

      case GOOD:
        return Maneuverability.good;

      case AVERAGE:
        return Maneuverability.average;

      case POOR:
        return Maneuverability.poor;

      case CLUMSY:
        return Maneuverability.clumsy;

      case NONE:
        return Maneuverability.none;

      case UNRECOGNIZED:
      case UNKNOWN_MANEUVERABILITY:
      default:
        return Maneuverability.unknown;
    }
  }

  private static Mode convert(Value.SpeedProto.Mode mode) {
    switch (mode) {
      default:
      case UNRECOGNIZED:
      case UNKNONW_MODE:
        return Mode.unknown;

      case BURROW:
        return Mode.burrow;

      case CLIMB:
        return Mode.climb;

      case FLY:
        return Mode.fly;

      case SWIM:
        return Mode.swim;

      case RUN:
        return Mode.run;
    }
  }

  public static Speed fromProto(Value.SpeedProto proto) {
    return new Speed(proto.getSquares(), convert(proto.getMode()),
        convert(proto.getManeuverability()));
  }
}
