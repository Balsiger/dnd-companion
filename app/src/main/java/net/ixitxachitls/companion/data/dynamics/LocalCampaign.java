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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.statics.World;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.data.values.CampaignDate;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;

import java.util.Optional;

/**
 * A locally available campaign (the local user is the DM).
 */
public class LocalCampaign extends Campaign {

  public static final String TABLE = Campaign.TABLE + "_local";
  public static final int DEFAULT_CAMPAIGN_ID = -1;

  private boolean published = false;

  protected LocalCampaign(CompanionContext companionContext, long id, String name) {
    super(companionContext, id, name, true, DataBaseContentProvider.CAMPAIGNS_LOCAL);
  }

  public static LocalCampaign createDefault(CompanionContext companionContext) {
    LocalCampaign campaign = new LocalCampaign(companionContext, DEFAULT_CAMPAIGN_ID, "Default Campaign");
    campaign.setWorld("Generic");
    return campaign;
  }

  public static LocalCampaign createNew(CompanionContext companionContext) {
    return new LocalCampaign(companionContext, 0, "");
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
    return companionContext.settings().getNickname();
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
    Multimap<String, TimedCondition> expired = expiredConditions(this.date, date);
    this.date = date;
    store();

    for (String id : expired.keySet()) {
      for (TimedCondition condition : expired.get(id)) {
        context.histories().expired(condition.getName(), getDate(), id, getCampaignId());
      }
    }
  }

  private Multimap<String, TimedCondition> expiredConditions(CampaignDate last,
                                                             CampaignDate current) {
    Multimap<String, TimedCondition> expired = HashMultimap.create();
    for (Character character : getCharacters()) {
      for (TimedCondition condition : character.getAffectedConditions()) {
        if (!condition.endedBefore(last) && condition.endedBefore(current)) {
          expired.put(character.getCharacterId(), condition);
        }
      }
    }

    return expired;
  }

  public void publish() {
    if (!published) {
      published = true;
      // Storing will also publish the updated campaign.
      store();
    } else {
      context.messenger().send(this);
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
        context.messenger().send(this);
      }
    }

    return true;
  }

  @Override
  @CallSuper
  public void delete() {
    context.messenger().sendDeletion(this);
    super.delete();
  }

  @Override
  public String toString() {
    return super.toString() + "/local";
  }

  public static LocalCampaign fromProto(CompanionContext companionContext, long id, Entry.CampaignProto proto) {
    LocalCampaign campaign = new LocalCampaign(companionContext, id, proto.getName());
    campaign.entryId =
        proto.getId().isEmpty() ? companionContext.settings().getAppId() + "-" + id : proto.getId();
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
    ConfirmationPrompt.create(context)
        .title(context.getString(R.string.main_campaign_publish_title))
        .message(context.getString(R.string.main_campaign_publish_message))
        .yes(() -> { publish(); okAction.ok(); })
        .no(cancelAction::cancel)
        .show();
  }

  public void unpublish(Context context, OKAction okAction, CancelAction cancelAction) {
    ConfirmationPrompt.create(context)
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
