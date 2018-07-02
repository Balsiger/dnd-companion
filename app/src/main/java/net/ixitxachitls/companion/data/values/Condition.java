/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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
 * along with the Roleplay Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.values;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.rules.Conditions;

import java.util.Objects;

/**
 * A condition a character can have.
 */
public class Condition {

  private final String name;
  private final String description;
  private final String summary;
  private final Duration duration;
  private final boolean predefined;
  private final boolean endsBeforeTurn;
  @DrawableRes private final int icon;

  public Condition(String name, String description, String summary, Duration duration,
                   boolean predefined, boolean endsBeforeTurn, @DrawableRes int icon,
                   @ColorRes int iconColor) {
    this.name = name;
    this.description = description;
    this.summary = summary;
    this.duration = duration;
    this.predefined = predefined;
    this.endsBeforeTurn = endsBeforeTurn;
    this.icon = icon;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getSummary() {
    return summary;
  }

  public Duration getDuration() {
    return duration;
  }

  public boolean isPredefined() {
    return predefined;
  }

  public boolean endsBeforeTurn() {
    return endsBeforeTurn;
  }

  public boolean showsIcon() {
    return icon != 0;
  }

  @DrawableRes public int getIcon() {
    return icon;
  }

  public static Condition fromProto(Value.ConditionProto proto) {
    return Condition.newBuilder(proto.getName())
        .description(proto.getDescription())
        .summary(proto.getSummary())
        .duration(Duration.fromProto(proto.getDuration()))
        .predefined(false)
        .endsBeforeTurn(proto.getEndsBeforeTurn())
        .build();
  }

  public Value.ConditionProto toProto() {
    return Value.ConditionProto.newBuilder()
        .setName(name)
        .setDescription(description)
        .setSummary(summary)
        .setDuration(duration.toProto())
        .setEndsBeforeTurn(endsBeforeTurn)
        .build();
  }

  @Override
  public String toString() {
    return name;
  }

  public static Builder newBuilder(String name) {
    return new Builder(name);
  }

  public static class Builder {
    private String name;
    private String description;
    private String summary;
    private Duration duration = Duration.NULL;
    private boolean predefined;
    private boolean endsBeforeTurn;
    @DrawableRes private int icon = 0;
    @ColorRes private int iconColor = 0;

    public Builder(String name) {
      this.name = name;

      Condition existing = Conditions.get(name);
      if (existing != null) {
        this.description = existing.description;
        this.summary = existing.summary;
        this.duration = existing.duration;
        this.predefined = existing.predefined;
        this.endsBeforeTurn = existing.endsBeforeTurn;
        this.icon = existing.icon;
      }
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder summary(String summary) {
      this.summary = summary;
      return this;
    }

    public Builder duration(Duration duration) {
      this.duration = duration;
      return this;
    }

    public Builder predefined() {
      this.predefined = true;
      return this;
    }

    public Builder predefined(boolean predefined) {
      this.predefined = predefined;
      return this;
    }

    public Builder endsBeforeTurn() {
      this.endsBeforeTurn = true;
      return this;
    }

    public Builder endsBeforeTurn(boolean value) {
      this.endsBeforeTurn = value;
      return this;
    }

    public Builder icon(@DrawableRes int icon) {
      this.icon = icon;
      return this;
    }

    public Condition build() {
      return new Condition(name, description, summary, duration, predefined, endsBeforeTurn,
          icon, iconColor);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Condition other = (Condition) o;
    return predefined == other.predefined
        && endsBeforeTurn == other.endsBeforeTurn
        && icon == other.icon
        && Objects.equals(name, other.name)
        && Objects.equals(description, other.description)
        && Objects.equals(summary, other.summary)
        && Objects.equals(duration, other.duration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, summary, duration, predefined, endsBeforeTurn, icon);
  }
}
