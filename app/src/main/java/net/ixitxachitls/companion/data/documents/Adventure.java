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

import android.support.annotation.CallSuper;

import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.data.CompanionContext;

import java.util.Map;

/**
 * An adventure the campaign plays.
 */
public class Adventure extends Document<Adventure> {

  private static final String FIELD_NAME = "name";

  private static final Document.DocumentFactory<Adventure> FACTORY = () -> new Adventure();

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();

    name = data.get(FIELD_NAME, "");
  }

  @Override
  protected Map<String, Object> write(Map<String, Object> data) {
    data.put(FIELD_NAME, name);

    return data;
  }

  public static Adventure create(CompanionContext context, String campaignId, String name) {
    Adventure adventure = Document.create(FACTORY, context, campaignId + "/" + Adventures.PATH);
    adventure.name = name;

    return adventure;
  }

  protected static Adventure fromData(CompanionContext context, DocumentSnapshot snapshot) {
    return Document.fromData(FACTORY, context, snapshot);
  }
}
