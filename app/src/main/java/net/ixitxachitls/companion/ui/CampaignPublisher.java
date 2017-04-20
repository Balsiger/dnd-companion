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

package net.ixitxachitls.companion.ui;

import android.content.Context;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.data.dynamics.Campaign;

/**
 * Supporter class used to publish or unpublish a campaign.
 */
public class CampaignPublisher {

  @FunctionalInterface
  public interface OKAction {
    public void ok();
  }

  @FunctionalInterface
  public interface CancelAction {
    public void cancel();
  }

  public static OKAction EmptyOkAction = () -> {};
  public static CancelAction EmptyCancelAction = () -> {};

  private CampaignPublisher() {}

  public static void toggle(Context context, Campaign campaign, OKAction okAction,
                            CancelAction cancelAction) {
    if (campaign.isPublished()) {
      unpublish(context, campaign, okAction, cancelAction);
    } else {
      publish(context, campaign, okAction, cancelAction);
    }
  }

  public static void publish(Context context, Campaign campaign, OKAction okAction,
                             CancelAction cancelAction) {
    ConfirmationDialog.create(context)
        .title(context.getString(R.string.main_campaign_publish_title))
        .message(context.getString(R.string.main_campaign_publish_message))
        .yes(() -> { campaign.publish(); okAction.ok(); })
        .no(cancelAction::cancel)
        .show();
  }

  public static void unpublish(Context context, Campaign campaign, OKAction okAction,
                               CancelAction cancelAction) {
    ConfirmationDialog.create(context)
        .title(context.getString(R.string.main_campaign_unpublish_title))
        .message(context.getString(R.string.main_campaign_unpublish_message))
        .yes(() -> { campaign.unpublish(); okAction.ok(); })
        .no(cancelAction::cancel)
        .show();
  }
}
