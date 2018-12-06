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

  private static final String FIELD_TARGET = "target";
  private static final String FIELD_SOURCE = "source";
  private static final String FIELD_TYPE = "type";
  private static final String FIELD_XP = "xp";
  private static final String FIELD_ITEM = "item";
  private static final String FIELD_TEXT = "text";

  public enum Type { unknown, xp, itemAdd, itemDelete, itemSell, text };

  private static final Document.DocumentFactory<Message> FACTORY = () -> new Message();

  private String targetId;
  private String sourceId;
  private Type type;
  private int xp;
  private Optional<Item> item = Optional.empty();
  private String text = "";

  public static Message createForXp(CompanionContext context, String targetId, int xp) {
    Message message = createBase(context, context.me().getId(), targetId, Type.xp);
    message.xp = xp;
    message.store();

    return message;
  }

  public static Message createForItemAdd(CompanionContext context, String sourceId, String targetId, Item item) {
    Message message = createBase(context, sourceId, targetId, Type.itemAdd);
    message.item = Optional.of(item);
    message.store();

    return message;
  }

  public static Message createForItemDelete(CompanionContext context, String targetId, Item item) {
    Message message = createBase(context, context.me().getId(), targetId, Type.itemDelete);
    message.item = Optional.of(item);
    message.store();

    return message;
  }

  public static Message createForItemSell(CompanionContext context, String sourceId,
                                          String targetId, Item item) {
    Message message = createBase(context, sourceId, targetId, Type.itemSell);
    message.item = Optional.of(item);
    message.store();

    return message;
  }

  public static Message createForText(CompanionContext context, String sourceId,
                                      String targetId, String text) {
    Message message = createBase(context, sourceId, targetId, Type.text);
    message.text = text;
    message.store();

    return message;
  }

  private static Message createBase(CompanionContext context, String sourceId, String targetId,
                                    Type type) {
    Message message = Document.create(FACTORY, context, targetId + "/" + Messages.PATH);
    message.targetId = targetId;
    message.sourceId = sourceId;
    message.type = type;

    return message;
  }

  protected static Message fromData(CompanionContext context, DocumentSnapshot snapshot) {
    return Document.fromData(FACTORY, context, snapshot);
  }

  public String getTargetId() {
    return targetId;
  }

  public Type getType() {
    return type;
  }

  public int getXP() {
    return xp;
  }

  public Optional<Item> getItem() {
    return item;
  }

  public String getText() {
    return text;
  }

  public String getSourceId() {
    return sourceId;
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();

    targetId = get(FIELD_TARGET, "");
    sourceId = get(FIELD_SOURCE, "");
    type = get(FIELD_TYPE, Type.unknown);
    xp = (int) get(FIELD_XP, 0);
    if (has(FIELD_ITEM)) {
      item = Optional.of(Item.read(get(FIELD_ITEM)));
    }
    text = get(FIELD_TEXT, "");
  }

  @Override
  @CallSuper
  protected Map<String, Object> write(Map<String, Object> data) {
    data.put(FIELD_TARGET, targetId);
    data.put(FIELD_SOURCE, sourceId);
    data.put(FIELD_TYPE, type.toString());
    if (xp != 0) {
      data.put(FIELD_XP, xp);
    }
    if (item.isPresent()) {
      data.put(FIELD_ITEM, item.get().write());
    }
    data.put(FIELD_TEXT, text);

    return data;
  }

  @Override
  public String toString() {
    switch (type) {
      case xp:
        return xp + " xp for " + targetId;
      case itemAdd:
        return item.get() + " added for " + targetId;
      case itemDelete:
        return item.get() + " removed for " + targetId;
      case text:
        return text + " sent to " + targetId;
      default:
        return "unknown to " + targetId;
    }
  }
}
