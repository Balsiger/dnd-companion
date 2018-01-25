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

import android.arch.lifecycle.LiveData;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

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
import net.ixitxachitls.companion.util.Misc;

import java.util.List;

/**
 * A campaign with all its data.
 */
public class Campaign extends StoredEntry<Data.CampaignProto> {

  // Constants.
  public static final String TYPE = "campaign";
  public static final String TABLE = "campaigns";
  public static final String TABLE_LOCAL = TABLE + "_local";
  public static final String TABLE_REMOTE = TABLE + "_remote";
  public static final int DEFAULT_CAMPAIGN_ID = -1;

  // External Data.
  private LiveData<List<Character>> characters;

  // Values.
  private World world;
  private String dm = "";
  private boolean published = false;
  private CampaignDate date;
  private Battle battle;
  private int nextBattleNumber = 0;

  private Campaign(long id, String name, boolean local) {
    super(id, TYPE, name, local,
        local ? DataBaseContentProvider.CAMPAIGNS_LOCAL : DataBaseContentProvider.CAMPAIGNS_REMOTE);

    world = Entries.get().getWorlds().get("Generic").get();
    date = new CampaignDate(world.getCalendar());
    dm = Settings.get().getNickname();
    battle = new Battle(this);
  }

  public int getMaxPartyLevel() {
    int max = 0;
    for (String characterId : getCharacterIds().getValue()) {
      Character character = Characters.getCharacter(characterId).getValue().get();
      max = Math.max(max, character.getLevel());
    }

    return max;
  }

  public int getMinPartyLevel() {
    int min = Integer.MAX_VALUE;
    for (String characterId : getCharacterIds().getValue()) {
      Character character = Characters.getCharacter(characterId).getValue().get();
      min = Math.min(min, character.getLevel());
    }

    if (min == Integer.MAX_VALUE) {
      return 0;
    }

    return min;
  }

  public boolean isCloseECL(int level) {
    return getMinPartyLevel() <= level && getMaxPartyLevel() >= level;
  }

  public void awardXp(Character character, int xp) {
    CompanionPublisher.get().awardXp(this, character, xp);
  }

  public Campaign refresh() {
    return Campaigns.getCampaign(entryId, isLocal()).getValue().or(this);
  }

  public String getWorld() {
    return world.getName();
  }

  public String getDm() {
    return dm;
  }

  public boolean amDM(){
    return dm.equals(Settings.get().getNickname());
  }

  public Calendar getCalendar() {
    return world.getCalendar();
  }

  public String getCampaignId() {
    if (!isLocal() && Misc.onEmulator()) {
      return StoredEntries.REMOTE + getEntryId();
    }

    return getEntryId();
  }

  public LiveData<ImmutableList<String>> getCharacterIds() {
    return Characters.getCampaignCharacterIds(getCampaignId());
  }

  public LiveData<ImmutableList<String>> getCreatureIds() {
    return Creatures.getCampaignCreatureIds(getCampaignId());
  }

  public CampaignDate getDate() {
    return date;
  }

  public Battle getBattle() {
    return battle;
  }

  public boolean inBattle() {
    return !battle.isEnded();
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
      if (Campaigns.has(this)) {
        Campaigns.update(this);
      } else {
        Campaigns.add(this);
      }

      if (isLocal() && published) {
        CompanionPublisher.get().publish(this);
      }
    }

    return changed;
  }

  @Override
  public String toString() {
    return getName() + " (" + getCampaignId() + "/" + (isLocal() ? "local" : "remote") + ")";
  }

  public void delete() {
    if (isLocal()) {
      CompanionPublisher.get().delete(this);
    }
    Campaigns.remove(this);
  }

  private static class XPAward {
    private String characterId;
    private int xp;

    private XPAward(String characterId, int xp) {
      this.characterId = characterId;
      this.xp = xp;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    Campaign campaign = (Campaign) o;

    if (published != campaign.published) return false;
    if (nextBattleNumber != campaign.nextBattleNumber) return false;
    if (!characters.equals(campaign.characters)) return false;
    if (!world.equals(campaign.world)) return false;
    if (!dm.equals(campaign.dm)) return false;
    if (!date.equals(campaign.date)) return false;
    return battle.equals(campaign.battle);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + characters.hashCode();
    result = 31 * result + world.hashCode();
    result = 31 * result + dm.hashCode();
    result = 31 * result + (published ? 1 : 0);
    result = 31 * result + date.hashCode();
    result = 31 * result + battle.hashCode();
    result = 31 * result + nextBattleNumber;
    return result;
  }
}
