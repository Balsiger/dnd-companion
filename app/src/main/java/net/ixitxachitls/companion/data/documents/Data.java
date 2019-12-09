/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.Status;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Container for data read from or written to Firebase.
 */
public abstract class Data {

  @FunctionalInterface
  public interface Factory<T> {
    T create(Data values);
  }

  @FunctionalInterface
  public interface Condition<T> {
    boolean is(T value);
  }

  @FunctionalInterface
  public interface Accessor<S, T> {
    S get(T value);
  }

  public <T> T get(String field, T defaultValue) {
    return getRaw(field, defaultValue);
  }

  @SuppressWarnings("unchecked")
  public <E extends Enum<E>> E get(String field, E defaultValue) {
    String raw = getRaw(field, defaultValue.toString());
    return (E) Enum.valueOf(defaultValue.getClass(), raw);
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
    try {
      return new MapData(getRaw(field, Collections.emptyMap()));
    } catch (ClassCastException e) {
      Status.error("Error converting data for " + field);
      return empty();
    }
  }

  public List<Data> getNestedList(String field) {
    return getRaw(field, Collections.<Map<String, Object>>emptyList()).stream()
        .map(Data::fromMap)
        .collect(Collectors.toList());
  }

  public abstract boolean has(String field);

  public <T> Optional<T> read(String field, Factory<T> factory) {
    if (has(field)) {
      return Optional.of(factory.create(getNested(field)));
    }

    return Optional.empty();
  }

  public <T> Data set(String field, Optional<T> value) {
    if (value.isPresent()) {
      set(field, value.get());
    }
    return this;
  }

  public <S, T> Data set(String field, Optional<T> value, Accessor<S, T> accessor) {
    if (value.isPresent()) {
      set(field, accessor.get(value.get()));
    }
    return this;
  }

  public <T extends NestedDocument> Data setNested(String field, T value) {
    if (!field.isEmpty() && !value.toString().isEmpty()) {
      set(field, value.write());
    }
    return this;
  }

  public <T extends NestedDocument> Data setNested(String field, Optional<T> value) {
    if (value.isPresent()) {
      setNested(field, value.get());
    }
    return this;
  }

  public <T> Data setIf(String field, T value, Condition<T> condition) {
    if (condition.is(value)) {
      set(field, value);
    }
    return this;
  }

  public <T extends NestedDocument> Data setNested(String field, Collection<T> values) {
    if (!values.isEmpty()) {
      set(field, values.stream().map(NestedDocument::write).collect(Collectors.toList()));
    }
    return this;
  }

  public <T extends Data> Data set(String field, List<T> values) {
    if (!values.isEmpty()) {
      set(field, values.stream().map(Data::asMap).collect(Collectors.toList()));
    }
    return this;
  }

  public <T> Data set(String field, T value) {
    setRaw(field, value);
    return this;
  };

  public <T extends Data> Data set(String field, T data) {
    set(field, data.asMap());
    return this;
  }

  public abstract Map<String, Object> asMap();

  protected abstract <T> T getRaw(String field, T defaultValue);

  protected abstract <T> void setRaw(String field, T value);

  public static Data empty() {
    return new MapData(new HashMap<>());
  }

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
    public boolean has(String field) {
      return snapshot.get(field) != null;
    }

    @Override
    public Map<String, Object> asMap() {
      throw new IllegalStateException("SnapshotData cannot be converted to a map!");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T getRaw(String field, T defaultValue) {
      try {
        T value = (T) snapshot.get(field);
        if (value != null) {
          return value;
        }
      } catch (ClassCastException e) {
        Status.error("Error converting data for " + field);
      }

      return defaultValue;
    }

    @Override
    protected <T> void setRaw(String field, T value) {
      throw new IllegalStateException("SnapshotData cannot set data!");
    }
  }

  protected static class MapData extends Data {

    private final Map<String, Object> data;

    private MapData(Map<String, Object> data) {
      this.data = data;
    }

    @Override
    public boolean has(String field) {
      return data.get(field) != null;
    }

    @Override
    public Map<String, Object> asMap() {
      return data;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T getRaw(String field, T defaultValue) {
      try {
        T value = (T) data.get(field);
        if (value != null) {
          return value;
        }
      } catch (ClassCastException e) {
        Status.error("Error converting data for " + field);
      }

      return defaultValue;
    }

    @Override
    protected <T> void setRaw(String field, T value) {
      data.put(field, value);
    }
  }
}
