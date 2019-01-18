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

package net.ixitxachitls.companion.data.documents;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.ixitxachitls.companion.Status;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Storage for all dynamic images of entries.
 */
public class Images extends Observable<Documents.Update> {

  public static final int MAX_PX = 500;
  private static final int MAX_SIZE_BYTES = 1024 * 1024;
  private final Map<String, String> imageHashesById = new HashMap<>();
  private final Map<String, Bitmap> imagesById = new HashMap<>();

  private final Cache<String, String> pendingById = CacheBuilder.newBuilder()
      .expireAfterWrite(10, TimeUnit.SECONDS)
      .build();
  private final Cache<String, String> inexistentById = CacheBuilder.newBuilder()
      .expireAfterWrite(1, TimeUnit.MINUTES)
      .build();

  public Images() {}

  public Optional<Bitmap> get(String id) {
    maybeLoad(id);

    return Optional.ofNullable(imagesById.get(id));
  }

  public void set(String id, Bitmap bitmap) {
    pendingById.invalidate(id);
    inexistentById.invalidate(id);
    bitmap = scale(bitmap);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);

    imagesById.put(id, bitmap);
    storage.getReference(id).putBytes(out.toByteArray())
        .addOnSuccessListener(task -> updated(new Documents.Update(id)))
        .addOnFailureListener(e -> Status.silentException("Could not upload image", e));
  }

  private void load(String id) {
    storage.getReference(id).getBytes(MAX_SIZE_BYTES).addOnSuccessListener(bytes -> {
      Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
      imagesById.put(id, bitmap);
      updated(new Documents.Update(Collections.singletonList(id)));
      pendingById.invalidate(id);
    }).addOnFailureListener(e -> {
      Status.silentException("Cannot load file", e);
      imageHashesById.remove(id);
      imagesById.remove(id);
      pendingById.invalidate(id);
    });
  }

  private void maybeLoad(String id) {
    if (pendingById.getIfPresent(id) != null || inexistentById.getIfPresent(id) != null) {
      return;
    }

    pendingById.put(id, id);
    storage.getReference(id).getMetadata()
        .addOnSuccessListener(metadata -> {
          String hash = metadata.getMd5Hash();
          if (!hash.equals(imageHashesById.get(id))) {
            load(id);
            imageHashesById.put(id, hash);
          }
        })
        .addOnFailureListener(e -> {
          inexistentById.put(id, id);
          Status.log("No image for " + id);
        });
  }

  private static Bitmap scale(Bitmap bitmap) {
    // Scale bitmap down if it's too large.
    float factor = (float) MAX_PX
        / (bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight());
    // Scale bitmap to the appropriate size.
    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * factor),
        (int) (bitmap.getHeight() * factor), false);

    // Crop the image to the desired size.
    if (MAX_PX <= scaled.getHeight() && MAX_PX <= scaled.getWidth()) {
      return Bitmap.createBitmap(scaled, 0, 0, MAX_PX, MAX_PX);
    } else {
      return scaled;
    }
  }
}
