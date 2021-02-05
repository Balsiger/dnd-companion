/*
 * Copyright (c) 2017-2019 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.MessageDialog;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

import androidx.annotation.DrawableRes;

/**
 * Image view for an image from cloud storage.
 */
public class CloudImageView extends LinearLayout {
  private static final String PATH = "images/";
  private final Wrapper<ImageView> image;
  private final ProgressBar progressBar;
  private Optional<String> imageId = Optional.empty();
  private boolean reload = false;

  public CloudImageView(Context context) {
    this(context, null, 0);
  }

  public CloudImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CloudImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_cloud_image, null, false);
    view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));

    image = Wrapper.<ImageView>wrap(view, R.id.image).onDoubleTap(this::reload)
        .onClick(this::enlarged);
    progressBar = view.findViewById(R.id.progress);

    addView(view);
  }

  public void setImage(@DrawableRes int drawable) {
    progressBar.setIndeterminate(true);
    image.get().setImageDrawable(getContext().getDrawable(drawable));
  }

  public void setImage(String id, @DrawableRes int drawable) {
    setImage(id, drawable, 7 * 24);
  }

  public void setImage(String id, @DrawableRes int drawable, int maxAgeHours) {
    this.imageId = Optional.of(id);

    if (drawable != 0) {
      setImage(drawable);
    }
    progressBar.setVisibility(VISIBLE);
    CompanionApplication.get().context().images().get(PATH + normalize(id), maxAgeHours,
        image -> {
          progressBar.setVisibility(GONE);
          if (image.isPresent()) {
            this.image.get().setImageDrawable(new BitmapDrawable(getResources(), image.get()));
          }
        });
  }

  private String normalize(String id) {
    return id.toLowerCase();
  }

  private void reload() {
    if (imageId.isPresent()) {
      setImage(imageId.get(), 0, 0);
    }
  }

  private void enlarged() {
    if (imageId.isPresent()) {
      CompanionApplication.get().context().images().get(PATH + normalize(imageId.get()), 7 * 24,
          image -> {
            if (image.isPresent()) {
              MessageDialog.create(getContext())
                  .image(image.get())
                  .show();
            }
          });
    }
  }
}
