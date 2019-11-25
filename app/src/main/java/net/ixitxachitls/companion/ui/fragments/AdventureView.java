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

import com.google.android.material.tabs.TabLayout;
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
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.util.Strings;
import net.ixitxachitls.companion.util.Texts;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

/**
 * A view for displaying adventure and encounterView information.
 */
public class AdventureView extends LinearLayout {

  private TextWrapper<TextView> adventure;
  private TextWrapper<TextView> encounterView;
  private TextWrapper<TextView> description;
  private TextWrapper<TextView> locations;
  private LinearLayout categoryText;
  private TabLayout categoryTabs;
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
    updateEncounter(adventureTemplate.isPresent()
        ? adventureTemplate.get().getEncounter(campaign.getEncounterId()) : Optional.empty());
  }

  private String formatEncounterTitle(AdventureTemplate.EncounterTemplate template) {
    return template.getId() + ": " + template.getName() + ", EL " + template.getEncounterLevel();
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
    locations = TextWrapper.wrap(view, R.id.locations);
    categoryText = view.findViewById(R.id.category_text);
    categoryTabs = view.findViewById(R.id.category_tabs);
    categoryTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override
      public void onTabSelected(TabLayout.Tab tab) {
        selectedCategory(tab.getPosition());
      }

      @Override
      public void onTabUnselected(TabLayout.Tab tab) {}

      @Override
      public void onTabReselected(TabLayout.Tab tab) {}
    });

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

  private void showCeilings() {
    categoryText.removeAllViews();
    if (encounterTemplate.isPresent() && !encounterTemplate.get().getCeilings().isEmpty()) {
      for (AdventureTemplate.EncounterTemplate.Ceiling ceiling :
          encounterTemplate.get().getCeilings()) {
        categoryText.addView(createCeiling(ceiling));
      }
    } else {
      showNoData();
    }
  }

  private void showDoors() {
    categoryText.removeAllViews();
    if (encounterTemplate.isPresent() && !encounterTemplate.get().getDoors().isEmpty()) {
      for (AdventureTemplate.EncounterTemplate.Door door : encounterTemplate.get().getDoors()) {
        categoryText.addView(createDoor(door));
      }
    } else {
      showNoData();
    }
  }

  private void showFeels() {
    categoryText.removeAllViews();
    if (encounterTemplate.isPresent() && !encounterTemplate.get().getFeels().isEmpty()) {
      for (String feel : encounterTemplate.get().getFeels()) {
        categoryText.addView(createText(feel));
      }
    } else {
      showNoData();
    }

  }

  private void showFloors() {
    categoryText.removeAllViews();
    if (encounterTemplate.isPresent() && encounterTemplate.get().getFloors().isEmpty()) {
      for (AdventureTemplate.EncounterTemplate.Spot floor : encounterTemplate.get().getFloors()) {
        categoryText.addView(createSpot(floor));
      }
    } else {
      showNoData();
    }
  }

  private void showLight() {
    categoryText.removeAllViews();
    if (encounterTemplate.isPresent() && !encounterTemplate.get().getLights().isEmpty()) {
      for (String light : encounterTemplate.get().getLights()) {
        categoryText.addView(createText(light));
      }
    } else {
      showNoData();
    }
  }

  private void showReadAloud() {
    categoryText.removeAllViews();
    if (encounterTemplate.isPresent() && !encounterTemplate.get().getReadAlouds().isEmpty()) {
      for (AdventureTemplate.EncounterTemplate.ReadAloud read
          : encounterTemplate.get().getReadAlouds()) {
        categoryText.addView(createReadAloudLine(read));
      }
    } else {
      showNoData();
    }
  }

  private void showNoData() {
    categoryText.addView(createText("No data available."));
  }

  private void showSmells() {
    categoryText.removeAllViews();
    if (encounterTemplate.isPresent() && !encounterTemplate.get().getSmells().isEmpty()) {
      for (String smell : encounterTemplate.get().getSmells()) {
        categoryText.addView(createText(smell));
      }
    } else {
      showNoData();
    }
  }

  private void showSounds() {
    categoryText.removeAllViews();
    if (encounterTemplate.isPresent() && !encounterTemplate.get().getSounds().isEmpty()) {
      for (String sound : encounterTemplate.get().getSounds()) {
        categoryText.addView(createText(sound));
      }
    } else {
      showNoData();
    }
  }

  private void showTerrains() {
    categoryText.removeAllViews();
    if (encounterTemplate.isPresent() && !encounterTemplate.get().getTerrains().isEmpty()) {
      for (AdventureTemplate.EncounterTemplate.Spot terrain :
          encounterTemplate.get().getTerrains()) {
        categoryText.addView(createSpot(terrain));
      }
    } else {
      showNoData();
    }
  }

  private void showTouch() {
    categoryText.removeAllViews();
    if (encounterTemplate.isPresent() && !encounterTemplate.get().getTouchs().isEmpty()) {
      for (String touch : encounterTemplate.get().getTouchs()) {
        categoryText.addView(createText(touch));
      }
    } else {
      showNoData();
    }
  }

  private void showTraps() {
    categoryText.removeAllViews();
    if (encounterTemplate.isPresent() && !encounterTemplate.get().getTraps().isEmpty()) {
      for (AdventureTemplate.EncounterTemplate.Spot trap : encounterTemplate.get().getTraps()) {
        categoryText.addView(createSpot(trap));
      }
    } else {
      showNoData();
    }
  }

  private void showWalls() {
    categoryText.removeAllViews();
    if (encounterTemplate.isPresent() && !encounterTemplate.get().getWalls().isEmpty()) {
      for (AdventureTemplate.EncounterTemplate.Spot wall : encounterTemplate.get().getWalls()) {
        categoryText.addView(createSpot(wall));
      }
    } else {
      showNoData();
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
      encounterView.text(formatEncounterTitle(encounterTemplate.get()));
      locations.text(Strings.COMMA_JOINER.join(encounterTemplate.get().getLocations()));
      categoryTabs.getTabAt(0).select();
      showReadAloud();

      creatures.removeAllViews();
      if (campaign.isPresent()) {
        Optional<Encounter> encounter =
            CompanionApplication.get().encounters().get(campaign.get().getId(),
                campaign.get().getAdventureId(), campaign.get().getEncounterId());
        if (encounter.isPresent()) {
          for (Monster monster : encounter.get().getMonsters()) {
            MonsterChipView chip = new MonsterChipView(getContext(), monster);
            creatures.addView(chip);
          }
        }
      }
    } else {
      description.text("");
      encounterView.text(campaign.isPresent() ? campaign.get().getEncounterId() : "");
      locations.text("");
      categoryText.removeAllViews();
      creatures.removeAllViews();
    }
  }

  private void selectedCategory(int position) {
    switch (position) {
      case 0: showReadAloud(); break;
      case 1: showCeilings(); break;
      case 2: showFloors(); break;
      case 3: showWalls(); break;
      case 4: showDoors(); break;
      case 5: showTerrains(); break;
      case 6: showTraps(); break;
      case 7: showLight(); break;
      case 8: showSounds(); break;
      case 9: showSmells(); break;
      case 10: showTouch(); break;
      case 11: showFeels(); break;
    }
  }
}
