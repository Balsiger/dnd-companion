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
import net.ixitxachitls.companion.data.values.Item;

import java.util.Map;
import java.util.Optional;

/**
 * A message given to a character from the DM or another character.
 */
public class Message extends Document<Message> {

  private static final String FIELD_CREATURE = "creature";
  private static final String FIELD_SOURCE = "source";
  private static final String FIELD_XP = "xp";
  private static final String FIELD_ITEM = "item";

  private static final Document.DocumentFactory<Message> FACTORY = () -> new Message();

  private String creatureId;
  private String sourceId;
  private int xp;
  private Optional<Item> item = Optional.empty();

  public static Message createForXp(CompanionContext context, String creatureId, int xp) {
    Message message = Document.create(FACTORY, context, creatureId + "/" + Messages.PATH);
    message.creatureId = creatureId;
    message.xp = xp;
    message.store();

    return message;
  }

  public static Message createForItem(CompanionContext context, String creatureId, Item item,
                                      String sourceId) {
    Message message = Document.create(FACTORY, context, creatureId + "/" + Messages.PATH);
    message.creatureId = creatureId;
    message.item = Optional.of(item);
    message.sourceId = sourceId;
    message.store();

    return message;
  }

  protected static Message fromData(CompanionContext context, DocumentSnapshot snapshot) {
    return Document.fromData(FACTORY, context, snapshot);
  }

  public String getCreatureId() {
    return creatureId;
  }

  public boolean isXP() {
    return xp != 0;
  }

  public int getXP() {
    return xp;
  }

  public boolean isItem() {
    return item.isPresent();
  }

  public Optional<Item> getItem() {
    return item;
  }

  public String getSourceId() {
    return sourceId;
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();

    creatureId = get(FIELD_CREATURE, "");
    sourceId = get(FIELD_SOURCE, "");
    xp = (int) get(FIELD_XP, 0);
    if (has(FIELD_ITEM)) {
      item = Optional.of(Item.read(get(FIELD_ITEM)));
    }
  }

  @Override
  @CallSuper
  protected Map<String, Object> write(Map<String, Object> data) {
    data.put(FIELD_CREATURE, creatureId);
    data.put(FIELD_SOURCE, sourceId);
    if (xp != 0) {
      data.put(FIELD_XP, xp);
    }
    if (item.isPresent()) {
      data.put(FIELD_ITEM, item.get().write());
    }

    return data;
  }

  @Override
  public String toString() {
    if (isXP()) {
      return xp + " for " + creatureId;
    }
    if (isItem()) {
      return item + " for " + creatureId;
    }

    return creatureId;
  }
}
