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

package net.ixitxachitls.companion.data.dynamics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Storage for all dynamic images of entries.
 */
public class Images extends StoredEntries {

  private static final String TAG = "Images";

  private static Images singleton;

  protected Images(Context context) {
    super(context);
  }

  public static Images get() {
    Preconditions.checkNotNull(singleton, "Characters have to be loaded!");
    return singleton;
  }

  public static Images load(Context context) {
    if (singleton != null) {
      Log.d(TAG, "images already loaded");
      return singleton;
    }

    Log.d(TAG, "loading images");
    singleton = new Images(context);

    return singleton;
  }

  public void save(String type, String id, Bitmap bitmap) {
    File file = file(type, id);
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file);
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
    } catch (Exception e) {
      Log.e(TAG, "Cannot write image bitmap", e);
    } finally {
      try {
        out.close();
      } catch (IOException e) {
        Log.e(TAG, "Cannot close output image", e);
      }
    }
  }

  public Optional<Bitmap> load(String type, String id) {
    File file = file(type, id);
    try {
      return Optional.of(BitmapFactory.decodeStream(new FileInputStream(file)));
    } catch (FileNotFoundException e) {
      return Optional.absent();
    }
  }

  private File file(String type, String id) {
    return new File(context.getExternalFilesDir(type), id + ".png");
  }
}
