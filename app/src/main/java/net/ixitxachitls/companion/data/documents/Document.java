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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import androidx.annotation.CallSuper;

/**
 * The base document for all documents stored in firestore.
 */
public abstract class Document<D extends Document<D>> extends Observable<D> {

  private final List<Action> whenCompleted = new ArrayList<>();
  private final List<Action> whenReady = new ArrayList<>();
  private final List<Action> whenFailed = new ArrayList<>();
  protected CompanionContext context;
  protected boolean temporary = true;
  protected String path;
  protected String id;
  protected CollectionReference collection;
  protected Optional<DocumentReference> reference = Optional.empty();
  protected Optional<DocumentSnapshot> snapshot = Optional.empty();
  protected Data data = Data.empty();
  private boolean failed = false;

  @FunctionalInterface
  protected interface DocumentFactory<D> {
    D create();
  }

  @FunctionalInterface
  public interface Callback {
    void done();
  }

  public CompanionContext getContext() {
    return context;
  }

  public String getId() {
    return path + "/" + id;
  }

  public String getPath() {
    return path;
  }

  public String getShortId() {
    return id;
  }

  public boolean isReady() {
    return snapshot.isPresent();
  }

  public boolean isDM(User user) {
    return path.startsWith(user.getId());
  }

  public boolean hasId() {
    return !id.isEmpty();
  }

  @SuppressWarnings("unchecked")
  public void onUpdate(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
    if (snapshot != null || e == null) {
      this.snapshot = Optional.of(snapshot);
      this.data = Data.fromSnapshot(snapshot);
      temporary = false;
      read();
      execute(whenReady);
      updated((D) this);
      CompanionApplication.get().update("document " + getShortId() + " updated");
    } else {
      Status.error("Cannot find data for " + id);
      failed = true;
      execute(whenFailed);
    }
    startListening();
    execute(whenCompleted);
  }

  @SuppressWarnings("unchecked")
  public void store() {
    if (temporary) {
      throw new IllegalStateException("This temporary document cannot be stored.");
    }

    if (reference.isPresent()) {
      reference.get().set(write().asMap());
      CompanionApplication.get().update(getClass().getCanonicalName() + " updated");
      updated((D) this);
      CompanionApplication.get().update("document " + getShortId() + " updated");
    } else {
      collection.add(write().asMap()).addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
          reference = Optional.of(task.getResult());
          reference.get().addSnapshotListener(this::onUpdate);
        } else {
          Status.error("Writing failed for " + this);
        }
      });
    }
  }

  public void whenCompleted(Action action) {
    if (snapshot.isPresent() || failed) {
      action.execute();
    } else {
      whenCompleted.add(action);
    }
  }

  public void whenFailed(Action action) {
    if (failed) {
      action.execute();
    } else {
      whenFailed.add(action);
    }
  }

  public void whenReady(Action action) {
    if (snapshot.isPresent()) {
      action.execute();
    } else {
      whenReady.add(action);
    }
  }

  @CallSuper
  protected void read() {
    if (snapshot.isPresent()) {
      this.id = snapshot.get().getId();
    }
  }

  protected void startListening() {
    /*
    if (reference.isPresent()) {
      reference.get().addSnapshotListener(CompanionApplication.get().getCurrentActivity(),
          this::updated);
    }
    */
  }

  @SuppressWarnings("unchecked")
  private void updated(@Nullable DocumentSnapshot snapshot,
                       @Nullable FirebaseFirestoreException e) {
    if (e != null) {
      Status.exception("Cannot update document", e);
    } else {
      this.snapshot = Optional.ofNullable(snapshot);
      if (this.snapshot.isPresent() && this.snapshot.get().exists()) {
        data = Data.fromSnapshot(snapshot);
        read();
        updated((D) this);
      } else {
        data = Data.empty();
      }
    }
  }

  protected abstract Data write();

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

  /**
   * Create a new document that is not yet saved with a given id.
   */
  protected static <D extends Document<D>> D createWithId(DocumentFactory<D> factory,
                                                          CompanionContext context, String id) {
    D document = factory.create();
    document.context = context;
    document.reference = Optional.of(document.db.document(id));
    document.id = document.reference.get().getId();
    document.collection = document.reference.get().getParent();
    document.path = document.collection.getPath();
    document.temporary = false;

    return document;
  }

  private static void execute(List<Action> actions) {
    for (Action action : actions) {
      action.execute();
    }

    actions.clear();
  }

  protected static String extractId(String id) {
    return id.replaceAll(".*/", "");
  }

  protected static String extractPath(String id) {
    return id.replaceAll("/[^/]*$", "");
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
    document.reference.get().addSnapshotListener(document::onUpdate);
    document.snapshot = Optional.ofNullable(snapshot);
    document.data = snapshot == null ? Data.empty() : Data.fromSnapshot(snapshot);
    document.temporary = false;

    document.read();
    document.startListening();

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
    document.reference.get().addSnapshotListener(document::onUpdate);
    document.temporary = false;

    return document;
  }

  protected static boolean isA(String id, String type) {
    return id.contains("/" + type + "/");
  }
}
