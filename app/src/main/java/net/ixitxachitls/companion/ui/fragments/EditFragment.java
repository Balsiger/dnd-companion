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

package net.ixitxachitls.companion.ui.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Optional;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.ui.activities.MainActivity;

/**
 * Base for all the edit fragments for the companion.
 */
public abstract class EditFragment extends DialogFragment {

  private static final String ARG_LAYOUT = "layout";
  private static final String ARG_TITLE = "title";
  private static final String ARG_COLOR = "color";
  private static final int WIDTH = 1000;

  private Optional<SaveAction> save = Optional.absent();
  private Optional<CancelAction> cancel = Optional.absent();

  // The following values are only filled after onCreate().
  protected int layoutId;
  protected String title;
  protected int color;

  // Required empty constructor, don't add anything here.
  protected EditFragment() {}

  @FunctionalInterface
  public interface AttachAction {
    public void attached(EditFragment fragment);
  }

  @FunctionalInterface
  public interface CancelAction {
    public void cancel(EditFragment fragment);
  }

  @FunctionalInterface
  public interface SaveAction {
    public void save(EditFragment fragment);
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int color) {
    Bundle arguments = new Bundle();
    arguments.putInt(ARG_LAYOUT, layoutId);
    arguments.putInt(ARG_TITLE, titleId);
    arguments.putInt(ARG_COLOR, color);

    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      layoutId = getArguments().getInt(ARG_LAYOUT);
      title = getString(getArguments().getInt(ARG_TITLE));
      color = getArguments().getInt(ARG_COLOR);
    } else {
      layoutId = 0;
      title = "";
      color = 0;
    }
  }

  public void display(FragmentManager manager) {
    manager
        .beginTransaction()
        .addToBackStack(null)
        .add(this, null)
        .commit();
    manager.executePendingTransactions();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_edit, container, false);
    TextView titleView = (TextView) view.findViewById(R.id.title);
    titleView.setText(title);
    titleView.setBackgroundColor(getResources().getColor(color, null));

    View content = inflater.inflate(layoutId, container, false);
    createContent(content);
    view.addView(content, 1);

    ViewParent a = view.getParent();
    Fragment b = getParentFragment();
    ViewParent c = content.getParent();

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

    int width = WIDTH;
    if (Resources.getSystem().getDisplayMetrics().widthPixels <= WIDTH) {
      width = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    ViewGroup.LayoutParams params = getView().getLayoutParams();
    params.width = width;
    getView().setLayoutParams(params);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    if (context instanceof AttachAction) {
      ((AttachAction) context).attached(this);
    } else {
      Log.wtf("attach", "context " + context.getClass().getSimpleName()
          + " does not implement InteractionAction");
    }
  }

  public void setCancelListener(CancelAction cancel) {
    this.cancel = Optional.of(cancel);
  }

  public void setSaveListener(SaveAction save) {
    this.save = Optional.of(save);
  }

  protected void save() {
    if (save.isPresent()) {
      save.get().save(this);
    }

    close();
  }

  protected void cancel() {
    if (cancel.isPresent()) {
      cancel.get().cancel(this);
    }

    close();
  }

  private void close() {
    MainActivity activity = (MainActivity) getActivity();
    getFragmentManager().popBackStackImmediate();
    activity.refresh();
  }

  protected abstract void createContent(View view);
}
