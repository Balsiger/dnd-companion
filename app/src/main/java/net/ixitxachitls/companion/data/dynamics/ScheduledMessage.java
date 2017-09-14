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

import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.net.CompanionMessage;
import net.ixitxachitls.companion.net.CompanionPublisher;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.util.Strings;

import java.util.Date;

/**
 * A message scheduled for transmission.
 */
public class ScheduledMessage extends StoredEntry<Data.ScheduledMessageProto> {

  public static final String TABLE = "messages";
  private static final long LATE_MS = 60 * 1_000; // 1 minute

  public enum State { WAITING, PENDING, SENT, ACKED };

  private State state;
  private long lastInteraction;
  private Data.CompanionMessageProto proto;

  public ScheduledMessage(Data.CompanionMessageProto message) {
    this(State.WAITING, new Date().getTime(), message);
  }

  public ScheduledMessage(State state, long lastInteraction,
                          Data.CompanionMessageProto message) {
    super(message.getId(), Settings.get().getAppId() + "-" + message.getId(),
        "message", true, DataBaseContentProvider.MESSAGES);

    this.id = message.getId();
    this.state = state;
    this.lastInteraction = lastInteraction;
    this.proto = message;
  }

  public boolean isLate() {
    return new Date().getTime() - lastInteraction > LATE_MS;
  }

  public boolean matches(String senderId, String receiverId) {
    return proto.getSender().equals(senderId) && proto.getReceiver().equals(receiverId);
  }

  @Override
  public Data.ScheduledMessageProto toProto() {
    if (proto.getId() != id) {
      proto = proto.toBuilder().setId(id).build();
    }

    return Data.ScheduledMessageProto.newBuilder()
        .setState(convert(state))
        .setLastInteraction(lastInteraction)
        .setMessage(proto)
        .build();
  }

  public State getState() {
    return state;
  }

  public CompanionMessage toMessage() {
    return new CompanionMessage(proto);
  }

  public void markSent() {
    mark(State.SENT);
  }

  public void markAck() {
    mark(State.ACKED);
  }

  public void markPending() {
    mark(State.PENDING);
  }

  public void markWaiting() {
    mark(State.WAITING);
  }

  private void mark(State state){
    this.state = state;
    updateLastInteraction();
    store();
  }

  private void updateLastInteraction() {
    lastInteraction = new Date().getTime();
  }

  public boolean mayOverwrite() {
    switch (proto.getPayloadCase()) {
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

  public boolean overwrites(ScheduledMessage other) {
    switch (proto.getPayloadCase()) {
      default:
        return false;

      case CAMPAIGN_DELETE:
        return proto.getCampaignDelete().equals(other.proto.getCampaignDelete());

      case CAMPAIGN:
        return proto.getCampaign().getId().equals(other.proto.getCampaign().getId());

      case CHARACTER:
        return proto.getCharacter().getId().equals(other.proto.getCharacter().getId());

      case IMAGE:
        return proto.getImage().getId().equals(other.proto.getImage().getId());
    }
  }

  public boolean requiresAck() {
    switch (proto.getPayloadCase()) {
      default:
        return false;

      case XP_AWARD:
      case CAMPAIGN_DELETE:
        return true;
    }
  }

  public Data.CompanionMessageProto.PayloadCase getType() {
    return proto.getPayloadCase();
  }

  public long getMessageId() {
    return proto.getId();
  }

  public String getSender() {
    return proto.getSender();
  }

  public String getRecipient() {
    return proto.getReceiver();
  }

  private Data.ScheduledMessageProto.State convert(State state) {
    switch (state) {
      default:
      case WAITING:
        return Data.ScheduledMessageProto.State.WAITING;

      case PENDING:
        return Data.ScheduledMessageProto.State.PENDING;

      case SENT:
        return Data.ScheduledMessageProto.State.SENT;

      case ACKED:
        return Data.ScheduledMessageProto.State.ACKED;
    }
  }

  public static ScheduledMessage fromProto(Data.ScheduledMessageProto proto) {
    return new ScheduledMessage(convert(proto.getState()), proto.getLastInteraction(),
        proto.getMessage());
  }

  private static State convert(Data.ScheduledMessageProto.State state) {
    switch (state) {
      default:
      case UNRECOGNIZED:
      case WAITING:
        return State.WAITING;

      case PENDING:
        return State.PENDING;

      case SENT:
        return State.SENT;

      case ACKED:
        return State.ACKED;
    }
  }

  public String toSenderString() {
    return toString() + " to " + CompanionPublisher.get().getRecipientName(proto.getReceiver())
        + " (" + Strings.formatAgo(lastInteraction) + ")";
  }

  @Override
  public String toString() {
    return state + " - " + ScheduledMessage.toString(proto);
  }

  public static String toString(Data.CompanionMessageProto proto) {
    String message = proto.getPayloadCase() + " - ";

    switch (proto.getPayloadCase()) {
      case CAMPAIGN:
      case CAMPAIGN_DELETE:
        return message + proto.getCampaign().getName();

      case CHARACTER:
        return message + proto.getCharacter().getName();

      case IMAGE:
        return message + proto.getImage().getId();

      case WELCOME:
        return message + proto.getWelcome().getName();

      case XP_AWARD:
        return message + proto.getXpAward().getCharacterId() + "/"
            + proto.getXpAward().getXpAward();

      default:
        return message + "unknown";
    }
  }

  @Override
  public boolean equals(Object other) {
    if (!super.equals(other)) return false;
    if (this == other) return true;
    if (other == null || getClass() != other.getClass()) return false;
    if (!super.equals(other)) return false;

    ScheduledMessage that = (ScheduledMessage) other;

    if (lastInteraction != that.lastInteraction) return false;
    if (state != that.state) return false;
    return proto.equals(that.proto);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + state.hashCode();
    result = 31 * result + (int) (lastInteraction ^ (lastInteraction >>> 32));
    result = 31 * result + proto.hashCode();
    return result;
  }
}
