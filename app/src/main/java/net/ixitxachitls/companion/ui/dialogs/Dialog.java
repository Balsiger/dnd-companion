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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.documents.Adventures;
import net.ixitxachitls.companion.data.documents.Campaigns;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.Messages;
import net.ixitxachitls.companion.data.documents.Monsters;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.activities.MainActivity;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

/**
 * Base for all the editAction fragments for the companion.
 */
public abstract class Dialog<D extends Dialog, T> extends DialogFragment {

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
  private Optional<Action<T>> onSaved = Optional.empty();
  private View content;

  // UI elements.
  private TextWrapper<TextView> titleView;

  // Required empty constructor, don't add anything here.
  protected Dialog() {}

  @FunctionalInterface
  public interface Action<T> {
    void execute(T value);
  }

  protected T getValue() {
    return null;
  }

  public void setTitle(String title) {
    this.title = title;
    titleView.text(title);
  }

  public void display() {
    CompanionFragments.get().display(this);
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

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    LinearLayout view = (LinearLayout) inflater.inflate(R.layout.dialog, container, false);
    Wrapper.wrap(view, R.id.close).onClick(this::close);
    titleView = TextWrapper.wrap(view, R.id.title).text(title);
    Wrapper.wrap(view, R.id.titlebar).backgroundColor(color);
    if (textColor != 0) {
      titleView.textColor(textColor);
    }

    content = inflater.inflate(layoutId, container, false);
    createContent(content);
    view.addView(content);

    return view;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
  }

  @SuppressWarnings("unchecked")
  public D onSaved(Action<T> action) {
    onSaved = Optional.of(action);

    return (D)this;
  }

  @Override
  public void onStart() {
    ((MainActivity) getActivity()).logDialogEvent(getClass().getSimpleName());
    super.onStart();

    if (content != null && content.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) {
      int width = Resources.getSystem().getDisplayMetrics().widthPixels;
      if (width < WIDTH) {
        width = ViewGroup.LayoutParams.MATCH_PARENT;
      }

      getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    // Ensure that when the soft keyboard comes up, it will scroll the views instead of simply
    // moving everything up.
    getDialog().getWindow()
        .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
            | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
  }

  protected Adventures adventures() {
    return application().adventures();
  }

  protected CompanionApplication application() {
    return CompanionApplication.get(getContext());
  }

  protected Campaigns campaigns() {
    return application().campaigns();
  }

  protected Characters characters() {
    return application().characters();
  }

  protected void close() {
    dismiss();
  }

  protected CompanionContext context() {
    return application().context();
  }

  protected abstract void createContent(View view);

  protected User me() {
    return application().me();
  }

  protected Messages messages() {
    return application().messages();
  }

  protected Monsters monsters() {
    return application().monsters();
  }

  @CallSuper
  protected void save() {
    close();

    if (onSaved.isPresent()) {
      onSaved.get().execute(getValue());
    }
  }

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
}
