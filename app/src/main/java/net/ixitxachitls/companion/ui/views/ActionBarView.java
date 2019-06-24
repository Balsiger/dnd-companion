/*
 * Copyright (c) 2017-2019 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Tabletop Companion.
 *
 * The Tabletop Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Tabletop Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.views.wrappers.AbstractWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * View for actionsView (and progress).
 */
public class ActionBarView extends LinearLayout {

  private final List<String> progressGroups = new ArrayList<>();
  private final LinearLayout progress;
  private final ProgressBar progressBar;
  private final TextWrapper<TextView> progressText;
  private final LinearLayout actionsView;

  public ActionBarView(Context context) {
    this(context, null, 0, 0);
  }

  public ActionBarView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0, 0);
  }

  public ActionBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  public ActionBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_action_bar, null, false);
    view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));

    progress = view.findViewById(R.id.progress);
    progressBar = view.findViewById(R.id.progressBar);
    progressBar.setIndeterminate(true);
    progressText = TextWrapper.wrap(view, R.id.progressText);
    actionsView = view.findViewById(R.id.actions);

    addView(view);
  }

  public Action addAction(@DrawableRes int icon, String title, String description) {
    Action action = new Action(icon, title, description);
    actionsView.addView(action.getButton().get());

    return action;
  }

  public ActionGroup addActionGroup(@DrawableRes int icon, String title, String description) {
    ActionGroup group = new ActionGroup(icon, title, description);
    actionsView.addView(group.getButton().get());
    actionsView.addView(group.getContainer());

    return group;
  }

  public void clearActions() {
    actionsView.removeAllViews();
    progressGroups.clear();
    refreshProgress();
  }

  public void finishLoading(String text) {
    progressGroups.remove(text);
    refreshProgress();
  }

  public void startLoading(String text) {
    progressGroups.add(text);
    refreshProgress();
  }

  private void refreshProgress() {
    if (progressGroups.isEmpty()) {
      progressText.text("");
      progress.setVisibility(GONE);
    } else {
      progressText.text("loading " + Strings.COMMA_JOINER.join(progressGroups) + "...");
      progress.setVisibility(VISIBLE);
    }
  }

  public class Action {
    private final Wrapper<ImageButton> button;
    private final String title;
    private Optional<Wrapper.Action> action = Optional.empty();

    private Action(@DrawableRes int icon, String title, String description) {
      this.title = title;
      button = createButton(icon, title, description);
    }

    protected Wrapper<ImageButton> getButton() {
      return button;
    }

    public Action color(@ColorRes int color) {
      button.tint(color);

      return this;
    }

    public Action hide() {
      button.gone();
      return this;
    }

    public Action noClick() {
      button.removeClick();
      return this;
    }

    public Action onClick(Wrapper.Action action) {
      this.action = Optional.of(action);
      return this;
    }

    public Action show(boolean show) {
      if (show) {
        return show();
      } else {
        return hide();
      }
    }

    public Action show() {
      button.visible();
      return this;
    }

    public Action uncolor() {
      button.tint(R.color.action);

      return this;
    }

    private void clicked() {
      CompanionApplication.get().logEvent(title, title, "action");
      if (action.isPresent()) {
        action.get().execute();
      }
    }

    private Wrapper<ImageButton> createButton(int icon, String title, String description) {
      Wrapper<ImageButton> button = Wrapper.wrap(new ImageButton(getContext()));
      button.get().setImageDrawable(getContext().getDrawable(icon));
      button.description(title, description).padding(AbstractWrapper.Padding.ALL, 10);
      button.get().setBackgroundColor(getContext().getColor(R.color.black));
      button.get().setForeground(getContext().getDrawable(R.drawable.ripple_dark));
      button.tint(R.color.action);
      button.get().setScaleType(ImageView.ScaleType.FIT_CENTER);
      button.get().setAdjustViewBounds(true);
      button.onClick(this::clicked);
      return button;
    }
  }

  public class ActionGroup extends Action {
    private final LinearLayout container;

    private ActionGroup(@DrawableRes int icon, String title, String description) {
      super(icon, title, description);

      container = new LinearLayout(getContext());
      container.setOrientation(HORIZONTAL);
    }

    protected LinearLayout getContainer() {
      return container;
    }

    public Action addAction(@DrawableRes int icon, String title, String description) {
      Action action = new Action(icon, title, description);
      container.addView(action.getButton().get());

      return action;
    }

    public ActionGroup expand() {
      TransitionManager.beginDelayedTransition(container);
      getButton().disabled().tint(R.color.actionDisabled);
      setLayoutWidth(container, LayoutParams.WRAP_CONTENT);

      return this;
    }

    @Override
    public ActionGroup onClick(Wrapper.Action action) {
      super.onClick(action);
      return this;
    }

    public ActionGroup shrink() {
      TransitionManager.beginDelayedTransition(container);
      getButton().enabled().tint(R.color.action);
      setLayoutWidth(container, 0);

      return this;
    }

    private void setLayoutWidth(LinearLayout layout, int width) {
      ViewGroup.LayoutParams params = layout.getLayoutParams();
      params.width = width;
      layout.setLayoutParams(params);
    }
  }
}
