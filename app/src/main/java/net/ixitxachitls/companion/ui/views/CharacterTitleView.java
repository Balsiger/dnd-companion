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
import android.util.AttributeSet;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Message;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;

import java.util.List;
import java.util.Optional;

import androidx.annotation.Nullable;

/**
 * A tile view for characters.
 */
public class CharacterTitleView extends CreatureTitleView<Character> {

  public CharacterTitleView(Context context) {
    this(context, null);
  }

  public CharacterTitleView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes, R.color.characterLight, R.color.character,
        R.drawable.noun_viking_30736);
  }

  @Override
  public void update(Character character) {
    super.update(character);

    CompanionApplication.get().conditions().readConditions(character.getId());
    setAction(() -> CompanionFragments.get().showCharacter(character, Optional.of(this)));

    updateIcons();
  }

  @Override
  public void update() {
    super.update();

    // Messages.
    updateMessages();
  }

  @Override
  protected MessageView createMessageIcon(Message message) {
    return new CharacterMessageView(getContext(), creature.get(), message);
  }

  @Override
  protected List<Integer> iconDrawableResources() {
    List<Integer> resources = super.iconDrawableResources();

    if (creature.isPresent()) {
      if (creature.get().amPlayer()) {
        resources.add(R.drawable.noun_puppet_52120);
      }

      Campaign campaign = creature.get().getCampaign();
      if (campaign.getBattle().isOngoing()
          && campaign.getBattle().includes(creature.get().getId())) {
        resources.add(R.drawable.ic_sword_cross_black_18dp);
      }
    }

    return resources;
  }

  @Override
  protected List<Message> messageIcons() {
    List<Message> messages = super.messageIcons();
    if (creature.isPresent()) {
      messages.addAll(CompanionApplication.get().messages().getMessages(creature.get().getId()));
    }

    return messages;
  }
}
