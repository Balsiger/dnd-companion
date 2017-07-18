/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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

package net.ixitxachitls.companion.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.ui.views.wrappers.EditTextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * Dialog to enter monster initiative stats.
 */
public class MonsterInitiativeDialog extends Dialog {

  private static final String ARG_CAMPAIGN_ID = "campaign-id";
  private static final String ARG_MONSTER_ID = "monster-id";
  private static final String DEFAULT_NAME = "Monsters";

  private Optional<Campaign> campaign = Optional.absent();
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
    campaign = Campaigns.getLocalCampaign(getArguments().getString(ARG_CAMPAIGN_ID));
    monsterId = getArguments().getInt(ARG_MONSTER_ID);
  }

  @Override
  protected void createContent(View view) {
    name = EditTextWrapper.wrap(view, R.id.name);
    name.text(makeName()).onClick(this::edit).lineColor(R.color.monster);
    modifier = Wrapper.wrap(view, R.id.modifier);
    modifier.get().setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
      @Override
      public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        edit();
      }
    });
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

  private void edit() {
  }

  private void add() {
    if (monsterId > 0 || !campaign.isPresent()) {
      // Already added.
      return;
    }

    campaign.get().getBattle().addMonster(name.getText().toString(),
        modifier.get().getValue() - 20);
    save();
  }

  private String makeName() {
    if (!campaign.isPresent()) {
      return DEFAULT_NAME;
    }

    String name = campaign.get().getBattle().getLastMonsterName().or("");
    if (name.isEmpty()) {
      return DEFAULT_NAME;
    }

    String []parts = name.split(" ");
    if (parts.length == 1) {
      return name + " 2";
    }

    try {
      int number = Integer.parseInt(parts[parts.length - 1]) + 1;
      String result = "";
      for (int i = 0; i < parts.length - 1; i++) {
        result += parts[i] + " ";
      }

      result += number;
      return result;
    } catch (NumberFormatException e) {
      return name + " 2";
    }
  }
}
