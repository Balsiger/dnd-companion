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

import com.google.common.base.Optional;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Creatures;
import net.ixitxachitls.companion.data.dynamics.StoredEntries;
import net.ixitxachitls.companion.data.dynamics.StoredEntry;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.net.CompanionMessenger;
import net.ixitxachitls.companion.ui.MessageDialog;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Strings;

import java.util.List;
import java.util.stream.Collectors;

/**
 * View for a single condition line.
 */
public class ConditionLineView extends LinearLayout {

  private final TimedCondition condition;
  private final String sourceId;
  private final List<String> targetIds;
  private final boolean initiated;

  public ConditionLineView(Context context, TimedCondition condition, String sourceName,
                           String sourceId, List<String> targetIds, int rounds, boolean affected,
                           boolean canDelete) {
    super(context);

    this.condition = condition;
    this.sourceId = sourceId;
    this.targetIds = targetIds;
    this.initiated = !affected;

    View view =
        LayoutInflater.from(getContext()).inflate(R.layout.view_condition_line, this, false);

    if (affected) {
      TextWrapper.wrap(view, R.id.name)
          .text(condition.getName() + " (from " + sourceName + ")");
    } else {
      TextWrapper.wrap(view, R.id.name)
          .text(condition.getName() + " (affecting " +
              Strings.COMMA_JOINER.join(targetIds.stream()
                  .map(StoredEntries::nameFor)
                  .collect(Collectors.toList())) + ")");
    }
    TextWrapper.wrap(view, R.id.duration).text(rounds + " rounds");
    TextWrapper.wrap(view, R.id.summary).text(condition.getSummary());
    Wrapper.wrap(view, R.id.delete_condition)
        .visible(canDelete)
        .onClick(this::deleteCondition)
        .description("Dismiss Condition",
            "Dismiss the condition and remove it from this character. Note that the deletion will "
                + "only happen once the other players character has been updated to all players.");

    view.setOnLongClickListener((v) -> {
      MessageDialog.create(context)
          .title(condition.getName())
          .message(condition.getDescription())
          .show();
      return true;
    });

    addView(view);
  }

  private void deleteCondition() {
    for (String targetId : targetIds) {
      CompanionMessenger.get().sendDeletion(condition.getName(), sourceId, targetId);
    }

    if (initiated) {
      Optional<? extends BaseCreature> creature;
      if (StoredEntry.hasType(sourceId, Character.TYPE)) {
        creature = Characters.getCharacter(sourceId).getValue();
      } else {
        creature = Creatures.getCreature(sourceId).getValue();
      }

      if (creature.isPresent()) {
        Status.log("Remving initiated condition " + condition.getName() + " from "
            + creature.get().getName());
        creature.get().removeInitiatedCondition(condition.getName());
      } else {
        Status.log("Cannot remove initiated condtion for creature " + sourceId);
      }
    }
  }
}
