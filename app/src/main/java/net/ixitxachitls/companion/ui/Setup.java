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

import android.content.res.ColorStateList;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import net.ixitxachitls.companion.ui.fragments.ListSelectFragment;
import net.ixitxachitls.companion.util.Edits;

import java.util.ArrayList;

/**
 * Collection of utilities to setup ui elements.
 */
public class Setup {
  private Setup() {}

  @FunctionalInterface
  public interface Action {
    void execute();
  }

  @FunctionalInterface
  public interface SelectAction {
    void select(int position);
  }

  @FunctionalInterface
  public interface SwitchAction {
    void checked(Switch widget);
  }

  public static EditText editText(View view, int id, String value, int label, int color) {
    return editText(view, id, value, label, color, null);
  }

  public static EditText editText(View view, int id, String value, int label, int color,
                                  @Nullable Action action) {
    return editText(view, id, value, label, color, action, null);
  }

  public static EditText editText(View view, int id, String value, int label, int color,
                                  @Nullable Action editAction, @Nullable Action keyAction) {
    EditText edit = (EditText) view.findViewById(id);
    edit.setText(value);
    edit.setHint(view.getContext().getString(label));
    edit.setBackgroundTintList(ColorStateList.valueOf(color));
    edit.setOnKeyListener(new View.OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        // Ignore key downs to not treat things twice.
        if (event.getAction() != KeyEvent.ACTION_UP)
          return false;

        if (keyCode == KeyEvent.KEYCODE_ENTER) {
          if (editAction != null) {
            editAction.execute();
          }
          Edits.hideKeyboard(view, edit);
          return true;
        }

        if (keyAction != null) {
          keyAction.execute();
        }

        return false;
      }
    });

    return edit;
  }

  public static TextView textView(View container, int id) {
    return (TextView) container.findViewById(id);
  }

  public static TextView textView(View container, int id, int labelId, Action action) {
    textView(container, labelId, action);
    return textView(container, id, action);
  }

  public static TextView textView(View container, int id, Action action) {
    TextView view = textView(container, id);
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (action != null) {
          action.execute();
        }
      }
    });

    return view;
  }

  public static Button button(View container, int id, Action action) {
    Button button = (Button) container.findViewById(id);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        action.execute();
      }
    });

    return button;
  }

  public static ImageButton imageButton(View container, int id) {
    return (ImageButton) container.findViewById(id);
  }

  public static ImageButton imageButton(View container, int id, Action action) {
    ImageButton button = imageButton(container, id);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          action.execute();
        }
    });

    return button;
  }

  public static ImageView imageView(View container, int id) {
    return (ImageView) container.findViewById(id);
  }

  public static ImageView imageView(View container, int id, Action action) {
    ImageView view = imageView(container, id);
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          action.execute();
        }
    });

    return view;
  }

  public static FloatingActionButton floatingButton(View container, int id, Action action) {
    FloatingActionButton button = (FloatingActionButton) container.findViewById(id);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        action.execute();
      }
    });

    return button;
  }

  public static ListView list(View container, int id, int itemLayout,
                              ArrayList<String> values, String selected,
                              ListSelectFragment.SelectAction action) {
    ListView list = (ListView) container.findViewById(id);
    ArrayAdapter<String> itemAdapter =
        new ArrayAdapter<>(container.getContext(), itemLayout, values);
    list.setAdapter(itemAdapter);
    list.setSelection(values.indexOf(selected));
    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        action.select(values.get(position), position);
      }
    });

    return list;
  }

  public static <T> ListView listView(View container, int id, ListAdapter<T> itemAdapter,
                                      SelectAction action) {
    ListView list = (ListView) container.findViewById(id);
    list.setAdapter(itemAdapter);
    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        action.select(position);
      }
    });

    return list;
  }

  public static Switch switchButton(View container, int id, SwitchAction action) {
    Switch button = (Switch) container.findViewById(id);

    button.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        action.checked(button);
      }
    });

    return button;
  }
}
