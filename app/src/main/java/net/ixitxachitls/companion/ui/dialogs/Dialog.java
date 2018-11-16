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

package net.ixitxachitls.companion.ui.dialogs;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.documents.Campaigns;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.Messages;
import net.ixitxachitls.companion.data.documents.Monsters;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;

/**
 * Base for all the edit fragments for the companion.
 */
public abstract class Dialog extends DialogFragment {

  private static final String ARG_LAYOUT = "layout";
  private static final String ARG_TITLE = "title";
  private static final String ARG_TITLE_STRING = "title-string";
  private static final String ARG_COLOR = "color";
  private static final String ARG_TEXT_COLOR = "text-color";
  private static final int WIDTH = 1500;

  // The following values are only filled after onCreate().
  protected int layoutId;
  protected String title;
  protected int color;
  protected int textColor;

  private View content;

  // Required empty constructor, don't add anything here.
  protected Dialog() {}

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int color) {
    Bundle arguments = new Bundle();
    arguments.putInt(ARG_LAYOUT, layoutId);
    arguments.putInt(ARG_TITLE, titleId);
    arguments.putInt(ARG_COLOR, color);

    return arguments;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int color, @ColorRes int textColor) {
    Bundle arguments = new Bundle();
    arguments.putInt(ARG_LAYOUT, layoutId);
    arguments.putInt(ARG_TITLE, titleId);
    arguments.putInt(ARG_COLOR, color);
    arguments.putInt(ARG_TEXT_COLOR, textColor);

    return arguments;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, String title, @ColorRes int color) {
    Bundle arguments = new Bundle();
    arguments.putInt(ARG_LAYOUT, layoutId);
    arguments.putString(ARG_TITLE_STRING, title);
    arguments.putInt(ARG_COLOR, color);

    return arguments;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, String title, @ColorRes int color,
                                    @ColorRes int textColor) {
    Bundle arguments = new Bundle();
    arguments.putInt(ARG_LAYOUT, layoutId);
    arguments.putString(ARG_TITLE_STRING, title);
    arguments.putInt(ARG_COLOR, color);
    arguments.putInt(ARG_TEXT_COLOR, textColor);

    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      layoutId = getArguments().getInt(ARG_LAYOUT);
      title = getArguments().getString(ARG_TITLE_STRING, "");
      if (title == null || title.isEmpty()) {
        title = getString(getArguments().getInt(ARG_TITLE));
      }
      color = getArguments().getInt(ARG_COLOR, 0);
      textColor = getArguments().getInt(ARG_TEXT_COLOR, 0);
    } else {
      layoutId = 0;
      title = "";
      color = 0;
      textColor = 0;
    }
  }

  public void display() {
    CompanionFragments.get().display(this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_edit, container, false);
    TextView titleView = view.findViewById(R.id.title);
    titleView.setText(title);
    titleView.setBackgroundColor(getResources().getColor(color, null));
    if (textColor != 0) {
      titleView.setTextColor(getResources().getColor(textColor, null));
    }

    content = inflater.inflate(layoutId, container, false);
    createContent(content);
    view.addView(content);

    return view;
  }

  @Override
  public void onStart() {
    super.onStart();

    if (content != null && content.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) {
      int width = Resources.getSystem().getDisplayMetrics().widthPixels;
      if (width < WIDTH) {
        width = ViewGroup.LayoutParams.MATCH_PARENT;
      }

      getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
  }

  @CallSuper
  protected void save() {
    close();
  }

  private void close() {
    dismiss();
  }

  protected CompanionApplication application() {
    return CompanionApplication.get(getContext());
  }

  protected CompanionContext context() {
    return application().context();
  }

  protected Campaigns campaigns() {
    return application().campaigns();
  }

  protected Messages messages() {
    return application().messages();
  }

  protected Monsters monsters() {
    return application().monsters();
  }

  protected Characters characters() {
    return application().characters();
  }

  protected User me() {
    return application().me();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
  }

  protected abstract void createContent(View view);
}
