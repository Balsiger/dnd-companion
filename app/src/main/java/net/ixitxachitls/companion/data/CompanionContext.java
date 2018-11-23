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

package net.ixitxachitls.companion.data;

import net.ixitxachitls.companion.data.documents.Adventures;
import net.ixitxachitls.companion.data.documents.Campaigns;
import net.ixitxachitls.companion.data.documents.Characters;
import net.ixitxachitls.companion.data.documents.CreatureConditions;
import net.ixitxachitls.companion.data.documents.Images;
import net.ixitxachitls.companion.data.documents.Invites;
import net.ixitxachitls.companion.data.documents.Messages;
import net.ixitxachitls.companion.data.documents.Monsters;
import net.ixitxachitls.companion.data.documents.User;
import net.ixitxachitls.companion.data.documents.Users;
import net.ixitxachitls.companion.data.values.Encounters;

/**
 * Reference to all global data.
 */
public abstract class CompanionContext {

  protected Users users;
  protected Campaigns campaigns;
  protected Adventures adventures;
  protected Encounters encounters;
  protected Monsters monsters;
  protected Characters characters;
  protected Invites invites;
  protected CreatureConditions conditions;
  protected Messages messages;
  protected Images images;

  protected CompanionContext() {}

  public void loggedIn(User me) {}

  public User me() {
    return users.getMe();
  }

  public Users users() {
    return users;
  }

  public Campaigns campaigns() {
    return campaigns;
  }

  public Characters characters() {
    return characters;
  }

  public Encounters encounters() {
    return encounters;
  }

  public Monsters monsters() {
    return monsters;
  }

  public Invites invites() {
    return invites;
  }

  public CreatureConditions conditions() {
    return conditions;
  }

  public Adventures adventures() {
    return adventures;
  }

  public Messages messages() {
    return messages;
  }

  public Images images() {
    return images;
  }
}
