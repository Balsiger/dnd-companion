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

import android.support.annotation.Nullable;

import net.ixitxachitls.companion.proto.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A condition affecting a character for a specific amount of time.
 */
public class TimedCondition {

  private static final String FIELD_CONDITION = "condition";
  private static final String FIELD_SOURCE = "source";
  private static final String FIELD_END_ROUND = "end_round";
  private static final String FIELD_END_DATE = "end_date";

  private final ConditionData condition;
  private String sourceId;
  private final int endRound;
  private final CampaignDate endDate;

  public TimedCondition(ConditionData condition, String sourceId) {
    this(condition, sourceId, Integer.MAX_VALUE);
  }

  public TimedCondition(ConditionData condition, String sourceId, int endRound) {
    this(condition, sourceId, endRound, new CampaignDate());
  }

  public TimedCondition(ConditionData condition, String sourceId, CampaignDate endDate) {
    this(condition, sourceId, 0, endDate);
  }

  private TimedCondition(ConditionData condition, String sourceId, int endRound, CampaignDate endDate) {
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

  public ConditionData getCondition() {
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

  public boolean isPermanent() {
    return getDuration().isPermanent();
  }

  public boolean endedAfter(CampaignDate date) {
    return hasEndDate() && endDate.after(date);
  }

  public boolean endedBefore(CampaignDate date) {
    return hasEndDate() && endDate.before(date);
  }

  public boolean hasEndDate() {
    return endRound == 0 && !endDate.isEmpty();
  }

  public boolean active(Encounter encounter) {
    return hasEndDate()
        || getEndRound() == Integer.MAX_VALUE
        || getEndRound() > encounter.getTurn()
        || (getEndRound() == encounter.getTurn()
            && (getCondition().endsBeforeTurn()
                ? !encounter.acting(sourceId) : !encounter.acted(sourceId)));
  }

  public static TimedCondition fromProto(Value.TimedConditionProto proto) {
    return new TimedCondition(ConditionData.fromProto(proto.getCondition()),
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

  public static TimedCondition read(@Nullable Map<String, Object> data) {
    if (data == null) {
      throw new IllegalArgumentException("Data cannot be null");
    }

    ConditionData condition = ConditionData.read(Values.get(data, FIELD_CONDITION));
    String source = Values.get(data, FIELD_SOURCE, "");
    if (Values.has(data, FIELD_END_DATE)) {
      CampaignDate endDate = CampaignDate.read(Values.get(data, FIELD_END_DATE));
      return new TimedCondition(condition, source, endDate);
    } else {
      int endRound = (int) Values.get(data, FIELD_END_ROUND, 0);
      return new TimedCondition(condition, source, endRound);
    }

  }

  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_CONDITION, condition.write());
    data.put(FIELD_SOURCE, sourceId);
    if (endRound > 0) {
      data.put(FIELD_END_ROUND, endRound);
    } else {
      data.put(FIELD_END_DATE, endDate.write());
    }

    return data;
  }

  @Override
  public String toString() {
    return condition
        + (endRound != Integer.MAX_VALUE ? " until " + (endRound > 0 ? endRound : endDate) : "");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TimedCondition other = (TimedCondition) o;
    return endRound == other.endRound &&
        Objects.equals(condition, other.condition) &&
        Objects.equals(sourceId, other.sourceId) &&
        Objects.equals(endDate, other.endDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(condition, sourceId, endRound, endDate);
  }
}
