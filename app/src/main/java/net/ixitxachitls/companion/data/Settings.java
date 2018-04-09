/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
 *
 * The Roleplay Companion is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Roleplay Companion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Roleplay Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ContentValues;
import android.database.CursorIndexOutOfBoundsException;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Strings;
import com.google.inject.Singleton;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.dynamics.StoredEntry;
import net.ixitxachitls.companion.proto.Entry;
import net.ixitxachitls.companion.storage.DataBase;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.util.Misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * All the settings value of the user.
 */
@Singleton
public class Settings extends StoredEntry<Entry.SettingsProto> {
  public static final String TABLE = "settings";
  private static final int ID = 1;

  private String appId;
  private long lastMessageId = 1;
  private MutableLiveData<Boolean> showStatus = new MutableLiveData<>();
  private boolean remoteCampaigns = false;
  private boolean remoteCharacters = false;
  private List<String> features = new ArrayList<>();

  @VisibleForTesting
  public Settings(Data data, String name, String id) {
    super(data, ID, TABLE, TABLE + "-" + ID, name, true, DataBaseContentProvider.SETTINGS);

    showStatus.setValue(false);
    this.appId = id;
  }

  public List<String> getFeatures() {
    return Collections.unmodifiableList(features);
  }

  public void setFeatures(List<String> features) {
    this.features.clear();
    this.features.addAll(features);
  }

  public boolean isEnabled(String feature) {
    return features.contains(feature);
  }

  public static ContentValues defaultSettings() {
    ContentValues values = new ContentValues();
    values.put(DataBase.COLUMN_ID, ID);
    values.put(DataBase.COLUMN_PROTO,
        Entry.SettingsProto.newBuilder().build().toByteArray());

    return values;
  }

  public static Settings load(Data data) {
    Settings settings;
    try {
      settings = fromProto(data, Entry.SettingsProto.getDefaultInstance().getParserForType()
          .parseFrom(loadBytes(data.getDataBaseAccessor(), ID, DataBaseContentProvider.SETTINGS)));
    } catch (InvalidProtocolBufferException | CursorIndexOutOfBoundsException e) {
      Status.error("Cannot load previous settings, creating new.");
      data.getDataBaseAccessor().insert(DataBaseContentProvider.SETTINGS,
          Settings.defaultSettings());

      settings = new Settings(data, "", "");
    }

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
  public Entry.SettingsProto toProto() {
    return Entry.SettingsProto.newBuilder()
        .setNickname(name)
        .setAppId(appId)
        .setLastMessageId(lastMessageId)
        .setRemoteCampaigns(remoteCampaigns)
        .setRemoteCharacters(remoteCharacters)
        .addAllFeatures(features)
        .build();
  }

  /*
  public static Settings get() {
    Preconditions.checkNotNull(settings);
    return settings;
  }
  */

  private static Settings fromProto(Data data, Entry.SettingsProto proto) {
    Settings settings = new Settings(data, proto.getNickname(), proto.getAppId());
    settings.lastMessageId = proto.getLastMessageId();
    settings.remoteCampaigns = proto.getRemoteCampaigns();
    settings.remoteCharacters = proto.getRemoteCharacters();
    settings.features.addAll(proto.getFeaturesList());

    // Don't use 0 as it could be confused with an unset message id.
    if (settings.lastMessageId == 0) {
      settings.lastMessageId = 1;
    }

    return settings;
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

  public boolean useRemoteCampaigns() {
    return Misc.onEmulator() && remoteCampaigns;
  }

  public boolean useRemoteCharacters() {
    return Misc.onEmulator() && remoteCharacters;
  }

  public String getAppId() {
    return appId;
  }

  public void setNickname(String name) {
    setName(name);
  }

  public void useRemote(boolean remoteCampaigns, boolean remoteCharacters) {
    this.remoteCampaigns = remoteCampaigns;
    this.remoteCharacters = remoteCharacters;
  }

  public long getNextMessageId() {
    lastMessageId++;
    store();

    return lastMessageId;
  }

  public void setDebugStatus(boolean showStatus) {
    this.showStatus.setValue(showStatus);
  }

  public LiveData<Boolean> shouldShowStatus() {
    return showStatus;
  }
}
