/*
 * Copyright (c) 2017-2019 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Monster;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.views.ModifiedValueView;
import net.ixitxachitls.companion.ui.views.MonsterTitleView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;

import java.util.Optional;

/**
 * Fragment for displaying monster information.
 */
public class MonsterFragment extends CompanionFragment {

  private Optional<Monster> monster = Optional.empty();

  // UI elements.
  private TextWrapper<TextView> campaignTitle;
  private MonsterTitleView title;
  private ModifiedValueView ac;
  private ModifiedValueView acTouch;
  private ModifiedValueView acFlat;
  private TextWrapper<TextView> hitPoints;

  public MonsterFragment() {
    super(Type.monster);
  }

  @Override
  public boolean goBack() {
    if (monster.isPresent() && !monster.get().getCampaign().isDefault()) {
      CompanionFragments.get().showCampaign(monster.get().getCampaign(), Optional.of(title));
    } else {
      CompanionFragments.get().show(Type.campaigns, Optional.empty());
    }

    return true;
  }

  @Override
  public void update() {
    if (monster.isPresent()) {
      campaignTitle.text(monster.get().getCampaign().getName());
      title.update(monster.get());
      ac.set(monster.get().normalArmorClass());
      acTouch.set(monster.get().touchArmorClass());
      acFlat.set(monster.get().flatFootedArmorClass());
      hitPoints.text(formatHp(monster.get()));
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
    super.onCreateView(inflater, container, state);

    LinearLayout view = (LinearLayout)
        inflater.inflate(R.layout.fragment_monster, container, false);

    campaignTitle = TextWrapper.wrap(view, R.id.campaign);
    title = view.findViewById(R.id.title);
    ac = view.findViewById(R.id.ac);
    acTouch = view.findViewById(R.id.ac_touch);
    acFlat = view.findViewById(R.id.ac_flatfooted);
    hitPoints = TextWrapper.wrap(view, R.id.hit_points);

    clearActions();
    addAction(R.drawable.ic_arrow_back_black_24dp, "Back to Campaign",
        "Go back to this characters campaign view.")
        .onClick(this::goBack);

    return view;
  }

  public void showMonster(Monster monster) {
    this.monster = Optional.of(monster);

    update();
  }

  private static String formatHp(Monster monster) {
    String hp = "";
    hp += monster.getHp();

    if (monster.getHp() != monster.getMaxHp()) {
      hp += "/" + monster.getMaxHp();
    }

    hp += " (" + monster.getHitDice() + "d" + monster.getHitDie() + "+"
        + (monster.getConstitutionModifier() * monster.getHitDice()) + ")";

    return hp;
  }
}
