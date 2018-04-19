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

import android.support.annotation.Nullable;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.net.CompanionMessage;
import net.ixitxachitls.companion.net.CompanionMessageData;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.Date;

/**
 * A message scheduled for transmission.
 */
public class ScheduledMessage extends StoredEntry<Entry.ScheduledMessageProto> {

  public static final String TYPE = "message";
  public static final String TABLE = "messages";
  private static final long LATE_MS = 60 * 1_000; // 1 minute.
  private static final long OUTDATED_MS = 300 * 24 * 60 * 60 * 1_000; // 300 days.

  public enum State { WAITING, PENDING, SENT, ACKED };

  private final CompanionMessage message;
  private State state;
  private long lastInteraction;

  public ScheduledMessage(Campaigns campaigns, CompanionMessage message) {
    this(campaigns, 0, State.WAITING, new Date().getTime(), message);
  }

  private ScheduledMessage(Campaigns campaigns, long id, State state, long lastInteraction,
                           CompanionMessage message) {
    super(campaigns.context(), id, TYPE,
        TYPE + "-" + campaigns.context().settings().getAppId() + "-" + message.getMessageId(), TYPE,
        true, DataBaseContentProvider.MESSAGES);

    this.message = message;
    this.state = state;
    this.lastInteraction = lastInteraction;
  }

  public CompanionMessageData getData() {
    return message.getData();
  }

  public boolean isLate() {
    return new Date().getTime() - lastInteraction > LATE_MS;
  }

  public boolean isOutdated() {
    return new Date().getTime() - lastInteraction > OUTDATED_MS;
  }

  public boolean matches(String senderId, String recieverId) {
    return message.getSenderId().equals(senderId) && message.getRecieverId().equals(recieverId);
  }

  @Override
  public Entry.ScheduledMessageProto toProto() {
    return Entry.ScheduledMessageProto.newBuilder()
        .setState(convert(state))
        .setLastInteraction(lastInteraction)
        .setMessage(message.toProto())
        .build();
  }

  public State getState() {
    return state;
  }

  public CompanionMessage getMessage() {
    return message;
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

  public void markUpdated() {
    updateLastInteraction();
    store();
  }

  private void updateLastInteraction() {
    lastInteraction = new Date().getTime();
  }

  public boolean mayOverwrite() {
    return message.getData().mayOverwrite();
  }

  public boolean overwrites(ScheduledMessage other) {
    return message.getData().overwrites(other.message.getData());
  }

  public boolean requiresAck() {
    return message.getData().requiresAck();
  }

  public long getMessageId() {
    return message.getMessageId();
  }

  public String getSenderId() {
    return message.getSenderId();
  }

  public String getRecieverId() {
    return message.getRecieverId();
  }

  private Entry.ScheduledMessageProto.State convert(State state) {
    switch (state) {
      default:
      case WAITING:
        return Entry.ScheduledMessageProto.State.WAITING;

      case PENDING:
        return Entry.ScheduledMessageProto.State.PENDING;

      case SENT:
        return Entry.ScheduledMessageProto.State.SENT;

      case ACKED:
        return Entry.ScheduledMessageProto.State.ACKED;
    }
  }

  public static ScheduledMessage fromProto(CompanionContext companionContext, long id, Entry.ScheduledMessageProto proto) {
    return new ScheduledMessage(companionContext.campaigns(), id, convert(proto.getState()),
        proto.getLastInteraction(), CompanionMessage.fromProto(companionContext, proto.getMessage()));
  }

  private static State convert(Entry.ScheduledMessageProto.State state) {
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

  @Override
  public String toString() {
    return state + " - " + message;
  }

  @Override
  public boolean equals(@Nullable Object other) {
    if (!super.equals(other)) return false;
    if (this == other) return true;
    if (other == null || getClass() != other.getClass()) return false;
    if (!super.equals(other)) return false;

    ScheduledMessage that = (ScheduledMessage) other;

    return state == that.state
        && message.equals(that.message);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + state.hashCode();
    result = 31 * result + message.hashCode();
    return result;
  }

  public static String info(Entry.CompanionMessageProto.Header.Id id) {
    if (!id.getName().isEmpty()) {
      return id.getName();
    }

    return id.getId();
  }

  public static String info(Entry.CompanionMessageProto message) {
    String postfix = "";
    if (message.getHeader().getId() != 0) {
      postfix = " (id " + message.getHeader().getId() + ")";
    }
    switch (message.getData().getPayloadCase()) {
      case DEBUG:
        return "DEBUG - " + message.getData().getDebug() + postfix;

      case WELCOME:
        return "WELCOME" + postfix;

      case ACK:
        return "ACK - " + message.getData().getAck() + postfix;

      case CAMPAIGN:
        return "CAMPAIGN - " + message.getData().getCampaign().getName() + postfix;

      case CAMPAIGN_DELETE:
        return "CAMPAIGN DELETE - " + message.getData().getCampaignDelete() + postfix;

      case CHARACTER:
        return "CHARACTER - " + message.getData().getCharacter().getCreature().getName()
            + postfix;

      case CHARACTER_DELETE:
        return "CHARACTER DELETE - " + message.getData().getCharacterDelete() + postfix;

      case IMAGE:
        return "IMAGE - " + message.getData().getImage().getId() + "/"
            + message.getData().getImage().getType() + postfix;

      case XP_AWARD:
        return "XP - " + message.getData().getXpAward().getCampaignId() + "/"
            + message.getData().getXpAward().getCharacterId() + "/"
            + message.getData().getXpAward().getXpAward() + postfix;

      case CONDITION:
        return "CONDITION - " + Status.nameFor(message.getData().getCondition().getTargetId())
            + "/" + message.getData().getCondition().getCondition().getCondition().getName();

      case CONDITION_DELETE:
        return "CONDITION DELETE - " + message.getData().getConditionDelete().getName()
            + "/" + Status.nameFor(message.getData().getConditionDelete().getSourceId()
            + ">" + Status.nameFor(message.getData().getConditionDelete().getTargetId()));

      case PAYLOAD_NOT_SET:
        return "NOT SET" + postfix;

      default:
        return "NOT HANDLED!" + postfix;
    }
  }
}
