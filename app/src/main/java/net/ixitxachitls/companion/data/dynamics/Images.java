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

package net.ixitxachitls.companion.data.dynamics;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.storage.AssetAccessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Storage for all dynamic images of entries.
 */
public class Images {

  private static Images local;
  private static Images remote;

  private final CompanionContext context;
  private final AssetAccessor assetAccessor;
  private final boolean isLocal;
  private final Map<String, MutableLiveData<Optional<Image>>> imagesByKey = new HashMap<>();

  public Images(CompanionContext context, AssetAccessor assetAccessor, boolean isLocal) {
    this.context = context;
    this.assetAccessor = assetAccessor;
    this.isLocal = isLocal;
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

  //public static void update(boolean isLocal, Image image) {
  //  Images.get(isLocal).update(image);
  //}

  public void update(Image image) {
    MutableLiveData<Optional<Image>> liveImage = imagesByKey.get(image.getId());
    if (liveImage == null) {
      liveImage = new MutableLiveData<>();
    }

    liveImage.setValue(Optional.of(image));
  }

  private Optional<Image> load(String type, String id) {
    File file = file(type, id);
    try (InputStream input = new FileInputStream(file)) {
      Bitmap bitmap = BitmapFactory.decodeStream(input);
      if (bitmap != null) {
        return Optional.of(new Image(context, type, id, bitmap));
      }
    } catch (FileNotFoundException e) {
      // File does not exist, just return empty.
    } catch (IOException e) {
      Status.error("Cannot read image file " + file + " (" + e + ")");
    }

    return Optional.empty();
  }

  public Optional<InputStream> read(String type, String id) {
    File file = file(type, id);
    try {
      return Optional.of(new FileInputStream(file));
    } catch (FileNotFoundException e) {
      return Optional.empty();
    }
  }

  public void remove(String type, String id) {
    File file = file(type, id);
    file.delete();

    if (imagesByKey.containsKey(id)) {
      imagesByKey.get(id).setValue(Optional.empty());
    }
  }

  public void publishImageFor(Character character) {
    Optional<Image> image = load(Character.TABLE, character.getCharacterId());
    if (image.isPresent()) {
      image.get().publish();
    }
  }

  private File file(String type, String id) {
    return new File(assetAccessor.getExternalFilesDir(type + (isLocal() ? "-local" : "-remote")),
        id + ".jpg");
  }

  protected File file(Image image) {
    return file(image.getType(), image.getId());
  }
}
