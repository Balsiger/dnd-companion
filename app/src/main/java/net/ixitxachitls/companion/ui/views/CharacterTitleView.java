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
import android.graphics.Bitmap;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Images;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;

import java.util.Optional;

/**
 * A tile view for characters.
 */
public class CharacterTitleView extends TitleView {

  Optional<Character> character = Optional.empty();

  public CharacterTitleView(Context context) {
    super(context);
  }

  public CharacterTitleView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);
  }

  @Override
  @CallSuper
  protected View init(AttributeSet attributes) {
    View view = super.init(attributes);

    view.setBackgroundColor(getContext().getColor(R.color.character));
    setDefaultImage(R.drawable.noun_viking_30736);
    return view;
  }

  public void update(Character character) {
    this.character = Optional.of(character);

    setTitle(character.getName());
    setSubtitle(subtitle(character));
    setAction(() -> {
      CompanionFragments.get().showCharacter(character, Optional.of(this));
    });

    clearIcons();
    if (character.amPlayer()) {
      addIcon(R.drawable.noun_puppet_52120);
    }
    if (character.amDM()) {
      addIcon(R.drawable.noun_eye_of_providence_24673);
    }

    update(CompanionApplication.get().images());
  }

  public void update(Images images) {
    if (character.isPresent()) {
      Optional<Bitmap> bitmap = images.get(character.get().getId());
      if (bitmap.isPresent()) {
        setImageBitmap(bitmap.get());
      } else {
        clearImage(R.drawable.noun_viking_30736);
      }
    }
  }

  private String subtitle(Character character) {
    String subtitle = character.getGender().getName();
    if (character.getRace().isPresent()) {
      subtitle += " " + character.getRace().get();
    }

    Optional<Campaign> campaign =
        CompanionApplication.get(getContext()).campaigns().get(character.getCampaignId());
    if (campaign.isPresent()) {
      subtitle += ", " + campaign.get().getName();
    }

    return subtitle;
  }
}
