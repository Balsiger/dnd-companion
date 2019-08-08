/*
 * Copyright (c) 2017-2018 Peter Balsiger
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

package net.ixitxachitls.companion.ui.dialogs;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ImageView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

/**
 * A dialog to adjust a number.
 */
public class NumberAdjustDialog extends Dialog {

  private static final String ARG_LABEL = "label";
  private static final String ARG_DESCRIPTION = "description";
  private LabelledEditTextView input;
  private Action action;
  @FunctionalInterface
  public interface Action {
    void adjust(int value);
  }

  public NumberAdjustDialog setAdjustAction(Action action) {
    this.action = action;

    return this;
  }

  private void add() {
    if (action != null && !input.getText().isEmpty()) {
      try {
        action.adjust(Integer.parseInt(input.getText()));
      } catch (NumberFormatException e) {
        Status.toast("Invalid number ignored");
      }
    }

    save();
  }

  @Override
  protected void createContent(View view) {
    input = view.findViewById(R.id.input);
    input.lineColor(color)
        .description(getArguments().getString(ARG_LABEL), getArguments().getString(ARG_DESCRIPTION))
        .label(getArguments().getString(ARG_LABEL))
        .type(InputType.TYPE_CLASS_NUMBER);

    Wrapper.<ImageView>wrap(view, R.id.add)
        .description("Add", "Add the value.")
        .onClick(this::add);
    Wrapper.<ImageView>wrap(view, R.id.subtract)
        .description("Subtract", "Subtract the value.")
        .onClick(this::subtract);
  }

  private void subtract() {
    if (action != null && !input.getText().isEmpty()) {
      try {
        action.adjust(-Integer.parseInt(input.getText()));
      } catch (NumberFormatException e) {
        Status.toast("Invalid number ignored");
      }
    }

    save();
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String label , String description) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_LABEL, label);
    arguments.putString(ARG_DESCRIPTION, description);
    return arguments;
  }

  public static NumberAdjustDialog newInstance(@StringRes int title, @ColorRes int color,
                                               String label, String description) {
    NumberAdjustDialog dialog = new NumberAdjustDialog();
    dialog.setArguments(arguments(R.layout.dialog_number_adjust, title, color, label, description));
    return dialog;
  }
}
