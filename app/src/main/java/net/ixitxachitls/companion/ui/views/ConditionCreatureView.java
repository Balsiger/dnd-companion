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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.StoredEntries;
import net.ixitxachitls.companion.data.values.TargetedTimedCondition;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;

import java.util.List;

/**
 * View for the conditions of a single ceature.
 */
public class ConditionCreatureView extends LinearLayout {

  private final LinearLayout container;
  private final int battleTurn;

  private boolean hasConditions = false;

  public ConditionCreatureView(Context context, String name, int battleTurn) {
    super(context);

    this.battleTurn = battleTurn;

    View view =
        LayoutInflater.from(getContext()).inflate(R.layout.view_condition_creature, this, false);

    TextWrapper.wrap(view, R.id.creature_name).text(name);
    container = view.findViewById(R.id.creature_conditions);

    addView(view);
  }

  public void addConditions(BaseCreature<?> creature, boolean isDM) {
    for (TargetedTimedCondition condition : creature.getInitiatedConditions()) {
      addCondition(creature.getName(), creature.getCreatureId(), condition.getTargetIds(),
          condition.getTimedCondition(), false, true);
    }

    for (TimedCondition condition : creature.getAffectedConditions()) {
      addCondition(StoredEntries.nameFor(condition.getSourceId()), condition.getSourceId(),
          ImmutableList.<String>of(creature.getCreatureId()), condition, true, isDM);
    }
  }

  private void addCondition(String sourceName, String sourceId, List<String> targetIds,
                            TimedCondition condition, boolean affected, boolean canDelete) {
    int remainingRounds = condition.getEndRound() - battleTurn;
    if (remainingRounds > 0) {
      hasConditions = true;
      container.addView(new ConditionLineView(getContext(), condition, sourceName, sourceId,
          targetIds, remainingRounds, affected, canDelete));
    }
  }

  public boolean hasConditions() {
    return hasConditions;
  }
}
