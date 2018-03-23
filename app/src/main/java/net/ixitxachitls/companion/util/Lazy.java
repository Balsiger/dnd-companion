/*
 * Copyright (c) 2017-{2017} Peter Balsiger
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
 * along with the Tabletop Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.util;

import android.support.annotation.Nullable;

/**
 * A container for handling a lazy object.
 */

public class Lazy<T> {
  private @Nullable  T object = null;
  private final Data<T> loader;

  @FunctionalInterface
  public interface Data<T> {
    T load();
  }

  public Lazy(Data<T> loader) {
    this.loader = loader;
  }

  public T get() {
    if (object == null)
      object = loader.load();

    return object;
  }
}
