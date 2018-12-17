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
import android.support.annotation.CallSuper;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Creature;
import net.ixitxachitls.companion.data.documents.CreatureCondition;
import net.ixitxachitls.companion.data.values.Encounter;

import java.util.List;
import java.util.Optional;

/**
 * An title view in the encounter display.
 */
public class EncounterTitleView<T extends Creature<?>> extends CreatureTitleView<T> {

  private final int selectedColor;
  protected ConditionIconsView conditions;
  private Transition transition = new AutoTransition();
  private Optional<Encounter> encounter;
  // UI elements.
  private View view;

  public EncounterTitleView(Context context, @Nullable AttributeSet attributeSet,
                            @ColorRes int foregroundColor, @ColorRes int backgroundColor,
                            @ColorRes int selectedColor, @DrawableRes int defaultImage) {
    super(context, attributeSet, foregroundColor, backgroundColor, defaultImage);
    this.selectedColor = selectedColor;
  }

  public void showSelected(boolean selected) {
    TransitionManager.beginDelayedTransition((ViewGroup) view, transition);
    container.setBackgroundColor(getContext().getColor(
        selected ? selectedColor : backgroundColor));
  }

  @Override
  public void update(T creature) {
    super.update(creature);

    conditions.update(creature);
  }

  @Override
  protected String formatSubtitle() {
    if (creature.isPresent() && encounter.isPresent()) {
      if (encounter.get().isStarting()) {
        if (creature.get().hasInitiative(encounter.get().getNumber())) {
          return "Init " + creature.get().getInitiative();
        } else {
          return "Waiting for initiative...";
        }
      } else {
        return conditions.summary();
      }
    }

    return super.formatSubtitle();
  }

  @Override
  @CallSuper
  protected View init(AttributeSet attributes) {
    view = super.init(attributes);

    conditions = new ConditionIconsView(getContext(), LinearLayout.HORIZONTAL, foregroundColor,
        backgroundColor);
    LinearLayout titleContainer = view.findViewById(R.id.conditions);
    titleContainer.setVisibility(VISIBLE);
    titleContainer.addView(conditions);

    return view;
  }

  public void update(List<CreatureCondition> conditions) {
    this.conditions.update(conditions);
    refresh();
  }

  public void update(Encounter encounter, T creature) {
    this.encounter = Optional.of(encounter);
    update(creature);
  }
}
