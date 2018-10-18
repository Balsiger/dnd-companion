/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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

package net.ixitxachitls.companion.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.google.common.base.Preconditions;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.values.Encounter;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

/**
 * Dialog to enter monster initiative stats.
 */
public class MonsterInitiativeDialog extends Dialog {

  private static final String ARG_CAMPAIGN_ID = "campaign-id";
  private static final String ARG_MONSTER_ID = "monster-id";

  private Optional<Campaign> campaign = Optional.empty();
  private int monsterId;

  // Ui elements;
  private EditTextWrapper<EditText> name;
  private Wrapper<NumberPicker> modifier;
  private Wrapper<Button> add;

  public MonsterInitiativeDialog() {}

  public static MonsterInitiativeDialog newInstance(String campaignId, int monsterId) {
    MonsterInitiativeDialog dialog = new MonsterInitiativeDialog();
    dialog.setArguments(arguments(R.layout.dialog_monster_initiative,
        R.string.title_monster_initiative, R.color.monster, campaignId, monsterId));
    return dialog;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String campaignId, int monsterId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_CAMPAIGN_ID, campaignId);
    arguments.putInt(ARG_MONSTER_ID, monsterId);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preconditions.checkNotNull(getArguments(), "Cannot create without arguments.");
    campaign = campaigns().get(getArguments().getString(ARG_CAMPAIGN_ID));
    monsterId = getArguments().getInt(ARG_MONSTER_ID);
  }

  @Override
  protected void createContent(View view) {
    name = EditTextWrapper.wrap(view, R.id.name);
    name.text(makeName()).lineColor(R.color.monster);
    modifier = Wrapper.wrap(view, R.id.modifier);
    modifier.get().setMaxValue(20 + 20);
    modifier.get().setMinValue(0);
    modifier.get().setValue(20);
    modifier.get().setFormatter(new NumberPicker.Formatter() {
      @Override
      public String format(int index) {
        return Integer.toString(index - 20);
      }
    });

    add = Wrapper.wrap(view, R.id.add);
    add.onClick(this::add);
    add.visible(monsterId <= 0);
  }

  private void add() {
    if (monsterId > 0 || !campaign.isPresent()) {
      // Already added.
      return;
    }

    /*
    Creature creature = new Creature(data(), 0, name.getText(), campaign.get().getId(),
        Dice.d20() + modifier.get().getValue() - 20);
    creature.store();  // Storing will also add the creature.
    CompanionApplication.get(getContext()).battles().get(campaign.get().getId())
        .setLastMonsterName(name.getText());
    save();
    */
  }

  private String makeName() {
    if (!campaign.isPresent()) {
      return Encounter.DEFAULT_MONSTER_NAME;
    }

    return CompanionApplication.get(getContext()).encounters().get(campaign.get().getId())
        .numberedMonsterName();
  }
}
