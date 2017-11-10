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

package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.base.Optional;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.dialogs.Dialog;

import java.util.ArrayList;

/**
 * Simple fragmemt to select an item from a list.
 */
public class ListSelectFragment extends Dialog {

  private static final String ARG_SELECTED = "selected";
  private static final String ARG_VALUES = "values";

  private String selected;
  private ArrayList<String> values;
  protected Optional<SelectAction> selectAction = Optional.absent();

  @FunctionalInterface
  public interface SelectAction {
    void select(String value, int position);
  }

  public ListSelectFragment() {
    // Required empty public constructor
  }

  public static ListSelectFragment newInstance(int titleId, String selected,
                                               ArrayList<String> values, int color) {
    ListSelectFragment fragment = new ListSelectFragment();
    Bundle arguments = arguments(R.layout.fragment_list_select, titleId, color, selected, values);
    fragment.setArguments(arguments);
    return fragment;
  }

  protected static Bundle arguments(int layoutId, int titleId, int color, String selected,
                                    ArrayList<String> values) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, color);
    arguments.putString(ARG_SELECTED, selected);
    arguments.putStringArrayList(ARG_VALUES, values);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      selected = getArguments().getString(ARG_SELECTED);
      values = getArguments().getStringArrayList(ARG_VALUES);
    } else {
      selected = "";
      values = new ArrayList<>();
    }
  }

  @Override
  public void createContent(View view) {
    ListView list = (ListView) view.findViewById(R.id.listSelectView);
    ArrayAdapter<String> itemAdapter =
        new ArrayAdapter<>(view.getContext(), R.layout.list_item_select, values);
    list.setAdapter(itemAdapter);
    list.setSelection(values.indexOf(selected));
    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        edited(values.get(position), position);
      }
    });
  }

  public void setSelectListener(SelectAction action) {
    this.selectAction = Optional.of(action);
  }

  protected void edited(String value, int position) {
    save();

    if (selectAction.isPresent()) {
      selectAction.get().select(value, position);
    } else {
      Log.wtf("select", "listener not set");
    }
  }
}
