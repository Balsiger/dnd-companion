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

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * The base document for all documents stored in firestore.
 */
public abstract class Document<D extends Document<D>> extends AbstractDocument<D> {

  private final String path;
  private final String id;
  private final List<Action> whenCompleted = new ArrayList<>();
  private final List<Action> whenReady = new ArrayList<>();
  private final List<Action> whenFailed = new ArrayList<>();

  // TODO(merlin): Check which of these are actually needed.
  private final CollectionReference collection;
  private Optional<DocumentReference> reference = Optional.empty();
  private Optional<DocumentSnapshot> snapshot = Optional.empty();
  private boolean failed = false;

  /**
   * Create a new document that is not yet saved and will get an automatic id.
   *
   * @param path The path to the document.
   */
  protected Document(String path) {
    this.id = "";
    this.path = path;
    this.collection = db.collection(path);
  }

  /**
   * Create an existing document that is read from firestore.
   *
   * @param id The id of the document.
   * @param path The path to the document
   */
  protected Document(String id, String path) {
    this.id = id;
    this.path = path;
    this.collection = db.collection(path);
    reference = Optional.of(this.collection.document(id));
    reference.get().get().addOnCompleteListener(this::onComplete);
  }

  /**
   * Create a document from an existing, already ready snapshot from firestore.
   *
   * @param snapshot The snapshot with all the data.
   */
  protected Document(DocumentSnapshot snapshot) {
    this.snapshot = Optional.of(snapshot);
    this.reference = Optional.of(snapshot.getReference());
    this.collection = reference.get().getParent();
    this.path = this.reference.get().getParent().getPath();
    this.id = snapshot.getId();

    read();
    startListening();
  }

  public void onComplete(Task<DocumentSnapshot> task) {
    if (task.isSuccessful() && task.getResult().exists()) {
      snapshot = Optional.of(task.getResult());
      read();
      execute(whenReady);
    } else {
      Status.error("Cannot find data for " + id);
      failed = true;
      execute(whenFailed);
    }
    startListening();
    execute(whenCompleted);
  }

  public String getId() {
    return path + "/" + id;
  }

  public String getPath() {
    return path;
  }

  public boolean isDM(User user) {
    return path.startsWith(user.getEmail());
  }

  public boolean isReady() {
    return snapshot.isPresent();
  }

  public void whenCompleted(Action action) {
    if (snapshot.isPresent() || failed) {
      action.execute();
    } else {
      whenCompleted.add(action);
    }
  }

  public void whenReady(Action action) {
    if (snapshot.isPresent()) {
      action.execute();
    } else {
      whenReady.add(action);
    }
  }

  public void whenFailed(Action action) {
    if (failed) {
      action.execute();
    } else {
      whenFailed.add(action);
    }
  }

  public void store() {
    if (reference.isPresent()) {
      reference.get().set(write(new HashMap<>()));
    } else {
      collection.add(write(new HashMap<>())).addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
          reference = Optional.of(task.getResult());
          reference.get().get().addOnCompleteListener(this::onComplete);
        }
      });
    }
  }

  protected abstract void read();
  protected abstract Map<String, Object> write(Map<String, Object> data);

  private static void execute(List<Action> actions) {
    for (Action action : actions) {
      action.execute();
    }

    actions.clear();
  }

  protected String get(String field, String defaultValue) {
    if (snapshot.isPresent()) {
      String value = snapshot.get().getString(field);
      if (value != null && !value.isEmpty()) {
        return value;
      }
    }

    return defaultValue;
  }

  protected <T> T get(String field, T defaultValue) {
    if (snapshot.isPresent()) {
      T value = (T) snapshot.get().get(field);
      if (value != null) {
        return value;
      }
    }

    return defaultValue;
  }

  protected <T> List<T> get(String field, List<T> defaultValue) {
    if (snapshot.isPresent()) {
      List<T> value = (List<T>) snapshot.get().get(field);
      if (value != null) {
        return value;
      }
    }

    return defaultValue;
  }

  private void startListening() {
    if (reference.isPresent()) {
      reference.get().addSnapshotListener(CompanionApplication.get().getCurrentActivity(),
          this::updated);
    }
  }

  private void updated(@Nullable DocumentSnapshot snapshot,
                       @Nullable FirebaseFirestoreException e) {
    this.snapshot = Optional.ofNullable(snapshot);
    read();
    updated();
  }
}
