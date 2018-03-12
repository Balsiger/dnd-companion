/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

package net.ixitxachitls.companion.data.values;

import net.ixitxachitls.companion.proto.Value;

/**
 * A condition a character can have.
 */
public class Condition {

  private final String name;
  private final String description;
  private final String summary;
  private final Duration duration;
  private final boolean predefined;

  public Condition(String name, String description, String summary) {
    this(name, description, summary, new Duration(), true);
  }

  public Condition(String name, String description, String summary, Duration duration) {
    this(name, description, summary, duration, true);
  }

  public Condition(String name, String description, String summary, Duration duration,
                   boolean predefined) {
    this.name = name;
    this.description = description;
    this.summary = summary;
    this.duration = duration;
    this.predefined = predefined;
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

  public static Condition fromProto(Value.ConditionProto proto) {
    return new Condition(proto.getName(), proto.getDescription(), proto.getSummary(),
        Duration.fromProto(proto.getDuration()));
  }

  public Value.ConditionProto toProto() {
    return Value.ConditionProto.newBuilder()
        .setName(name)
        .setDescription(description)
        .setSummary(summary)
        .setDuration(duration.toProto())
        .build();
  }

  @Override
  public String toString() {
    return name;
  }
}
