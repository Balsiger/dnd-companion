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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.ixitachitls.companion.R;

import java.util.ArrayList;

/**
 * Simple fragmemt to select an item from a list.
 */
public class ListSelectFragment extends EditFragment {
  private static final String ARG_SELECTED = "selected";
  private static final String ARG_VALUES = "values";

  private String mSelected;
  private ArrayList<String> mValues;
  protected @Nullable Edit mEdit;

  @FunctionalInterface
  public interface Edit {
    void edit(String value, int position);
  }

  public ListSelectFragment() {
    // Required empty public constructor
  }

  public static ListSelectFragment newInstance(int titleId, String selected,
                                               ArrayList<String> values, int color) {
    ListSelectFragment fragment = new ListSelectFragment();
    Bundle arguments = arguments(titleId, color, selected, values);
    fragment.setArguments(arguments);
    return fragment;
  }

  protected static Bundle arguments(int titleId, int color, String selected,
                                    ArrayList<String> values) {
    Bundle arguments = EditFragment.arguments(titleId, color);
    arguments.putString(ARG_SELECTED, selected);
    arguments.putStringArrayList(ARG_VALUES, values);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      mSelected = getArguments().getString(ARG_SELECTED);
      mValues = getArguments().getStringArrayList(ARG_VALUES);
    } else {
      mSelected = "";
      mValues = new ArrayList<>();
    }
  }

  @Override
  public View onCreateContent(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_list_select, container, false);
    final ListView value = (ListView) view.findViewById(R.id.listSelectView);
    ArrayAdapter<String> itemAdapter =
        new ArrayAdapter<>(getContext(), R.layout.list_item_select, mValues);
    value.setAdapter(itemAdapter);
    value.setSelection(mValues.indexOf(mSelected));
    value.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        edited(mValues.get(position), position);
      }
    });

    return view;
  }

  public void setListener(Edit edit) {
    mEdit = edit;
  }

  protected void edited(String value, int position) {
    if (mEdit != null) {
      mEdit.edit(value, position);
    } else {
      Log.wtf("edit", "listener not set");
    }

    close();
  }

}
