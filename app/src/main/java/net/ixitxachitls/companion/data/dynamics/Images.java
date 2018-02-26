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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage for all dynamic images of entries.
 */
public class Images {

  private static final String TAG = "Images";

  private static Images local;
  private static Images remote;

  private final Context context;
  private final boolean isLocal;
  private final Map<String, MutableLiveData<Optional<Image>>> imagesByKey = new HashMap<>();

  protected Images(Context context, boolean isLocal) {
    this.context = context;
    this.isLocal = isLocal;
  }

  public static Images local() {
    Preconditions.checkNotNull(local, "local images have to be loaded!");
    return local;
  }

  public static Images remote() {
    Preconditions.checkNotNull(remote, "remote images have to be loaded!");
    return remote;
  }

  public static Images get(boolean local) {
    return local ? Images.local : Images.remote;
  }

  public static void load(Context context) {
    if (local != null) {
      Log.d(TAG, "local images already loaded");
    } else {
      Log.d(TAG, "loading local images");
      local = new Images(context, true);
    }

    if (remote != null) {
      Log.d(TAG, "remote images already loaded");
    } else {
      Log.d(TAG, "loading remote images");
      remote = new Images(context, false);
    }
  }

  public boolean isLocal() {
    return isLocal;
  }

  public boolean hasImage(String id) {
    return imagesByKey.containsKey(id);
  }

  public LiveData<Optional<Image>> getImage(String type, String id) {
    if (!hasImage(id)) {
      MutableLiveData<Optional<Image>> image = new MutableLiveData<>();
      image.setValue(load(type, id));
      imagesByKey.put(id, image);
    }

    return imagesByKey.get(id);
  }

  public static void update(boolean isLocal, Image image) {
    Images.get(isLocal).update(image);
  }

  public void update(Image image) {
    MutableLiveData<Optional<Image>> liveImage = imagesByKey.get(image.getId());
    if (liveImage == null) {
      liveImage = new MutableLiveData<>();
    }

    liveImage.setValue(Optional.of(image));
  }

  private Optional<Image> load(String type, String id) {
    File file = file(type, id);
    try {
      Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
      if (bitmap == null) {
        return Optional.absent();
      }

      return Optional.of(new Image(type, id, bitmap));
    } catch (FileNotFoundException e) {
      return Optional.absent();
    }
  }

  public Optional<InputStream> read(String type, String id) {
    File file = file(type, id);
    try {
      return Optional.of(new FileInputStream(file));
    } catch (FileNotFoundException e) {
      return Optional.absent();
    }
  }

  public void remove(String type, String id) {
    File file = file(type, id);
    file.delete();

    if (imagesByKey.containsKey(id)) {
      imagesByKey.get(id).setValue(Optional.absent());
    }
  }

  public void publishImageFor(Character character) {
    Optional<Image> image = load(Character.TABLE, character.getCharacterId());
    if (image.isPresent()) {
      image.get().publish();
    }
  }

  private File file(String type, String id) {
    return new File(context.getExternalFilesDir(type + (isLocal() ? "-local" : "-remote")),
        id + ".jpg");
  }

  protected File file(Image image) {
    return file(image.getType(), image.getId());
  }
}
