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

import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.util.Strings;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.CallSuper;

/**
 * Base class for all document collections.
 */
public abstract class Documents<D extends Documents<D>> extends Observable<Documents.Update> {

  public final static Update FULL_UPDATE = new FullUpdate();

  protected final CompanionContext context;

  protected Documents(CompanionContext context) {
    this.context = context;
  }

  public CompanionContext getContext() {
    return context;
  }

  @CallSuper
  protected void delete(String id) {
    db.document(id).delete();
  }

  protected void updated(String id) {
    updated(Collections.singletonList(id));
  }

  protected void updated(List<String> ids) {
    if (!ids.isEmpty()) {
      updated(new Update(ids));
    }
  }

  protected void updatedDocuments(List<DocumentSnapshot> snapshots) {
    updated(snapshots.stream().map(d -> d.getReference().getPath()).collect(Collectors.toList()));
  }

  public static class Update {
    List<String> ids;

    public Update(List<String> ids) {
      this.ids = ids;
    }

    public Update(String id) {
      this.ids = Collections.singletonList(id);
    }

    public boolean hasId(String id) {
      return ids.contains(id);
    }

    public boolean hasSupId(String sup) {
      for (String id : ids) {
        if (id.startsWith(sup)) {
          return true;
        }
      }

      return false;
    }

    public boolean hasType(String type) {
      for (String id: ids) {
        if (Character.isA(id, type)) {
          return true;
        }
      }

      return false;
    }

    @Override
    public String toString() {
      return Strings.COMMA_JOINER.join(ids);
    }
  }

  private static class FullUpdate extends Update {
    private FullUpdate() {
      super(Collections.emptyList());
    }

    @Override
    public boolean hasId(String id) {
      return true;
    }

    @Override
    public boolean hasSupId(String sup) {
      return true;
    }
  }
}
