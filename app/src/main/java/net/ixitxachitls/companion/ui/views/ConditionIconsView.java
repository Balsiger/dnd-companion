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
import android.widget.ImageView;
import android.widget.LinearLayout;

import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Creature;
import net.ixitxachitls.companion.data.values.Condition;
import net.ixitxachitls.companion.rules.Conditions;

import java.util.Optional;

/**
 * Created by balsiger on 5/28/18.
 */
public class ConditionIconsView extends LinearLayout { //FlexboxLayout {

  private final HPImageView hp;
  private final NonlethalImageView nonlethal;

  public ConditionIconsView(Context context) {
    super(context);

    setOrientation(VERTICAL);
    //setFlexWrap(FlexWrap.WRAP);
    hp = new HPImageView(getContext());
    nonlethal = new NonlethalImageView(getContext());
  }

  public void update(Creature<?> creature) {
    hp.setHp(creature.getHp(), creature.getMaxHp());
    nonlethal.setNonlethalDamage(creature.getNonlethalDamage(), creature.getHp());

    removeAllViews();
    addView(hp);
    addView(nonlethal);


    addView(createImage(Conditions.BLINDED, Optional.empty()));
    addView(createImage(Conditions.COWERING, Optional.empty()));
    addView(createImage(Conditions.DEAD, Optional.empty()));
    addView(createImage(Conditions.ENTANGLED, Optional.empty()));
    addView(createImage(Conditions.FASCINATED, Optional.empty()));
    addView(createImage(Conditions.GRAPPLING, Optional.empty()));
    addView(createImage(Conditions.CHECKED, Optional.empty()));
    addView(createImage(Conditions.PANICKED, Optional.empty()));
    addView(createImage(Conditions.ABILITY_DAMAGED, Optional.empty()));
    addView(createImage(Conditions.PRONE, Optional.empty()));
    addView(createImage(Conditions.PETRIFIED, Optional.empty()));
    addView(createImage(Conditions.UNCONSCIOUS, Optional.empty()));

    /*
    Encounter encounter = CompanionApplication.get(getContext()).encounters().get(creature.getCampaignId());
    for (Condition condition : creature.getActiveConditions(encounter)) {
      if (condition.showsIcon()) {
        Optional<Character> character = Optional.empty();
        if (creature instanceof Character) {
          character = Optional.of((Character) creature);
        }

        addView(createImage(condition, character));
      }
    }
    */
  }

  private ImageView createImage(Condition condition, Optional<Character> character) {
    return new ConditionIconView(getContext(), condition);
  }
}
