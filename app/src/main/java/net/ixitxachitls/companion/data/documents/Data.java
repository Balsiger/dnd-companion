/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Container for data read from or written to Firebase.
 */
public abstract class Data {

  public String get(String field, String defaultValue) {
    return getRaw(field, defaultValue);
  }

  public int get(String field, int defaultValue) {
    // Firebase stores everything as long.
    return (int) (long) getRaw(field, (long) defaultValue);
  }

  public <T> List<T> getList(String field, List<T> defaultValue) {
    return getRaw(field, defaultValue);
  }

  public <T> Map<String, T> getMap(String field, T defaultValue) {
    return getRaw(field, Collections.emptyMap());
  }

  public Data getNested(String field) {
    return new MapData(getRaw(field, Collections.emptyMap()));
  }

  public List<Data> getNestedList(String field) {
    return getRaw(field, Collections.<Map<String, Object>>emptyList()).stream()
        .map(Data::fromMap)
        .collect(Collectors.toList());
  }

  protected abstract <T> T getRaw(String field, T defaultValue);

  protected static Data fromMap(Map<String, Object> data) {
    return new MapData(data);
  }

  protected static Data fromSnapshot(DocumentSnapshot snapshot) {
    return new SnapshotData(snapshot);
  }

  protected static class SnapshotData extends Data {

    private final DocumentSnapshot snapshot;

    private SnapshotData(DocumentSnapshot snapshot) {
      this.snapshot = snapshot;
    }

    @Override
    protected <T> T getRaw(String field, T defaultValue) {
      T value = (T) snapshot.get(field);
      if (value == null) {
        return defaultValue;
      }

      return value;
    }
  }

  protected static class MapData extends Data {

    private final Map<String, Object> data;

    private MapData(Map<String, Object> data) {
      this.data = data;
    }

    @Override
    protected <T> T getRaw(String field, T defaultValue) {
      T value = (T) data.get(field);
      if (value == null) {
        return defaultValue;
      }

      return value;
    }
  }
}
