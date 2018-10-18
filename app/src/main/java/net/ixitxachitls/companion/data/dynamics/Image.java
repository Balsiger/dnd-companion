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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.protobuf.ByteString;

import net.ixitxachitls.companion.proto.Entry;

import java.io.InputStream;
import java.util.Optional;

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

  public Optional<Bitmap> load() {
    return Optional.empty();
  }

  public void save() {
  }

  public Entry.CompanionMessageProto.Payload.Image toProto() {
    return Entry.CompanionMessageProto.Payload.Image.newBuilder()
        .setType(type)
        .setId(id)
        .setImage(asByteString(bitmap))
        .build();
  }

  public static Image fromProto(Entry.CompanionMessageProto.Payload.Image proto) {
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

}
