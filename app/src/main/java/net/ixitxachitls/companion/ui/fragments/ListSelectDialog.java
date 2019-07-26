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

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.ui.dialogs.Dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Simple dialog to select an item from a list.
 */
public class ListSelectDialog extends Dialog {

  private static final String ARG_SELECTED = "selected";
  private static final String ARG_VALUES = "values";
  private static final String ARG_REQUIRED = "required";

  protected Optional<SelectAction> selectAction = Optional.empty();
  protected int selectionsRequired = 1;
  protected int selectedValues = 0;
  protected List<String> selected = new ArrayList<>();
  private List<Entry> values;
  private boolean multiple = false;
  private SelectionArrayAdapter<String> itemAdapter;

  public ListSelectDialog() {
    // Required empty public constructor
  }

  @FunctionalInterface
  public interface SelectAction {
    void select(List<String> id);
  }

  public ListSelectDialog setSelectListener(SelectAction action) {
    this.selectAction = Optional.of(action);

    return this;
  }

  public ListSelectDialog multiple() {
    this.multiple = true;

    return this;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      selected = getArguments().getStringArrayList(ARG_SELECTED);
      values = getArguments().getParcelableArrayList(ARG_VALUES);
      selectionsRequired = getArguments().getInt(ARG_REQUIRED);
    } else {
      selected = new ArrayList<>();
      values = new ArrayList<>();
      selectionsRequired = 1;
    }
  }

  @Override
  public void createContent(View view) {
    ListView list = view.findViewById(R.id.listSelectView);
    itemAdapter =
        new SelectionArrayAdapter<>(view.getContext(), R.layout.list_item_select,
            R.color.character,
            values.stream().map(m -> m.name).collect(Collectors.toList()), selected);
    list.setAdapter(itemAdapter);
    list.setOnItemClickListener(
        (parent, view1, position, id) -> edited(values.get(position), position));
    if (multiple) {
      list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    if (!selected.isEmpty()) {
      list.setSelection(indexOfValue(selected.get(0)) - 3);
    }
  }

  protected void edited(Entry value, int position) {
    selectedValues++;
    if (selected.size() >= selectedValues) {
      selected.set(selectedValues - 1, value.id);
    } else {
      selected.add(value.id);
    }

    if (selectAction.isPresent()) {
      if (selectedValues >= selectionsRequired) {
        save();
        selectAction.get().select(selected);
      } else {
        itemAdapter.updateSelected(selected);
      }
    } else {
      save();
      Status.error("Expected listener not set for list select dialog.");
    }
  }

  private int indexOfValue(String id) {
    for (int i = 0; i < values.size(); i++) {
      if (values.get(i).id.equals(id)) {
        return i;
      }
    }

    return -1;
  }

  protected static Bundle arguments(int layoutId, int titleId, int color, List<String> selected,
                                    int required, Collection<Entry> values) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, color);
    arguments.putStringArrayList(ARG_SELECTED, new ArrayList<>(selected));
    arguments.putParcelableArrayList(ARG_VALUES, new ArrayList<>(values));
    arguments.putInt(ARG_REQUIRED, required);
    return arguments;
  }

  public static ListSelectDialog newInstance(int titleId, List<String> selected,
                                             Collection<Entry> values, int color) {
    ListSelectDialog dialog = new ListSelectDialog();
    Bundle arguments = arguments(R.layout.fragment_list_select, titleId, color, selected, 1, values);
    dialog.setArguments(arguments);
    return dialog;
  }

  public static ListSelectDialog newInstance(int titleId, List<String> selected, int required,
                                             Collection<Entry> values, int color) {
    ListSelectDialog fragment = new ListSelectDialog();
    Bundle arguments = arguments(R.layout.fragment_list_select, titleId, color, selected, required,
        values);
    fragment.setArguments(arguments);
    return fragment;
  }

  public static ListSelectDialog newStringInstance(int titleId, List<String> selected,
                                                   Collection<String> values, int color) {
    return newInstance(titleId, selected,
        values.stream().map(m -> new Entry(m, m)).collect(Collectors.toList()), color);
  }

  public static ListSelectDialog newStringInstance(int titleId, List<String> selected, int required,
                                                   Collection<String> values, int color) {
    return newInstance(titleId, selected, required,
        values.stream().map(m -> new Entry(m, m)).collect(Collectors.toList()), color);
  }

  public static class Entry implements Parcelable {
    public static final Parcelable.Creator<Entry> CREATOR
        = new Parcelable.Creator<Entry>() {
      @Override
      public Entry createFromParcel(Parcel parcel) {
        return new Entry(parcel.readString(), parcel.readString());
      }

      @Override
      public Entry[] newArray(int size) {
        return new Entry[size];
      }
    };
    private final String name;
    private final String id;

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

  private class SelectionArrayAdapter<T> extends ArrayAdapter<T> {
    private final int selectedColor;
    private final List<T> entries;
    private List<Integer> selected;

    public SelectionArrayAdapter(Context context, @LayoutRes int layout,
                                 @ColorRes int selectedColor, List<T> entries, List<T> selected) {
      super(context, layout, entries);
      this.selectedColor = selectedColor;
      this.entries = entries;
      updateSelected(selected);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = super.getView(position, convertView, parent);

      if (selected.contains(position)) {
        view.setBackgroundColor(getResources().getColor(selectedColor, null));
      } else {
        view.setBackgroundColor(getResources().getColor(R.color.transparent, null));
      }

      return view;
    }

    public void updateSelected(List<T> selected) {
      this.selected = selected.stream()
          .map(s -> entries.indexOf(s))
          .collect(Collectors.toList());
      notifyDataSetChanged();
    }
  }
}
