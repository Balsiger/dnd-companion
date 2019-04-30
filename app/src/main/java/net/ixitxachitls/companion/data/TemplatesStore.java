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

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import net.ixitxachitls.companion.Status;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Store to access templates of a specific type.
 */
public class TemplatesStore<T extends Entry<? extends MessageLite>> {

  protected final SortedMap<String, T> byName = new TreeMap<>();
  protected final Map<String, T> byNormalizedName = new HashMap<>();
  private final Class<T> entryClass;

  protected TemplatesStore(Class<T> entryClass) {
    this.entryClass = entryClass;
  }

  public List<String> getNames() {
    return new ArrayList<>(byName.keySet());
  }

  public List<T> getValues() {
    return new ArrayList<>(byName.values());
  }

  public Optional<T> get(String name) {
    return Optional.ofNullable(byNormalizedName.get(name.toLowerCase()));
  }

  public void loaded() {
  }

  private MessageLite defaultProto()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    return (MessageLite) entryClass.getMethod("defaultProto").invoke(null, (Object [])null);
  }

  @SuppressWarnings("unchecked")
  private T fromProto(InputStream file)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
      InvalidProtocolBufferException {
    MessageLite proto = defaultProto().getParserForType().parseFrom(file);
    @SuppressWarnings("unchecked")
    T entry = (T) entryClass.getMethod("fromProto", proto.getClass()).invoke(null, proto);
    return entry;
  }

  protected void read(String name, InputStream input)
      throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    T entry = fromProto(input);
    if (entry.getName().isEmpty()) {
      Status.toast("Empty name when reading: '" + name + "'");
    }

    byName.put(entry.getName(), entry);
    byNormalizedName.put(entry.getName().toLowerCase(), entry);
  }
}
