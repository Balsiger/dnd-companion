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

package net.ixitxachitls.companion.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.google.protobuf.MessageLite;

import net.ixitxachitls.companion.data.dynamics.Creature;
import net.ixitxachitls.companion.data.dynamics.LocalCampaign;
import net.ixitxachitls.companion.data.dynamics.LocalCharacter;
import net.ixitxachitls.companion.data.dynamics.RemoteCampaign;
import net.ixitxachitls.companion.data.dynamics.RemoteCharacter;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.proto.Entry;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Fake context for the database, used for testing.
 */
public abstract class FakeDataBaseAccessor implements DataBaseAccessor {

  protected final Map<Long, byte []> localCampaignsById = new HashMap<>();
  protected final Map<Long, byte []> remoteCampaignsById = new HashMap<>();
  protected final Map<Long, byte []> localCharactersById = new HashMap<>();
  protected final Map<Long, byte []> remoteCharactersById = new HashMap<>();
  private final Map<Long, byte []> creaturesById = new HashMap<>();
  private final Map<Long, byte []> messagesById = new HashMap<>();

  public FakeDataBaseAccessor() {
  }

  @Override
  public Cursor queryAll(Uri table) {
    MatrixCursor cursor = new MatrixCursor(DataBase.COLUMNS);
    for (Map.Entry<Long, byte []> entry : table(table).entrySet()) {
      cursor.addRow(new Object[] { entry.getKey(), entry.getValue() });
    }
    return cursor;
  }

  @Override
  public Cursor query(Uri table, long id) {
    MatrixCursor cursor = new MatrixCursor(DataBase.COLUMNS);
    for (Map.Entry<Long, byte []> entry : table(table).entrySet()) {
      if (entry.getKey() == id) {
        cursor.addRow(new Object[]{entry.getKey(), entry.getValue()});
      }
    }
    return cursor;
  }

  @Override
  public void delete(Uri table, long id) {
    table(table).remove(id);
  }

  @Override
  public long insert(Uri table, ContentValues values) {
    long id = 1;
    if (!table(table).isEmpty()) {
      id = table(table).keySet().stream().max(Comparator.naturalOrder()).get() + 1;
    }
    table(table).put(id, values.getAsByteArray(DataBase.COLUMN_PROTO));
    return id;
  }

  @Override
  public void update(Uri table, long id, ContentValues values) {
    table(table).put(id, values.getAsByteArray(DataBase.COLUMN_PROTO));
  }

  private Map<Long, byte []> table(Uri table) {
    switch (table.getEncodedPath()) {
      case "/" + LocalCampaign.TABLE:
        return localCampaignsById;

      case "/" + RemoteCampaign.TABLE:
        return remoteCampaignsById;

      case "/" + LocalCharacter.TABLE:
        return localCharactersById;

      case "/" + RemoteCharacter.TABLE:
        return remoteCharactersById;

      case "/" + Creature.TABLE_LOCAL:
        return remoteCharactersById;

      case "/" + ScheduledMessage.TABLE:
        return messagesById;

      default:
        throw new IllegalArgumentException("Don't know how to handle table "
            + table.getEncodedPath());
    }
  }

  protected static void add(Map<Long, byte[]> table, MessageLite message) {
    table.put(table.size() + 1L, message.toByteArray());
  }

  protected static Entry.SettingsProto settings(String id, String nickName, long lastMessageId) {
    return Entry.SettingsProto.newBuilder()
        .setAppId(id)
        .setNickname(nickName)
        .setLastMessageId(lastMessageId)
        .build();
  }

  protected static Entry.CampaignProto campaign(String campaignId, String name, String world,
                                               boolean published) {
    return Entry.CampaignProto.newBuilder()
        .setId(campaignId)
        .setName(name)
        .setWorld(world)
        .setPublished(published)
        .build();
  }

  protected static Entry.CharacterProto character(String characterId, String name, String campaignId) {
    return Entry.CharacterProto.newBuilder()
        .setCreature(creature(characterId, name, campaignId))
        .build();
  }

  private static Entry.CreatureProto creature(String creatureId, String name, String campaignId) {
    return Entry.CreatureProto.newBuilder()
        .setId(creatureId)
        .setName(name)
        .setCampaignId(campaignId)
        .build();
  }
}
