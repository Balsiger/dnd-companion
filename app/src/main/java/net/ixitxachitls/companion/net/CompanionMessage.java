/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Tabletop Companion.
 *
 * The Tabletop Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Tabletop Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Tabletop Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.net;

import net.ixitxachitls.companion.proto.Data;

/**
 * Wrapper for a companion message with data and all necessary routing information.
 */
public class CompanionMessage {

  private final String senderId;
  private final String senderName;
  private final String recieverId;
  private final long messageId;
  private final CompanionMessageData data;

  public CompanionMessage(String senderId, String senderName,
                          String recieverId, long messageId,
                          CompanionMessageData data) {
    this.senderId = senderId;
    this.senderName = senderName;
    this.recieverId = recieverId;
    this.messageId = messageId;
    this.data = data;
  }

  public String getSenderId() {
    return senderId;
  }

  public String getSenderName() {
    return senderName;
  }

  public String getRecieverId() {
    return recieverId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CompanionMessage that = (CompanionMessage) o;

    if (messageId != that.messageId) return false;
    if (!senderId.equals(that.senderId)) return false;
    if (!senderName.equals(that.senderName)) return false;
    if (!recieverId.equals(that.recieverId)) return false;
    return data.equals(that.data);
  }

  @Override
  public int hashCode() {
    int result = senderId.hashCode();
    result = 31 * result + senderName.hashCode();
    result = 31 * result + recieverId.hashCode();
    result = 31 * result + (int) (messageId ^ (messageId >>> 32));
    result = 31 * result + data.hashCode();
    return result;
  }

  public long getMessageId() {
    return messageId;
  }

  public CompanionMessageData getData() {
    return data;
  }

  public Data.CompanionMessageProto toProto() {
    return Data.CompanionMessageProto.newBuilder()
        .setHeader(Data.CompanionMessageProto.Header.newBuilder()
            .setSender(Data.CompanionMessageProto.Header.Id.newBuilder()
                .setId(senderId)
                .setName(senderName)
                .build())
            .setReceiver(Data.CompanionMessageProto.Header.Id.newBuilder()
                .setId(recieverId)
                .build())
            .setId(messageId)
            .build())
        .setData(data.toProto())
        .build();
  }

  public static CompanionMessage fromProto(Data.CompanionMessageProto proto) {
    return new CompanionMessage(proto.getHeader().getSender().getId(),
        proto.getHeader().getSender().getName(),
        proto.getHeader().getReceiver().getId(),
        proto.getHeader().getId(),
        CompanionMessageData.fromProto(proto.getData()));
  }

  @Override
  public String toString() {
    return senderName + ": " + messageId + " / " + data;
  }
}
