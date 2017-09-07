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

import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.statics.World;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.data.values.Calendar;
import net.ixitxachitls.companion.data.values.CampaignDate;
import net.ixitxachitls.companion.net.CompanionPublisher;
import net.ixitxachitls.companion.net.CompanionSubscriber;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * A campaign with all its data.
 */
public class Campaign extends StoredEntry<Data.CampaignProto> {

  public static final String TYPE = "campaigns";
  public static final String TABLE_LOCAL = TYPE + "_local";
  public static final String TABLE_REMOTE = TYPE + "_remote";
  public static final int DEFAULT_CAMPAIGN_ID = -1;

  private World world;
  private String dm = "";
  private boolean published = false;
  private CampaignDate date;
  private Battle battle;
  private int nextBattleNumber = 0;
  private List<XPAward> xpAwards = new ArrayList<>();

  private Campaign(long id, String name, boolean local) {
    super(id, Settings.get().getAppId() + "-" + id, name, local,
    local ? DataBaseContentProvider.CAMPAIGNS_LOCAL : DataBaseContentProvider.CAMPAIGNS_REMOTE);
    world = Entries.get().getWorlds().get("Generic").get();
    date = new CampaignDate(world.getCalendar());
    dm = Settings.get().getNickname();
    battle = new Battle(this);
  }

  public void awardXp(Character character, int xp) {
    xpAwards.add(new XPAward(character.getCharacterId(), xp));
  }

  public Campaign refresh() {
    return Campaigns.get(isLocal()).get(entryId).or(this);
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
    return getEntryId();
  }

  public List<Character> getCharacters() {
    return Characters.getAllCharacters(getCampaignId());
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

  public boolean isPublished() {
    return published;
  }

  public boolean isOnline() {
    if (!isPublished()) {
      return false;
    }

    if (isLocal()) {
      return CompanionPublisher.get().isOnline();
    } else {
      return CompanionSubscriber.get().isOnline(this);
    }
  }

  public void setWorld(String name) {
    Optional<World> world = Entries.get().getWorlds().get(name);
    if (world.isPresent())
      this.world = world.get();
    else
      // We "know" it should be there.
      this.world = Entries.get().getWorlds().get("Generic").get();

    this.date.setCalendar(world.get().getCalendar());
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
      CompanionPublisher.get().publish(this);
    }
  }

  @Override
  public Data.CampaignProto toProto() {
    return Data.CampaignProto.newBuilder()
        .setId(getEntryId())
        .setName(name)
        .setWorld(world.getName())
        .setPublished(published)
        .setDm(dm)
        .setDate(date.toProto())
        .setBattle(battle.toProto())
        .setNextBattleNumber(nextBattleNumber)
        .build();
  }

  public void unpublish() {
    published = false;
    store();
    CompanionPublisher.get().unpublish(this);
  }

  public static Campaign createNew() {
    return new Campaign(0, "", true);
  }

  public static Campaign createDefault() {
    Campaign campaign = new Campaign(DEFAULT_CAMPAIGN_ID, "Default Campaign", true);
    campaign.setWorld("Generic");
    return campaign;
  }

  public static Campaign fromProto(long id, boolean local, Data.CampaignProto proto) {
    Campaign campaign = new Campaign(id, proto.getName(), local);
    campaign.entryId =
        proto.getId().isEmpty() ? Settings.get().getAppId() + "-" + id : proto.getId();
    campaign.world = Entries.get().getWorlds().get(proto.getWorld())
      .or(Entries.get().getWorlds().get("Generic").get());
    campaign.dm = proto.getDm();
    campaign.published = proto.getPublished();
    campaign.date = CampaignDate.fromProto(campaign.getCalendar(), proto.getDate());
    campaign.battle = Battle.fromProto(campaign, proto.getBattle());
    campaign.nextBattleNumber = proto.getNextBattleNumber();

    return campaign;
  }

  @Override
  public boolean store() {
    boolean changed = super.store();
    if (changed) {
      Campaigns.get(isLocal()).add(this);
      if (isLocal() && published) {
        CompanionPublisher.get().publish(this);
      }
    }

    return changed;
  }

  private static class XPAward {
    private String characterId;
    private int xp;

    private XPAward(String characterId, int xp) {
      this.characterId = characterId;
      this.xp = xp;
    }
  }
}
