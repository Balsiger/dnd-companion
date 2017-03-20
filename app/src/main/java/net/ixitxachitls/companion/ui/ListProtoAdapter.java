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

package net.ixitxachitls.companion.ui;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import net.ixitachitls.companion.R;
import net.ixitxachitls.companion.storage.DataBase;

/**
 * An adapter that for a list view with a cursor as input.
 */

public class ListProtoAdapter<P extends MessageLite> extends SimpleCursorAdapter {

  private static final String ID = "_id";
  private OnItemClick<P> mClick;
  private P mDefaultProto;
  private Binder<P> mBinder;

  @FunctionalInterface
  public interface OnItemClick<P> {
    void click(long id, P proto);
  }

  @FunctionalInterface
  public interface Binder<P> {
    void bind(View view, P proto);
  }

  public ListProtoAdapter(Context context, int layout, OnItemClick<P> click, P defaultProto,
                          Binder<P> binder) {
    super(context, layout, null, new String [] { ID, DataBase.COLUMN_PROTO }, null, 0);
    this.mClick = click;
    mDefaultProto = defaultProto;
    mBinder = binder;
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(DataBase.COLUMN_PROTO));
    try {
      @SuppressWarnings("unchecked")
      P proto = (P) mDefaultProto.getParserForType().parseFrom(bytes);
      view.setTag(proto);
      mBinder.bind(view, proto);
    } catch (InvalidProtocolBufferException e) {
      Log.e("proto list", "Cannot parse proto from db");
      e.printStackTrace();
    }
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = super.getView(position, convertView, parent);
    long id = getItemId(position);
    view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
      @Override
      @SuppressWarnings("unchecked")
      public void onClick(View v) {
        mClick.click(id, (P) view.getTag());
      }
    });

    return view;
  }
}
