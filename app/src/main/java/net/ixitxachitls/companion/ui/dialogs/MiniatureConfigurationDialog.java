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

import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * A dialog to configure the miniatures.
 */
public class MiniatureConfigurationDialog extends Dialog {

  private LinearLayout container;

  private CheckBox createCheckBox(String name) {
    CheckBox checkBox = new CheckBox(getContext());
    checkBox.setText(name);
    checkBox.setChecked(!me().hasSetHidden(name));

    return checkBox;
  }

  @Override
  protected void createContent(View view) {
    container = view.findViewById(R.id.sets);
    Wrapper.wrap(view, R.id.save).onClick(this::save);

    for (String set : Templates.get().getMiniatureTemplates().getAllSets()) {
      container.addView(createCheckBox(set));
    }
  }

  @Override
  public void save() {
    me().setHiddenSets(hiddenSets());

    super.save();
  }

  private List<String> hiddenSets() {
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

  public static MiniatureConfigurationDialog newInstance() {
    MiniatureConfigurationDialog dialog = new MiniatureConfigurationDialog();
    dialog.setArguments(arguments(R.layout.dialog_miniature_configuration, "Configuration",
        R.color.miniature));
    return dialog;
  }
}
