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

import net.ixitxachitls.companion.proto.Data;

/**
 * A message transmitted between client and server.
 */
public class CompanionMessage {
  private final String id;
  private final String name;
  private final Data.CompanionMessageProto proto;

  public CompanionMessage(String id, String name, Data.CompanionMessageProto proto) {
    this.id = id;
    this.name = name;
    this.proto = proto;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Data.CompanionMessageProto getProto() {
    return proto;
  }
}
