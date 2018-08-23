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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.dynamics.Campaign;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Image;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.activities.CompanionFragments;
import net.ixitxachitls.companion.ui.views.ConditionIconsView;
import net.ixitxachitls.companion.ui.views.RoundImageView;
import net.ixitxachitls.companion.ui.views.TitleView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

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
  protected TitleView title;
  protected TextWrapper<TextView> campaignTitle;
  protected RoundImageView image;
  protected Wrapper<FloatingActionButton> copy;
  protected Wrapper<FloatingActionButton> edit;
  protected Wrapper<FloatingActionButton> delete;
  protected Wrapper<FloatingActionButton> move;
  protected Wrapper<FloatingActionButton> timed;
  protected Wrapper<FloatingActionButton> back;
  protected ConditionIconsView conditions;
  protected HistoryFragment history;
  protected ViewPager pager;
  protected @Nullable CharacterStatisticsFragment statisticsFragment;
  protected @Nullable CharacterInventoryFragment inventoryFragment;

  public CharacterFragment() {
    super(Type.character);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    storeOnPause = true;

    RelativeLayout view = (RelativeLayout)
        inflater.inflate(R.layout.fragment_character, container, false);

    back = Wrapper.<FloatingActionButton>wrap(view, R.id.back)
        .onClick(this::goBack)
        .description("Back to Campaign", "Go back to this characters campaign view.");
    image = view.findViewById(R.id.image);
    title = view.findViewById(R.id.title);
    campaignTitle = TextWrapper.wrap(view, R.id.campaign);

    LinearLayout conditionsContainer = view.findViewById(R.id.conditions);
    conditions = new ConditionIconsView(view.getContext());
    conditionsContainer.addView(conditions);

    copy = Wrapper.<FloatingActionButton>wrap(view, R.id.copy).gone().onClick(this::copy)
        .description("Copy Character",
            "Copies the character to the current device as a local character.");
    edit = Wrapper.<FloatingActionButton>wrap(view, R.id.edit).gone();
    delete = Wrapper.<FloatingActionButton>wrap(view, R.id.delete).onClick(this::delete)
        .description("Delete Character", "This will remove this character from your device. If the "
            + "player is active on your WiFi, the character most likely will immediately "
            + "reappear, though.");
    move = Wrapper.<FloatingActionButton>wrap(view, R.id.move).gone();
    timed = Wrapper.<FloatingActionButton>wrap(view, R.id.timed).gone();

    history = (HistoryFragment) getChildFragmentManager().findFragmentById(R.id.history);

    pager = view.findViewById(R.id.pager);
    pager.setAdapter(new CharacterPagerAdapter(getChildFragmentManager()));

    update(character);
    return view;
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
      characters().getCharacter(this.character.get().getCharacterId()).removeObservers(this);
    }

    this.character = Optional.of(character);
    this.campaign = campaigns().getCampaign(character.getCampaignId()).getValue();

    characters().getCharacter(character.getCharacterId()).observe(this, this::update);
  }

  private void update(Optional<Character> character) {
    this.character = character;
    if (statisticsFragment != null) {
      statisticsFragment.update(character);
    }
    if (!character.isPresent() || !campaign.isPresent()) {
      this.campaign = Optional.empty();
      return;
    }

    if (inventoryFragment != null) {
      inventoryFragment.update(character.get());
    }

    campaign = CompanionApplication.get(getContext()).campaigns()
        .getCampaign(character.get().getCampaignId()).getValue();

    campaignTitle.text(campaign.get().getName());
    title.setTitle(character.get().getName());
    title.setSubtitle(character.get().getGender().getName() + " " + character.get().getRace()
        + ", " + character.get().getPlayerName());
    Optional<Image> characterImage = CompanionApplication.get(getContext())
        .images(character.get().isLocal()).getImage(
            Character.TABLE, character.get().getCharacterId()).getValue();
    if (characterImage.isPresent()) {
      image.setImageBitmap(characterImage.get().getBitmap());
    } else {
      image.clearImage();
    }

    conditions.update(character.get());
    copy.visible(!character.get().isLocal() && campaign.get().amDM());
    history.update(character.get().getCharacterId());
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
      character.get().delete();
      Toast.makeText(getActivity(), getString(R.string.character_deleted),
          Toast.LENGTH_SHORT).show();

      storeOnPause = false;
      if (campaign.isPresent()) {
        show(campaign.get().isLocal() ? Type.localCampaign : Type.campaign);
      }
    }
  }

  private void copy() {
    if (character.isPresent()) {
      character.get().copy();
      Status.toast("The character has been copied.");
    }
  }

  @Override
  public boolean goBack() {
    if (campaign.isPresent()) {
      CompanionFragments.get().showCampaign(campaign.get(), Optional.of(title));
    } else {
      CompanionFragments.get().show(Type.campaigns, Optional.empty());
    }
    return true;
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
          if (character.isPresent() && character.get().isLocal()) {
            statisticsFragment = new LocalCharacterStatisticsFragment();
          } else {
            statisticsFragment = new CharacterStatisticsFragment();
          }
          statisticsFragment.update(character);
          return statisticsFragment;

        case 1:
          if (character.isPresent()) {
            inventoryFragment = new CharacterInventoryFragment();
            inventoryFragment.update(character.get());
          }
          return inventoryFragment;
      }
    }

    @Override
    public String getPageTitle(int position) {
      switch (position) {
        case 0:
          return "Statistics";

        case 1:
          return "Inventory";

        default:
          return "Unknown";
      }
    }
  }
}
