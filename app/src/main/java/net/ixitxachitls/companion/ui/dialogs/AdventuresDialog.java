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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Adventure;
import net.ixitxachitls.companion.data.documents.Adventures;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Campaigns;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

/**
 * Dialog to select, add or remove an adventure.
 */
public class AdventuresDialog extends Dialog {

  private static final String ARG_ID = "id";

  private Optional<Campaign> campaign = Optional.empty();
  private LinearLayout adventures;
  private LabelledEditTextView name;
  private Wrapper<Button> add;

  public static AdventuresDialog newInstance(String campaignId) {
    AdventuresDialog dialog = new AdventuresDialog();
    dialog.setArguments(arguments(R.layout.dialog_adventures, R.string.dialog_adventures_title,
        R.color.campaign, campaignId));
    return dialog;
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String campaignId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, campaignId);
    return arguments;
  }

  @Override
  protected void createContent(View view) {
    adventures = view.findViewById(R.id.adventures);
    name = view.findViewById(R.id.name);
    add = Wrapper.<Button>wrap(view, R.id.add).onClick(this::add);

    campaign = campaigns().get(getArguments().getString(ARG_ID));
    refresh();

    campaigns().observe(this, this::update);
    adventures().observe(this, this::update);
  }

  private void add() {
    if (campaign.isPresent() && !name.getText().isEmpty()) {
      Adventure.create(context(), campaign.get().getId(), name.getText()).store();
      refresh();
    }
  }

  private void update(Campaigns campaigns) {
    refresh();
  }

  private void update(Adventures adventures) {
    refresh();
  }

  private void refresh() {
    adventures.removeAllViews();
    if (campaign.isPresent()) {
      for (Adventure adventure : adventures().getForCampaign(campaign.get().getId())) {
        adventures.addView(new LineView(getContext(), adventure,
            campaign.get().getAdventure().isPresent()
                && campaign.get().getAdventure().get().getId().equals(adventure.getId())));
      }
    }
  }

  private class LineView extends LinearLayout {
    private final Adventure adventure;

    private LineView(Context context, Adventure adventure, boolean selected) {
      super(context);
      this.adventure = adventure;

      View view = LayoutInflater.from(getContext()).inflate(R.layout.view_line_adventure, this,
          false);
      TextWrapper<TextView> name =
          TextWrapper.wrap(view, R.id.name).text(adventure.getName()).onClick(this::select);
      Wrapper.wrap(view, R.id.remove).onClick(this::remove);

      if (selected) {
        name.textColor(R.color.campaign);
      }

      addView(view);
    }

    private void select() {
      if (campaign.isPresent()) {
        campaign.get().setAdventure(adventure);
      }
    }

    private void remove() {
      adventures().delete(adventure.getId());
      refresh();
    }
  }
}
