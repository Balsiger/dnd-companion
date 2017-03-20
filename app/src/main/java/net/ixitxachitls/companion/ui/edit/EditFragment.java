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

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitachitls.companion.R;

/**
 * Fragment for editing values.
 */
public abstract class EditFragment extends DialogFragment {

  private static final String ARG_TITLE = "title";
  private static final String ARG_COLOR = "color";

  protected String mTitle;
  protected int mColor;

  protected EditFragment() {
  }

  protected static Bundle arguments(int titleId, int color) {
    Bundle arguments = new Bundle();
    arguments.putInt(ARG_TITLE, titleId);
    arguments.putInt(ARG_COLOR, color);

    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      mTitle = getString(getArguments().getInt(ARG_TITLE));
      mColor = getArguments().getInt(ARG_COLOR);
    } else {
      mTitle = "";
      mColor = 0;
    }
  }

  public void display(FragmentManager manager) {
    FragmentTransaction transaction = manager.beginTransaction();
    // mTitle is not yet filled.
    String name = "edit-" + getClass().getSimpleName();
    show(transaction, name);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_edit, container, false);
    TextView title = (TextView) view.findViewById(R.id.title);
    title.setText(mTitle);
    title.setBackgroundColor(mColor);

    View content = onCreateContent(inflater, container, savedInstanceState);
    content.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));
    view.addView(content);

    return view;
  }

  protected void close() {
    FragmentTransaction transaction = getFragmentManager().beginTransaction();
    transaction.detach(this);
    transaction.commit();
  }


  protected abstract View onCreateContent(LayoutInflater inflater, ViewGroup container,
                                          Bundle savedInstanceState);
}
