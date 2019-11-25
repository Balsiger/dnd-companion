/*
 * Copyright (c) 2017-2019 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
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

package net.ixitxachitls.companion;

import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.documents.FakeUser;
import net.ixitxachitls.companion.data.documents.User;

/**
 * Fake implementation of the companion context for tests.
 */
public class FakeCompanionContext extends CompanionContext {

  public FakeCompanionContext() {
    super();
  }

  @Override
  public User me() {
    return new FakeUser();
  }
}
