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
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Message;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

/**
 * Widget for displaying a title and subtitle.
 */
public class TitleView extends LinearLayout {

  protected final int foregroundColor;
  protected final int backgroundColor;
  protected final int defaultImage;
  protected boolean hasImage = false;

  // UI elements.
  protected LinearLayout container;
  protected TextWrapper<TextView> title;
  protected TextWrapper<TextView> subtitle;
  protected RoundImageView image;
  protected UpdatableViewGroup<LinearLayout, ImageView, Integer> icons;
  protected UpdatableViewGroup<LinearLayout, MessageView, Message> messages;

  public TitleView(Context context) {
    this(context, null);
  }

  public TitleView(Context context, @Nullable AttributeSet attributes) {
    this(context, attributes, 0, 0, 0);
  }

  public TitleView(Context context, @Nullable AttributeSet attributes,
                   @ColorRes int foregroundColor, @ColorRes int backgroundColor,
                   @DrawableRes int defaultImage) {
    super(context, attributes);
    this.foregroundColor = foregroundColor;
    this.backgroundColor = backgroundColor;
    this.defaultImage = defaultImage;

    init(attributes);
  }

  public boolean hasImage() {
    return hasImage;
  }

  public void setAction(Wrapper.Action action) {
    container.setOnClickListener(v -> action.execute());
  }

  public void setDefaultImage(@DrawableRes int drawable) {
    image.setImageDrawable(getContext().getDrawable(drawable));
    hasImage = false;
  }

  public void setImageAction(Wrapper.Action action) {
    image.setOnClickListener(v -> action.execute());
  }

  public void setImageBitmap(Bitmap bitmap) {
    hasImage = true;
    image.setImageBitmap(bitmap);
  }

  public void setSubtitle(String text) {
    subtitle.text(text);
  }

  public void setTitle(String text) {
    title.text(text);
  }

  public void clearImage(@DrawableRes int defaultDrawable) {
    image.clearImage(defaultDrawable);
  }

  public void loadImageUrl(String url) {
    image.loadImageUrl(url);
  }

  public void removeAction() {
    setOnClickListener(null);
  }

  public void removeImageAction() {
    image.setOnClickListener(null);
  }

  public void updateIcons() {
    icons.ensureOnly(iconDrawableResources(), this::createIcon);
  }

  protected ImageView createIcon(@DrawableRes int id) {
    ImageView icon = new ImageView(getContext());
    icon.setImageDrawable(getContext().getDrawable(id));
    icon.setMaxWidth(50);
    icon.setMaxHeight(50);
    icon.setAdjustViewBounds(true);

    return icon;
  }

  protected @Nullable MessageView createMessageIcon(Message message) {
    return null;
  }

  protected List<Integer> iconDrawableResources() {
    return new ArrayList<>();
  }

  @CallSuper
  protected View init(AttributeSet attributes) {
    TypedArray array = getContext().obtainStyledAttributes(attributes, R.styleable.TitleView);

    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_title, null, false);
    // Ensure the view uses the full width.
    view.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
    LinearLayout.LayoutParams.WRAP_CONTENT));
    addView(view);

    container = view.findViewById(R.id.container);
    container.setBackgroundColor(array.getColor(R.styleable.TitleView_color,
        getResources().getColor(R.color.white, null)));
    title = TextWrapper.wrap(view, R.id.title)
        .text(array.getString(R.styleable.TitleView_title));
    subtitle = TextWrapper.wrap(view, R.id.subtitle)
        .text(array.getString(R.styleable.TitleView_subtitle));
    image = view.findViewById(R.id.image);
    icons = new UpdatableViewGroup<>(view.findViewById(R.id.icons));
    messages = new UpdatableViewGroup<>(view.findViewById(R.id.messages));
    if (array.getBoolean(R.styleable.TitleView_dark, false)) {
      title.textColor(R.color.white);
      subtitle.textColor(R.color.white);
    }

    return view;
  }

  protected List<Message> messageIcons() {
    return new ArrayList<>();
  }

  protected void updateMessages() {
    messages.ensureOnly(messageIcons(), this::createMessageIcon);
  }
}
