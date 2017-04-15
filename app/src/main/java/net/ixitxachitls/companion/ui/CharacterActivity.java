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

package net.ixitxachitls.companion.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Optional;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Character;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBase;
import net.ixitxachitls.companion.ui.edit.EditLevelFragment;
import net.ixitxachitls.companion.ui.fragments.ListSelectFragment;

import java.util.ArrayList;

public class CharacterActivity extends AppCompatActivity {

  private int mColor;
  private Character mCharacter;
  private TextView mLevel;
  private ListSelectFragment mLevelsEdit;

  @FunctionalInterface
  interface EditListener {
    void edit();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mColor = getColor(R.color.character);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_character);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mCharacter =
        Character.load(getIntent().getLongExtra(DataBase.COLUMN_ID, 0)).or(new Character(0, "", ""));
    mLevel = setupTextView(R.id.characterLevelValue, this::editLevels);
    update();
  }

  protected TextView setupTextView(int id, EditListener listener) {
    TextView view = (TextView) findViewById(id);
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.edit();
      }
    });

    return view;
  }

  public void editLevels() {
    ArrayList<String> summaries = mCharacter.levelSummaries();
    summaries.add("<add level>");
    ListSelectFragment edit = ListSelectFragment.newInstance(R.string.character_edit_levels,
        "", summaries, mColor);
    edit.setSelectListener(this::editLevel);
    edit.display(getFragmentManager());
  }

  private boolean editLevel(String value, int position) {
    Optional<Character.Level> level = mCharacter.getLevel(position);
    EditLevelFragment edit = EditLevelFragment.newInstance(R.string.character_edit_level,
        getColor(R.color.character),  level.isPresent()
            ? level.get().toProto() : Data.CharacterProto.Level.getDefaultInstance(),
        position + 1);
    edit.setListener(v -> updateLevel(v, position));
    edit.display(getFragmentManager());

    return false;
  }

  private boolean updateLevel(Character.Level value, int position) {
    mCharacter.setLevel(position, value);
    update();
    editLevels();

    return false;
  }

  private void update() {
    mLevel.setText(mCharacter.summarizeLevels());
  }
}
