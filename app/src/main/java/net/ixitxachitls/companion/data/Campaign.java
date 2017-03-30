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

import android.content.Context;

import com.google.common.base.Optional;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.net.CompanionPublisher;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.Random;

/**
 * A campaign with all its data.
 */
public class Campaign extends StoredEntry<Data.CampaignProto> {

  public static final String TABLE = "campaigns";

  private static final int RANDOM_LENGTH = 20;
  private static final Random random = new Random();

  private String campaignId;
  private String world;
  private String dm;
  private boolean remote;
  private boolean published;

  public Campaign(long id, String name) {
    super(id, name, DataBaseContentProvider.CAMPAIGNS);
    campaignId = Settings.get().getAppId() + "-" + id;
  }

  public String getWorld() {
    return world;
  }

  public boolean isDefault() {
    return getId() == 1;
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

  private void makeRemote() {
    remote = true;
  }

  public static Campaign createDefault() {
    Campaign campaign = new Campaign(1, "Default Campaign");
    campaign.setWorld("Generic");
    campaign.makeRemote();
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

  public static Optional<Campaign> load(Context context, long id) {
    try {
      return Optional.of(fromProto(id, Data.CampaignProto.getDefaultInstance().getParserForType()
          .parseFrom(loadBytes(context, id, DataBaseContentProvider.CAMPAIGNS))));
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
      return Optional.absent();
    }
  }
}
