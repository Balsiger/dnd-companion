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

package net.ixitxachitls.companion.net;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.data.dynamics.RemoteCampaign;
import net.ixitxachitls.companion.data.dynamics.RemoteCharacter;
import net.ixitxachitls.companion.data.dynamics.XpAward;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseAccessor;

/**
 * A message transmitted between client and server.
 */
public class CompanionMessageData {
  private final Data.CompanionMessageProto.Payload data;
  private final DataBaseAccessor dataBaseAccessor;

  private CompanionMessageData(Data.CompanionMessageProto.Payload data,
                               DataBaseAccessor dataBaseAccessor) {
    this.data = data;
    this.dataBaseAccessor = dataBaseAccessor;
  }

  enum Type {
    UNKNOWN, DEBUG, WELCOME, ACK, CAMPAIGN, CAMPAIGN_DELETE, CHARACTER, CHARACTER_DELETE, IMAGE,
    XP_AWARD, CONDITION, CONDITION_DELETE
  };

  public Type getType() {
    switch(data.getPayloadCase()) {
      case DEBUG:
        return Type.DEBUG;

      case WELCOME:
        return Type.WELCOME;

      case ACK:
        return Type.ACK;

      case CAMPAIGN:
        return Type.CAMPAIGN;

      case CAMPAIGN_DELETE:
        return Type.CAMPAIGN_DELETE;

      case CHARACTER:
        return Type.CHARACTER;

      case CHARACTER_DELETE:
        return Type.CHARACTER_DELETE;

      case IMAGE:
        return Type.IMAGE;

      case XP_AWARD:
        return Type.XP_AWARD;

      case CONDITION:
        return Type.CONDITION;

      case CONDITION_DELETE:
        return Type.CONDITION_DELETE;

      default:
      case PAYLOAD_NOT_SET:
        return Type.UNKNOWN;
    }
  }

  public String getDebug() {
    return data.getDebug();
  }

  public RemoteCampaign getCampaign() {
    return RemoteCampaign.fromProto(Campaigns.getRemoteIdFor(data.getCampaign().getId()),
        data.getCampaign(), dataBaseAccessor);
  }

  public RemoteCharacter getCharacter() {
    return RemoteCharacter.fromProto(
        Characters.getRemoteIdFor(data.getCharacter().getCreature().getId()),
        data.getCharacter(), dataBaseAccessor);
  }

  public Image getImage() {
    return Image.fromProto(data.getImage());
  }

  public String getConditionTargetId() {
    return data.getCondition().getTargetId();
  }

  public TimedCondition getCondition() {
    return TimedCondition.fromProto(data.getCondition().getCondition());
  }

  public long getAck() {
    return data.getAck();
  }

  public String getCampaignDelete() {
    return data.getCampaignDelete();
  }

  public String getCharacterDelete() {
    return data.getCharacterDelete();
  }

  public String getConditionDeleteName() {
    return data.getConditionDelete().getName();
  }

  public String getConditionDeleteSourceId() {
    return data.getConditionDelete().getSourceId();
  }

  public String getConditionDeleteTargetId() {
    return data.getConditionDelete().getTargetId();
  }

  public XpAward getXpAward() {
    return XpAward.fromProto(data.getXpAward());
  }

  public String getWelcomeId() {
    return data.getWelcome().getId();
  }

  public String getWelcomeName() {
    return data.getWelcome().getName();
  }

  public boolean mayOverwrite() {
    switch (data.getPayloadCase()) {
      default:
        return false;

      case CAMPAIGN_DELETE:
      case CHARACTER:
      case CAMPAIGN:
      case IMAGE:
      case PAYLOAD_NOT_SET:
        return true;
    }
  }

  public boolean overwrites(CompanionMessageData other) {
    switch (data.getPayloadCase()) {
      default:
        return false;

      case CAMPAIGN_DELETE:
        return data.getCampaignDelete().equals(other.data.getCampaignDelete());

      case CAMPAIGN:
        return data.getCampaign().getId().equals(other.data.getCampaign().getId());

      case CHARACTER:
        return data.getCharacter().getCreature().getId().equals(
            other.data.getCharacter().getCreature().getId());

      case CHARACTER_DELETE:
        return data.getCharacterDelete().equals(other.data.getCharacterDelete());

      case IMAGE:
        return data.getImage().getId().equals(other.data.getImage().getId());
    }
  }

