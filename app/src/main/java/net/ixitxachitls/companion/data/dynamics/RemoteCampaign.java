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

import android.support.annotation.CallSuper;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.values.CampaignDate;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

/**
 * A campaign that is remotely available (the local user can be a player).
 */
public class RemoteCampaign extends Campaign {

  public static final String TABLE = Campaign.TABLE + "_remote";

  private RemoteCampaign(CompanionContext companionContext, long id, String name) {
    super(companionContext, id, name, false, DataBaseContentProvider.CAMPAIGNS_REMOTE);
  }

  @Override
  public boolean isDefault() {
    return false;
  }

  @Override
  public String toString() {
    return super.toString() + "/remote";
  }

  @Override
  public boolean isPublished() {
    return false;
  }

  @Override
  public boolean isOnline() {
    return context.messenger().isOnline(this);
  }

  public void setDate(CampaignDate date) {
    Status.toast("Cannot set the date of a remote campaign!");
  }

  @CallSuper @Override
  public void delete() {
    // Also delete all remote characters.
    for (Character character : context.characters().getCampaignCharacters(getCampaignId())) {
      if (!character.isLocal()) {
        context.characters().remove(character);
      }
    }

    super.delete();
  }

  public static RemoteCampaign fromProto(CompanionContext companionContext, long id, Entry.CampaignProto proto) {
    RemoteCampaign campaign = new RemoteCampaign(companionContext, id, proto.getName());
    campaign.entryId = proto.getId();
    campaign.world = Entries.get().getWorlds().get(proto.getWorld())
        .orElse(Entries.get().getWorlds().get("Generic").get());
    campaign.dm = proto.getDm();
    campaign.date = CampaignDate.fromProto(proto.getDate());
    //campaign.battle = Battle.fromProto(campaign, proto.getBattle());
    campaign.nextBattleNumber = proto.getNextBattleNumber();

    return campaign;
  }
}
