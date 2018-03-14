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

package net.ixitxachitls.companion.net;

import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.data.dynamics.RemoteCampaign;
import net.ixitxachitls.companion.data.dynamics.XpAward;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.proto.Data;

/**
 * A message transmitted between client and server.
 */
public class CompanionMessageData {
  private final Data.CompanionMessageProto.Payload data;

  private CompanionMessageData(Data.CompanionMessageProto.Payload data) {
    this.data = data;
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

      case CONDTION_DELETE:
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
        data.getCampaign());
  }

  public Character getCharacter() {
    return Character.fromProto(Characters.getRemoteIdFor(data.getCharacter().getCreature().getId()),
        false, data.getCharacter());
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
    return data.getCondtionDelete().getName();
  }

  public String getConditionDeleteSourceId() {
    return data.getCondtionDelete().getSourceId();
  }

  public String getConditionDeleteTargetId() {
    return data.getCondtionDelete().getTargetId();
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
      case CONDTION_DELETE:
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
        return message + data.getCondtionDelete().getName() + "/"
            + data.getCondtionDelete().getSourceId() + ">"
            + data.getCondtionDelete().getTargetId();

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
        .build());
  }

  public static CompanionMessageData from(Character character) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setCharacter(character.toProto())
        .build());
  }

  public static CompanionMessageData from(Image image) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setImage(image.toProto())
        .build());
  }

  public static CompanionMessageData from(String targetId, TimedCondition condition) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setCondition(Data.CompanionMessageProto.Payload.Condition.newBuilder()
            .setTargetId(targetId)
            .setCondition(condition.toProto())
            .build())
        .build());
  }

  public static CompanionMessageData fromDelete(Character character) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setCharacterDelete(character.getCharacterId())
        .build());
  }

  public static CompanionMessageData fromDelete(Campaign campaign) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setCampaignDelete(campaign.getCampaignId())
        .build());
  }

  public static CompanionMessageData fromDelete(String conditionName,
                                                String sourceId,
                                                String targetId) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setCondtionDelete(Data.CompanionMessageProto.Payload.DeleteCondition.newBuilder()
            .setName(conditionName)
            .setSourceId(sourceId)
            .setTargetId(targetId)
            .build())
        .build());
  }

  public static CompanionMessageData fromWelcome(String id, String name) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setWelcome(Data.CompanionMessageProto.Payload.Welcome.newBuilder()
            .setId(id)
            .setName(name)
            .build())
        .build());
  }

  public static CompanionMessageData from(XpAward award) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setXpAward(award.toProto())
        .build());
  }

  public static CompanionMessageData fromAck(long messageId) {
    return fromProto(Data.CompanionMessageProto.Payload.newBuilder()
        .setAck(messageId)
        .build());
  }

  public static CompanionMessageData fromProto(Data.CompanionMessageProto.Payload data) {
    return new CompanionMessageData(data);
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
