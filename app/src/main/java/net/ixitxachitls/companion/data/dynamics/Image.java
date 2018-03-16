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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.common.base.Optional;
import com.google.protobuf.ByteString;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.net.CompanionMessenger;
import net.ixitxachitls.companion.proto.Data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Representation of an image.
 */
public class Image {
  public static final int MAX = 500;

  private final String type;
  private final String id;
  private Bitmap bitmap;

  public Image(String type, String id, Bitmap bitmap) {
    this.type = type;
    this.id = id;
    this.bitmap = bitmap;
  }

  public String getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public Bitmap getBitmap() {
    return bitmap;
  }

  public void publish() {
    CompanionMessenger.get().send(this);
  }

  public Optional<Bitmap> load(boolean local) {
    File file = Images.get(local).file(this);
    try {
      return Optional.fromNullable(BitmapFactory.decodeStream(new FileInputStream(file)));
    } catch (FileNotFoundException e) {
      return Optional.absent();
    }
  }

  public void save(boolean local) {
    bitmap = scale(bitmap);

    File file = Images.get(local).file(this);
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
    } catch (Exception e) {
      Status.toast("Cannot write image bitmap: " + e);
    } finally {
      try {
        out.close();
      } catch (IOException e) {
        Status.toast("Cannot close output image" + e);
      }
    }

    Images.get(local).update(this);
    Status.log("Saved image " + type + " " + id);
  }

  public Data.CompanionMessageProto.Payload.Image toProto() {
    return Data.CompanionMessageProto.Payload.Image.newBuilder()
        .setType(type)
        .setId(id)
        .setImage(asByteString(bitmap))
        .build();
  }

  public static Image fromProto(Data.CompanionMessageProto.Payload.Image proto) {
    return new Image(proto.getType(), proto.getId(), asBitmap(proto.getImage()));
  }

  private static ByteString asByteString(Bitmap bitmap) {
    ByteString.Output out = ByteString.newOutput();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
    return out.toByteString();
  }

  private static Bitmap asBitmap(ByteString bytes) {
    return asBitmap(bytes.newInput());
  }

  public static Bitmap asBitmap(InputStream input) {
    return BitmapFactory.decodeStream(input);
  }

  private static Bitmap scale(Bitmap bitmap) {
    // Scale bitmap down if it's too large.
    float factor = (float) MAX
        / (bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight());
    // Scale bitmap to the appropriate size.
    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * factor),
        (int) (bitmap.getHeight() * factor), false);

    // Crop the image to the desired size.
    Bitmap cropped = Bitmap.createBitmap(scaled, 0, 0, MAX, MAX);
    return cropped;
  }
}
