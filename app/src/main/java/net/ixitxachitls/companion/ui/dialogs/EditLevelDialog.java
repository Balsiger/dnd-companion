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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Level;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.ui.edit.EditNumberFragment;
import net.ixitxachitls.companion.ui.fragments.ListSelectFragment;

/**
 * Simple fragmemt to execute a level.
 */
public class EditLevelDialog extends Dialog {
  /*
  repeated ParametrizedEntityProto quality = 3;
  repeated ParametrizedEntityProto feat = 4;
  Ability ability_increase = 5;
  repeated string spell_known = 6;
  */

  private static final String ARG_PROTO = "proto";
  private static final String ARG_LEVEL = "level";

  private Character.Level level;
  private int levelNumber;
  private TextView name;
  private TextView hp;
  private @Nullable TextView abilityIncrease;
  private Edit edit;

  public EditLevelDialog() {
    // Required empty public constructor
  }

  public static EditLevelDialog newInstance(@StringRes int titleId, @ColorRes int color,
                                            Data.CharacterProto.Level levelProto, int level) {
    EditLevelDialog fragment = new EditLevelDialog();
    Bundle args = arguments(titleId, color, levelProto, level);
    fragment.setArguments(args);
    return fragment;
  }

  protected static Bundle arguments(@StringRes int titleId, @ColorRes int color,
                                    Data.CharacterProto.Level levelProto, int level) {
    Bundle arguments = Dialog.arguments(R.layout.fragment_edit_level, titleId, color);
    arguments.putByteArray(ARG_PROTO, levelProto.toByteArray());
    arguments.putInt(ARG_LEVEL, level);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      try {
        level = Character.fromProto(
            Data.CharacterProto.Level.parseFrom(getArguments().getByteArray(ARG_PROTO)));
      } catch (InvalidProtocolBufferException e) {
        Toast.makeText(getContext(), "Cannot parse proto: " + e, Toast.LENGTH_SHORT).show();
        level = new Character.Level("");
      }
      levelNumber = getArguments().getInt(ARG_LEVEL);
    } else {
      level = new Character.Level("");
      levelNumber = 1;
    }
  }

  @Override
  public void createContent(View view) {
    name = setupTextView(view, R.id.name, this::editName);
    hp = setupTextView(view, R.id.hp, R.id.hp_label, this::editHp);
    if (Level.hasAbilityIncrease(levelNumber) || level.hasAbilityIncrease()) {
      abilityIncrease = setupTextView(view, R.id.abilityIncrease, R.id.abilityIncreaseLabel,
          this::editAbilityIncrease);
    } else {
      view.findViewById(R.id.abilityIncrease).setVisibility(View.GONE);
      view.findViewById(R.id.abilityIncreaseLabel).setVisibility(View.GONE);
    }

    update();
  }

  @FunctionalInterface
  interface EditListener {
    void edit();
  }

  private TextView setupTextView(View container, int id, int labelId, EditListener listener) {
    setupTextView(container, labelId, listener);
    return setupTextView(container, id, listener);
  }

  private TextView setupTextView(View container, int id, EditListener listener) {
    TextView view = (TextView) container.findViewById(id);
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.edit();
      }
    });

    return view;
  }

  public void setListener(Edit edit) {
    this.edit = edit;
  }

  public void editName() {
    ListSelectFragment edit = ListSelectFragment.newInstance(R.string.character_select_class,
        level.getName(), Entries.get().getLevels().getNames(), color);
    edit.setSelectListener(this::updateName);
    edit.display(getFragmentManager());
  }

  public void editHp() {
    EditNumberFragment edit = EditNumberFragment.newInstance(R.string.hit_points,
        R.string.hit_points, level.getHp(), color);
    edit.setListener(this::updateHp);
    edit.display(getFragmentManager());
  }

  private void editAbilityIncrease() {
    ListSelectFragment edit = ListSelectFragment.newInstance(
        R.string.abilityIncreaseTitle, level.getAbilityIncrease().getName(), Ability.names(),
        color);
    edit.setSelectListener(this::updateAbilityIncrease);
    edit.display(getFragmentManager());
  }

  private void updateName(String value, int position) {
    level.setName(value);
    update();
  }

  private void updateHp(int value) {
    level.setHp(value);
    update();
  }

  private void updateAbilityIncrease(String value, int position) {
    level.setAbilityIncrease(Ability.fromName(value));
    update();
  }

  private void update() {
    if (level.getName().isEmpty()) {
      name.setText("<" + getString(R.string.character_select_class) + ">");
    } else {
      name.setText(level.getName());
    }

    hp.setText(String.valueOf(level.getHp()));

    if (abilityIncrease != null) {
      abilityIncrease.setText(level.getAbilityIncrease().getName());
    }
  }

  @FunctionalInterface
  public interface Edit {
    void edit(Character.Level level);
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    if (edit != null) {
      edit.edit(level);
    } else {
      Log.wtf("execute", "listener not set");
    }

    super.onDismiss(dialog);
  }
}
