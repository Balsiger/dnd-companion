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
import android.widget.ImageView;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.data.values.Condition;
import net.ixitxachitls.companion.ui.MessageDialog;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;

import java.util.Optional;

/**
 * Created by balsiger on 5/28/18.
 */
public class ConditionIconsView extends FlexboxLayout {

  private final HPImageView hp;
  private final NonlethalImageView nonlethal;

  public ConditionIconsView(Context context) {
    super(context);

    setFlexWrap(FlexWrap.WRAP);
    hp = new HPImageView(getContext());
    nonlethal = new NonlethalImageView(getContext());
  }

  public void update(BaseCreature<?> creature) {
    hp.setHp(creature.getHp(), creature.getMaxHp());
    nonlethal.setNonlethalDamage(creature.getNonlethalDamage(), creature.getHp());

    removeAllViews();
    addView(hp);
    addView(nonlethal);

    Optional<Battle> battle = creature.getBattle();
    Optional<Character> character = Optional.empty();
    if (creature instanceof Character) {
      character = Optional.of((Character) creature);
    }
    if (battle.isPresent()) {
      for (Condition condition : creature.getActiveConditions(battle.get())) {
        if (condition.showsIcon()) {
          addView(createImage(condition, character));
        }
      }
    }
  }

  private ImageView createImage(Condition condition, Optional<Character> character) {
    ImageView image = new ImageView(getContext());
    image.setImageResource(condition.getIcon());
    image.setImageTintList(ColorStateList.valueOf(getContext().getColor(R.color.icon)));
    image.setBackground(getContext().getDrawable(R.drawable.icon_back));
    image.setOnLongClickListener(v ->  {
      new MessageDialog(getContext())
          .title(condition.getName())
          .message(condition.getSummary() + "\n\n" + condition.getDescription())
          .show();
      return true;
    });
    // NOTE: For some reason, Android seems to intercepts clicks when adding a long click handler.
    // Since we want clicks to be ignored (and be interpreted in the chip to open the character
    // fragment, we do it here too).
    if (character.isPresent()) {
      image.setOnClickListener(v -> {
        CompanionFragments.get().showCharacter(character.get(), Optional.of(v));
      });
    }

    return image;
  }
}
