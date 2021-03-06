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

package net.ixitxachitls.companion.ui.views;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.MessageDialog;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * A view to display an icon.
 */
public class IconView extends AppCompatImageView {
  private int bleepBrightColor;
  private int bleepDarkColor;
  private int bleepDuration;

  public IconView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    init(attributes);
  }

  public void setAction(Wrapper.Action action) {
    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        action.execute();
      }
    });
  }

  public void bleep() {
    if (bleepBrightColor == 0 || bleepDarkColor == 0) {
      Toast.makeText(getContext(), "Cannot set bleep without defining colors", Toast.LENGTH_LONG)
          .show();
      return;
    }

    ValueAnimator animator =
        ValueAnimator.ofObject(new ArgbEvaluator(), bleepBrightColor, bleepDarkColor);
    animator.setDuration(bleepDuration);
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        Integer color = (Integer) animation.getAnimatedValue();
        if (color != null) {
          setColorFilter(color);
        }
      }
    });
    animator.start();
  }

  public void setDescription(String title, @LayoutRes int layout) {
    setOnLongClickListener(v -> { MessageDialog.create(getContext())
        .layout(layout).title(title).show(); return true; });
  }

  public void setDescription(String title, String text) {
    setOnLongClickListener(v -> { MessageDialog.create(getContext())
        .message(text).title(title).show(); return true; });
  }

  private void init(AttributeSet attributes) {
    TypedArray array = getContext().obtainStyledAttributes(attributes, R.styleable.IconView);

    bleepBrightColor = array.getColor(R.styleable.IconView_bleep_bright, 0);
    bleepDarkColor = array.getColor(R.styleable.IconView_bleep_dark, 0);
    bleepDuration = array.getInteger(R.styleable.IconView_bleep_duration, 250);
  }
}
