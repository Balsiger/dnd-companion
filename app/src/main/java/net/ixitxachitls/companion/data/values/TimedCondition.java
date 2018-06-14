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

import net.ixitxachitls.companion.proto.Value;

/**
 * A condition affecting a character for a specific amount of time.
 */
public class TimedCondition {

  private final Condition condition;
  private String sourceId;
  private final int endRound;
  private final CampaignDate endDate;

  public TimedCondition(Condition condition, String sourceId) {
    this(condition, sourceId, Integer.MAX_VALUE);
  }

  public TimedCondition(Condition condition, String sourceId, int endRound) {
    this(condition, sourceId, endRound, new CampaignDate());
  }

  public TimedCondition(Condition condition, String sourceId, CampaignDate endDate) {
    this(condition, sourceId, 0, endDate);
  }

  private TimedCondition(Condition condition, String sourceId, int endRound, CampaignDate endDate) {
    this.condition = condition;
    this.sourceId = sourceId;
    this.endRound = endRound;
    this.endDate = endDate;
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

  public Condition getCondition() {
    return condition;
  }

  public String getSourceId() {
    return sourceId;
  }

  public int getEndRound() {
    return endRound;
  }

  public CampaignDate getEndDate() {
    return endDate;
  }

  public boolean hasEndDate() {
    return endRound == 0 && !endDate.isEmpty();
  }

  public boolean active(Battle battle) {
    return hasEndDate()
        || getEndRound() == Integer.MAX_VALUE
        || getEndRound() > battle.getTurn()
        || (getEndRound() == battle.getTurn()
            && (getCondition().endsBeforeTurn()
                ? !battle.acting(sourceId) : !battle.acted(sourceId)));
  }

  public static TimedCondition fromProto(Value.TimedConditionProto proto) {
    return new TimedCondition(Condition.fromProto(proto.getCondition()),
        proto.getSourceId(), proto.getEndRound(), CampaignDate.fromProto(proto.getEndDate()));
  }

  public Value.TimedConditionProto toProto() {
    return Value.TimedConditionProto.newBuilder()
        .setCondition(condition.toProto())
        .setSourceId(sourceId)
        .setEndRound(endRound)
        .setEndDate(endDate.toProto())
        .build();
  }

  @Override
  public String toString() {
    return condition
        + (endRound != Integer.MAX_VALUE ? " until " + (endRound > 0 ? endRound : endDate) : "");
  }
}
