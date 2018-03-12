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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A timed condition targeted at a specific character (from a specific source).
 */
public class TargetedTimedCondition {
  private final TimedCondition condition;

  private final List<String> targetIds;

  public TargetedTimedCondition(TimedCondition condition, List<String> targetId) {
    this.condition = condition;
    this.targetIds = new ArrayList<>(targetId);
  }

  public Condition getCondition() {
    return condition.getCondition();
  }

  public String getName() {
    return condition.getName();
  }

  public String getDescription() {
    return condition.getDescription();
  }

  public String getSummary() {
    return condition.getSummary();
  }

  public Duration getDuration() {
    return condition.getDuration();
  }

  public boolean isPredefined() {
    return condition.isPredefined();
  }

  public int getEndRound() {
    return condition.getEndRound();
  }

  public CampaignDate getEndDate() {
    return condition.getEndDate();
  }

  public List<String> getTargetIds() {
    return Collections.unmodifiableList(targetIds);
  }

  public TimedCondition getTimedCondition() {
    return condition;
  }

  public static TargetedTimedCondition fromProto(Value.TargetedTimedConditionProto proto) {
    return new TargetedTimedCondition(TimedCondition.fromProto(proto.getCondition()),
        proto.getTargetIdList());
  }

  public Value.TargetedTimedConditionProto toProto() {
    return Value.TargetedTimedConditionProto.newBuilder()
        .setCondition(condition.toProto())
        .addAllTargetId(targetIds)
        .build();
  }
}
