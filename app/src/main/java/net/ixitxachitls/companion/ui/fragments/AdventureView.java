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

package net.ixitxachitls.companion.ui.fragments;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Encounter;
import net.ixitxachitls.companion.data.documents.Monster;
import net.ixitxachitls.companion.data.templates.AdventureTemplate;
import net.ixitxachitls.companion.ui.views.MonsterChipView;
import net.ixitxachitls.companion.ui.views.TooltipImageView;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Strings;
import net.ixitxachitls.companion.util.Texts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;

/**
 * A view for displaying adventure and encounterView information.
 */
public class AdventureView extends LinearLayout {

  private TextWrapper<TextView> adventure;
  private TextWrapper<TextView> encounterView;
  private TextWrapper<TextView> description;
  private TextWrapper<TextView> level;
  private TextWrapper<TextView> locations;
  private List<Wrapper<TooltipImageView>> categoryIcons = new ArrayList<>();
  private LinearLayout categoryText;
  private LinearLayout creatures;

  private Optional<Campaign> campaign = Optional.empty();
  private Optional<Encounter> encounter = Optional.empty();
  private Optional<AdventureTemplate> adventureTemplate;
  private Optional<AdventureTemplate.EncounterTemplate> encounterTemplate;

  public AdventureView(Context context) {
    super(context);

    init();
  }

