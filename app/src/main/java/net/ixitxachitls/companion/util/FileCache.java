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

package net.ixitxachitls.companion.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows to store and retrieve files from a cache.
 */
public class FileCache {

  private Context context;
  private String path;
  private Map<String, String> md5ById = new HashMap<>();

  public FileCache(Context context, String path) {
    this.context = context;
    this.path = path;
  }

  public boolean isOld(String id, int maxAgeHours) {
    if (!getFile(id).exists()) {
      return true;
    }

    return new Date().getTime() - getFile(id).lastModified() > maxAgeHours * 60 * 60 * 1_000;
  }

  public boolean exists(String id) {
    return getFile(id).exists();
  }

  public FileInputStream get(String id) throws IOException {
    return new FileInputStream(getFile(id).getAbsolutePath());
  }

  public String getHash(String id) {
    return "";
  }

  public FileOutputStream write(String id) throws IOException {
    File file = getFile(id);
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    return new FileOutputStream(file);
  }

  private File getFile(String id) {
    return new File(context.getCacheDir(), path + id);
  }
}
