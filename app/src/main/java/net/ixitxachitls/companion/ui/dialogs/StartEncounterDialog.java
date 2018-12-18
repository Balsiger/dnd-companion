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

package net.ixitxachitls.companion.ui.dialogs;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Documents;
import net.ixitxachitls.companion.data.documents.Monster;
import net.ixitxachitls.companion.ui.views.StartEncounterLineView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Dialog to setup and start a new battle.
 */
public class StartEncounterDialog extends Dialog {

  private static final String ARG_ID = "id";

  private Optional<Campaign> campaign;

  // UI elements.
  private Wrapper<CheckBox> includeAll;
  private Wrapper<CheckBox> surpriseAll;
  private Wrapper<ImageView> addMonster;
  private LinearLayout characters;
  private LinearLayout monsters;
  private Wrapper<Button> save;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String id = getArguments().getString(ARG_ID);
    campaign = campaigns().get(id);
  }

  @Override
  public void save() {
    super.save();

    List<String> includedCreatureIds = new ArrayList<>();
    List<String> surprisedCreatureIds = new ArrayList<>();

    for (int i = 0; i < characters.getChildCount(); i++) {
      if (characters.getChildAt(i) instanceof StartEncounterLineView) {
        StartEncounterLineView line = (StartEncounterLineView) characters.getChildAt(i);
        if (line.isIncluded()) {
          includedCreatureIds.add(line.getCreatureId());
        }
        if (line.isSurprised()) {
          surprisedCreatureIds.add(line.getCreatureId());
        }
      }
    }
    for (int i = 0; i < monsters.getChildCount(); i++) {
      if (monsters.getChildAt(i) instanceof StartEncounterLineView) {
        StartEncounterLineView line = (StartEncounterLineView) monsters.getChildAt(i);
        if (line.isIncluded()) {
          includedCreatureIds.add(line.getCreatureId());
        }
        if (line.isSurprised()) {
          surprisedCreatureIds.add(line.getCreatureId());
        }
      }
    }

    if (campaign.isPresent()) {
      campaign.get().getEncounter().starting(includedCreatureIds, surprisedCreatureIds);
    }
  }

  @Override
  protected void createContent(View view) {
    characters = view.findViewById(R.id.characters);
    monsters = view.findViewById(R.id.monsters);
    includeAll = Wrapper.<CheckBox>wrap(view, R.id.include_all).onClick(this::toggleIncludeAll);
    surpriseAll = Wrapper.<CheckBox>wrap(view, R.id.surprise_all).onClick(this::toggleSurpriseAll);
    addMonster = Wrapper.<ImageView>wrap(view, R.id.add_monster).onClick(this::addMonster);
    save = Wrapper.<Button>wrap(view, R.id.save).onClick(this::save);

    for (Character character : characters().getCampaignCharacters(campaign.get().getId())) {
      characters.addView(new StartEncounterLineView(getContext(), character.getId(),
          character.getName(), R.color.characterDark, this::update));
    }

    characters().observe(this, this::refresh);
    monsters().observe(this, this::refresh);
  }

  private void addMonster() {
    if (campaign.isPresent()) {
      MonsterInitiativeDialog.newInstance(campaign.get().getId(), -1).display();
    }
  }

  private void refresh(Documents.Update update) {
    if (campaign.isPresent()) {
      // Characters.
      this.characters.removeAllViews();
      for (Character character : characters().getCampaignCharacters(campaign.get().getId())) {
        this.characters.addView(new StartEncounterLineView(getContext(), character.getId(),
            character.getName(), R.color.characterDark, this::update));
      }

      // Monsters.
      this.monsters.removeAllViews();
      for (Monster monster : monsters().getCampaignMonsters(campaign.get().getId())) {
        this.monsters.addView(new StartEncounterLineView(getContext(), monster.getId(),
            monster.getName(), R.color.monsterDark, this::update));
      }
    }
  }

  private void toggleIncludeAll() {
    boolean checked = includeAll.get().isChecked();
    for (int i = 0; i < characters.getChildCount(); i++) {
      if (characters.getChildAt(i) instanceof StartEncounterLineView) {
        StartEncounterLineView line = (StartEncounterLineView) characters.getChildAt(i);
        line.setIncluded(checked);
      }
    }
    for (int i = 0; i < monsters.getChildCount(); i++) {
      if (monsters.getChildAt(i) instanceof StartEncounterLineView) {
        StartEncounterLineView line = (StartEncounterLineView) monsters.getChildAt(i);
        line.setIncluded(checked);
      }
    }
  }

  private void toggleSurpriseAll() {
    boolean checked = surpriseAll.get().isChecked();
    for (int i = 0; i < characters.getChildCount(); i++) {
      if (characters.getChildAt(i) instanceof StartEncounterLineView) {
        StartEncounterLineView line = (StartEncounterLineView) characters.getChildAt(i);
        if (line.isIncluded()) {
          line.setSurprised(checked);
        }
      }
    }
    for (int i = 0; i < monsters.getChildCount(); i++) {
      if (monsters.getChildAt(i) instanceof StartEncounterLineView) {
        StartEncounterLineView line = (StartEncounterLineView) monsters.getChildAt(i);
        if (line.isIncluded()) {
          line.setSurprised(checked);
        }
      }
    }
  }

  private void update() {
    boolean allIncluded = true;
    boolean noneIncluded = true;
    boolean allSurprised = true;
    boolean noneSurprised = true;
    for (int i = 0; i < characters.getChildCount(); i++) {
      if (characters.getChildAt(i) instanceof StartEncounterLineView) {
        StartEncounterLineView line = (StartEncounterLineView) characters.getChildAt(i);
        allIncluded &= line.isIncluded();
        noneIncluded &= !line.isIncluded();
        if (line.isIncluded()) {
          allSurprised &= line.isSurprised();
          noneSurprised &= !line.isSurprised();
        }
      }
    }
    for (int i = 0; i < monsters.getChildCount(); i++) {
      if (monsters.getChildAt(i) instanceof StartEncounterLineView) {
        StartEncounterLineView line = (StartEncounterLineView) monsters.getChildAt(i);
        allIncluded &= line.isIncluded();
        noneIncluded &= !line.isIncluded();
        if (line.isIncluded()) {
          allSurprised &= line.isSurprised();
          noneSurprised &= !line.isSurprised();
        }
      }
    }

    includeAll.get().setChecked(allIncluded);
    includeAll.get().setButtonTintList(ColorStateList.valueOf(getResources()
        .getColor(allIncluded || noneIncluded ? R.color.battle : R.color.cell, null)));
    surpriseAll.get().setChecked(allSurprised);
    surpriseAll.get().setButtonTintList(ColorStateList.valueOf(getResources()
        .getColor(allSurprised || noneSurprised ? R.color.battle : R.color.cell, null)));
    save.enabled(!noneIncluded);
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String campaignId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId, R.color.battleText);
    arguments.putString(ARG_ID, campaignId);
    return arguments;
  }

  public static StartEncounterDialog newInstance(String campaignId) {
    StartEncounterDialog dialog = new StartEncounterDialog();
    dialog.setArguments(arguments(R.layout.dialog_start_encounter,
        R.string.dialog_title_start_battle, R.color.battle, campaignId));
    return dialog;
  }
}
