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

package net.ixitxachitls.companion.data.values;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Some auxiliary functions for values.
 */
public class Values {

  public static Map<String, Object> get(Map<String, Object> data, String field) {
    if (data.containsKey(field)) {
      return (Map<String, Object>) data.get(field);
    }

    return Collections.emptyMap();
  }

  public static String get(Map<String, Object> data, String field, String defaultValue) {
    if (data.containsKey(field)) {
      return (String) data.get(field);
    }

    return defaultValue;
  }

  public static long get(Map<String, Object> data, String field, long defaultValue) {
    if (data.containsKey(field)) {
      return (long) data.get(field);
    }

    return defaultValue;
  }

  public static boolean get(Map<String, Object> data, String field, boolean defaultValue) {
    if (data.containsKey(field)) {
      return (boolean) data.get(field);
    }

    return defaultValue;
  }

  public static <E extends Enum<E>> E get(Map<String, Object> data, String field, E defaultValue) {
    if (data.containsKey(field)) {
      return (E) Enum.valueOf(defaultValue.getClass(), (String) data.get(field));
    }

    return defaultValue;
  }

  public static List<String> get(Map<String, Object> data, String field,
                                 List<String> defaultValue) {
    if (data.containsKey(field)) {
      return (List<String>) data.get(field);
    }

    return defaultValue;
  }

  public static boolean has(Map<String, Object> data, String field) {
    return data.containsKey(field);
  }
}
