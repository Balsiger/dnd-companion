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

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * The base document for all documents stored in firestore.
 */
public abstract class Document<D extends Document<D>> extends Observable<D> {

  protected CompanionContext context;
  protected boolean temporary = true;
  protected String path;
  protected String id;
  protected CollectionReference collection;
  protected Optional<DocumentReference> reference = Optional.empty();
  protected Optional<DocumentSnapshot> snapshot = Optional.empty();
  private boolean failed = false;
  private final List<Action> whenCompleted = new ArrayList<>();
  private final List<Action> whenReady = new ArrayList<>();
  private final List<Action> whenFailed = new ArrayList<>();

  /**
   * Create a new document that is not yet saved and will get an automatic id.
   */
  protected static <D extends Document<D>> D create(DocumentFactory<D> factory,
                                                    CompanionContext context, String path) {
    D document = factory.create();
    document.context = context;
    document.id = "";
    document.path = path;
    document.collection = document.db.collection(path);
    document.temporary = false;

    return document;
  }

  protected static <D extends Document<D>> D get(DocumentFactory<D> factory,
                                                 CompanionContext context,
                                                 String id) {
    D document = getOrCreate(factory, context, id);
    document.temporary = true;

    return document;
  }

  protected static <D extends Document<D>> D getOrCreate(DocumentFactory<D> factory,
                                                         CompanionContext context,
                                                         String id) {
    D document = factory.create();
    document.context = context;
    document.id = extractId(id);
    document.path = extractPath(id);
    document.collection = document.db.collection(document.path);
    document.reference = Optional.of(document.collection.document(document.id));
    document.reference.get().get().addOnCompleteListener(document::onComplete);
    document.temporary = false;

    return document;
  }

  protected static <D extends Document<D>> D fromData(DocumentFactory<D> factory,
                                                      CompanionContext context,
                                                      DocumentSnapshot snapshot) {
    D document = factory.create();
    document.context = context;
    document.id = snapshot.getId();
    document.path = snapshot.getReference().getParent().getPath();
    document.collection = snapshot.getReference().getParent();
    document.reference = Optional.of(snapshot.getReference());
    document.snapshot = Optional.ofNullable(snapshot);
    document.temporary = false;

    document.read();
    document.startListening();

    return document;
  }

  public void onComplete(Task<DocumentSnapshot> task) {
    if (task.isSuccessful() && task.getResult().exists()) {
      snapshot = Optional.of(task.getResult());
      temporary = false;
      read();
      execute(whenReady);
      updated();
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

  public CompanionContext getContext() {
    return context;
  }

  public boolean isDM(User user) {
    return path.startsWith(user.getId());
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
    if (temporary) {
      throw new IllegalStateException("This temporary document cannot be stored.");
    }

    if (reference.isPresent()) {
      reference.get().set(write(new HashMap<>()));
    } else {
      collection.add(write(new HashMap<>())).addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
          reference = Optional.of(task.getResult());
          reference.get().get().addOnCompleteListener(this::onComplete);
        } else {
          Status.error("Writing failed for " + this);
        }
      });
    }
  }

  @CallSuper
  protected void read() {
    if (snapshot.isPresent()) {
      this.id = snapshot.get().getId();
    }
  }
  protected abstract Map<String, Object> write(Map<String, Object> data);

  private static void execute(List<Action> actions) {
    for (Action action : actions) {
      action.execute();
    }

    actions.clear();
  }

  protected boolean has(String field) {
    return snapshot.isPresent() && snapshot.get().get(field) != null;
  }

  protected Map<String, Object> get(String field) {
    if (snapshot.isPresent()) {
      return (Map<String, Object>) snapshot.get().get(field);
    }

    return Collections.emptyMap();
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

  protected long get(String field, long defaultValue) {
    if (snapshot.isPresent()) {
      Long value = snapshot.get().getLong(field);
      if (value != null) {
        return value;
      }
    }

    return defaultValue;
  }
  protected boolean get(String field, boolean defaultValue) {
    if (snapshot.isPresent()) {
      Boolean value = snapshot.get().getBoolean(field);
      if (value != null) {
        return value;
      }
    }

    return defaultValue;
  }

  public <E extends Enum<E>> E get(String field, E defaultValue) {
    if (snapshot.isPresent()) {
      String value = snapshot.get().getString(field);
      if (value != null && !value.isEmpty()) {
        return (E) Enum.valueOf(defaultValue.getClass(), (String) value);
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

  protected void startListening() {
    if (reference.isPresent()) {
      reference.get().addSnapshotListener(CompanionApplication.get().getCurrentActivity(),
          this::updated);
    }
  }

  private void updated(@Nullable DocumentSnapshot snapshot,
                       @Nullable FirebaseFirestoreException e) {
    if (e != null) {
      Status.exception("Cannot update document", e);
    } else {
      this.snapshot = Optional.ofNullable(snapshot);
      if (this.snapshot.isPresent() && this.snapshot.get().exists()) {
        read();
        updated();
      }
    }
  }

  protected static String extractId(String id) {
    return id.replaceAll(".*/", "");
  }

  protected static String extractPath(String id) {
    return id.replaceAll("/[^/]*$", "");
  }

  protected interface DocumentFactory<D> {
    D create();
  }
}
