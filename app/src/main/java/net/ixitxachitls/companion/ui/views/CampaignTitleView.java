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
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Message;
import net.ixitxachitls.companion.data.documents.User;

import java.util.List;
import java.util.Optional;

import androidx.annotation.CallSuper;

/**
 * View for a campaign title.
 */
public class CampaignTitleView extends TitleView {

  private Optional<Campaign> campaign = Optional.empty();

  public CampaignTitleView(Context context) {
    super(context);
  }

  public CampaignTitleView(Context context, AttributeSet attributes) {
    super(context, attributes);
  }

  public void setCampaign(Campaign campaign) {
    this.campaign = Optional.of(campaign);
    campaign.getContext().messages().readMessages(campaign.getId());
    update();
  }

  public void setCampaign(User dm) {
    update();
  }

  public void update() {
    // Messages.
    updateMessages();

    if (campaign.isPresent()) {
      setTitle(campaign.get().getName());
      setSubtitle(campaign.get().getWorldTemplate().toString() + ", " + campaign.get().getDm());

      updateIcons();

      if (!hasImage()) {
        Optional<Bitmap> bitmap = CompanionApplication.get().images().get(campaign.get().getId(), 1);
        if (bitmap.isPresent()) {
          setImageBitmap(bitmap.get());
        } else {
          clearImage(R.drawable.image_filter_hdr);
        }
      }
    }
  }

  @Override
  protected MessageView createMessageIcon(Message message) {
    return new CampaignMessageView(getContext(), campaign.get(), message);
  }

  @Override
  protected List<Integer> iconDrawableResources() {
    List<Integer> resources = super.iconDrawableResources();
    if (campaign.get().amDM()) {
      resources.add(R.drawable.noun_eye_of_providence_24673);
    }

    return resources;
  }

  @Override
  @CallSuper
  protected View init(AttributeSet attributes) {
    View view = super.init(attributes);

    container.setBackgroundColor(getContext().getColor(R.color.campaign));
    setDefaultImage(R.drawable.image_filter_hdr);
    return view;
  }

  @Override
  protected List<Message> messageIcons() {
    List<Message> messages = super.messageIcons();
    if (campaign.isPresent() && campaign.get().amDM()) {
      messages.addAll(CompanionApplication.get().messages().getMessages(campaign.get().getId()));
    }

    return messages;
  }
}
