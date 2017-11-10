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

import android.support.annotation.Nullable;

import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.net.CompanionMessage;
import net.ixitxachitls.companion.net.CompanionMessageData;
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

  private final CompanionMessage message;
  private State state;
  private long lastInteraction;

  public ScheduledMessage(CompanionMessage message) {
    this(0, State.WAITING, new Date().getTime(), message);
  }

  private ScheduledMessage(long id, State state, long lastInteraction, CompanionMessage message) {
    super(id, Settings.get().getAppId() + "-" + message.getMessageId(),
        "message", true, DataBaseContentProvider.MESSAGES);

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

  public boolean matches(String senderId, String recieverId) {
    return message.getSenderId().equals(senderId) && message.getRecieverId().equals(recieverId);
  }

  @Override
  public Data.ScheduledMessageProto toProto() {
    return Data.ScheduledMessageProto.newBuilder()
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

  public static ScheduledMessage fromProto(long id, Data.ScheduledMessageProto proto) {
    return new ScheduledMessage(id, convert(proto.getState()), proto.getLastInteraction(),
        CompanionMessage.fromProto(proto.getMessage()));
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
    return toString() + " to " + CompanionPublisher.get().getRecipientName(getRecieverId())
        + " (" + Strings.formatAgo(lastInteraction) + ")";
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

    return lastInteraction == that.lastInteraction
        && state == that.state
        && message.equals(that.message);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + state.hashCode();
    result = 31 * result + (int) (lastInteraction ^ (lastInteraction >>> 32));
    result = 31 * result + message.hashCode();
    return result;
  }
}
