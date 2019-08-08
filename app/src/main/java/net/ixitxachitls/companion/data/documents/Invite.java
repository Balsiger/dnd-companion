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

import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.data.CompanionContext;

import java.util.Map;

import androidx.annotation.CallSuper;

/**
 * An inviteAction from one user to another to participate in a campaign.
 */
public class Invite extends Document<Invite> {

  private static final DocumentFactory<Invite> FACTORY = () -> new Invite();


  private String campaign;

  @Override
  @CallSuper
  protected void read() {
    super.read();
  }

  @Override
  @CallSuper
  protected Map<String, Object> write(Map<String, Object> data) {
    return data;
  }

  public static void createAndStore(CompanionContext context, String email, String campaignId) {
  }

  protected static Invite fromData(CompanionContext context, DocumentSnapshot snapshot) {
    return Document.fromData(FACTORY, context, snapshot);
  }
}
