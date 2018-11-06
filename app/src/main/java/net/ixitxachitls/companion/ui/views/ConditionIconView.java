/*
 * Copyright (c) 2017-2018 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Tabletop Companion.
 *
 * The Tabletop Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Tabletop Companion is distributed in the hope that it will be useful,
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
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.CreatureCondition;
import net.ixitxachitls.companion.data.documents.Monster;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.MessageDialog;

import java.util.Optional;

/**
 * An icon for a condition.
 */
public class ConditionIconView extends android.support.v7.widget.AppCompatImageView {
  public ConditionIconView(Context context) {
    this(context, null, 0, 0);
  }

  public ConditionIconView(Context context, @Nullable CreatureCondition condition,
                           @ColorRes int foregroundColor, @ColorRes int backgroundColor) {
    super(context);

    if (condition != null) {
      setImageResource(condition.getCondition().getCondition().getIcon());
      setImageTintList(ColorStateList.valueOf(getContext().getColor(backgroundColor)));
      Drawable back = getContext().getDrawable(R.drawable.icon_back);
      back.setTint(getContext().getColor(foregroundColor));
      setBackground(back);
      setOnLongClickListener(v -> {
        new MessageDialog(getContext())
            .title(condition.getCondition().getName())
            .message(condition.getCondition().getSummary() + "\n\n"
                + condition.getCondition().getDescription() + "\n\n"
                + sourceName(condition.getCondition()))
            .show();
        return true;
      });
      if (CompanionApplication.get().me().amDM(condition.getId())
          || CompanionApplication.get().me().amPlayer(condition.getCondition().getSourceId())) {
        setOnClickListener(v -> {
          new ConfirmationPrompt(getContext())
              .title("Dismiss Condition?")
              .message("Do you really want to dismiss this condition?")
              .yes(() -> dismiss(condition.getId()))
              .show();
        });
      }
    }
  }

  private String sourceName(TimedCondition condition) {
    if (condition.getSourceId().isEmpty()) {
      return "This condition kindly provided by your DM";
    }

    Optional<Character> character =
        CompanionApplication.get().characters().get(condition.getSourceId());
    if (character.isPresent()) {
      return "Through the support of " + character.get().getName() + "!";
    }

    Optional<Monster> monster =
        CompanionApplication.get().monsters().get(condition.getSourceId());
    if (monster.isPresent()) {
      return "You suffer this because of " + monster.get().getName() + "...";
    }

    return "I'm sorry, I have no clue where this came from...";
  }

  private void dismiss(String conditionId) {
    CompanionApplication.get().context().conditions().delete(conditionId);
  }
}
