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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.values.Duration;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.ui.MessageDialog;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.List;

/**
 * View for a single condition line.
 */
public abstract class ConditionLineView extends LinearLayout {

  protected final TimedCondition condition;
  protected final String sourceName;
  protected final String sourceId;
  protected final List<String> targetIds;
  protected final boolean isDM;

  protected final Wrapper<FloatingActionButton> delete;

  protected ConditionLineView(Context context, TimedCondition condition, String sourceName,
                              String sourceId, List<String> targetIds, Duration remaining,
                              boolean isDM) {
    super(context);

    this.condition = condition;
    this.sourceName = sourceName;
    this.sourceId = sourceId;
    this.targetIds = targetIds;
    this.isDM = isDM;

    View view =
        LayoutInflater.from(getContext()).inflate(R.layout.view_condition_line, this, false);

    TextWrapper.wrap(view, R.id.duration).text(remaining.toString());

    delete = Wrapper.<FloatingActionButton>wrap(view, R.id.delete_condition)
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

    setUp(view);
    addView(view);
  }

  protected abstract void setUp(View view);
  protected abstract void deleteCondition();
  public abstract void update(Campaign campaign);
}
