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

import android.view.View;
import android.widget.ArrayAdapter;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.MiniatureFilter;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledMultiAutocompleteTextView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Strings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog for filtering miniatures.
 */
public class MiniatureFilterDialog extends Dialog<MiniatureFilterDialog, MiniatureFilter> {

  private static final String EMPTY = "(empty)";

  private LabelledEditTextView name;
  private LabelledMultiAutocompleteTextView races;
  private LabelledMultiAutocompleteTextView sets;
  private LabelledMultiAutocompleteTextView types;
  private LabelledMultiAutocompleteTextView classes;
  private LabelledMultiAutocompleteTextView origins;

  protected MiniatureFilter getValue() {
    return new MiniatureFilter(name.getText(),
        parseMulti(races.getText()), parseMulti(sets.getText()), parseMulti(types.getText()),
        parseMulti(classes.getText()), parseMulti(origins.getText()));
  }

  @Override
  protected void createContent(View view) {
    name = view.findViewById(R.id.name);
    name.text(Templates.get().getMiniatureTemplates().getFilter().getName());
    races = view.findViewById(R.id.races);
    races.text(Strings.COMMA_JOINER.join(
        Templates.get().getMiniatureTemplates().getFilter().getRaces()))
        .onFocus(races::showDropDown).threshold(1);
    races.setAdapter(new ArrayAdapter<>(getContext(),
        R.layout.list_item_select,
        showEmpty(Templates.get().getMiniatureTemplates().getRaces())));
    sets = view.findViewById(R.id.sets);
    sets.text(formatMulti(Templates.get().getMiniatureTemplates().getFilter().getSets()))
        .onFocus(sets::showDropDown).threshold(1);
    sets.setAdapter(new ArrayAdapter<>(getContext(),
        R.layout.list_item_select,
        showEmpty(Templates.get().getMiniatureTemplates().getSets())));
    types = view.findViewById(R.id.types);
    types.text(formatMulti(Templates.get().getMiniatureTemplates().getFilter().getTypes()))
        .onFocus(types::showDropDown).threshold(1);
    types.setAdapter(new ArrayAdapter<>(getContext(),
        R.layout.list_item_select,
        showEmpty(Templates.get().getMiniatureTemplates().getTypes())));
    classes = view.findViewById(R.id.classes);
    classes.text(formatMulti(Templates.get().getMiniatureTemplates().getFilter().getClasses()))
        .onFocus(classes::showDropDown).threshold(1);
    classes.setAdapter(new ArrayAdapter<>(getContext(),
        R.layout.list_item_select,
        Templates.get().getMiniatureTemplates().getClasses()));
    origins = view.findViewById(R.id.origins);
    origins.text(formatMulti(Templates.get().getMiniatureTemplates().getFilter().getOrigins()))
        .onFocus(origins::showDropDown).threshold(1);
    origins.setAdapter(new ArrayAdapter<>(getContext(),
        R.layout.list_item_select,
        Templates.get().getMiniatureTemplates().getOrigins()));

    Wrapper.wrap(view, R.id.save).onClick(this::save);
  }

  private String formatMulti(List<String> texts) {
    return Strings.COMMA_JOINER.join(showEmpty(texts));
  }

  private List<String> hideEmpty(List<String> texts) {
    return texts.stream().map(s -> s.equals(EMPTY) ? "" : s).collect(Collectors.toList());
  }

  private List<String> parseMulti(String text) {
    if (text.isEmpty()) {
      return Collections.emptyList();
    }

    return hideEmpty(Arrays.asList(text.split(",\\s*")));
  }

  private List<String> showEmpty(List<String> texts) {
    return texts.stream().map(s -> s.isEmpty() ? EMPTY : s).collect(Collectors.toList());
  }

  public static MiniatureFilterDialog newInstance() {
    MiniatureFilterDialog dialog = new MiniatureFilterDialog();
    dialog.setArguments(arguments(R.layout.dialog_miniatures_filter, "Filter Miniatures",
        R.color.miniature));

    return dialog;
  }
}
