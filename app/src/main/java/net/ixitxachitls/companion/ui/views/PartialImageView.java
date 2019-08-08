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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import net.ixitxachitls.companion.ui.MessageDialog;

import javax.annotation.Nullable;

import androidx.annotation.IntRange;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * View for a partially visible image.
 */
public class PartialImageView extends AppCompatImageView {

  private ClipDrawable clipped;

  public PartialImageView(Context context) {
    this(context, null);
  }

  public PartialImageView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    setOnLongClickListener(this::longClicked);
  }

  @Override
  public void setImageDrawable(Drawable draw) {
    clipped = new ClipDrawable(draw, Gravity.BOTTOM, ClipDrawable.VERTICAL);
    super.setImageDrawable(clipped);
  }

  public void setPartial(@IntRange(from=0,to=10000) int visiblePercentsE2) {
    clipped.setLevel(visiblePercentsE2);
  }

  protected boolean longClicked(View view) {
    // Add relevant things in derivations.
    return false;
  }

  protected void showDescription(String name, String description) {
    new MessageDialog(getContext()).title(name).message(description).show();
  }
}
