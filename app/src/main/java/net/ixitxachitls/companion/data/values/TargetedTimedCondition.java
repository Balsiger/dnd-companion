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

import net.ixitxachitls.companion.data.documents.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A timed condition targeted at a specific character (from a specific source).
 */
public class TargetedTimedCondition {

  private static final String FIELD_TARGET_IDS = "targets";
  private static final String FIELD_CONDITION = "condition";

  private final TimedCondition condition;
  private final List<String> targetIds;

  public TargetedTimedCondition(TimedCondition condition, List<String> targetId) {
    this.condition = condition;
    this.targetIds = new ArrayList<>(targetId);
  }

  public ConditionData getCondition() {
    return condition.getCondition();
  }

  public String getDescription() {
    return condition.getDescription();
  }

  public Duration getDuration() {
    return condition.getDuration();
  }

  public CampaignDate getEndDate() {
    return condition.getEndDate();
  }

  public int getEndRound() {
    return condition.getEndRound();
  }

  public String getName() {
    return condition.getName();
  }

  public String getSummary() {
    return condition.getSummary();
  }

  public List<String> getTargetIds() {
    return Collections.unmodifiableList(targetIds);
  }

  public TimedCondition getTimedCondition() {
    return condition;
  }

  public boolean isPredefined() {
    return condition.isPredefined();
  }

  public Map<String, Object> write() {
    Map<String, Object> data = new HashMap<>();
    data.put(FIELD_TARGET_IDS, targetIds);
    data.put(FIELD_CONDITION, condition.write());

    return data;
  }

  public static TargetedTimedCondition read(@Nullable Data data) {
    if (data == null) {
      throw new IllegalArgumentException("Data cannot be null");
    }

    List<String> targetIds = data.getList(FIELD_TARGET_IDS, Collections.emptyList());
    TimedCondition condition = TimedCondition.read(data.getNested(FIELD_CONDITION));

    return new TargetedTimedCondition(condition, targetIds);
  }
}
