/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

package net.ixitxachitls.companion.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.MiniatureFilter;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.SortedSet;

/**
 * Dialog for editing a single miniature location.
 */
public class MiniatureLocationEditDialog extends Dialog {

  private static final String ARG_LOCATION = "location";
  private LabelledEditTextView locationInput;
  private SortedSet<MiniatureFilter> filters;
  private LinearLayout filterContainer;

  private void add(MiniatureFilter filter) {
    filters.add(filter);
    update();
  }

  private void addFilter() {
    MiniatureFilterDialog.newInstance(new MiniatureFilter("dialog")).onSaved(this::addedFilter)
    .display();
  }

  private void addedFilter(MiniatureFilter filter) {
    filters.add(filter);
    update();
  }

  @Override
  protected void createContent(View view) {
    String location = getArguments().getString(ARG_LOCATION);
    filters = me().getLocationFilters(location);

    locationInput = view.findViewById(R.id.location);
    locationInput.text(location);
    filterContainer = view.findViewById(R.id.filters);
    Wrapper.wrap(view, R.id.save).onClick(this::save);
    Wrapper.wrap(view, R.id.add_filter).onClick(this::addFilter);

    update();
  }

  @Override
  public void save() {
    super.save();

    me().setLocation(locationInput.getText(), filters);
  }

  private void delete(MiniatureFilter filter) {
    filters.remove(filter);
    update();
  }

  private void update() {
    filterContainer.removeAllViews();
    for (MiniatureFilter filter : filters) {
      filterContainer.addView(new LocationLineView(getContext(), filter));
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
      MiniatureFilterDialog.newInstance(filter)
          .onSaved(this::update).display();
      MiniatureLocationEditDialog.this.update();
    }

    private void update(MiniatureFilter filter) {
      MiniatureLocationEditDialog.this.delete(this.filter);
      MiniatureLocationEditDialog.this.add(filter);
      this.filter = filter;
    }
  }
}
