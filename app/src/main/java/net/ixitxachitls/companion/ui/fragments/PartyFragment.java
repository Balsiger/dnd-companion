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
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.ui.ConfirmationPrompt;
import net.ixitxachitls.companion.ui.dialogs.CharacterDialog;
import net.ixitxachitls.companion.ui.views.CharacterChipView;
import net.ixitxachitls.companion.ui.views.ChipView;
import net.ixitxachitls.companion.ui.views.CreatureChipView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A fragment displaying the complete party of the current campaign.
 */
public class PartyFragment extends NestedCompanionFragment {

  // External data.
  private Optional<Campaign> campaign = Optional.empty();
  private Optional<Battle> encounter;

  // UI.
  private Wrapper<View> scroll;
  private FlexboxLayout party;
  private AdventureView adventure;
  private TextWrapper<TextView> title;
  private Wrapper<FloatingActionButton> addCharacter;
  private Transition transition = new AutoTransition();
  private Wrapper<ImageView> resetEncounter;
  private Wrapper<ImageView> fullScreen;
  private Wrapper<ImageView> fullScreenExit;

  // State.
  private Map<String, CreatureChipView> chipsById = new ConcurrentHashMap<>();
  private Map<String, Character> charactersNeedingInitiative = new HashMap<>();
  private boolean fullScreenState = false;

  public PartyFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    view = (ViewGroup) inflater.inflate(R.layout.fragment_party, container, false);
    view.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));

    title = TextWrapper.wrap(view, R.id.title);
    party = view.findViewById(R.id.party);
    adventure = view.findViewById(R.id.adventure);
    adventure.setVisibility(View.GONE);
    scroll = Wrapper.wrap(view, R.id.scroll);
    resetEncounter = Wrapper.wrap(view, R.id.reset_encounter);
    resetEncounter.onClick(this::maybeResetEncounter);
    fullScreen = Wrapper.wrap(view, R.id.full_screen);
    fullScreen.onClick(this::fullScreen);
    fullScreenExit = Wrapper.wrap(view, R.id.full_screen_exit);
    fullScreenExit.onClick(this::exitFullScreen);

    addCharacter = Wrapper.<FloatingActionButton>wrap(view, R.id.add_character)
        .onClick(this::createCharacter)
        .description("Add Character", "Add a new character to the party. The character will "
            + "automatically be shared with other players.");

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

    update();
  }

  public void show(Campaign campaign) {
    this.campaign = Optional.of(campaign);
    this.encounter = Optional.empty();

    update();
  }

  public void update() {
    if (isHidden()) {
      return;
    }

    if (campaign.isPresent()) {
      // Adventures.
      if (campaign.get().amDM()) {
        adventure.setVisibility(View.VISIBLE);
        adventure.updateCampaign(campaign.get());
      } else {
        adventure.setVisibility(View.GONE);
      }

      // Characters.
      // Refresh the view buttons and such.
      TransitionManager.beginDelayedTransition(view, transition);

      addCharacter.visible(!fullScreenState);
      if (encounter.isPresent() && encounter.get().inBattle()) {
        title.text("Battle - " +
            (encounter.get().isSurprised() ? "Surprise" : "Turn " + encounter.get().getTurn()))
            .backgroundColor(R.color.battleLight);
        scroll.backgroundColor(R.color.battleDark);
      } else {
        title.text("Party");
        title.backgroundColor(R.color.characterLight);
        scroll.backgroundColor(R.color.cell);
      }


      updateChips();

      if (campaign.get().amDM()) {
        messages().readMessages(characters().getCampaignCharacters(campaign.get().getId()).stream()
            .map(Character::getId)
            .collect(Collectors.toList()));
      }
    }

    // Images.
    for (ChipView chip : chipsById.values()) {
      chip.update();
    }

    // Messages.
    for (ChipView chip : chipsById.values()) {
      if (chip instanceof CharacterChipView) {
        ((CharacterChipView) chip).update(messages());
      }
    }

    // Conditions.
    for (ChipView chip : chipsById.values()) {
      if (chip instanceof CharacterChipView) {
        ((CharacterChipView) chip).update(((CharacterChipView) chip).getCharacter());
      }
    }
  }

  private void createCharacter() {
    CharacterDialog.newInstance("", campaign.get().getId()).display();
  }

  private void exitFullScreen() {
    fullScreenState = false;
    TransitionManager.beginDelayedTransition(view, transition);
    party.setVisibility(View.VISIBLE);
    title.visible();
    addCharacter.visible();
    fullScreen.visible();
    fullScreenExit.gone();
    adventure.hideDetails();
  }

  private void fullScreen() {
    fullScreenState = true;
    TransitionManager.beginDelayedTransition(view, transition);
    party.setVisibility(View.GONE);
    title.gone();
    addCharacter.gone();
    fullScreen.gone();
    fullScreenExit.visible();
    adventure.showDetails();
  }

  private void maybeResetEncounter() {
    ConfirmationPrompt.create(getContext()).title("Reset encounter")
        .message("Do you really want to reset the encounter. Any changes you made will be lost "
            + "and random values with be newly selected. This cannot be undone.")
        .yes(this::resetEncounter)
        .show();
  }

  private void redrawChips() {
    TransitionManager.beginDelayedTransition(view, transition);
    party.removeAllViews();

    if (campaign.isPresent()) {
      for (Character character : characters().getCampaignCharacters(campaign.get().getId())) {
        CreatureChipView chip = chipsById.get(character.getId());
        if (chip != null) {
          chip.addTo(party);
        }
      }
    }
  }

  private void resetEncounter() {
    adventure.resetEncounter();
  }

  private void updateChips() {
    Collection<Character> campaignCharacters =
        characters().getCampaignCharacters(campaign.get().getId());
    Set<String> characterIds = campaignCharacters.stream()
        .map(Character::getId)
        .collect(Collectors.toSet());

    // Remove all chips for which we don't have characters anymore.
    for (Iterator<String> i = chipsById.keySet().iterator(); i.hasNext(); ) {
      String chipId = i.next();
      if (!characterIds.contains(chipId)) {
        CharacterChipView chip = (CharacterChipView) chipsById.remove(chipId);
      }
    }

    // Add all new chips.
    for (Character character : campaignCharacters) {
      if (!chipsById.containsKey(character.getId())) {
        CharacterChipView chip = new CharacterChipView(getContext(), character,
            getResources().getInteger(R.integer.chipsPerLine));
        chipsById.put(character.getId(), chip);
      }
    }

    // Remove any character needing initative that are no more available.
    for (Iterator<String> i = charactersNeedingInitiative.keySet().iterator(); i.hasNext(); ) {
      if (!characterIds.contains(i.next())) {
        i.remove();
      }
    }

    redrawChips();
  }
}
