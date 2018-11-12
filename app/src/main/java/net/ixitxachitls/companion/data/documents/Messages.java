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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handling of messages between DM and players or between players.
 */
public class Messages extends Documents<Messages> {
  protected static final String PATH = "messages";

  private final Map<String, List<Message>> messagesByCharacterId = new HashMap<>();

  public Messages(CompanionContext context) {
    super(context);
  }

  public List<Message> getMessages(String creatureId) {
    return messagesByCharacterId.getOrDefault(creatureId, Collections.emptyList());
  }

  public void readMessages(List<String> characterIds) {
    for (String characterId : characterIds) {
      readMessages(characterId);
    }
  }

  public void readMessages(String characterId) {
    if (!messagesByCharacterId.containsKey(characterId)) {
      CollectionReference reference = db.collection(characterId + "/" + PATH);
      reference.addSnapshotListener((s, e) -> {
        if (e == null) {
          readMessages(characterId, s.getDocuments());
        } else {
          Status.exception("Cannot read messages!", e);
        }
      });
    }
  }

  public void deleteMessage(String messageId) {
    delete(messageId);
  }

  private void readMessages(String characterId, List<DocumentSnapshot> snapshots) {
    List<Message> messages = new ArrayList<>();
    for (DocumentSnapshot snapshot : snapshots) {
      messages.add(Message.fromData(context, snapshot));
    }

    messagesByCharacterId.put(characterId, messages);
    updated();
  }
}
