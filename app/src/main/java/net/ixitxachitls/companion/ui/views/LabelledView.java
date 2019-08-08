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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.MessageDialog;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Strings;

import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;

/**
 * A view with a line and label.
 */
public class LabelledView<T extends LabelledView, S extends View> extends LinearLayout {

  // UI elements.
  protected Wrapper<View> line;
  protected TextWrapper<TextView> label;
  private Wrapper<ViewGroup> container;
  private S view;
  private @ColorInt int lineColor;
  private @ColorInt int labelColor;

  public LabelledView(Context context, String label, String description, @ColorRes int labelColor,
                      @ColorRes int lineColor) {
    super(context);

    setup(label, description, context.getColor(labelColor), context.getColor(lineColor), null);
  }

  public LabelledView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    TypedArray styles =
        getContext().obtainStyledAttributes(attributes, R.styleable.LabelledView);

    setup(styles.getString(R.styleable.LabelledView_labelText),
        styles.getString(R.styleable.LabelledView_descriptionText),
        styles.getColor(R.styleable.LabelledView_labelColor,
            getContext().getColor(R.color.colorPrimary)),
        styles.getColor(R.styleable.LabelledView_lineColor,
            getContext().getColor(R.color.colorPrimary)), attributes);

    styles.recycle();
  }

  public S getView() {
    return view;
  }

  public T description(String title, String description) {
    if (description != null && !description.isEmpty()) {
      label.onLongClick(() -> showDescription(title, description));
    }

    return (T) this;
  }

  public T disabled() {
    return enabled(false);
  }

  public T enabled() {
    return enabled(true);
  }

  public T enabled(boolean enabled) {
    if (enabled) {
      line.backgroundColorValue(lineColor);
      label.textColorValue(labelColor);
    } else {
      line.backgroundColor(R.color.labelled_line_disabled);
      label.textColor(R.color.labelled_label_disabled);
    }

    return (T) this;
  }

  public T error(List<String> errors) {
    if (errors.isEmpty()) {
      line.backgroundColorValue(lineColor);
      label.textColorValue(labelColor);
      container.clearClick();
      label.clearClick();
    } else {
      line.backgroundColor(R.color.error);
      label.textColor(R.color.error);
      container.onClick(() -> showErrors(errors));
      label.onClick(() -> showErrors(errors));
    }

    return (T) this;
  }

  public T gone() {
    container.gone();

    return (T) this;
  }

  public T label(String label) {
    this.label.text(label);

    return (T) this;
  }

  public T labelColor(@ColorRes int color) {
    label.textColor(color);

    return (T) this;
  }

  public T lineColor(@ColorRes int color) {
    line.backgroundColor(color);

    return (T) this;
  }

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();

    while (getChildCount() > 0) {
      // The view has children from xml, move them into the container.
      View child = getChildAt(getChildCount() - 1);
      removeViewAt(getChildCount() - 1);
      container.get().addView(child, 0);
    }

    addView(container.get());
  }

  public T view(S view) {
    this.view = view;
    container.get().addView(view, 0);

    return (T) this;
  }

  private void setup(String labelText, String description, @ColorInt int labelColor,
                     @ColorInt int lineColor, @Nullable AttributeSet attributes) {
    container = Wrapper.wrap((ViewGroup)
        LayoutInflater.from(getContext()).inflate(R.layout.view_labelled, null, false));
    container.get().setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));

    this.lineColor = lineColor;
    line = Wrapper.wrap(container.get(), R.id.line).backgroundColorValue(lineColor);
    this.labelColor = labelColor;
    label = TextWrapper.wrap(container.get(), R.id.label).text(labelText).textColorValue(labelColor);

    description(labelText, description);
  }

  private void showDescription(String name, String description) {
    new MessageDialog(getContext()).title(name).message(description).show();
  }

  private void showErrors(List<String> errors) {
    MessageDialog.create(getContext()).title("Invalid Data")
        .message(Strings.NEWLINE_JOINER.join(errors)).show();
  }
}
