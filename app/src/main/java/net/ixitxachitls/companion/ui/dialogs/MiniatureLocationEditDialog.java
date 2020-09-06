/*
 * Copyright (c) 2017-2019 Peter Balsiger
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
 * along with the Player Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.ui.dialogs;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.MiniatureFilter;
import net.ixitxachitls.companion.data.documents.MiniatureLocation;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

/**
 * Dialog for editing a single miniature location.
 */
public class MiniatureLocationEditDialog
    extends Dialog<MiniatureLocationEditDialog, MiniatureLocation> {

  private static final String ARG_LOCATION = "location";

  private String originalName;
  private String originalLocation;
  private MiniatureLocation location;

  // UI.
  private LabelledEditTextView locationInput;
  private LinearLayout filterContainer;
  private LinearLayout colorContainer;
  private Map<Integer, ImageView> colorViews = new HashMap<>();
  private Wrapper<View> noColor;

  private void setColor(int color) {
    location = location.withColor(color);

    update();
  }

  @Override
  public void save() {
    if (!originalLocation.equals(locationInput.getText())) {
      location = location.withName(locationInput.getText());
      me().replaceLocation(originalName, location);
    }

    super.save();
  }

  private void addFilter() {
    MiniatureFilterDialog.newInstance(new MiniatureFilter(), false).onSaved(this::addFilter)
    .display();
  }

  private void addFilter(MiniatureFilter filter) {
    location.add(filter);
    update();
  }

  @Override
  protected void createContent(View view) {
    originalName = getArguments().getString(ARG_LOCATION);
    Optional<MiniatureLocation> location = me().getLocation(originalName);
    if (location.isPresent()) {
      this.location = location.get();
      this.originalLocation = location.get().getName();
    } else {
      this.location = new MiniatureLocation();
      this.originalLocation = "";
    }

    locationInput = view.findViewById(R.id.location);
    locationInput.text(originalName);
    filterContainer = view.findViewById(R.id.filters);
    colorContainer = view.findViewById(R.id.colors);
    noColor = Wrapper.wrap(view, R.id.color_none).onClick(() -> setColor(0));

    // Colors.
    for (int i = 0; i < colorContainer.getChildCount(); i++) {
      if (colorContainer.getChildAt(i) instanceof ImageView) {
        ImageView colorView = (ImageView) colorContainer.getChildAt(i);
        if (colorView.getDrawable() instanceof ColorDrawable) {
          int color = ((ColorDrawable) colorView.getDrawable()).getColor();
          colorViews.put(color, colorView);
          Wrapper.wrap(colorView).onClick(() -> setColor(color));
        }
      }
    }

    Wrapper.wrap(view, R.id.save).onClick(this::save);
    Wrapper.wrap(view, R.id.add_filter).onClick(this::addFilter);

    update();
  }

  private void delete(MiniatureFilter filter) {
    location.remove(filter);
    update();
  }

  private void select(View view) {
    view.setBackground(new ColorDrawable(getContext().getColor(R.color.location_selected_bright)));
    if (view instanceof TextView) {
      ((TextView) view).setTextColor(getContext().getColor(R.color.white));
    }
  }

  private void unselect(View view) {
    view.setBackground(null);
    if (view instanceof TextView) {
      ((TextView) view).setTextColor(getContext().getColor(R.color.black));
    }
  }

  private void update() {
    filterContainer.removeAllViews();
    for (MiniatureFilter filter : location.getFilters()) {
      filterContainer.addView(new LocationLineView(getContext(), filter));
    }

    unselect(noColor.get());
    for (ImageView view : colorViews.values()) {
      unselect(view);
    }

    if (colorViews.containsKey(location.getColor())) {
      select(colorViews.get(location.getColor()));
    } else {
      select(noColor.get());
    }
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String location) {
    Bundle bundle = arguments(layoutId, titleId, colorId);
    bundle.putString(ARG_LOCATION, location);
    return bundle;
  }

  public static MiniatureLocationEditDialog newInstance(String location) {
    MiniatureLocationEditDialog dialog = new MiniatureLocationEditDialog();
    dialog.setArguments(arguments(R.layout.dialog_miniature_location_edit, R.string.edit_location,
        R.color.miniature, location));
    return dialog;
  }

  private class LocationLineView extends LinearLayout {

    private final TextWrapper<TextView> filterText;

    private MiniatureFilter filter;

    private LocationLineView(Context context, MiniatureFilter filter) {
      super(context);

      this.filter = filter;

      View view =
          LayoutInflater.from(getContext()).inflate(R.layout.fragment_miniature_location_line,
              this, false);

      filterText = TextWrapper.wrap(view, R.id.location).onClick(this::edit);
      Wrapper.wrap(view, R.id.delete).onClick(this::delete);

      filterText.text(filter.getSummary());
      addView(view);
    }

    private void delete() {
      MiniatureLocationEditDialog.this.delete(filter);
    }

    private void edit() {
      MiniatureFilterDialog.newInstance(filter, false)
          .onSaved(this::update).display();
      MiniatureLocationEditDialog.this.update();
    }

    private void update(MiniatureFilter filter) {
      MiniatureLocationEditDialog.this.delete(this.filter);
      MiniatureLocationEditDialog.this.addFilter(filter);
      this.filter = filter;
    }
  }
}
