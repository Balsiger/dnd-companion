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

package net.ixitxachitls.companion.data.dynamics;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.statics.World;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.data.values.Calendar;
import net.ixitxachitls.companion.data.values.CampaignDate;
import net.ixitxachitls.companion.net.CompanionPublisher;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.util.Ids;

import java.util.List;

/**
 * A campaign with all its data.
 */
public class Campaign extends StoredEntry<Data.CampaignProto> {

  public static final String TYPE = "campaigns";
  public static final String TABLE_LOCAL = TYPE + "-local";
  public static final String TABLE_REMOTE = TYPE + "-remote";
  public static final int DEFAULT_CAMPAIGN_ID = -1;

  private String campaignId = "";
  private World world;
  private String dm = "";
  private boolean remote = false;
  private boolean published = false;
  private CampaignDate date;
  private Battle battle;
  private int nextBattleNumber = 0;

  private Campaign(long id, String name) {
    super(id, name, DataBaseContentProvider.CAMPAIGNS);
    world = Entries.get().getWorlds().get("Generic").get();
    date = new CampaignDate(world.getCalendar());
    battle = new Battle(this);
  }

  public String getWorld() {
    return world.getName();
  }

  public String getDm() {
    return dm;
  }

  public Calendar getCalendar() {
    return world.getCalendar();
  }

  public String getCampaignId() {
    return campaignId;
  }

  public List<Character> getCharacters() {
    return Characters.get().getCharacters(getCampaignId());
  }

  public CampaignDate getDate() {
    return date;
  }

  public Battle getBattle() {
    return battle;
  }

  public boolean isDefault() {
    return getId() == DEFAULT_CAMPAIGN_ID;
  }

  public boolean isLocal() {
    return !remote || isDefault();
  }

  public boolean isPublished() {
    return published;
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
    CompanionPublisher.get().publish(this);
    if (!published) {
      published = true;
      store();
    }
  }

  @Override
  public Data.CampaignProto toProto() {
    return Data.CampaignProto.newBuilder()
        .setId(campaignId)
        .setName(name)
        .setWorld(world.getName())
        .setRemote(remote)
        .setPublished(published)
        .setDm(dm)
        .setDate(date.toProto())
        .setBattle(battle.toProto())
        .setNextBattleNumber(nextBattleNumber)
        .build();
  }

  public void unpublish() {
    CompanionPublisher.get().unpublish(this);
    published = false;
    store();
  }

  public static Campaign createNew() {
    return new Campaign(0, "");
  }

  public static Campaign createDefault() {
    Campaign campaign = new Campaign(DEFAULT_CAMPAIGN_ID, "Default Campaign");
    campaign.setWorld("Generic");
    return campaign;
  }

  public static Campaign fromProto(long id, Data.CampaignProto proto) {
    Campaign campaign = new Campaign(id, proto.getName());
    campaign.campaignId =
        proto.getId().isEmpty() ? Settings.get().getAppId() + "-" + id : proto.getId();
    campaign.world = Entries.get().getWorlds().get(proto.getWorld())
      .or(Entries.get().getWorlds().get("Generic").get());
    campaign.remote = proto.getRemote();
    campaign.dm = proto.getDm();
    campaign.published = proto.getPublished();
    campaign.date = CampaignDate.fromProto(campaign.getCalendar(), proto.getDate());
    campaign.battle = Battle.fromProto(campaign, proto.getBattle());
    campaign.nextBattleNumber = proto.getNextBattleNumber();

    return campaign;
  }

  public static Campaign fromRemoteProto(Data.CampaignProto proto) {
    Campaign campaign = fromProto(0, proto);
    campaign.campaignId = Ids.makeLocal(campaign.campaignId);
    campaign.remote = true;

    return campaign;
  }

  @Override
  public boolean store() {
    boolean changed = super.store();
    if (Strings.isNullOrEmpty(campaignId)) {
      // Now we finally have an id.
      campaignId = Settings.get().getAppId() + "-" + getId();
      super.store();
    }

    if (changed) {
      Campaigns.get().ensureAdded(this);
      if (published) {
        CompanionPublisher.get().publish(this);
      }
    }

    return changed;
  }
}
