/*
 * Copyright (c) 2017-2018 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.documents;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handling of messages between DM and players or between players.
 */
public class Messages extends Documents<Messages> {
  public static final String PATH = "messages";

  private final Map<String, Message> messagesById = new HashMap<>();
  private final Map<String, List<Message>> messagesByOwnerId = new HashMap<>();
  private boolean loaded = false;

  public Messages(CompanionContext context) {
    super(context);
  }

  public boolean isLoaded() {
    return loaded;
  }

  public void deleteMessage(String messageId) {
    delete(messageId);
  }

  public Optional<Message> get(String id) {
    return Optional.ofNullable(messagesById.get(id));
  }

  public List<Message> getMessages(String id) {
    return messagesByOwnerId.getOrDefault(id, Collections.emptyList());
  }

  public void readMessages(String id) {
    if (!messagesByOwnerId.containsKey(id)) {
      messagesByOwnerId.put(id, Collections.emptyList());
      Status.log("reading messages for " + id);
      CollectionReference reference = db.collection(id + "/" + PATH);
      reference.addSnapshotListener((s, e) -> {
        if (e == null) {
          readMessages(id, s.getDocuments());
        } else {
          Status.exception("Cannot read messages!", e);
        }
      });
    }
  }

  public void readMessages(List<String> ids) {
    for (String id : ids) {
      readMessages(id);
    }


    loaded = true;
  }

  private void readMessages(String id, List<DocumentSnapshot> snapshots) {
    List<Message> messages = new ArrayList<>();
    for (DocumentSnapshot snapshot : snapshots) {
      Message message = Message.fromData(context, snapshot);
      messages.add(message);
      messagesById.put(message.getId(), message);
    }

    messagesByOwnerId.put(id, messages);
    updatedDocuments(snapshots);
    CompanionApplication.get().update("messages loaded");
  }
}
