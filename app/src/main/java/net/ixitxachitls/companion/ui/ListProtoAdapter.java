/*
 * Copyright (c) 2017-{2017} Peter Balsiger
 * All rights reserved
s *
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

package net.ixitxachitls.companion.ui;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import net.ixitxachitls.companion.storage.DataBase;

/**
 * An adapter that for a list view with a cursor as input.
 */

public class ListProtoAdapter<P extends MessageLite> extends SimpleCursorAdapter {

  private static final String ID = "_id";
  private P defaultProto;
  private ContentCreator<P> creator;

  @FunctionalInterface
  public interface OnItemClick<P> {
    void click(long id, P proto);
  }

  @FunctionalInterface
  public interface ContentCreator<P> {
    void create(View view, long id, P proto);
  }

  public ListProtoAdapter(Context context, int layout, P defaultProto, ContentCreator<P> creator) {
    super(context, layout, null, new String [] { ID, DataBase.COLUMN_PROTO }, null, 0);
    this.defaultProto = defaultProto;
    this.creator = creator;
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    long id = cursor.getInt(cursor.getColumnIndex(ID));
    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(DataBase.COLUMN_PROTO));
    try {
      @SuppressWarnings("unchecked")
      P proto = (P) defaultProto.getParserForType().parseFrom(bytes);
      view.setTag(proto);
      creator.create(view, id, proto);
    } catch (InvalidProtocolBufferException e) {
      Log.e("proto list", "Cannot parse proto from db");
      e.printStackTrace();
    }
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = super.getView(position, convertView, parent);
    long id = getItemId(position);

    return view;
  }
}
