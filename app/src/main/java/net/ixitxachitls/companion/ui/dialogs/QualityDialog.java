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

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Quality;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Strings;
import net.ixitxachitls.companion.util.Texts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;

/**
 * A dialog to display information about a quality.
 */
public class QualityDialog extends Dialog<QualityDialog, Void> {

  private static final String ARG_CHARACTER = "character";
  private static final String ARG_QUALITY = "quality";
  private static final String ARG_COUNT = "count";

  private Character character;
  private Quality quality;
  private int count;

  private TextWrapper<TextView> shortDescription;
  private TextWrapper<TextView> description;
  private Wrapper<CheckBox> processParams;
  private TextWrapper<TextView> params;

  @Override
  protected void createContent(View view) {
    if (!characters().get(getArguments().getString(ARG_CHARACTER)).isPresent()) {
      Status.error("Cannot find character to show quality");
      super.save();
    }

    character = characters().get(getArguments().getString(ARG_CHARACTER)).get();
    quality = getArguments().getParcelable(ARG_QUALITY);
    count = getArguments().getInt(ARG_COUNT);

    TextWrapper.wrap(view, R.id.source).text(quality.getEntity() + ", " + character.getName());
    TextWrapper.wrap(view, R.id.type).text(quality.getTemplate().getTypeFormatted());
    shortDescription = TextWrapper.wrap(view, R.id.short_description);
    description = TextWrapper.wrap(view, R.id.description);
    params = TextWrapper.wrap(view, R.id.params).text(formatParams(
        quality.collectFormatValues(count, character)));
    processParams = Wrapper.<CheckBox>wrap(view, R.id.process_params).onClick(this::updateParams);
    updateParams();
  }

  protected static Bundle arguments(@LayoutRes int layoutId, String title,
                                    @ColorRes int colorId, Character character, Quality quality,
                                    int count) {
    Bundle arguments = Dialog.arguments(layoutId, title, colorId);
    arguments.putString(ARG_CHARACTER, character.getId());
    arguments.putParcelable(ARG_QUALITY, quality);
    arguments.putInt(ARG_COUNT, count);
    return arguments;
  }

  public static QualityDialog newInstance(Character character, Quality quality, String title,
                                          int count) {
    QualityDialog dialog = new QualityDialog();
    dialog.setArguments(arguments(R.layout.dialog_quality, title, R.color.quality, character,
        quality, count));
    return dialog;
  }

  private void updateParams() {
    if (processParams.get().isChecked()) {
      shortDescription.text(Texts.toSpanned(getContext(),
          quality.getTemplate().getShortDescription(),
          quality.collectFormatValues(count, character)));
      description.text(Texts.toSpanned(getContext(), quality.getTemplate().getDescription(),
          quality.collectFormatValues(count, character)));
      params.gone();
    } else {
      shortDescription.text(quality.getTemplate().getShortDescription());
      description.text(quality.getTemplate().getDescription());
      params.visible();
    }
  }

  private String formatParams(Map<String, Texts.Value> params) {
    List<String> lines = new ArrayList<>();
    for (Map.Entry<String, Texts.Value> entry : params.entrySet()) {
      lines.add(entry.getKey() + " = " + entry.getValue());
    }

    return Strings.NEWLINE_JOINER.join(lines);
  }
}
