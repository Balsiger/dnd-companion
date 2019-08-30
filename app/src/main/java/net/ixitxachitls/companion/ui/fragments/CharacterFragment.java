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
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


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
  private Optional<Handler> updateHandler = Optional.empty();

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
  public void onResume() {
    super.onResume();

    if (character.isPresent()) {
      observe(character.get());
    }
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

    unobserve();
    if (storeOnPause && character.isPresent()) {
      character.get().store();
    }
  }

  public void showCharacter(Character character) {
    unobserve();

    this.character = Optional.of(character);
    this.campaign = campaigns().getOptional(character.getCampaignId());

    observe(character);
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

  private void observe(Character character) {
    images().observe(this, title::refresh);
    characters().observe(this, this::refresh);
    messages().observe(this, this::refresh);
    conditions().observe(this, this::refresh);

    character.observe(this, this::update);
  }

  private void refresh(Documents.Update update) {
    if (character.isPresent()) {
      update(character.get());
    }
    title.refresh(update);
  }

  private void unobserve() {
    images().unobserve(this);
    characters().unobserve(this);
    messages().unobserve(this);
    conditions().unobserve(this);

    if (this.character.isPresent()) {
      this.character.get().unobserve(this);
    }
  }

  private void update(Character character) {
    Status.log("update character");
    this.character = Optional.of(character);

    // We did not create the view yet, thus there is no point in updating.
    if (getView() == null) {
      return;
    }

    if (statisticsFragment != null) {
      statisticsFragment.update(character);
    }

    if (inventoryFragment != null) {
      inventoryFragment.update(character);
    }

    campaign = CompanionApplication.get(getContext()).campaigns()
        .getOptional(character.getCampaignId());

    delete.show(character.amPlayer() || character.amDM());

    campaignTitle.text(campaign.isPresent() ? campaign.get().getName() : "");
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
