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

package net.ixitxachitls.companion.data.dynamics;

import android.arch.lifecycle.LiveData;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.statics.World;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.data.values.Calendar;
import net.ixitxachitls.companion.data.values.CampaignDate;
import net.ixitxachitls.companion.proto.Data;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A campaign with all its data.
 */
public abstract class Campaign extends StoredEntry<Data.CampaignProto>
    implements Comparable<Campaign> {

  // Constants.
  public static final String TYPE = "campaign";
  public static final String TABLE = "campaigns";

  // Values.
  protected World world;
  protected String dm = "";
  protected Battle battle;
  protected CampaignDate date;
  protected int nextBattleNumber = 0;

  protected Campaign(long id, String name, boolean local,
                     Uri dbUrl) {
    super(id, TYPE, name, local, dbUrl);

    world = Entries.get().getWorlds().get("Generic").get();
    date = new CampaignDate(world.getCalendar().getYears().get(0).getNumber());
    dm = Settings.get().getNickname();
    battle = new Battle(this);
  }

  public int getMaxPartyLevel() {
    int max = 1;
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
      return 1;
    }

    return min;
  }

  public boolean isCloseECL(int level) {
    return getMinPartyLevel() <= level && getMaxPartyLevel() >= level;
  }

  public LocalCampaign asLocal() {
    IllegalStateException e = new IllegalStateException("Cannot access remote campaign as local!");
    Status.exception("Invalid local conversion", e);
    throw e;
  }

  public String getWorld() {
    return world.getName();
  }

  public String getDm() {
    return dm;
  }

  public boolean amDM(){
    return isLocal();
  }

  public Calendar getCalendar() {
    return world.getCalendar();
  }

  public String getCampaignId() {
    return getEntryId();
  }

  public LiveData<ImmutableList<String>> getCharacterIds() {
    return Characters.getCampaignCharacterIds(getCampaignId());
  }

  public List<Character> getCharacters() {
    return getCharacterIds().getValue().stream()
        .map(id -> Characters.getCharacter(id).getValue().get())
        .collect(Collectors.toList());
  }

  public LiveData<ImmutableList<String>> getCreatureIds() {
    return Creatures.getCampaignCreatureIds(getCampaignId());
  }

  public List<Creature> getCreatures() {
    return Creatures.getCampaignCreatures(getCampaignId());
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

  public static Optional<LocalCampaign> asLocal(Optional<Campaign> campaign) {
    if (!campaign.isPresent() || !campaign.get().isLocal()) {
      return Optional.empty();
    }

    return Optional.of((LocalCampaign) campaign.get());
  }

  public abstract boolean isDefault();
  public abstract boolean isPublished();
  public abstract boolean isOnline();

  @Override
  public Data.CampaignProto toProto() {
    return Data.CampaignProto.newBuilder()
        .setId(getEntryId())
        .setName(name)
        .setWorld(world.getName())
        .setPublished(isPublished())
        .setDm(getDm())
        .setDate(date.toProto())
        .setBattle(battle.toProto())
        .setNextBattleNumber(nextBattleNumber)
        .build();
  }

  @Override
  public boolean store() {
    if (super.store()) {
      if (Campaigns.has(this)) {
        Campaigns.update(this);
      } else {
        Campaigns.add(this);
      }

      return true;
    }

    return false;
  }

  @Override
  public String toString() {
    return getName() + " (" + getCampaignId() + ")";
  }

  @CallSuper
  public void delete() {
    Campaigns.remove(this);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null || getClass() != other.getClass()) return false;
    if (!super.equals(other)) return false;

    Campaign campaign = (Campaign) other;

    if (nextBattleNumber != campaign.nextBattleNumber) return false;
    if (!world.equals(campaign.world)) return false;
    if (!dm.equals(campaign.dm)) return false;
    if (!date.equals(campaign.date)) return false;
    return battle.equals(campaign.battle);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + world.hashCode();
    result = 31 * result + dm.hashCode();
    result = 31 * result + date.hashCode();
    result = 31 * result + battle.hashCode();
    result = 31 * result + nextBattleNumber;
    return result;
  }

  @Override
  public int compareTo(@NonNull Campaign other) {
    if (getCampaignId().equals(other.getCampaignId())) {
      return 0;
    }

    if (this.isDefault()) {
      return -1;
    }

    if (other.isDefault()) {
      return +1;
    }

    int compare = getName().compareToIgnoreCase(other.getName());
    if (compare != 0) {
      return compare;
    }

    if (isLocal() && !other.isLocal()) {
      return -1;
    }

    if (!isLocal() && other.isLocal()) {
      return +1;
    }

    return getCampaignId().compareTo(other.getCampaignId());
  }
}
