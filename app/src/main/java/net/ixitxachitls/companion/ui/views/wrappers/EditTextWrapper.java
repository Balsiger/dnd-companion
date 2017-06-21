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

package net.ixitxachitls.companion.ui.views.wrappers;

import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.EditText;

/**
 * Wrapper for edit texts.
 */
public final class EditTextWrapper<V extends EditText>
    extends AbstractTextWrapper<V, EditTextWrapper<V>> {

  private EditTextWrapper(View parent, @IdRes int id) {
    super(parent, id);
  }

  public static <V extends EditText> EditTextWrapper<V> wrap(View parent, @IdRes int id) {
    return new EditTextWrapper<>(parent, id);
  }

  public EditTextWrapper<V> label(@StringRes int label) {
    return label(view.getContext().getString(label));
  }

  public EditTextWrapper<V> label(String label) {
    view.setHint(label);

    return this;
  }
}
