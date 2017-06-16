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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.view.View;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.ui.Setup;

/**
 * An image view that shows the image round.
 */
public class RoundImageView extends android.support.v7.widget.AppCompatImageView {

  private final float radius;

  public RoundImageView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    TypedArray array = getContext().obtainStyledAttributes(attributes, R.styleable.RoundImageView);
    radius = array.getDimension(R.styleable.RoundImageView_radius, -1.0f);
  }

  @Override
  public void setImageDrawable(Drawable draw) {
    Bitmap bitmap = computeBitmap(draw);
    RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
    drawable.setAntiAlias(true);
    if (radius <= 0) {
      drawable.setCircular(true);
    } else {
      drawable.setCornerRadius(bitmap.getHeight() * radius / 100);
    }
    super.setImageDrawable(drawable);
  }

  private Bitmap computeBitmap(Drawable drawable) {
    Bitmap bitmap = Bitmap.createBitmap(Math.max(2, drawable.getIntrinsicWidth()),
        Math.max(2, drawable.getIntrinsicHeight()), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }

  public void setAction(Setup.Action action) {
    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        action.execute();
      }
    });
  }
}
