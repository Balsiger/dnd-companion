/*
 * Copyright (c) 2017-2018 Peter Balsiger
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

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.dynamics.BaseCreature;
import net.ixitxachitls.companion.data.dynamics.Item;
import net.ixitxachitls.companion.data.statics.ItemTemplate;
import net.ixitxachitls.companion.data.values.Duration;
import net.ixitxachitls.companion.data.values.Money;
import net.ixitxachitls.companion.ui.views.LabelledAutocompleteTextView;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledMultiAutocompleteTextView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A dialog to add a new baseTemplate.
 */
public class AddItemDialog extends Dialog {

  private static final String ARG_ID = "id";

  private Optional<? extends BaseCreature> creature = Optional.empty();
  private Optional<ItemTemplate> baseTemplate = Optional.empty();
  private List<ItemTemplate> templates = Collections.emptyList();

  private LabelledAutocompleteTextView itemSelection;
  private LabelledMultiAutocompleteTextView templatesSelection;
  private LabelledEditTextView name;
  private LabelledEditTextView value;
  private LabelledEditTextView weight;
  private LabelledEditTextView hp;
  private LabelledEditTextView appearance;
  private LabelledEditTextView multiple;
  private LabelledEditTextView multiuse;
  private Wrapper<Button> add;

  public AddItemDialog() {}

  public static AddItemDialog newInstance(String creatureId) {
    AddItemDialog dialog = new AddItemDialog();
    dialog.setArguments(arguments(creatureId, R.layout.dialog_add_item, R.string.character_add_item,
        R.color.item));
    return dialog;
  }

  protected static Bundle arguments(String creatureId, @LayoutRes int layoutId,
                                    @StringRes int titleId, @ColorRes int colorId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, creatureId);
    return arguments;
  }

  @Override
  public void onCreate(Bundle savedInstance) {
    super.onCreate(savedInstance);

    if (getArguments() != null) {
      String creatureId = getArguments().getString(ARG_ID);
      creature =
          CompanionApplication.get(getContext()).creatures().getCreatureOrCharacter(creatureId);
    }
  }

  @Override
  protected void createContent(View view) {
    itemSelection = view.findViewById(R.id.item);
    itemSelection.setAdapter(new ArrayAdapter<String>(getContext(),
        R.layout.list_item_select,
        Entries.get().getItems().realItems()));
    itemSelection.onChange(this::selectItem);

    templatesSelection = view.findViewById(R.id.templates);
    templatesSelection.setAdapter(new ArrayAdapter<String>(getContext(),
        R.layout.list_item_select,
        Entries.get().getItems().templates()));
    templatesSelection.onChange(this::selectItem);

    name = view.findViewById(R.id.name);
    value = view.findViewById(R.id.value);
    value.validate(Money::validate);
    weight = view.findViewById(R.id.weight);
    weight.disabled();
    hp = view.findViewById(R.id.hp);
    appearance = view.findViewById(R.id.appearance);
    multiple = view.findViewById(R.id.multiple);
    multiuse = view.findViewById(R.id.multiuse);
    add = Wrapper.<Button>wrap(view, R.id.add).onClick(this::add).disabled();
  }

  private void selectItem() {
    baseTemplate = Entries.get().getItems().get(itemSelection.getText());

    if (baseTemplate.isPresent()) {
      templates = new ArrayList<>();
      templates.add(baseTemplate.get());
      for (String name : templatesSelection.getText().split("\\s*,\\s*")) {
        Optional<ItemTemplate> template = Entries.get().getItems().get(name);
        if (template.isPresent()) {
          templates.add(template.get());
        }
      }

      name.text(Item.name(templates));
      value.text(Item.value(templates).toString());
      weight.text(Item.weight(templates).toString());
      hp.text(String.valueOf(Item.hp(templates)));
      appearance.text(Item.appearance(templates));
      add.enabled();
    } else {
      name.text("");
      value.text("");
      weight.text("");
      hp.text("");
      appearance.text("");
      add.disabled();
    }

    multiple.text("1");
    multiuse.text("1");
  }

  private void add() {
    if (!baseTemplate.isPresent()) {
      return;
    }

    // Create the corresponding item.
    Optional<Money> itemValue = Money.parse(value.getText());
    if (!itemValue.isPresent()) {
      Status.error("Cannot parse item value!");
    } else if (!creature.isPresent()) {
      Status.error("No creature to add item to!");
    } else {
      Item item = new Item(baseTemplate.get().getName(),
          templates.stream().skip(1).collect(Collectors.toList()), Integer.parseInt(hp.getText()),
          itemValue.get(), appearance.getText(), "", "", "", Integer.parseInt(multiple.getText()),
          Integer.parseInt(multiuse.getText()), Duration.ZERO, false, Collections.emptyList());
      creature.get().add(item);
      save();
    }
  }
}