  public boolean requiresAck() {
    switch (data.getPayloadCase()) {
      default:
        return false;

      case XP_AWARD:
      case CONDITION:
      case CAMPAIGN_DELETE:
      case CHARACTER_DELETE:
      case CONDITION_DELETE:
        return true;
    }
  }

  @Override
  public String toString() {
    String message = data.getPayloadCase() + " - ";

    switch (getType()) {
      default:
      case UNKNOWN:
        return message + "unknown";

      case DEBUG:
        return message + getDebug();

      case WELCOME:
        return message + data.getWelcome().getName();

      case ACK:
        return message + getAck();

      case CAMPAIGN:
        return message + data.getCampaign().getName();

      case CAMPAIGN_DELETE:
        return message + data.getCampaignDelete();

      case CHARACTER:
        return message + data.getCharacter().getCreature().getName();

      case CHARACTER_DELETE:
        return message + data.getCharacterDelete();

      case CONDITION:
        return message + data.getCondition().getCondition().getCondition().getName();

      case CONDITION_DELETE:
        return message + data.getConditionDelete().getName() + "/"
            + Status.nameFor(data.getConditionDelete().getSourceId()) + ">"
            + Status.nameFor(data.getConditionDelete().getTargetId());

      case IMAGE:
        return message + data.getImage().getId();

      case XP_AWARD:
        return message + data.getXpAward().getXpAward()
            + " (" + data.getXpAward().getCharacterId() + ")";
    }
  }

  public Data.CompanionMessageProto.Payload toProto() {
    return data;
  }

  public static CompanionMessageData from(Campaign campaign) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setCampaign(campaign.toProto())
        .build(), campaign.getDataBaseAccessor());
  }

  public static CompanionMessageData from(Character character) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setCharacter(character.toProto())
        .build(), character.getDataBaseAccessor());
  }

  public static CompanionMessageData from(Image image, DataBaseAccessor dataBaseAccessor) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setImage(image.toProto())
        .build(), dataBaseAccessor);
  }

  public static CompanionMessageData from(String targetId, TimedCondition condition,
                                          DataBaseAccessor dataBaseAccessor) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setCondition(Data.CompanionMessageProto.Payload.Condition.newBuilder()
            .setTargetId(targetId)
            .setCondition(condition.toProto())
            .build())
        .build(), dataBaseAccessor);
  }

  public static CompanionMessageData fromDelete(Character character) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setCharacterDelete(character.getCharacterId())
        .build(), character.getDataBaseAccessor());
  }

  public static CompanionMessageData fromDelete(Campaign campaign) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setCampaignDelete(campaign.getCampaignId())
        .build(), campaign.getDataBaseAccessor());
  }

  public static CompanionMessageData fromDelete(String conditionName, String sourceId,
                                                String targetId,
                                                DataBaseAccessor dataBaseAccessor) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setConditionDelete(Data.CompanionMessageProto.Payload.DeleteCondition.newBuilder()
            .setName(conditionName)
            .setSourceId(sourceId)
            .setTargetId(targetId)
            .build())
        .build(), dataBaseAccessor);
  }

  public static CompanionMessageData fromWelcome(String id, String name,
                                                 DataBaseAccessor dataBaseAccessor) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setWelcome(Data.CompanionMessageProto.Payload.Welcome.newBuilder()
            .setId(id)
            .setName(name)
            .build())
        .build(), dataBaseAccessor);
  }

  public static CompanionMessageData from(XpAward award, DataBaseAccessor dataBaseAccessor) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setXpAward(award.toProto())
        .build(), dataBaseAccessor);
  }

  public static CompanionMessageData fromAck(long messageId, DataBaseAccessor dataBaseAccessor) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setAck(messageId)
        .build(), dataBaseAccessor);
  }

  public static CompanionMessageData fromProto(Data.CompanionMessageProto.Payload data,
                                               DataBaseAccessor dataBaseAccessor) {
    return new CompanionMessageData(data ,dataBaseAccessor);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CompanionMessageData that = (CompanionMessageData) o;

    return data.equals(that.data);
  }

  @Override
  public int hashCode() {
    return data.hashCode();
  }
}
