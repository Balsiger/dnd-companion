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

package net.ixitxachitls.companion.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Button;

import com.google.common.base.Strings;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.fragments.ListSelectDialog;
import net.ixitxachitls.companion.ui.views.LabelledEditTextView;
import net.ixitxachitls.companion.ui.views.LabelledTextView;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Optional;

/**
 * Fragment for editing a campaign
 */
public class EditCampaignDialog extends Dialog {

  private static final String ARG_ID = "id";

  private Optional<Campaign> campaign = Optional.empty();

  // The following values are only valid after onCreate().
  private LabelledEditTextView name;
  private LabelledTextView world;
  private String selectedWorld;
  private Wrapper<Button> save;

  public EditCampaignDialog() {}

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      String id = getArguments().getString(ARG_ID);
      if (Strings.isNullOrEmpty(id)) {
        campaign = Optional.of(application().context().campaigns().create());
      } else {
        campaign = campaigns().get(id);
      }
    } else {
      campaign = Optional.of(application().context().campaigns().create());
    }
  }

  protected void save() {
    if (campaign.isPresent()) {
      campaign.get().setName(name.getText());
      campaign.get().setWorldTemplate(selectedWorld);
      campaign.get().store();

      super.save();
      campaign.get().whenReady(() ->
          CompanionFragments.get().showCampaign(campaign.get(), Optional.empty()));
    }

  }

  @Override
  protected void createContent(View view) {
    if (campaign.isPresent()) {
      name = view.findViewById(R.id.edit_name);
      name.text(campaign.get().getName());
      name.onChange(this::update);
      world = view.findViewById(R.id.world);
      world.onClick(this::selectWorld);
      world.text(campaign.get().getWorldTemplate().getName());
      selectedWorld = campaign.get().getWorldTemplate().getName();
      save = Wrapper.wrap(view, R.id.save);
      save.onClick(this::save);
      update();
    }
  }

  private void editWorld(String value) {
    selectedWorld = value;
    update();
  }

  private void selectWorld() {
    if (campaign.isPresent()) {
      ListSelectDialog fragment = ListSelectDialog.newStringInstance(
          R.string.campaign_select_world, campaign.get().getWorldTemplate().getName(),
          Templates.get().getWorldTemplates().getNames(), R.color.campaign);
      fragment.setSelectListener(this::editWorld);
      fragment.display();
    }
  }

  protected void update() {
    world.text(selectedWorld);
    save.visible(name.getText().length() > 0);
  }

  protected static Bundle arguments(@LayoutRes int layoutId, @StringRes int titleId,
                                    @ColorRes int colorId, String campaignId) {
    Bundle arguments = Dialog.arguments(layoutId, titleId, colorId);
    arguments.putString(ARG_ID, campaignId);
    return arguments;
  }

  public static EditCampaignDialog newInstance(String id) {
    EditCampaignDialog fragment = new EditCampaignDialog();
    fragment.setArguments(arguments(R.layout.dialog_edit_campaign,
        id.isEmpty() ? R.string.campaign_title_add : R.string.campaign_title_edit,
        R.color.campaign, id));
    return fragment;
  }

  public static EditCampaignDialog newInstance() {
    return newInstance("");
  }
}
