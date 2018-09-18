/*
 * Copyright (c) 2017-2018 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.documents;

import java.util.HashMap;
import java.util.Map;

/**
 * Collection allowing access to user data.
 */
public class Users extends Documents<Users> {
  private final User me;

  public Users(User me) {
    this.me = me;

    usersById.put(me.getId(), me);
  }

  private Map<String, User> usersById = new HashMap<>();

  public User getMe() {
    return me;
  }

  public User get(String email) {
    User user = usersById.get(email);
    if (user == null) {
      user = new User(email);
      usersById.put(email, user);
    }

    return user;
  }

  public User fromPath(String path) {
    String email = path.replaceAll(User.PATH + "/(.*?)/.*", "$1");
    return get(email);
  }
}
