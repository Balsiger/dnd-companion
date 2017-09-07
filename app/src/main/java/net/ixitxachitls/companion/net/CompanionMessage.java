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

import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.proto.Data;

/**
 * A message transmitted between client and server.
 */
public class CompanionMessage {
  private final String senderId;
  private final String senderName;
  private final Data.CompanionMessageProto proto;

  public CompanionMessage(Data.CompanionMessageProto proto) {
    this.senderId = Settings.get().getAppId();
    this.senderName = Settings.get().getNickname();
    this.proto = proto;
  }

  public CompanionMessage(String id, String name, Data.CompanionMessageProto proto) {
    this.senderId = id;
    this.senderName = name;
    this.proto = proto;
  }

  public String getSenderId() {
    return senderId;
  }

  public String getSenderName() {
    return senderName;
  }

  public Data.CompanionMessageProto getProto() {
    return proto;
  }
}
