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

package net.ixitxachitls.companion.net;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.data.Settings;
import net.ixitxachitls.companion.data.dynamics.ScheduledMessage;
import net.ixitxachitls.companion.data.dynamics.StoredEntries;
import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Storage for all schedule messages.
 */
public class ScheduledMessages extends StoredEntries<ScheduledMessage> {

  private static final String TAG = "SchdldMsgs";

  private static ScheduledMessages singleton = null;

  protected ScheduledMessages(Context context) {
    super(context, DataBaseContentProvider.MESSAGES, true);
  }

  public static void init(CompanionApplication application) {
    Preconditions.checkArgument(singleton == null);
    singleton = new ScheduledMessages(application);
  }

  public static ScheduledMessages get() {
    Preconditions.checkNotNull(singleton);
    return singleton;
  }

  @Override
  protected Optional<ScheduledMessage> parseEntry(long id, byte[] blob) {
    try {
      return Optional.of(ScheduledMessage.fromProto(id, Data.ScheduledMessageProto
          .getDefaultInstance().getParserForType().parseFrom(blob)));
    } catch (InvalidProtocolBufferException e) {
      Log.e(TAG, "Cannot parse proto for message: " + e);
      Toast.makeText(context, "Cannot parse proto for message: " + e, Toast.LENGTH_LONG);
      return Optional.absent();
    }
  }

  public List<ScheduledMessage> getMessagesByReceiver(String receiverId) {
    List<ScheduledMessage> messages = new ArrayList<>();

    for (ScheduledMessage message : getAll()) {
      if (message.matches(Settings.get().getAppId(), receiverId)) {
        messages.add(message);
      }
    }

    return messages;
  }

  public List<ScheduledMessage> getMessagesBySender(String senderId) {
    List<ScheduledMessage> messages = new ArrayList<>();

    for (ScheduledMessage message : getAll()) {
      if (message.getSenderId().equals(senderId)) {
        messages.add(message);
      }
    }

    return messages;
  }
}
