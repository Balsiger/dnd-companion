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

package net.ixitxachitls.companion.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.CursorIndexOutOfBoundsException;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.data.dynamics.StoredEntry;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBase;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.UUID;

/**
 * All the settings value of the user.
 */
@Singleton
public class Settings extends StoredEntry<Data.SettingsProto> {
  public static final String TABLE = "settings";
  public static final int ID = 1;

  private static Settings settings = null;

  private String appId;
  private boolean showStatus = false;

  private Settings(String name) {
    super(ID, String.valueOf(ID), name, true, DataBaseContentProvider.SETTINGS);
  }

  public void setDebugStatus(boolean showStatus) {
    this.showStatus = showStatus;
    store();
  }

  public boolean showStatus() {
    return showStatus;
  }

  public static ContentValues defaultSettings() {
    ContentValues values = new ContentValues();
    values.put(DataBase.COLUMN_ID, ID);
    values.put(DataBase.COLUMN_PROTO,
        Data.SettingsProto.newBuilder().build().toByteArray());

    return values;
  }

  public static Settings init(Context context) {
    settings = load(context).or(new Settings(""));
    settings.ensureAppId();
    return settings;
  }

  private void ensureAppId() {
    if (Strings.isNullOrEmpty(appId)) {
      appId = UUID.randomUUID().toString();
      store();
    }
  }

  @Override
  public Data.SettingsProto toProto() {
    return Data.SettingsProto.newBuilder()
        .setNickname(name)
        .setAppId(appId)
        .setShowStatus(showStatus)
        .build();
  }

  public static Settings get() {
    Preconditions.checkNotNull(settings);
    return settings;
  }

  private static Settings fromProto(Data.SettingsProto proto) {
    Settings settings = new Settings(proto.getNickname());
    settings.appId = proto.getAppId();
    settings.showStatus = proto.getShowStatus();

    return settings;
  }

  private static Optional<Settings> load(Context context) {
    try {
      return Optional.of(fromProto(Data.SettingsProto.getDefaultInstance().getParserForType()
          .parseFrom(loadBytes(context, ID, DataBaseContentProvider.SETTINGS))));
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
      return Optional.absent();
    } catch (CursorIndexOutOfBoundsException e) {
      return Optional.absent();
    }
  }

  public boolean isDefined() {
    return !name.isEmpty();
  }

  public String getNickname() {
    if (name == null || name.isEmpty()) {
      return getAppId();
    }

    return name;
  }

  public String getAppId() {
    return appId;
  }

  public void setNickname(String name) {
    setName(name);
  }
}
