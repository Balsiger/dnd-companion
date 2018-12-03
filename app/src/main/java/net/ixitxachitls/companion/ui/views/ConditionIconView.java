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
import net.ixitxachitls.companion.data.documents.Users;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.MessageDialog;

import java.util.Optional;

/**
 * An icon for a condition.
 */
public class ConditionIconView extends android.support.v7.widget.AppCompatImageView {

  private static final int SIZE_PX = 50;

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
      setMaxHeight(SIZE_PX);
      setMaxWidth(SIZE_PX);
      setAdjustViewBounds(true);
      setOnLongClickListener(v -> {
        new MessageDialog(getContext())
            .title(condition.getCondition().getName())
            .message(summary(condition.getCondition()))
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

  private String summary(TimedCondition condition) {
    Optional<Character> character =
        CompanionApplication.get().characters().get(condition.getSourceId());

    String summary = condition.getSummary() + "\n\n"
        + condition.getDescription()
        + sourceName(character, condition)
        + duration(character, condition);

    return summary;
  }

  private String duration(Optional<Character> character, TimedCondition condition) {
    if (character.isPresent() && (character.get().amPlayer() || character.get().amDM())) {
      if (condition.isPermanent()) {
        return "\n\nThis condition is permanent";
      }
      if (condition.hasEndDate()) {
        return "\n\nThis condition is active until " + condition.getEndDate();
      }

      return "\n\nThis condition is active until round " + condition.getEndRound();
    }

    return "";
  }

  private String sourceName(Optional<Character> character, TimedCondition condition) {
    if (character.isPresent()) {
      return "\\n\\nThrough the support of " + character.get().getName() + "!";
    }

    Optional<Monster> monster =
        CompanionApplication.get().monsters().get(condition.getSourceId());
    if (monster.isPresent()) {
      return "\n\nYou suffer this because of " + monster.get().getName() + "...";
    }

    if (Users.isUserId(condition.getSourceId())) {
      return "\n\nThis condition kindly provided by your DM.";
    }

    return "I'm sorry, I have no clue where this came from...";
  }

  private void dismiss(String conditionId) {
    CompanionApplication.get().context().conditions().delete(conditionId);
  }
}
