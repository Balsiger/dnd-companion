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
import android.widget.CheckBox;
import android.widget.LinearLayout;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * A dialog to configure the products shown.
 */
public class ProductConfigurationDialog extends Dialog<ProductConfigurationDialog, Void> {

  private LinearLayout producers;
  private LinearLayout worlds;
  private LinearLayout systems;
  private LinearLayout types;

  @Override
  protected void createContent(View view) {
    Wrapper.wrap(view, R.id.save).onClick(this::save);

    producers = view.findViewById(R.id.producers);
    for (String producer : Templates.get().getProductTemplates().extractAllProducers()) {
      producers.addView(createCheckBox(producer, !me().hasProducerHidden(producer)));
    }

    worlds = view.findViewById(R.id.worlds);
    for (String world : Templates.get().getProductTemplates().extractAllWorlds()) {
      worlds.addView(createCheckBox(world, !me().hasWorldHidden(world)));
    }

    systems = view.findViewById(R.id.systems);
    for (String system : Templates.get().getProductTemplates().extractAllSystems()) {
      systems.addView(createCheckBox(system, !me().hasSystemHidden(system)));
    }

    types = view.findViewById(R.id.types);
    for (String type : Templates.get().getProductTemplates().extractAllTypes()) {
      types.addView(createCheckBox(type, !me().hasTypeHidden(type)));
    }
  }

  private CheckBox createCheckBox(String name, boolean checked) {
    CheckBox checkBox = new CheckBox(getContext());
    checkBox.setText(name);
    checkBox.setChecked(checked);

    return checkBox;
  }

  @Override
  public void save() {
    me().setHiddenProducts(hidden(producers), hidden(worlds), hidden(systems), hidden(types));

    super.save();
  }

  private List<String> hidden(LinearLayout container) {
    List<String> hidden = new ArrayList<>();

    for (int i = 0; i < container.getChildCount(); i++) {
      View child = container.getChildAt(i);
      if (child instanceof CheckBox) {
        CheckBox checkBox = (CheckBox) child;
        if (!checkBox.isChecked()) {
          hidden.add(checkBox.getText().toString());
        }
      }
    }

    return hidden;
  }

  public static ProductConfigurationDialog newInstance() {
    ProductConfigurationDialog dialog = new ProductConfigurationDialog();
    dialog.setArguments(arguments(R.layout.dialog_product_configuration, "Configuration",
        R.color.product));
    return dialog;
  }
}
