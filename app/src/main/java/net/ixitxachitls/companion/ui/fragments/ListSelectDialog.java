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

package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.dialogs.Dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Simple fragmemt to select an item from a list.
 */
public class ListSelectDialog extends Dialog {

  private static final String ARG_SELECTED = "selected";
  private static final String ARG_VALUES = "values";

  private String selected;
  private ArrayList<Entry> values;
  protected Optional<SelectAction> selectAction = Optional.empty();

  @FunctionalInterface
  public interface SelectAction {
    void select(String id);
  }

  public static class Entry implements Parcelable {
    private final String name;
    private final String id;

    public static final Parcelable.Creator<Entry> CREATOR
        = new Parcelable.Creator<Entry>() {
      public Entry createFromParcel(Parcel parcel) {
        return new Entry(parcel.readString(), parcel.readString());
      }

      public Entry[] newArray(int size) {
        return new Entry[size];
      }
    };

    public Entry(String name, String id) {
      this.name = name;
      this.id = id;
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
      parcel.writeString(name);
      parcel.writeString(id);
    }
  }

  public ListSelectDialog() {
    // Required empty public constructor
  }

  public static ListSelectDialog newStringInstance(int titleId, String selected,
                                                   Collection<String> values, int color) {
    return newInstance(titleId, selected,
        values.stream().map(m -> new Entry(m, m)).collect(Collectors.toList()), color);
  }

  public static ListSelectDialog newInstance(int titleId, String selected,
                                             Collection<Entry> values, int color) {
    ListSelectDialog fragment = new ListSelectDialog();
    Bundle arguments = arguments(R.layout.fragment_list_select, titleId, color, selected, values);
    fragment.setArguments(arguments);
    return fragment;
  }

  protected static Bundle arguments(int layoutId, int titleId, int color, String selected,
                                    Collection<Entry> values) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, color);
    arguments.putString(ARG_SELECTED, selected);
    arguments.putParcelableArrayList(ARG_VALUES, new ArrayList<Entry>(values));
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      selected = getArguments().getString(ARG_SELECTED);
      values = getArguments().getParcelableArrayList(ARG_VALUES);
    } else {
      selected = "";
      values = new ArrayList<>();
    }
  }

  @Override
  public void createContent(View view) {
    ListView list = (ListView) view.findViewById(R.id.listSelectView);
    ArrayAdapter<String> itemAdapter =
        new ArrayAdapter<>(view.getContext(), R.layout.list_item_select,
            values.stream().map(m -> m.name).collect(Collectors.toList()));
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

  protected void edited(Entry value, int position) {
    save();

    if (selectAction.isPresent()) {
      selectAction.get().select(value.id);
    } else {
      Log.wtf("select", "listener not set");
    }
  }
}
