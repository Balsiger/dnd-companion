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
import android.support.annotation.ColorRes;
import android.support.v7.widget.LinearLayoutCompat;
import android.widget.LinearLayout;

import net.ixitxachitls.companion.data.documents.Creature;
import net.ixitxachitls.companion.data.documents.CreatureCondition;
import net.ixitxachitls.companion.util.Strings;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * View for all condition icons.
 */
public class ConditionIconsView extends LinearLayout {

  private final int backgroundColor;
  private final int foregroundColor;
  private List<CreatureCondition> conditions = Collections.emptyList();

  // UI elements.
  private final HPImageView hp;
  private final NonlethalImageView nonlethal;

  public ConditionIconsView(Context context, @LinearLayoutCompat.OrientationMode int orientation,
                            @ColorRes int foregroundColor, @ColorRes int backgroundColor) {
    super(context);
    this.foregroundColor = foregroundColor;
    this.backgroundColor = backgroundColor;

    setOrientation(orientation);
    hp = new HPImageView(getContext());
    nonlethal = new NonlethalImageView(getContext());
  }

  public void update(Creature<?> creature) {
    hp.setHp(creature.getHp(), creature.getMaxHp());
    nonlethal.setNonlethalDamage(creature.getNonlethalDamage(), creature.getHp());

    redraw();
  }

  private void redraw() {
    removeAllViews();
    addView(hp);
    addView(nonlethal);

    for (CreatureCondition condition : conditions) {
      addView(new ConditionIconView(getContext(), condition, foregroundColor, backgroundColor));
    }
  }

  public void update(List<CreatureCondition> conditions) {
    this.conditions = conditions;
    redraw();
  }

  public String summary() {
    String summary = Strings.SEMICOLON_JOINER.join(conditions.stream()
        .map(c -> c.getCondition().getSummary()).collect(Collectors.toList()));
    if (summary.isEmpty()) {
      return "No conditions";
    }

    return summary;
  }
}
