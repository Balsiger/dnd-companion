/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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

package net.ixitxachitls.companion.data.dynamics;

import android.content.Context;
import android.support.annotation.CallSuper;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Data;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.statics.World;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.data.values.CampaignDate;
import net.ixitxachitls.companion.net.CompanionMessenger;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.ui.ConfirmationDialog;

import java.util.Optional;

/**
 * A locally available campaign (the local user is the DM).
 */
public class LocalCampaign extends Campaign {

  public static final String TABLE = Campaign.TABLE + "_local";
  public static final int DEFAULT_CAMPAIGN_ID = -1;

  private boolean published = false;

  protected LocalCampaign(Data data, long id, String name) {
    super(data, id, name, true, DataBaseContentProvider.CAMPAIGNS_LOCAL);
  }

  public static LocalCampaign createDefault(Data data) {
    LocalCampaign campaign = new LocalCampaign(data, DEFAULT_CAMPAIGN_ID, "Default Campaign");
    campaign.setWorld("Generic");
    return campaign;
  }

  public static LocalCampaign createNew(Data data) {
    return new LocalCampaign(data, 0, "");
  }

  @Override
  public boolean isDefault() {
    return getId() == DEFAULT_CAMPAIGN_ID;
  }

  @Override
  public boolean isPublished() {
    return published;
  }

  @Override
  public boolean isOnline() {
    return published;
  }

  @Override
  public String getDm() {
    return data.settings().getNickname();
  }

  public LocalCampaign asLocal() {
    return this;
  }

  public void setWorld(String name) {
    Optional<World> world = Entries.get().getWorlds().get(name);
    if (world.isPresent())
      this.world = world.get();
    else
      // We "know" it should be there.
      this.world = Entries.get().getWorlds().get("Generic").get();
  }

  public void setDate(CampaignDate date) {
    this.date = date;
    store();
  }

  public void publish() {
    if (!published) {
      published = true;
      // Storing will also publish the updated campaign.
      store();
    } else {
      CompanionMessenger.get().send(this);
    }
  }

  public void unpublish() {
    published = false;
    store();
    // We don't unpublish the campaign in the companion server, as the server will be automatically
    // stopped if there are not more message and no published campaigns.
  }

  @Override
  public boolean store() {
    if (super.store()) {
      if (published && !isDefault()) {
        CompanionMessenger.get().send(this);
      }
    }

    return true;
  }

  @Override
  @CallSuper
  public void delete() {
    CompanionMessenger.get().sendDeletion(this);
    super.delete();
  }

  @Override
  public String toString() {
    return super.toString() + "/local";
  }

  public static LocalCampaign fromProto(Data data, long id, Entry.CampaignProto proto) {
    LocalCampaign campaign = new LocalCampaign(data, id, proto.getName());
    campaign.entryId =
        proto.getId().isEmpty() ? data.settings().getAppId() + "-" + id : proto.getId();
    campaign.world = Entries.get().getWorlds().get(proto.getWorld())
        .orElse(Entries.get().getWorlds().get("Generic").get());
    campaign.dm = proto.getDm();
    campaign.published = proto.getPublished();
    campaign.date = CampaignDate.fromProto(proto.getDate());
    campaign.battle = Battle.fromProto(campaign, proto.getBattle());
    campaign.nextBattleNumber = proto.getNextBattleNumber();

    return campaign;
  }

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

  public void publish(Context context, OKAction okAction, CancelAction cancelAction) {
    ConfirmationDialog.create(context)
        .title(context.getString(R.string.main_campaign_publish_title))
        .message(context.getString(R.string.main_campaign_publish_message))
        .yes(() -> { publish(); okAction.ok(); })
        .no(cancelAction::cancel)
        .show();
  }

  public void unpublish(Context context, OKAction okAction, CancelAction cancelAction) {
    ConfirmationDialog.create(context)
        .title(context.getString(R.string.main_campaign_unpublish_title))
        .message(context.getString(R.string.main_campaign_unpublish_message))
        .yes(() -> {
          unpublish();
          okAction.ok();
        })
        .no(cancelAction::cancel)
        .show();
  }

  public void toggle(Context context, OKAction okAction, CancelAction cancelAction) {
    if (isPublished()) {
      unpublish(context, okAction, cancelAction);
    } else {
      publish(context, okAction, cancelAction);
    }
  }

  @Override
  public boolean equals(Object other) {
    return super.equals(other)
        && published == ((LocalCampaign) other).published;
  }

  @Override
  public int hashCode() {
    return super.hashCode() * 31 + (published ? 1 : 0);
  }
}
