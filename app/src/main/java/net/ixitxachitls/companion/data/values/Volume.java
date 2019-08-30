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

package net.ixitxachitls.companion.data.values;

import net.ixitxachitls.companion.proto.Value;

/**
 * A representation of a volume value.
 */
public class Volume {

  public static final Volume ZERO = imperial(Rational.ZERO, Rational.ZERO);
  // Metric.
  private final Rational cubicMeters;
  private final Rational cubicDecimeters;
  private final Rational cubicCentimeters;

  // Imperial.
  private final Rational cubicFeet;
  private final Rational cubicInches;

  // Gallons.
  private final Rational gallons;
  private final Rational quarts;
  private final Rational pints;
  private final Rational cups;

  // Liters.
  private final Rational liters;
  private final Rational deciliters;
  private final Rational centiLiters;

  private Volume(Rational cubicMeters, Rational cubicDecimeters,
                 Rational cubicCentimeters,
                 Rational cubicFeet, Rational cubicInches,
                 Rational gallons, Rational quarts, Rational pints, Rational cups,
                 Rational liters, Rational deciliters, Rational centiLiters) {
    this.cubicMeters = cubicMeters;
    this.cubicDecimeters = cubicDecimeters;
    this.cubicCentimeters = cubicCentimeters;
    this.cubicFeet = cubicFeet;
    this.cubicInches = cubicInches;
    this.gallons = gallons;
    this.quarts = quarts;
    this.pints = pints;
    this.cups = cups;
    this.liters = liters;
    this.deciliters = deciliters;
    this.centiLiters = centiLiters;
  }

  public boolean isNull() {
    return isMetric() || isImperial() || isGallons() || isLiters();
  }

  public boolean isMetric() {
    return !cubicMeters.isNull() || !cubicDecimeters.isNull() || !cubicCentimeters.isNull();
  }

  public boolean isImperial() {
    return !cubicFeet.isNull() || !cubicInches.isNull();
  }

  public boolean isGallons() {
    return !gallons.isNull() || !quarts.isNull() || !pints.isNull() || !cups.isNull();
  }

  public boolean isLiters() {
    return !liters.isNull() || !deciliters.isNull() || !deciliters.isNull();
  }

  public static Volume metric(Rational cubicMeters, Rational cubicDecimeters,
                              Rational cubicCentimeters) {
    return new Volume(cubicMeters, cubicDecimeters, cubicCentimeters,
        Rational.ZERO, Rational.ZERO,
        Rational.ZERO, Rational.ZERO, Rational.ZERO, Rational.ZERO,
        Rational.ZERO, Rational.ZERO, Rational.ZERO);
  }

  public static Volume imperial(Rational cubicFeet, Rational cubicInches) {
    return new Volume(Rational.ZERO, Rational.ZERO, Rational.ZERO,
        cubicFeet, cubicInches,
        Rational.ZERO, Rational.ZERO, Rational.ZERO, Rational.ZERO,
        Rational.ZERO, Rational.ZERO, Rational.ZERO);
  }

  public static Volume gallons(Rational gallons, Rational quarts, Rational pints, Rational cups) {
    return new Volume(Rational.ZERO, Rational.ZERO, Rational.ZERO,
        Rational.ZERO, Rational.ZERO,
        gallons, quarts, pints, cups,
        Rational.ZERO, Rational.ZERO, Rational.ZERO);
  }

  public static Volume liters(Rational liters, Rational deciLiters, Rational centiLiters) {
    return new Volume(Rational.ZERO, Rational.ZERO, Rational.ZERO,
        Rational.ZERO, Rational.ZERO,
        Rational.ZERO, Rational.ZERO, Rational.ZERO, Rational.ZERO,
        liters, deciLiters, centiLiters);
  }

  public static Volume fromProto(Value.VolumeProto proto) {
    if (proto.hasMetric()) {
      return metric(Rational.fromProto(proto.getMetric().getCubicMeters()),
          Rational.fromProto(proto.getMetric().getCubicDecimeters()),
          Rational.fromProto(proto.getMetric().getCubicCentimeters()));
    } else if (proto.hasImperial()) {
      return imperial(Rational.fromProto(proto.getImperial().getCubicFeet()),
          Rational.fromProto(proto.getImperial().getCubicInches()));
    } else if (proto.hasGallons()) {
      return gallons(Rational.fromProto(proto.getGallons().getGallons()),
          Rational.fromProto(proto.getGallons().getQuarts()),
          Rational.fromProto(proto.getGallons().getPints()),
          Rational.fromProto(proto.getGallons().getCups()));
    } else {
      return liters(Rational.fromProto(proto.getLiters().getLiters()),
          Rational.fromProto(proto.getLiters().getLiters()),
          Rational.fromProto(proto.getLiters().getLiters()));
    }
  }

  public Value.VolumeProto toProto() {
    Value.VolumeProto.Builder proto = Value.VolumeProto.newBuilder();
    if (isMetric()) {
      proto.setMetric(Value.VolumeProto.Metric.newBuilder()
          .setCubicMeters(cubicMeters.toProto())
          .setCubicDecimeters(cubicDecimeters.toProto())
          .setCubicCentimeters(cubicCentimeters.toProto())
          .build());
    }
    if (isImperial()) {
      proto.setImperial(Value.VolumeProto.Imperial.newBuilder()
          .setCubicFeet(cubicFeet.toProto())
          .setCubicInches(cubicInches.toProto())
          .build());
    }
    if (isGallons()) {
      proto.setGallons(Value.VolumeProto.Gallons.newBuilder()
          .setGallons(gallons.toProto())
          .setQuarts(quarts.toProto())
          .setPints(pints.toProto())
          .setCups(cups.toProto())
          .build());
    }
    if (isLiters()) {
      proto.setLiters(Value.VolumeProto.Liters.newBuilder()
          .setLiters(liters.toProto())
          .setDeciliters(deciliters.toProto())
          .setCentiliters(centiLiters.toProto())
          .build());
    }

    return proto.build();
  }
}
