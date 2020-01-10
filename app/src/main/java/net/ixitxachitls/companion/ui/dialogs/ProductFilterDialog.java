/*
 * Copyright (c) 2017-2019 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
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
import android.widget.CheckBox;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.ProductFilter;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledMultiAutocompleteTextView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Strings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Dialog for filtering products.
 */
public class ProductFilterDialog extends Dialog<ProductFilterDialog, ProductFilter> {

  private static final String EMPTY = "(empty)";

  private Optional<ProductFilter> filter = Optional.empty();

  private Wrapper<CheckBox> owned;
  private Wrapper<CheckBox> notOwned;
  private LabelledEditTextView id;
  private LabelledEditTextView title;
  private LabelledEditTextView description;
  private LabelledEditTextView person;
  private LabelledMultiAutocompleteTextView worlds;
  private LabelledMultiAutocompleteTextView producers;
  private LabelledMultiAutocompleteTextView dates;
  private LabelledMultiAutocompleteTextView systems;
  private LabelledMultiAutocompleteTextView types;
  private LabelledMultiAutocompleteTextView audiences;
  private LabelledMultiAutocompleteTextView styles;
  private LabelledMultiAutocompleteTextView layouts;
  private LabelledEditTextView series;

  @Override
  protected ProductFilter getValue() {
    return new ProductFilter(id.getText(), title.getText(), owned.get().isChecked(),
        notOwned.get().isChecked(), description.getText(), person.getText(),
        parseMulti(worlds.getText()), parseMulti(producers.getText()), parseMulti(dates.getText()),
        parseMulti(systems.getText()), parseMulti(types.getText()), parseMulti(audiences.getText()),
        parseMulti(styles.getText()), parseMulti(layouts.getText()), series.getText());
  }

  @Override
  protected void createContent(View view) {
    owned = Wrapper.wrap(view, R.id.owned);
    notOwned = Wrapper.wrap(view, R.id.not_owned);
    id = view.findViewById(R.id.id);
    title = view.findViewById(R.id.title);
    description = view.findViewById(R.id.description);
    person = view.findViewById(R.id.person);
    worlds = view.findViewById(R.id.worlds);
    worlds.onFocus(worlds::showDropDown).threshold(1);
    worlds.setAdapter(new ArrayAdapter<>(getContext(),
        R.layout.list_item_select,
        showEmpty(Templates.get().getProductTemplates().getWorlds())));
    producers = view.findViewById(R.id.producers);
    producers.onFocus(producers::showDropDown).threshold(1);
    producers.setAdapter(new ArrayAdapter<>(getContext(),
        R.layout.list_item_select,
        showEmpty(Templates.get().getProductTemplates().getProducers())));
    dates = view.findViewById(R.id.dates);
    dates.onFocus(dates::showDropDown).threshold(1);
    dates.setAdapter(new ArrayAdapter<>(getContext(),
        R.layout.list_item_select,
        showEmpty(Templates.get().getProductTemplates().getDates())));
    systems = view.findViewById(R.id.systems);
    systems.onFocus(systems::showDropDown).threshold(1);
    systems.setAdapter(new ArrayAdapter<>(getContext(),
        R.layout.list_item_select,
        showEmpty(Templates.get().getProductTemplates().getSystems())));
    types = view.findViewById(R.id.types);
    types.onFocus(types::showDropDown).threshold(1);
    types.setAdapter(new ArrayAdapter<>(getContext(),
        R.layout.list_item_select,
        showEmpty(Templates.get().getProductTemplates().getTypes())));
    audiences = view.findViewById(R.id.audiences);
    audiences.onFocus(audiences::showDropDown).threshold(1);
    audiences.setAdapter(new ArrayAdapter<>(getContext(),
        R.layout.list_item_select,
        showEmpty(Templates.get().getProductTemplates().getAudiences())));
    styles = view.findViewById(R.id.styles);
    styles.onFocus(styles::showDropDown).threshold(1);
    styles.setAdapter(new ArrayAdapter<>(getContext(),
        R.layout.list_item_select,
        showEmpty(Templates.get().getProductTemplates().getStyles())));
    layouts = view.findViewById(R.id.layouts);
    layouts.onFocus(layouts::showDropDown).threshold(1);
    layouts.setAdapter(new ArrayAdapter<>(getContext(),
        R.layout.list_item_select,
        showEmpty(Templates.get().getProductTemplates().getLayouts())));
    series = view.findViewById(R.id.series);

    Wrapper.wrap(view, R.id.save).onClick(this::save);
    Wrapper.wrap(view, R.id.clear).onClick(this::clear);

    if (filter.isPresent()) {
      setupFilter(filter.get());
    } else {
      setupFilter(Templates.get().getProductTemplates().getFilter());
    }
  }

  // TODO(merlin): Check whether we have to remove this and instead set the filter via
  // setArguments().
  private void setFilter(ProductFilter filter) {
    this.filter = Optional.of(filter);
  }

  private void clear() {
    owned.get().setChecked(false);
    notOwned.get().setChecked(false);
    id.text("");
    title.text("");
    description.text("");
    person.text("");
    worlds.text("");
    producers.text("");
    dates.text("");
    systems.text("");
    types.text("");
    audiences.text("");
    styles.text("");
    layouts.text("");
    series.text("");
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

  private void setupFilter(ProductFilter filter) {
    owned.get().setChecked(filter.isOwned());
    notOwned.get().setChecked(filter.isNotOwned());
    id.text(filter.getId());
    title.text(filter.getName());
    description.text(filter.getDescription());
    person.text(filter.getPerson());
    worlds.text(Strings.COMMA_JOINER.join(filter.getWorlds()));
    producers.text(Strings.COMMA_JOINER.join(filter.getProducers()));
    dates.text(Strings.COMMA_JOINER.join(filter.getDates()));
    systems.text(Strings.COMMA_JOINER.join(filter.getSystems()));
    types.text(Strings.COMMA_JOINER.join(filter.getTypes()));
    audiences.text(Strings.COMMA_JOINER.join(filter.getAudiences()));
    styles.text(Strings.COMMA_JOINER.join(filter.getStyles()));
    layouts.text(Strings.COMMA_JOINER.join(filter.getLayouts()));
    series.text(filter.getSeries());
  }

  private List<String> showEmpty(List<String> texts) {
    return texts.stream().map(s -> s.isEmpty() ? EMPTY : s).collect(Collectors.toList());
  }

  public static ProductFilterDialog newInstance(ProductFilter filter) {
    ProductFilterDialog dialog = new ProductFilterDialog();
    dialog.setArguments(arguments(R.layout.dialog_products_filter, "Filter Products",
        R.color.product));
    dialog.setFilter(filter);

    return dialog;
  }
}
