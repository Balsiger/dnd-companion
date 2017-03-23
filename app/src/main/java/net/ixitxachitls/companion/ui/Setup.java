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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.ixitxachitls.companion.util.Edits;

/**
 * Collection of utilities to setup ui elements.
 */
public class Setup {
  private Setup() {}

  @FunctionalInterface
  public interface Action {
    void execute();
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

  public static TextView textView(View container, int id, int labelId, Action action) {
    textView(container, labelId, action);
    return textView(container, id, action);
  }

  public static TextView textView(View container, int id, Action action) {
    TextView view = (TextView) container.findViewById(id);
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        action.execute();
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

  public static FloatingActionButton floatingButton(View container, int id, Action action) {
    FloatingActionButton button = (FloatingActionButton)
        container.findViewById(id);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        action.execute();
      }
    });

    return button;
  }
}