  public AdventureView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    init();
  }

  public void updateCampaign(Campaign campaign) {
    this.campaign = Optional.of(campaign);

    adventure.onClick(this::selectAdventure);
    encounterView.onClick(this::selectEncounter);
    updateAdventure(Templates.get().getAdventureTemplates().get(campaign.getAdventureId()));
    if (adventureTemplate.isPresent()) {
      encounterTemplate = adventureTemplate.get().getEncounter(campaign.getEncounterId());
      encounterView.text(encounterTemplate.isPresent()
          ? encounterTemplate.get().getId() + ":  " + encounterTemplate.get().getName()
          : campaign.getEncounterId());
    } else {
      adventure.text(campaign.getAdventureId());
      encounterView.text("");
    }
  }

  private TextView createCeiling(AdventureTemplate.EncounterTemplate.Ceiling ceiling) {
    return TextWrapper.wrap(new TextView(getContext()))
        .text(Texts.processCommands(getContext(), ceiling.format()))
        .get();
  }

  private TextView createDoor(AdventureTemplate.EncounterTemplate.Door door) {
    return TextWrapper.wrap(new TextView(getContext()))
        .text(Texts.processCommands(getContext(), door.format()))
        .get();
  }

  private LinearLayout createReadAloudLine(AdventureTemplate.EncounterTemplate.ReadAloud read) {
    LinearLayout view = (LinearLayout) LayoutInflater.from(getContext())
            .inflate(R.layout.view_adventure_read_aloud_line, null, false);
    TextWrapper.wrap(view, R.id.condition)
        .text(Texts.processCommands(getContext(), read.getCondition()));
    TextWrapper.wrap(view, R.id.text)
        .text(Texts.processCommands(getContext(), read.getText()));

    return view;
  }

  private TextView createSpot(AdventureTemplate.EncounterTemplate.Spot spot) {
    return TextWrapper.wrap(new TextView(getContext()))
        .text(Texts.processCommands(getContext(), spot.format()))
        .get();
  }

  private TextView createText(String text) {
    return TextWrapper.wrap(new TextView(getContext())).text(text).get();
  }

  @CallSuper
  protected void init() {
    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_adventure, null, false);

    adventure = TextWrapper.wrap(view, R.id.adventure);
    encounterView = TextWrapper.wrap(view, R.id.encounter);
    description = TextWrapper.wrap(view, R.id.description);
    level = TextWrapper.wrap(view, R.id.encounter_level);
    locations = TextWrapper.wrap(view, R.id.locations);
    categoryText = view.findViewById(R.id.category_text);
    setupCategoryIcon(view, R.id.icon_read_loud, this::showReadAloud);
    setupCategoryIcon(view, R.id.icon_ceiling, this::showCeilings);
    setupCategoryIcon(view, R.id.icon_floor, this::showFloors);
    setupCategoryIcon(view, R.id.icon_walls, this::showWalls);
    setupCategoryIcon(view, R.id.icon_doors, this::showDoors);
    setupCategoryIcon(view, R.id.icon_terrain, this::showTerrains);
    setupCategoryIcon(view, R.id.icon_trap, this::showTraps);
    setupCategoryIcon(view, R.id.icon_light, this::showLight);
    setupCategoryIcon(view, R.id.icon_sound, this::showSounds);
    setupCategoryIcon(view, R.id.icon_smell, this::showSmells);
    setupCategoryIcon(view, R.id.icon_touch, this::showTouch);
    setupCategoryIcon(view, R.id.icon_feel, this::showFeels);
    creatures = view.findViewById(R.id.creatures);

    addView(view);
  }

  private void selectAdventure() {
    if (campaign.isPresent()) {
      ListSelectDialog dialog = ListSelectDialog.newInstance(R.string.select_adventure,
          ImmutableList.of(campaign.get().getAdventureId()),
          Templates.get().getAdventureTemplates().getValues().stream()
              .map(t -> new ListSelectDialog.Entry(t.getTitle(), t.getId()))
              .collect(Collectors.toList()), R.color.campaign);
      dialog.setSelectListener(this::selectedAdventure);
      dialog.display();
    }
  }

  private void selectEncounter() {
    if (campaign.isPresent()) {
      Optional<AdventureTemplate> adventure =
          Templates.get().getAdventureTemplates().get(campaign.get().getAdventureId());
      if (adventure.isPresent()) {
        ListSelectDialog dialog = ListSelectDialog.newInstance(R.string.select_encounter,
            ImmutableList.of(campaign.get().getEncounterId()),
            adventure.get().getEncounters().stream()
                .map(t -> new ListSelectDialog.Entry(formatEncounterName(t.getId(), t.getName()),
                    t.getId()))
                .collect(Collectors.toList()), R.color.campaign);
        dialog.setSelectListener(this::selectedEncounter);
        dialog.display();
      }
    }
  }

  private String formatEncounterName(String encounterId, String name) {
    if (campaign.isPresent()) {
      if (CompanionApplication.get().encounters().hasEncounter(campaign.get().getId(),
          campaign.get().getAdventureId(), encounterId)) {
        return name;
      } else {
        return name + " (needs init)";
      }
    }

    return name;
  }

  private void selectedAdventure(List<String> strings) {
    if (strings.size() != 1) {
      Status.error("Expected a single adventure!");
    }

    if (campaign.isPresent()) {
      campaign.get().setAdventureId(strings.get(0));
    }
  }

  private void selectedEncounter(List<String> strings) {
    if (strings.size() != 1) {
      Status.error("Expected a single encounterView!");
    }

    if (campaign.isPresent()) {
      campaign.get().setEncounterId(strings.get(0));
      if (CompanionApplication.get().encounters()
          .hasLoaded(campaign.get().getId(), campaign.get().getAdventureId())) {
        encounter = Optional.of(CompanionApplication.get().encounters().getOrInitialize(campaign.get().getId(),
            campaign.get().getAdventureId(), strings.get(0)));
      } else {
        Status.error("Encounters have not yet been loaded for " + campaign.get().getName());
      }
    }
  }

  private void setupCategoryIcon(View view, @IdRes  int id, Wrapper.Action action) {
    Wrapper<TooltipImageView> icon = Wrapper.wrap(view, id);
    categoryIcons.add(icon);

    icon.onClick(() -> {
      action.execute();
      categoryIcons.stream().forEach(i -> i.tint(R.color.campaignDark));
      icon.tint(R.color.campaign);
    });
  }

  private void showCeilings() {
    categoryText.removeAllViews();
    for (AdventureTemplate.EncounterTemplate.Ceiling ceiling :
        encounterTemplate.get().getCeilings()) {
      categoryText.addView(createCeiling(ceiling));
    }
  }

  private void showDoors() {
    categoryText.removeAllViews();
    for (AdventureTemplate.EncounterTemplate.Door door : encounterTemplate.get().getDoors()) {
      categoryText.addView(createDoor(door));
    }
  }

  private void showFeels() {
    categoryText.removeAllViews();
    for (String feel : encounterTemplate.get().getFeels()) {
      categoryText.addView(createText(feel));
    }
  }

  private void showFloors() {
    categoryText.removeAllViews();
    for (AdventureTemplate.EncounterTemplate.Spot floor : encounterTemplate.get().getFloors()) {
      categoryText.addView(createSpot(floor));
    }
  }

  private void showLight() {
    categoryText.removeAllViews();
    for (String light : encounterTemplate.get().getLights()) {
      categoryText.addView(createText(light));
    }
  }

  private void showReadAloud() {
    categoryText.removeAllViews();
    for (AdventureTemplate.EncounterTemplate.ReadAloud read
        : encounterTemplate.get().getReadAlouds()) {
      categoryText.addView(createReadAloudLine(read));
    }
  }

  private void showSmells() {
    categoryText.removeAllViews();
    for (String smell : encounterTemplate.get().getSmells()) {
      categoryText.addView(createText(smell));
    }
  }

  private void showSounds() {
    categoryText.removeAllViews();
    for (String sound : encounterTemplate.get().getSounds()) {
      categoryText.addView(createText(sound));
    }
  }

  private void showTerrains() {
    categoryText.removeAllViews();
    for (AdventureTemplate.EncounterTemplate.Spot terrain : encounterTemplate.get().getTerrains()) {
      categoryText.addView(createSpot(terrain));
    }
  }

  private void showTouch() {
    categoryText.removeAllViews();
    for (String touch : encounterTemplate.get().getTouchs()) {
      categoryText.addView(createText(touch));
    }
  }

  private void showTraps() {
    categoryText.removeAllViews();
    for (AdventureTemplate.EncounterTemplate.Spot trap : encounterTemplate.get().getTraps()) {
      categoryText.addView(createSpot(trap));
    }
  }

  private void showWalls() {
    categoryText.removeAllViews();
    for (AdventureTemplate.EncounterTemplate.Spot wall: encounterTemplate.get().getWalls()) {
      categoryText.addView(createSpot(wall));
    }
  }

  private void updateAdventure(Optional<AdventureTemplate> adventureTemplate) {
    this.adventureTemplate = adventureTemplate;
    if (adventureTemplate.isPresent()) {
       adventure.text(adventureTemplate.get().getTitle());
       if (campaign.isPresent()) {
         updateEncounter(adventureTemplate.get().getEncounter(campaign.get().getEncounterId()));
       } else {
         updateEncounter(Optional.empty());
       }
    } else {
      adventure.text(campaign.isPresent() ? campaign.get().getAdventureId() : "");
      updateEncounter(Optional.empty());
    }
  }

  private void updateEncounter(Optional<AdventureTemplate.EncounterTemplate> encounterTemplate) {
    this.encounterTemplate = encounterTemplate;
    if (encounterTemplate.isPresent()) {
      description.text(Texts.processCommands(getContext(),
          encounterTemplate.get().getDescription()));
      encounterView.text(encounterTemplate.get().getId() + ":" + encounterTemplate.get().getName());
      level.text(String.valueOf(encounterTemplate.get().getEncounterLevel()));
      locations.text(Strings.COMMA_JOINER.join(encounterTemplate.get().getLocations()));
      categoryIcons.get(0).get().callOnClick();

      creatures.removeAllViews();
      if (campaign.isPresent()) {
        for (Monster monster : encounterTemplate.get().getMonsters(campaign.get().getId())) {
          MonsterChipView chip = new MonsterChipView(getContext(), monster);
          creatures.addView(chip);
        }
      }
    } else {
      description.text("");
      encounterView.text(campaign.isPresent() ? campaign.get().getEncounterId() : "");
      level.text("");
      locations.text("");
      categoryText.removeAllViews();
      creatures.removeAllViews();
    }
  }
}
