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

package net.ixitxachitls.companion.data;

import com.google.common.base.Strings;

import net.ixitxachitls.companion.net.CompanionPublisher;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

/**
 * A campaign with all its data.
 */
public class Campaign extends StoredEntry<Data.CampaignProto> {

  public static final String TABLE = "campaigns";
  public static final int DEFAULT_CAMPAIGN_ID = -1;

  private String campaignId = "";
  private String world = "";
  private String dm = "";
  private boolean remote = false;
  private boolean published = false;

  private Campaign(long id, String name) {
    super(id, name, DataBaseContentProvider.CAMPAIGNS);
  }

  public String getWorld() {
    return world;
  }

  public String getDm() {
    return dm;
  }

  public String getCampaignId() {
    return campaignId;
  }

  public String getServerId() {
    return campaignId.replaceAll("-\\d+-remote$", "");
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

  public void setWorld(String world) {
    this.world = world;
  }

  public void publish() {
    CompanionPublisher.get().publish(this);
    published = true;
    store();
  }

  @Override
  public Data.CampaignProto toProto() {
    return Data.CampaignProto.newBuilder()
        .setId(campaignId)
        .setName(name)
        .setWorld(world)
        .setRemote(remote)
        .setPublished(published)
        .setDm(dm)
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
    campaign.world = proto.getWorld();
    campaign.remote = proto.getRemote();
    campaign.dm = proto.getDm();
    campaign.published = proto.getPublished();

    return campaign;
  }

  public static Campaign fromRemoteProto(Data.CampaignProto proto) {
    Campaign campaign = fromProto(0, proto);
    campaign.campaignId += "-remote";
    campaign.remote = true;

    return campaign;
  }

  public static Campaign fromProto(String campaignId, Data.CampaignProto proto) {
    return null;
  }

  private static Campaign setData(Campaign campaign, Data.CampaignProto proto) {
    campaign.world = proto.getWorld();
    campaign.remote = proto.getRemote();
    campaign.dm = proto.getDm();
    campaign.published = proto.getPublished();

    return campaign;
  }

  @Override
  public void store() {
    super.store();
    if (Strings.isNullOrEmpty(campaignId)) {
      // Now we finally have an id.
      campaignId = Settings.get().getAppId() + "-" + getId();
      super.store();
    }

    Campaigns.get().ensureAdded(this);
  }
}
