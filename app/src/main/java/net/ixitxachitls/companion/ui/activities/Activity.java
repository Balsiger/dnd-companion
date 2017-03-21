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

package net.ixitxachitls.companion.ui.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.util.Edits;

/**
 * Base activity for all companion activities.
 */
public class Activity extends AppCompatActivity {

  @FunctionalInterface
  interface Action {
    void execute();
  }

  /** Setup the activity. */
  protected void setup(Bundle state, int id, int titleId) {
    setContentView(id);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    setTitle(getString(titleId));
  }

  protected EditText setupEditText(View view, int id, String value, int label, int color,
                                   Action action) {
    EditText edit = (EditText) view.findViewById(id);
    edit.setText(value);
    edit.setHint(getString(label));
    edit.setBackgroundTintList(ColorStateList.valueOf(color));
    edit.setOnKeyListener(new View.OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        // Ignore key downs to not treat things twice.
        if (event.getAction() != KeyEvent.ACTION_UP)
          return false;

        if (keyCode == KeyEvent.KEYCODE_ENTER) {
          action.execute();
          Edits.hideKeyboard(Activity.this, edit);
          return true;
        }

        return false;
      }
    });

    return edit;
  }

  protected TextView setupTextView(View container, int id, int labelId, Action action) {
    setupTextView(container, labelId, action);
    return setupTextView(container, id, action);
  }

  protected TextView setupTextView(View container, int id, Action action) {
    TextView view = (TextView) container.findViewById(id);
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        action.execute();
      }
    });

    return view;
  }

  protected Button setupButton(View container, int id, Action action) {
    Button button = (Button) container.findViewById(id);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        action.execute();
      }
    });

    return button;
  }


}
