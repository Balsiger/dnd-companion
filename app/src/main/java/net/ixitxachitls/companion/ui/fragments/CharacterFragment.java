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

package net.ixitxachitls.companion.ui.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Documents;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.views.ActionBarView;
import net.ixitxachitls.companion.ui.views.CharacterTitleView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Fragment for displaying character information.
 */
public class CharacterFragment extends CompanionFragment {

  protected Optional<Character> character = Optional.empty();
  protected Optional<Campaign> campaign = Optional.empty();
  protected boolean storeOnPause = true;

  // UI elements.
  protected CharacterTitleView title;
  protected TextWrapper<TextView> campaignTitle;
  protected ActionBarView.Action edit;
  protected ActionBarView.Action delete;
  protected ActionBarView.Action move;
  protected ActionBarView.Action timed;
  protected ActionBarView.Action message;
  protected ViewPager pager;
  protected @Nullable CharacterStatisticsFragment statisticsFragment;
  protected @Nullable CharacterInventoryFragment inventoryFragment;

  public CharacterFragment() {
    super(Type.character);
  }

  @Override
  public boolean goBack() {
    // TODO(merlin): Maybe there is a better way than this?
    // Remove the pager from the screen as otherwise transitions try to attach the
    // view pager title to a weird view and thus an exception is thrown.
    if (pager.getParent() != null) {
      ((ViewGroup) pager.getParent()).removeView(pager);
    }

    if (campaign.isPresent()) {
      CompanionFragments.get().showCampaign(campaign.get(), Optional.of(title));
    } else {
      CompanionFragments.get().show(Type.campaigns, Optional.empty());
    }
    return true;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    storeOnPause = true;

    LinearLayout view = (LinearLayout)
        inflater.inflate(R.layout.fragment_character, container, false);

    title = view.findViewById(R.id.title);
    campaignTitle = TextWrapper.wrap(view, R.id.campaign);

    pager = view.findViewById(R.id.pager);
    pager.setAdapter(new CharacterPagerAdapter(getChildFragmentManager()));

    clearActions();
    addAction(R.drawable.ic_arrow_back_black_24dp, "Back to Campaign",
        "Go back to this characters campaign view.")
        .onClick(this::goBack);
    edit = addAction(R.drawable.ic_mode_edit_black_24dp,
        "Edit Character", "Edit the basic character traits").hide();
    timed = addAction(R.drawable.icons8_treatment_100, "Add Condition",
        "Add a timed condition to this and/or other characters.")
        .hide();
    move = addAction(R.drawable.ic_launch_black_24dp, "Move Character",
        "This button moves the character to an other campaign.").hide();
    message = addAction(R.drawable.ic_message_text_black_48dp, "Send Message",
        "Send a message to other characters and the DM").hide();
    delete = addAction(R.drawable.ic_delete_black_24dp, "Delete Character",
        "This will remove this character from your device. If the "
            + "player is active on your WiFi, the character most likely will immediately "
            + "reappear, though.").onClick(this::delete);

    if (character.isPresent()) {
      update(character.get());
    }

    images().observe(this, title::refresh);
    characters().observe(this, this::refresh);
    messages().observe(this, this::refresh);
    conditions().observe(this, this::refresh);

    return view;
  }

  @Override
  public void onStart() {
    super.onStart();

    TabLayout tabs = pager.findViewById(R.id.tabs);
    tabs.getTabAt(0).setIcon(R.drawable.ic_information_outline_black_24dp);
    tabs.getTabAt(1).setIcon(R.drawable.noun_backpack_16138);
  }

  @Override
  public void onPause() {
    super.onPause();

    if (storeOnPause && character.isPresent()) {
      character.get().store();
    }
  }

  public void showCharacter(Character character) {
    if (this.character.isPresent()) {
      this.character.get().unobserve(this);
    }

    this.character = Optional.of(character);
    character.observe(this, this::update);
    this.campaign = campaigns().get(character.getCampaignId());

    update(character);
  }

  private void delete() {
    ConfirmationPrompt.create(getContext())
        .title(getResources().getString(R.string.character_delete_title))
        .message(getResources().getString(R.string.character_delete_message))
        .yes(this::deleteCharacterOk)
        .show();
  }

  private void deleteCharacterOk() {
    if (character.isPresent()) {
      characters().delete(character.get());
      Toast.makeText(getActivity(), getString(R.string.character_deleted),
          Toast.LENGTH_SHORT).show();

      storeOnPause = false;
      if (campaign.isPresent()) {
        show(Type.campaign);
      }
    }
  }

  private void refresh(Documents.Update update) {
    // Characters & conditions.
    if (character.isPresent()) {
      update(character.get());
    }

    // Messages.
    title.refresh(update);
  }

  private void update(Character character) {
    this.character = Optional.of(character);
    if (statisticsFragment != null) {
      statisticsFragment.update(character);
    }
    if (!campaign.isPresent()) {
      this.campaign = Optional.empty();
      return;
    }

    if (inventoryFragment != null) {
      inventoryFragment.update(character);
    }

    campaign = CompanionApplication.get(getContext()).campaigns()
        .get(character.getCampaignId());

    delete.show(character.amPlayer() || character.amDM());

    campaignTitle.text(campaign.get().getName());
    title.update(character);
    title.update(images());
    title.refresh(new Documents.Update(character.getId()));
  }

  public class CharacterPagerAdapter extends FragmentPagerAdapter {

    public CharacterPagerAdapter(FragmentManager manager) {
      super(manager);
    }

    @Override
    public int getCount() {
      return 2;
    }

    @Override
    public Fragment getItem(int position) {
      switch (position) {
        default:
        case 0:
          if (character.isPresent() && character.get().amPlayer()) {
            statisticsFragment = new LocalCharacterStatisticsFragment();
          } else {
            statisticsFragment = new CharacterStatisticsFragment();
          }
          statisticsFragment.update(character.get());
          return statisticsFragment;

        case 1:
          if (character.isPresent()) {
            inventoryFragment = new CharacterInventoryFragment();
            inventoryFragment.update(character.get());
          }
          return inventoryFragment;
      }
    }
  }
}
