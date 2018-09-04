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
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Campaigns;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.data.dynamics.Item;
import net.ixitxachitls.companion.data.dynamics.RemoteCampaign;
import net.ixitxachitls.companion.data.dynamics.RemoteCharacter;
import net.ixitxachitls.companion.data.dynamics.XpAward;
import net.ixitxachitls.companion.data.values.TimedCondition;
import net.ixitxachitls.companion.proto.Entry;

/**
 * A message transmitted between client and server.
 */
public class CompanionMessageData {
  private final CompanionContext context;
  private final Entry.CompanionMessageProto.Payload payload;

  private CompanionMessageData(CompanionContext context, Entry.CompanionMessageProto.Payload payload) {
    this.context = context;
    this.payload = payload;
  }

  public enum Type {
    UNKNOWN, DEBUG, WELCOME, ACK, CAMPAIGN, CAMPAIGN_DELETE, CHARACTER, CHARACTER_DELETE, IMAGE,
    XP_AWARD, CONDITION, CONDITION_DELETE, ITEM_ADD
  };

  public Type getType() {
    return getType(payload.getPayloadCase());
  }

  public static Type getType(Entry.CompanionMessageProto.Payload.PayloadCase type) {
    switch(type) {
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

      case ITEM_ADD:
        return Type.ITEM_ADD;

      default:
      case PAYLOAD_NOT_SET:
        return Type.UNKNOWN;
    }
  }

  public Campaigns campaigns() {
    return context.campaigns();
  }

  public String getDebug() {
    return payload.getDebug();
  }

  public RemoteCampaign getCampaign() {
    return RemoteCampaign.fromProto(context, campaigns().getRemoteIdFor(payload.getCampaign().getId()),
        payload.getCampaign());
  }

  public RemoteCharacter getCharacter() {
    return RemoteCharacter.fromProto(context,
        context.characters().getRemoteIdFor(payload.getCharacter().getCreature().getId()),
        payload.getCharacter());
  }

  public Image getImage() {
    return Image.fromProto(context, payload.getImage());
  }

  public Item getItemAdd() {
    return Item.fromProto(payload.getItemAdd().getItem());
  }

  public String getItemAddRecipient() {
    return payload.getItemAdd().getRecipient();
  }

  public String getConditionTargetId() {
    return payload.getCondition().getTargetId();
  }

  public TimedCondition getCondition() {
    return TimedCondition.fromProto(payload.getCondition().getCondition());
  }

  public long getAck() {
    return payload.getAck();
  }

  public String getCampaignDelete() {
    return payload.getCampaignDelete();
  }

  public String getCharacterDelete() {
    return payload.getCharacterDelete();
  }

  public String getConditionDeleteName() {
    return payload.getConditionDelete().getName();
  }

  public String getConditionDeleteSourceId() {
    return payload.getConditionDelete().getSourceId();
  }

  public String getConditionDeleteTargetId() {
    return payload.getConditionDelete().getTargetId();
  }

  public XpAward getXpAward() {
    return XpAward.fromProto(payload.getXpAward());
  }

  public String getWelcomeId() {
    return payload.getWelcome().getId();
  }

  public String getWelcomeName() {
    return payload.getWelcome().getName();
  }

  public boolean mayOverwrite() {
    switch (payload.getPayloadCase()) {
      default:
        return false;

      case CAMPAIGN_DELETE:
      case CHARACTER:
      case CAMPAIGN:
      case IMAGE:
      case ITEM_ADD:
      case PAYLOAD_NOT_SET:
        return true;
    }
  }

  public boolean overwrites(CompanionMessageData other) {
    switch (payload.getPayloadCase()) {
      default:
        return false;

      case CAMPAIGN_DELETE:
        return payload.getCampaignDelete().equals(other.payload.getCampaignDelete());

      case CAMPAIGN:
        return payload.getCampaign().getId().equals(other.payload.getCampaign().getId());

      case CHARACTER:
        return payload.getCharacter().getCreature().getId().equals(
            other.payload.getCharacter().getCreature().getId());

      case CHARACTER_DELETE:
        return payload.getCharacterDelete().equals(other.payload.getCharacterDelete());

      case IMAGE:
        return payload.getImage().getId().equals(other.payload.getImage().getId());

      case ITEM_ADD:
        return payload.getItemAdd().getId().equals(other.payload.getItemAdd().getId());
    }
  }

  public boolean requiresAck() {
    switch (payload.getPayloadCase()) {
      default:
        return false;

      case XP_AWARD:
      case CONDITION:
      case CAMPAIGN_DELETE:
      case CHARACTER_DELETE:
      case CONDITION_DELETE:
      case ITEM_ADD:
        return true;
    }
  }

