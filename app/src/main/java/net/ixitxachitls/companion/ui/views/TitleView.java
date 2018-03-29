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

package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

/**
 * Widget for displaying a title and subtitle.
 */
public class TitleView extends LinearLayout {

  // UI elements.
  private TextWrapper<TextView> title;
  private TextWrapper<TextView> subtitle;

  public TitleView(Context context) {
    super(context);
  }

  public TitleView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    init(attributes);
  }

  private void init(AttributeSet attributes) {
    TypedArray array = getContext().obtainStyledAttributes(attributes, R.styleable.TitleView);

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_title, null, false);
    view.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));
    addView(view);

    title = TextWrapper.wrap(view, R.id.title)
        .text(array.getString(R.styleable.TitleView_title));
    subtitle = TextWrapper.wrap(view, R.id.subtitle)
        .text(array.getString(R.styleable.TitleView_subtitle));
    view.setBackgroundColor(array.getColor(R.styleable.TitleView_color,
        getResources().getColor(R.color.white, null)));
    if (array.getBoolean(R.styleable.TitleView_dark, false)) {
      title.textColor(R.color.white);
      subtitle.textColor(R.color.white);
    }
  }

  public void setTitle(String text) {
    title.text(text);
  }

  public void setSubtitle(String text) {
    subtitle.text(text);
  }

  public void setAction(Wrapper.Action action) {
    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        action.execute();
      }
    });
  }
}
