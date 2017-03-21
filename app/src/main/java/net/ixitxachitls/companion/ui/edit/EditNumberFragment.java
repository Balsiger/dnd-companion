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

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.util.Edits;

/**
 * Fragment for editing a single number.
 */
public class EditNumberFragment extends EditIntegerFragment {
  private static final String ARG_VALUE = "value";
  private static final String ARG_LABEL = "label";

  private int mValue;
  protected String mLabel;

  public EditNumberFragment() {
    // Required empty public constructor
  }

  public static EditNumberFragment newInstance(int titleId, int labelId, int value, int color) {
    EditNumberFragment fragment = new EditNumberFragment();
    Bundle arguments = arguments(titleId, color, labelId, value);
    fragment.setArguments(arguments);
    return fragment;
  }

  protected static Bundle arguments(int titleId, int color, int labelId, int value) {
    Bundle arguments = EditFragment.arguments(titleId, color);
    arguments.putInt(ARG_LABEL, labelId);
    arguments.putInt(ARG_VALUE, value);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      mValue = getArguments().getInt(ARG_VALUE);
      mLabel = getString(getArguments().getInt(ARG_LABEL));
    } else {
      mValue = 0;
      mLabel = "";
    }
  }

  @Override
  public View onCreateContent(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_edit_number, container, false);
    EditText value = (EditText) view.findViewById(R.id.numberValue);
    value.setText(String.valueOf(mValue));
    value.setHint(mLabel);
    value.setBackgroundTintList(ColorStateList.valueOf(mColor));
    value.setOnKeyListener(new View.OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        // Ignore key downs to not treat things twice.
        if (event.getAction() != KeyEvent.ACTION_UP)
          return false;

        if (keyCode == KeyEvent.KEYCODE_ENTER) {
          edited(Integer.parseInt(value.getText().toString()));
          Edits.hideKeyboard(view, value);
          return true;
        }

        return false;
      }
    });

    Edits.focusWithKeyboard(getActivity(), value);

    return view;
  }
}
