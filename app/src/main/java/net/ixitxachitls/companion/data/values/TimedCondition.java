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

import net.ixitxachitls.companion.data.documents.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.Nullable;

/**
 * A condition affecting a character for a specific amount of time.
 */
public class TimedCondition {

  private static final String FIELD_CONDITION = "condition";
  private static final String FIELD_SOURCE = "source";
  private static final String FIELD_END_ROUND = "end_round";
  private static final String FIELD_END_DATE = "end_date";

  private final ConditionData condition;
  private final int endRound;
  private final CampaignDate endDate;
  private String sourceId;

  public TimedCondition(ConditionData condition, String sourceId) {
    this(condition, sourceId, Integer.MAX_VALUE);
  }

  public TimedCondition(ConditionData condition, String sourceId, int endRound) {
    this(condition, sourceId, endRound, new CampaignDate());
  }

  public TimedCondition(ConditionData condition, String sourceId, CampaignDate endDate) {
    this(condition, sourceId, 0, endDate);
  }

  private TimedCondition(ConditionData condition, String sourceId, int endRound,
                         CampaignDate endDate) {
    this.condition = condition;
    this.sourceId = sourceId;
    this.endRound = endRound;
    this.endDate = endDate;
  }

  public ConditionData getCondition() {
    return condition;
  }

  public String getDescription() {
    return condition.getDescription();
  }

  public Duration getDuration() {
    return condition.getDuration();
  }

  public CampaignDate getEndDate() {
    return endDate;
  }

  public int getEndRound() {
    return endRound;
  }

  public String getName() {
    return condition.getName();
  }

  public String getSourceId() {
    return sourceId;
  }

  public String getSummary() {
    return condition.getSummary();
  }

  public boolean isPermanent() {
    return getDuration().isPermanent();
  }

  public boolean isPredefined() {
    return condition.isPredefined();
  }

  public boolean active(Battle battle) {
    return hasEndDate()
        || getEndRound() == Integer.MAX_VALUE
        || getEndRound() > battle.getTurn()
        || (getEndRound() == battle.getTurn()
            && (getCondition().endsBeforeTurn()
                ? !battle.acting(sourceId) : !battle.acted(sourceId)));
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

  @Override
  public int hashCode() {
    return Objects.hash(condition, sourceId, endRound, endDate);
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
  public String toString() {
    return condition
        + (endRound != Integer.MAX_VALUE ? " until " + (endRound > 0 ? endRound : endDate) : "");
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

  public static TimedCondition read(@Nullable Data data) {
    if (data == null) {
      throw new IllegalArgumentException("Data cannot be null");
    }

    ConditionData condition = ConditionData.read(data.getNested(FIELD_CONDITION));
    String source = data.get(FIELD_SOURCE, "");
    if (data.has(FIELD_END_DATE)) {
      CampaignDate endDate = CampaignDate.read(data.getNested(FIELD_END_DATE));
      return new TimedCondition(condition, source, endDate);
    } else {
      int endRound = data.get(FIELD_END_ROUND, 0);
      return new TimedCondition(condition, source, endRound);
    }

  }
}