  @Override
  public String toString() {
    String message = payload.getPayloadCase() + " - ";

    switch (getType()) {
      default:
      case UNKNOWN:
        return message + "unknown";

      case DEBUG:
        return message + getDebug();

      case WELCOME:
        return message + payload.getWelcome().getName();

      case ACK:
        return message + getAck();

      case CAMPAIGN:
        return message + payload.getCampaign().getName();

      case CAMPAIGN_DELETE:
        return message + payload.getCampaignDelete();

      case CHARACTER:
        return message + payload.getCharacter().getCreature().getName();

      case CHARACTER_DELETE:
        return message + payload.getCharacterDelete();

      case CONDITION:
        return message + payload.getCondition().getCondition().getCondition().getName();

      case CONDITION_DELETE:
        return message + payload.getConditionDelete().getName() + "/"
            + Status.nameFor(payload.getConditionDelete().getSourceId()) + ">"
            + Status.nameFor(payload.getConditionDelete().getTargetId());

      case IMAGE:
        return message + payload.getImage().getId();

      case ITEM_ADD:
        return message + payload.getItemAdd().getId() + " ("
            + payload.getItemAdd().getItem().getName() + ")";

      case XP_AWARD:
        return message + payload.getXpAward().getXpAward()
            + " (" + payload.getXpAward().getCharacterId() + ")";
    }
  }

  public Entry.CompanionMessageProto.Payload toProto() {
    return payload;
  }

  public static CompanionMessageData from(Campaign campaign) {
    return fromProto(campaign.data(), Entry.CompanionMessageProto.Payload.newBuilder()
        .setCampaign(campaign.toProto())
        .build());
  }

  public static CompanionMessageData from(Character character) {
    return fromProto(character.data(), Entry.CompanionMessageProto.Payload.newBuilder()
        .setCharacter(character.toProto())
        .build());
  }

  public static CompanionMessageData from(CompanionContext companionContext, Image image) {
    return fromProto(companionContext, Entry.CompanionMessageProto.Payload.newBuilder()
        .setImage(image.toProto())
        .build());
  }

  public static CompanionMessageData from(CompanionContext companionContext, String targetId, TimedCondition condition) {
    return fromProto(companionContext, Entry.CompanionMessageProto.Payload.newBuilder()
        .setCondition(Entry.CompanionMessageProto.Payload.Condition.newBuilder()
            .setTargetId(targetId)
            .setCondition(condition.toProto())
            .build())
        .build());
  }

  public static CompanionMessageData from(CompanionContext context, Item item, String creatureId) {
    return fromProto(context, Entry.CompanionMessageProto.Payload.newBuilder()
        .setItemAdd(Entry.CompanionMessageProto.Payload.Item.newBuilder()
            .setId(item.getId())
            .setItem(item.toProto())
            .setRecipient(creatureId)
            .build())
        .build());
  }

  public static CompanionMessageData fromDelete(Character character) {
    return fromProto(character.data(), Entry.CompanionMessageProto.Payload.newBuilder()
        .setCharacterDelete(character.getCharacterId())
        .build());
  }

  public static CompanionMessageData fromDelete(Campaign campaign) {
    return fromProto(campaign.data(), Entry.CompanionMessageProto.Payload.newBuilder()
        .setCampaignDelete(campaign.getCampaignId())
        .build());
  }

  public static CompanionMessageData fromDelete(CompanionContext companionContext, String conditionName, String sourceId,
                                                String targetId) {
    return fromProto(companionContext, Entry.CompanionMessageProto.Payload.newBuilder()
        .setConditionDelete(Entry.CompanionMessageProto.Payload.DeleteCondition.newBuilder()
            .setName(conditionName)
            .setSourceId(sourceId)
            .setTargetId(targetId)
            .build())
        .build());
  }

  public static CompanionMessageData fromWelcome(CompanionContext companionContext, String id, String name) {
    return fromProto(companionContext, Entry.CompanionMessageProto.Payload.newBuilder()
        .setWelcome(Entry.CompanionMessageProto.Payload.Welcome.newBuilder()
            .setId(id)
            .setName(name)
            .build())
        .build());
  }

  public static CompanionMessageData from(CompanionContext companionContext, XpAward award) {
    return fromProto(companionContext, Entry.CompanionMessageProto.Payload.newBuilder()
        .setXpAward(award.toProto())
        .build());
  }

  public static CompanionMessageData fromAck(CompanionContext companionContext, long messageId) {
    return fromProto(companionContext, Entry.CompanionMessageProto.Payload.newBuilder()
        .setAck(messageId)
        .build());
  }

  public static CompanionMessageData fromProto(CompanionContext companionContext,
                                               Entry.CompanionMessageProto.Payload payload) {
    return new CompanionMessageData(companionContext, payload);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CompanionMessageData that = (CompanionMessageData) o;

    return payload.equals(that.payload);
  }

  @Override
  public int hashCode() {
    return payload.hashCode();
  }
}
