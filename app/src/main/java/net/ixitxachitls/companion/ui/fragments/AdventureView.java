/*
 * Copyright (c) 2017-2019 Peter Balsiger
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

package net.ixitxachitls.companion.ui.fragments;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.templates.AdventureTemplate;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.util.Strings;
import net.ixitxachitls.companion.util.Texts;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

/**
 * A view for displaying adventure and encounter information.
 */
public class AdventureView extends LinearLayout {

  private TextWrapper<TextView> adventure;
  private TextWrapper<TextView> encounter;
  private TextWrapper<TextView> description;
  private TextWrapper<TextView> level;
  private TextWrapper<TextView> locations;
  private LinearLayout readAlouds;
  private LinearLayout floors;
  private LinearLayout ceilings;
  private Optional<Campaign> campaign = Optional.empty();
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
    encounter.onClick(this::selectEncounter);
    updateAdventure(Templates.get().getAdventureTemplates().get(campaign.getAdventureId()));
    if (adventureTemplate.isPresent()) {
      encounterTemplate = adventureTemplate.get().getEncounter(campaign.getEncounterId());
      encounter.text(encounterTemplate.isPresent()
          ? encounterTemplate.get().getId() + ":  " + encounterTemplate.get().getName()
          : campaign.getEncounterId());
    } else {
      adventure.text(campaign.getAdventureId());
      encounter.text("");
    }
  }

  private TextView createCeiling(AdventureTemplate.EncounterTemplate.Ceiling ceiling) {
    return TextWrapper.wrap(new TextView(getContext()))
        .text(Texts.processCommands(getContext(), ceiling.format()))
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

  @CallSuper
  protected void init() {
    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_adventure, null, false);

    adventure = TextWrapper.wrap(view, R.id.adventure);
    encounter = TextWrapper.wrap(view, R.id.encounter);
    description = TextWrapper.wrap(view, R.id.description);
    level = TextWrapper.wrap(view, R.id.encounter_level);
    locations = TextWrapper.wrap(view, R.id.locations);
    readAlouds = view.findViewById(R.id.read_alouds);
    floors = view.findViewById(R.id.floors);
    ceilings = view.findViewById(R.id.ceilings);

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
                .map(t -> new ListSelectDialog.Entry(t.getName(), t.getId()))
                .collect(Collectors.toList()), R.color.campaign);
        dialog.setSelectListener(this::selectedEncounter);
        dialog.display();
      }
    }
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
      Status.error("Expected a single encounter!");
    }

    if (campaign.isPresent()) {
      campaign.get().setEncounterId(strings.get(0));
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
    readAlouds.removeAllViews();
    floors.removeAllViews();
    ceilings.removeAllViews();
    if (encounterTemplate.isPresent()) {
      description.text(Texts.processCommands(getContext(),
          encounterTemplate.get().getDescription()));
      encounter.text(encounterTemplate.get().getId() + ":" + encounterTemplate.get().getName());
      level.text(String.valueOf(encounterTemplate.get().getEncounterLevel()));
      locations.text(Strings.COMMA_JOINER.join(encounterTemplate.get().getLocations()));
      readAlouds.removeAllViews();
      for (AdventureTemplate.EncounterTemplate.ReadAloud read
          : encounterTemplate.get().getReadAlouds()) {
        readAlouds.addView(createReadAloudLine(read));
      }
      for (AdventureTemplate.EncounterTemplate.Spot floor: encounterTemplate.get().getFloors()) {
        floors.addView(createSpot(floor));
      }
      for (AdventureTemplate.EncounterTemplate.Ceiling ceiling: encounterTemplate.get().getCeilings()) {
        ceilings.addView(createCeiling(ceiling));
      }
    } else {
      description.text("");
      encounter.text(campaign.isPresent() ? campaign.get().getEncounterId() : "");
      level.text("");
      locations.text("");
    }
  }
}
