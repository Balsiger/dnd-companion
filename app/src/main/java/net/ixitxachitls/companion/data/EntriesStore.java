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

package net.ixitxachitls.companion.data;

import android.content.res.AssetManager;

import com.google.common.base.Optional;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Store to access entries of a specific type.
 */
public class EntriesStore<T extends Entry<? extends MessageLite>> {

  private final Class<T> entryClass;
  protected final Map<String, T> byName = new HashMap<>();
  private ArrayList<String> names = null;

  protected EntriesStore(Class<T> entryClass) {
    this.entryClass = entryClass;
  }

  protected void read(AssetManager assetManager, String file)
      throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    T entry = fromProto(assetManager.open(file));
    byName.put(entry.getName(), entry);
    names = null;
  }


  private T fromProto(InputStream file)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
      InvalidProtocolBufferException {
    MessageLite proto = defaultProto().getParserForType().parseFrom(file);
    @SuppressWarnings("unchecked")
    T entry = (T) entryClass.getMethod("fromProto", proto.getClass()).invoke(null, proto);
    return entry;
  }

  private MessageLite defaultProto()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    return (MessageLite) entryClass.getMethod("defaultProto").invoke(null, (Object [])null);
  }

  public Optional<T> get(String name) {
    return Optional.fromNullable(byName.get(name));
  }

  public ArrayList<String> getNames() {
    if (names == null) {
      names = new ArrayList<>(byName.keySet());
      Collections.sort(names);
    }

    return names;
  }
}
