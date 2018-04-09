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

import net.ixitxachitls.companion.proto.Entry;

/**
 * Simple wrapper for an xp award.
 */
public class XpAward {
  private final String characterId;
  private final String campaignId;
  private final int award;

  public XpAward(String characterId, String campaignId, int award) {
    this.characterId = characterId;
    this.campaignId = campaignId;
    this.award = award;
  }

  public String getCampaignId() {
    return campaignId;
  }

  public String getCharacterId() {
    return characterId;
  }

  public int getXp() {
    return award;
  }

  public Entry.CompanionMessageProto.Payload.Xp toProto() {
    return Entry.CompanionMessageProto.Payload.Xp.newBuilder()
        .setCharacterId(characterId)
        .setCampaignId(campaignId)
        .setXpAward(award)
        .build();
  }

  public static XpAward fromProto(Entry.CompanionMessageProto.Payload.Xp proto) {
    return new XpAward(proto.getCharacterId(), proto.getCampaignId(), proto.getXpAward());
  }
}
