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

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Data;
import net.ixitxachitls.companion.data.values.Condition;
import net.ixitxachitls.companion.net.CompanionMessenger;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A remote instance of a character.
 */
public class RemoteCharacter extends Character {

  public static final String TABLE = Character.TABLE + "_remote";

  public RemoteCharacter(Data data, long id, String name, String campaignId) {
    super(data, id, name, campaignId, false, DataBaseContentProvider.CHARACTERS_REMOTE);
  }

  @Override
  public void addXp(int xp) {
    // Send an award message to the remote character.
    Optional<Campaign> campaign = getCampaign();
    if (!campaign.isPresent() && !campaign.get().isLocal()) {
      Status.error("Cannot award xp for a remote or unknown campaign");
    }

    Status.log("sending xp award of " + xp + " for " + this);
    CompanionMessenger.get().sendXpAward(campaign.get().getCampaignId(), getCharacterId(), xp);
  }

  @Override
  public String toString() {
    return super.toString() + "/remote";
  }

  public static RemoteCharacter fromProto(Data data, long id, Entry.CharacterProto proto) {
    RemoteCharacter character = new RemoteCharacter(data, id,
        proto.getCreature().getName(), proto.getCreature().getCampaignId());
    character.fromProto(proto.getCreature());
    character.playerName = proto.getPlayer();
    character.conditionsHistory = proto.getConditionHistoryList().stream()
        .map(Condition::fromProto)
        .collect(Collectors.toList());
    character.xp = proto.getXp();

    for (Entry.CharacterProto.Level level : proto.getLevelList()) {
      character.levels.add(Character.fromProto(level));
    }

    if (character.playerName.isEmpty()) {
      character.playerName = data.settings().getNickname();
    }

    return character;
  }
}
