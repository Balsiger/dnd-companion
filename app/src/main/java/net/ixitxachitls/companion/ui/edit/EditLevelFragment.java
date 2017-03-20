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

package net.ixitxachitls.companion.ui.edit;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Character;
import net.ixitxachitls.companion.data.Level;
import net.ixitxachitls.companion.data.enums.Ability;
import net.ixitxachitls.companion.proto.Entity;

/**
 * Simple fragmemt to edit a level.
 */
public class EditLevelFragment extends EditFragment {
  /*
  repeated ParametrizedEntityProto quality = 3;
  repeated ParametrizedEntityProto feat = 4;
  Ability ability_increase = 5;
  repeated string spell_known = 6;
  */

  private static final String ARG_PROTO = "proto";
  private static final String ARG_LEVEL = "level";

  private Character.Level mLevel;
  private int mLevelNumber;
  private TextView mName;
  private TextView mHp;
  private @Nullable TextView mAbilityIncrease;
  private Edit mEdit;

  public EditLevelFragment() {
    // Required empty public constructor
  }

  public static EditLevelFragment newInstance(int titleId, int color,
                                              Entity.CharacterProto.Level levelProto, int level) {
    EditLevelFragment fragment = new EditLevelFragment();
    Bundle args = arguments(titleId, color, levelProto, level);
    fragment.setArguments(args);
    return fragment;
  }

  protected static Bundle arguments(int titleId, int color,
                                    Entity.CharacterProto.Level levelProto, int level) {
    Bundle arguments = EditFragment.arguments(titleId, color);
    arguments.putByteArray(ARG_PROTO, levelProto.toByteArray());
    arguments.putInt(ARG_LEVEL, level);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      try {
        mLevel = Character.fromProto(
            Entity.CharacterProto.Level.parseFrom(getArguments().getByteArray(ARG_PROTO)));
      } catch (InvalidProtocolBufferException e) {
        Toast.makeText(getContext(), "Cannot parse proto: " + e, Toast.LENGTH_SHORT).show();
        mLevel = new Character.Level("");
      }
      mLevelNumber = getArguments().getInt(ARG_LEVEL);
    } else {
      mLevel = new Character.Level("");
      mLevelNumber = 1;
    }
  }

  @Override
  public View onCreateContent(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_edit_level, container, false);
    mName = setupTextView(view, R.id.name, this::editName);
    mHp = setupTextView(view, R.id.hp, R.id.hp_label, this::editHp);
    if (Level.hasAbilityIncrease(mLevelNumber) || mLevel.hasAbilityIncrease()) {
      mAbilityIncrease = setupTextView(view, R.id.abilityIncrease, R.id.abilityIncreaseLabel,
          this::editAbilityIncrease);
    } else {
      view.findViewById(R.id.abilityIncrease).setVisibility(View.GONE);
      view.findViewById(R.id.abilityIncreaseLabel).setVisibility(View.GONE);
    }

    update();
    return view;
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
    mEdit = edit;
  }

  public void editName() {
    ListSelectFragment edit = ListSelectFragment.newInstance(R.string.character_select_class,
        mLevel.getName(), Entries.get().getLevels().getClasses(), mColor);
    edit.setListener(this::updateName);
    edit.display(getFragmentManager());
  }

  public void editHp() {
    EditNumberFragment edit = EditNumberFragment.newInstance(R.string.hit_points,
        R.string.hit_points, mLevel.getHp(), mColor);
    edit.setListener(this::updateHp);
    edit.display(getFragmentManager());
  }

  private void editAbilityIncrease() {
    ListSelectFragment edit = ListSelectFragment.newInstance(
        R.string.abilityIncreaseTitle, mLevel.getAbilityIncrease().getName(), Ability.names(),
        mColor);
    edit.setListener(this::updateAbilityIncrease);
    edit.display(getFragmentManager());
  }

  private void updateName(String value, int position) {
    mLevel.setName(value);
    update();
  }

  private void updateHp(int value) {
    mLevel.setHp(value);
    update();
  }

  private void updateAbilityIncrease(String value, int position) {
    mLevel.setAbilityIncrease(Ability.fromName(value));
    update();
  }

  private void update() {
    if (mLevel.getName().isEmpty()) {
      mName.setText("<" + getString(R.string.character_select_class) + ">");
    } else {
      mName.setText(mLevel.getName());
    }

    mHp.setText(String.valueOf(mLevel.getHp()));

    if (mAbilityIncrease != null) {
      mAbilityIncrease.setText(mLevel.getAbilityIncrease().getName());
    }
  }

  @FunctionalInterface
  public interface Edit {
    void edit(Character.Level level);
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    if (mEdit != null) {
      mEdit.edit(mLevel);
    } else {
      Log.wtf("edit", "listener not set");
    }

    close();
    super.onDismiss(dialog);
  }
}
