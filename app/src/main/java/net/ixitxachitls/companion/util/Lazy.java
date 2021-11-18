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

package net.ixitxachitls.companion.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A container for handling a lazy object.
 */
public class Lazy<T> {
  private final Data<T> loader;
  protected Optional<T> data = Optional.empty();

  public Lazy(Data<T> loader) {
    this.loader = loader;
  }

  @FunctionalInterface
  public interface Data<T> {
    T load();
  }

  public T get() {
    if (!data.isPresent()) {
      data = Optional.of(loader.load());
    }

    return data.get();
  }

  public static class State {
    private List<Resettable<?>> lazies = new ArrayList<>();

    public State() {
    }

    public void reset() {
      for (Resettable<?> lazy : lazies) {
        lazy.reset();
      }
    }

    protected void add(Resettable<?> lazy) {
      lazies.add(lazy);
    }
  }

  public static class Resettable<T> extends Lazy<T> {
    public Resettable(State state, Data<T> loader) {
      super(loader);

      state.add(this);
    }

    protected void reset() {
      data = Optional.empty();
    }
  }
}
