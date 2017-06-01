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
import com.google.protobuf.ByteString;

import net.ixitxachitls.companion.net.CompanionSubscriber;
import net.ixitxachitls.companion.proto.Data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Storage for all dynamic images of entries.
 */
public class Images {

  private static final String TAG = "Images";
  private static final int MAX = 500;

  private static Images local;
  private static Images remote;

  private final Context context;
  private final boolean isLocal;

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

  public Bitmap saveAndPublish(String campaignId, String type, String id, Bitmap bitmap) {
    bitmap = save(type, id, bitmap);
    publish(campaignId, type, id, bitmap);

    return bitmap;
  }

  public Bitmap save(String type, String id, Bitmap bitmap) {
    bitmap = scale(bitmap);

    File file = file(type, id);
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
    } catch (Exception e) {
      Log.e(TAG, "Cannot write image bitmap", e);
    } finally {
      try {
        out.close();
      } catch (IOException e) {
        Log.e(TAG, "Cannot close output image", e);
      }
    }

    Log.d(TAG, "Saved image " + type + " " + id);
    return bitmap;
  }


  public Optional<Bitmap> load(String type, String id) {
    File file = file(type, id);
    try {
      return Optional.fromNullable(BitmapFactory.decodeStream(new FileInputStream(file)));
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
  }

  private void publish(String campaignId, String type, String id, Bitmap bitmap) {
    CompanionSubscriber.get().publishImage(toProto(campaignId, type, id, bitmap));
  }

  public void publish(String campaignId, String type, String id) {
    Optional<Bitmap> bitmap = load(type, id);
    if (bitmap.isPresent()) {
      publish(campaignId, type, id, bitmap.get());
    }
  }

  private File file(String type, String id) {
    return new File(context.getExternalFilesDir(type + (isLocal() ? "-local" : "-remote")),
        id + ".jpg");
  }

  private Bitmap scale(Bitmap bitmap) {
    // Scale bitmap down if it's too large.
    if (bitmap.getWidth() <= MAX && bitmap.getHeight() <= MAX) {
      return bitmap;
    }

    float factor = (bitmap.getWidth() > bitmap.getHeight()
        ? bitmap.getWidth() : bitmap.getHeight()) / (float) MAX;
    return Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() / factor),
        (int) (bitmap.getHeight() / factor), false);
  }

  private static Data.CompanionMessageProto.Image toProto(String campaignId, String type, String id,
                                                         Bitmap bitmap) {
    return Data.CompanionMessageProto.Image.newBuilder()
        .setCampaignId(campaignId)
        .setType(type)
        .setId(id)
        .setImage(Images.asByteString(bitmap))
        .build();
  }

  public static ByteString asByteString(Bitmap bitmap) {
    ByteString.Output out = ByteString.newOutput();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 0, out);
    return out.toByteString();
  }

  public static Bitmap asBitmap(ByteString bytes) {
    return asBitmap(bytes.newInput());
  }

  public static Bitmap asBitmap(InputStream input) {
    return BitmapFactory.decodeStream(input);
  }
}
