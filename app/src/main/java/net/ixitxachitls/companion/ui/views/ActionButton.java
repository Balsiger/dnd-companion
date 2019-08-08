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

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.ixitxachitls.companion.R;

/**
 * Special floating action button that allows to pulse the icon.
 */
public class ActionButton extends FloatingActionButton {
  private int pulseBrightColor;
  private int pulseDarkColor;
  private int pulseDuration;
  private ValueAnimator colorAnimator;
  private ObjectAnimator sizeAnimator;

  public ActionButton(Context context, AttributeSet attributes) {
    super(context, attributes);

    init(attributes);
  }

  public ActionButton(Context context, AttributeSet attributes, int defStyleAttr) {
    super(context, attributes, defStyleAttr);

    init(attributes);
  }

  public void pulse(boolean start) {
    if (true) return;
    /*
    if (pulseBrightColor == 0 || pulseDarkColor == 0) {
      Toast.makeText(getContext(), "Cannot set bleep without defining colors", Toast.LENGTH_LONG)
          .show();
      return;
    }

    if (start) {
      if (colorAnimator != null && sizeAnimator != null
          && colorAnimator.isRunning() && sizeAnimator.isRunning()) {
        return;
      }

      colorAnimator =
          ValueAnimator.ofObject(new ArgbEvaluator(), pulseDarkColor, pulseBrightColor);
      colorAnimator.setDuration(pulseDuration);
      colorAnimator.setRepeatCount(ObjectAnimator.INFINITE);
      colorAnimator.setRepeatMode(ValueAnimator.REVERSE);
      colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
          Integer color = (Integer) animation.getAnimatedValue();
          if (color != null) {
            setBackgroundTintList(ColorStateList.valueOf(color));
          }
        }
      });
      colorAnimator.start();

      sizeAnimator = ObjectAnimator.ofPropertyValuesHolder(this,
          PropertyValuesHolder.ofFloat("scaleX", 1.2f),
          PropertyValuesHolder.ofFloat("scaleY", 1.2f));
      sizeAnimator.setDuration(pulseDuration);
      sizeAnimator.setRepeatCount(ObjectAnimator.INFINITE);
      sizeAnimator.setRepeatMode(ValueAnimator.REVERSE);
      sizeAnimator.start();
    } else {
      if (colorAnimator != null && sizeAnimator != null) {
        colorAnimator.end();
        sizeAnimator.end();
      }
    }
    */
  }

  private void init(AttributeSet attributes) {
    TypedArray array = getContext().obtainStyledAttributes(attributes, R.styleable.ActionButton);

    pulseBrightColor = array.getColor(R.styleable.ActionButton_pulse_bright, 0);
    pulseDarkColor = array.getColor(R.styleable.ActionButton_pulse_dark, 0);
    pulseDuration = array.getInteger(R.styleable.ActionButton_pulse_duration, 1000);
  }
}
