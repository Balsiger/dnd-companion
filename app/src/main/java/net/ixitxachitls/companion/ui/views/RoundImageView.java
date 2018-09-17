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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.view.View;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.io.IOException;
import java.net.URL;

/**
 * An image view that shows the image round.
 */
public class RoundImageView extends android.support.v7.widget.AppCompatImageView {

  private final int radius;
  private final int width;
  private final int height;

  public RoundImageView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    TypedArray array = getContext().obtainStyledAttributes(attributes, R.styleable.RoundImageView);
    width = array.getInt(R.styleable.RoundImageView_width_px, Image.MAX);
    height = array.getInt(R.styleable.RoundImageView_height_px, Image.MAX);
    radius = array.getInt(R.styleable.RoundImageView_radius_percents, -1);
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

  public void clearImage() {
    super.setImageResource(R.drawable.ic_person_black_48dp);
  }

  private Bitmap computeBitmap(Drawable drawable) {
    Bitmap bitmap = Bitmap.createBitmap(width > 0 ? width : Image.MAX,
        height > 0 ? height : Image.MAX, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }

  public void setAction(Wrapper.Action action) {
    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        action.execute();
      }
    });
  }

  public void loadImageUrl(String url) {
    if (!url.isEmpty()) {
      new LoadTask().execute(url);
    }
  }

  private class LoadTask extends AsyncTask<String, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(String ... input) {
      try {
        URL url = new URL(input[0]);
        return BitmapFactory.decodeStream(url.openConnection().getInputStream());
      } catch (IOException e) {
        return null;
      }
    }

    protected void onPostExecute(Bitmap bitmap) {
      if (bitmap != null) {
        setImageBitmap(bitmap);
      } else {
        net.ixitxachitls.companion.Status.error("Cannot load user photo!");
      }
    }
  }
}
