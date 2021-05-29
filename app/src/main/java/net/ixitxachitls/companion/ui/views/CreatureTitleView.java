/*
 * Copyright (c) 2017-2018 Peter Balsiger
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
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Creature;
import net.ixitxachitls.companion.data.documents.Images;

import java.util.Optional;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

/**
 * Base title view for creature (characters or monsters).
 */
public abstract class CreatureTitleView<T extends Creature> extends TitleView {

  Optional<T> creature = Optional.empty();

  public CreatureTitleView(Context context, @Nullable AttributeSet attributes,
                           @ColorRes int foregroundColor, @ColorRes int backgroundColor,
                           @DrawableRes int defaultImage) {
    super(context, attributes, foregroundColor, backgroundColor, defaultImage);
  }

  public Optional<T> getCreature() {
    return creature;
  }

  public String getCreatureId() {
    if (creature.isPresent()) {
      return creature.get().getId();
    }

    return "";
  }

  protected String getImagePath() {
    if (creature.isPresent()) {
      return creature.get().getId();
    }

    return "";
  }

  public void update(Images images) {
    if (creature.isPresent()) {

      if (!hasImage()) {
        Optional<Bitmap> bitmap = images.get(getImagePath(), 1);
        if (bitmap.isPresent()) {
          setImageBitmap(bitmap.get());
        } else {
          clearImage(defaultImage);
        }
      }
    }
  }

  @CallSuper
  public void update(T creature) {
    this.creature = Optional.of(creature);

    update();
    update(CompanionApplication.get().images());
  }

  protected String formatSubtitle() {
    if (creature.isPresent()) {
      String subtitle = creature.get().getGender().getName();
      if (creature.get().getRace().isPresent()) {
        subtitle += " " + creature.get().getRace().get();
      }

      Optional<Campaign> campaign =
          CompanionApplication.get(getContext()).campaigns().getOptional(creature.get().getCampaignId());
      if (campaign.isPresent()) {
        subtitle += ", " + campaign.get().getName();
      }

      return subtitle;
    }

    return "";
  }

  protected String formatTitle() {
    if (creature.isPresent()) {
      return creature.get().getName();
    }

    return "...loading...";
  }

  @CallSuper
  @Override
  protected View init(AttributeSet attributes) {
    View view = super.init(attributes);

    container.setBackgroundColor(getContext().getColor(backgroundColor));
    setDefaultImage(defaultImage);

    return view;
  }

  protected void show(T creature) {
    this.creature = Optional.of(creature);
    update();
  }

  protected void update() {
    setTitle(formatTitle());
    setSubtitle(formatSubtitle());
  }
}
