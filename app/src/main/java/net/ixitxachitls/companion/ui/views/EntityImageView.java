/*
 * Copyright (c) 2017-2019 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

/**
 * A view to display an entity image, with a caption and a number.
 */
public class EntityImageView extends LinearLayout {

  private final Wrapper<CloudImageView> image;
  private final TextWrapper<TextView> name;
  private final TextWrapper<TextView> number;

  private String path = "";
  private @DrawableRes int placeholder = R.drawable.close_circle;

  public EntityImageView(Context context) {
    this(context, null);
  }

  public EntityImageView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_entity_image, this, false);

    image = Wrapper.wrap(view, R.id.image);
    name = TextWrapper.wrap(view, R.id.name);
    number = TextWrapper.wrap(view, R.id.number);

    addView(view);
  }

  public void clearName() {
    this.name.text("(no miniatures found)");
    this.image.get().setImage(placeholder);
  }

  public void clearNumber() {
    this.number.text("");
  }

  public void setName(String id, String name) {
    this.name.text(name);
    this.image.get().setImage(path + id, placeholder);
  }

  public void setNumber(int number, int max, int owned) {
    this.number.text(number + " of " + max + " (" + owned + " owned)");
  }

  public void setup(String imagePath, @DrawableRes int imagePlaceholder) {
    this.path = imagePath;
    this.placeholder = imagePlaceholder;
  }
}
