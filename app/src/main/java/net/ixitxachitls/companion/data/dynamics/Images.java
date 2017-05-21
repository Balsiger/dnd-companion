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

/**
 * Storage for all dynamic images of entries.
 */
public class Images extends StoredEntries {

  private static final String TAG = "Images";
  private static final int MAX = 500;

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
    return new File(context.getExternalFilesDir(type), id + ".jpg");
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
    return BitmapFactory.decodeStream(bytes.newInput());
  }
}
