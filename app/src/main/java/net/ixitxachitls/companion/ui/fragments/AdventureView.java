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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.TemplatesStore;
import net.ixitxachitls.companion.data.documents.Adventures;
import net.ixitxachitls.companion.data.documents.Campaign;
import net.ixitxachitls.companion.data.documents.Character;
import net.ixitxachitls.companion.data.documents.Encounter;
import net.ixitxachitls.companion.data.documents.Encounters;
import net.ixitxachitls.companion.data.documents.Message;
import net.ixitxachitls.companion.data.documents.Monster;
import net.ixitxachitls.companion.data.templates.AdventureTemplate;
import net.ixitxachitls.companion.data.templates.SpellTemplate;
import net.ixitxachitls.companion.data.templates.values.SpellGroup;
import net.ixitxachitls.companion.data.values.Item;
import net.ixitxachitls.companion.ui.dialogs.SpellDialog;
import net.ixitxachitls.companion.ui.views.CloudImageView;
import net.ixitxachitls.companion.ui.views.ImageDropTarget;
import net.ixitxachitls.companion.ui.views.MonsterChipView;
import net.ixitxachitls.companion.ui.views.ViewViewPager;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Strings;
import net.ixitxachitls.companion.util.Texts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * A view for displaying adventure and encounterView information.
 */
public class AdventureView extends LinearLayout {

  // UI elements.
  private CloudImageView productImage;
  private TextWrapper<TextView> adventure;
  private TextWrapper<TextView> encounterView;
  private TextWrapper<TextView> description;
  private TextWrapper<TextView> locations;
  private TextWrapper<TextView> categoryTitle;
  private LinearLayout categoryText;
  private TabLayout categoryTabs;
  private ViewPager itemsPager;
  private HorizontalScrollView creaturesContainer;
  private LinearLayout creatures;
  private HorizontalScrollView charactersContainer;
  private LinearLayout characters;

  private Optional<Campaign> campaign = Optional.empty();
  private Optional<Encounter> encounter = Optional.empty();
  private Optional<AdventureTemplate> adventureTemplate;
  private Optional<AdventureTemplate.EncounterTemplate> encounterTemplate;
  private Map<String, Drawable> imageByCharacter = new HashMap<>();

  public AdventureView(Context context) {
    super(context);

    init();
  }

  public AdventureView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    init();
  }

  public void hideDetails() {
    categoryTabs.setVisibility(GONE);
    categoryTitle.gone();
    categoryText.setVisibility(GONE);
    creaturesContainer.setVisibility(GONE);
    itemsPager.setVisibility(GONE);
    charactersContainer.setVisibility(GONE);
  }

  public void resetEncounter() {
    if (campaign.isPresent()) {
      encounter = Optional.of(CompanionApplication.get().encounters()
          .reset(campaign.get().getEncounterId()));

      updateEncounter(adventureTemplate.get()
          .getEncounter(Encounters.extractShortId(campaign.get().getEncounterId())));
    }
  }

  public void showDetails() {
    categoryTabs.setVisibility(VISIBLE);
    categoryTitle.visible();
    categoryText.setVisibility(VISIBLE);
    creaturesContainer.setVisibility(VISIBLE);
    itemsPager.setVisibility(VISIBLE);
    charactersContainer.setVisibility(VISIBLE);
  }

  public void updateCampaign(Campaign campaign) {
    this.campaign = Optional.of(campaign);

    if (!campaign.getAdventureId().isEmpty()) {
      productImage.setImage("products/" + Adventures.extractShortId(campaign.getAdventureId()),
          R.drawable.noun_book_1411063);
    }
    adventure.onClick(this::selectAdventure);
    encounterView.onClick(this::selectEncounter);
    updateAdventure(Templates.get().getAdventureTemplates()
        .get(Adventures.extractShortId(campaign.getAdventureId())));
    updateEncounter(adventureTemplate.isPresent() ? adventureTemplate.get().getEncounter(
        Encounters.extractShortId(campaign.getEncounterId())) : Optional.empty());

    characters.removeAllViews();
    for (Character character :
        CompanionApplication.get().characters().getCampaignCharacters(campaign.getId())) {
      Drawable image = imageByCharacter.get(character.getId());
      if (image == null) {
        Optional<Bitmap> bitmap = CompanionApplication.get().images().get(character.getId(), 1);
        if (bitmap.isPresent()) {
          image = new BitmapDrawable(getResources(), bitmap.get());
          imageByCharacter.put(character.getId(), image);
        } else {
          image = getResources().getDrawable(R.drawable.ic_person_black_48dp, null);
        }
      }
      ImageDropTarget target =
          new ImageDropTarget(getContext(), image, character.getName(), true);
      target.setSupport(i -> i instanceof Item);
      target.setDropExecutor((i) -> moveItem(i, character));
      characters.addView(target);
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
    return TextWrapper.wrap(new TextView(getContext())).text(text).textColor(R.color.black).get();
  }

  private String formatEncounterName(String encounterId, String name) {
    if (campaign.isPresent()) {
      if (CompanionApplication.get().encounters().has(encounterId)) {
        return name;
      } else {
        return name + " (needs init)";
      }
    }

    return name;
  }

  private String formatEncounterTitle(AdventureTemplate.EncounterTemplate template) {
    return template.getId() + " - " + template.getName() + ", EL " + template.getEncounterLevel();
  }

  @CallSuper
  protected void init() {
    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_adventure, null, false);

    productImage = view.findViewById(R.id.product_image);
    adventure = TextWrapper.wrap(view, R.id.adventure);
    encounterView = TextWrapper.wrap(view, R.id.encounter);
    description = TextWrapper.wrap(view, R.id.description);
    locations = TextWrapper.wrap(view, R.id.locations);
    categoryTitle = TextWrapper.wrap(view, R.id.category_title);
    categoryText = view.findViewById(R.id.category_text);
    categoryTabs = view.findViewById(R.id.category_tabs);
    categoryTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override
      public void onTabReselected(TabLayout.Tab tab) {
      }

      @Override
      public void onTabSelected(TabLayout.Tab tab) {
        selectedCategory(tab.getPosition());
      }

      @Override
      public void onTabUnselected(TabLayout.Tab tab) {
      }
    });

    itemsPager = view.findViewById(R.id.items_pager);
    creaturesContainer = view.findViewById(R.id.creatures_container);
    creatures = view.findViewById(R.id.creatures);
    charactersContainer = view.findViewById(R.id.characters_container);
    characters = view.findViewById(R.id.characters);
    Wrapper<ImageDropTarget> drop = Wrapper.<ImageDropTarget>wrap(view, R.id.item_remove)
        .description("Remove Item", "Drag an item here to remove it.");
    drop.get().setSupport(i -> i instanceof Item);
    drop.get().setDropExecutor(this::removeItem);

    hideDetails();

    addView(view);
  }

  private boolean moveItem(Object state, Character character) {
    boolean removed = false;
    if (state instanceof Item && campaign.isPresent() && encounter.isPresent()) {
      removed = encounter.get().removeItem((Item) state);
      if (removed) {
        Message.createForItemAdd(CompanionApplication.get().context(),
            campaign.get().getAdventureId() + "#" + campaign.get().getAdventureId(),
            character.getId(), (Item) state);
      }
    }

    return removed;
  }

  private boolean removeItem(Object state) {
    if (state instanceof Item && campaign.isPresent() && encounter.isPresent()) {
      return encounter.get().removeItem((Item) state);
    }

    return false;
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
      Optional<AdventureTemplate> adventure = Templates.get().getAdventureTemplates()
          .get(Adventures.extractShortId(campaign.get().getAdventureId()));
      if (adventure.isPresent()) {
        ListSelectDialog dialog = ListSelectDialog.newInstance(R.string.select_encounter,
            ImmutableList.of(campaign.get().getEncounterId()),
            adventure.get().getEncounters().stream()
                .map(t -> new ListSelectDialog.Entry(formatEncounterTitle(t),
                    Encounters.createId(campaign.get().getAdventureId(), t.getId())))
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
      campaign.get().setAdventureId(Adventures.createId(campaign.get().getId(), strings.get(0)));
    }

    productImage.setImage("products/" + strings.get(0), R.drawable.noun_book_1411063);
  }

  private void selectedCategory(int position) {
    switch (position) {
      case 0:
        showReadAloud();
        break;
      case 1:
        showCeilings();
        break;
      case 2:
        showFloors();
        break;
      case 3:
        showWalls();
        break;
      case 4:
        showDoors();
        break;
      case 5:
        showTerrains();
        break;
      case 6:
        showTraps();
        break;
      case 7:
        showLight();
        break;
      case 8:
        showSounds();
        break;
      case 9:
        showSmells();
        break;
      case 10:
        showTouch();
        break;
      case 11:
        showFeels();
        break;
      case 12:
        showSpells();
        break;
    }
  }

  private void selectedEncounter(List<String> strings) {
    if (strings.size() != 1) {
      Status.error("Expected a single encounter!");
    }

    if (campaign.isPresent()) {
      campaign.get().setEncounterId(strings.get(0));
      if (CompanionApplication.get().encounters()
          .hasLoaded(campaign.get().getAdventureId())) {
        encounter = Optional.of(CompanionApplication.get().encounters()
            .getOrInitialize(strings.get(0)));
      } else {
        Status.error("Encounters have not yet been loaded for " + campaign.get().getName());
      }
    }
  }

  private void showCeilings() {
    categoryTitle.text("Ceiling");
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
    categoryTitle.text("Doors");
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
    categoryTitle.text("Feelings");
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
    categoryTitle.text("Floor");
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
    categoryTitle.text("Light");
    categoryText.removeAllViews();
    if (encounterTemplate.isPresent() && !encounterTemplate.get().getLights().isEmpty()) {
      for (String light : encounterTemplate.get().getLights()) {
        categoryText.addView(createText(light));
      }
    } else {
      showNoData();
    }
  }

  private void showNoData() {
    categoryText.addView(createText("No data available."));
  }

  private void showReadAloud() {
    categoryTitle.text("Description");
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

  private void showSmells() {
    categoryTitle.text("Smells");
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
    categoryTitle.text("Sounds");
    categoryText.removeAllViews();
    if (encounterTemplate.isPresent() && !encounterTemplate.get().getSounds().isEmpty()) {
      for (String sound : encounterTemplate.get().getSounds()) {
        categoryText.addView(createText(sound));
      }
    } else {
      showNoData();
    }
  }

  private void showSpells() {
    categoryTitle.text("Spells");
    categoryText.removeAllViews();

    TemplatesStore<SpellTemplate> spellTemplates = Templates.get().getSpellTemplates();
    for (SpellGroup group : encounterTemplate.get().getSpellGroups()) {
      categoryText.addView(TextWrapper.wrap(new TextView(getContext()))
          .text(group.getName() + " (" + Strings.numeral(group.getCasterLevel()) + ", "
              + Strings.signed(group.getAbilityBonus()) + ")")
          .textStyle(R.style.Title)
          /*.textColor(R.color.black)*/.get());
      for (SpellGroup.SpellReference spell : group.getSpells()) {
        Optional<SpellTemplate> template = spellTemplates.get(spell.getName());
        String name = template.isPresent() ? template.get().getName() : spell.getName();
        String description = template.isPresent() ?
            template.get().getShortDescription() : "(spell not found)";
        categoryText.addView(TextWrapper.wrap(new TextView(getContext()))
            .text(name + " - " + description)
            .textColor(R.color.grey_very_dark)
            .textStyle(R.style.SmallText)
            .onClick(() -> {
              SpellDialog.newInstance(name, group.getCasterLevel(),
                  group.getAbilityBonus(), group.getSpellClass(), spell.getMetaMagics()).display();
            }).get());
      }
    }
  }

  private void showTerrains() {
    categoryTitle.text("Terrain");
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
    categoryTitle.text("Touch");
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
    categoryTitle.text("Traps");
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
    categoryTitle.text("Walls");
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
        updateEncounter(adventureTemplate.get().getEncounter(
            Encounters.extractShortId(campaign.get().getEncounterId())));
      } else {
        updateEncounter(Optional.empty());
      }
    } else {
      if (campaign.isPresent()) {
        if (campaign.get().getAdventureId().isEmpty()) {
          adventure.text("<Select Adventure>");
        } else {
          adventure.text(campaign.get().getAdventureId());
        }
      } else {
        adventure.text("<No Campaign found>");
      }
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
        encounter = Optional.of(CompanionApplication.get().encounters()
            .getOrInitialize(campaign.get().getEncounterId()));

        List<ViewViewPager.StaticViewPagerAdapter.Entry> itemGroups = new ArrayList<>();
        for (Encounter.ItemGroup group : encounter.get().getItemGroups()) {
          EncounterItemGroupView view = new EncounterItemGroupView(getContext());
          view.setup(campaign.get(), encounter.get(), group.getDescription(), group.getItems());
          itemGroups.add(new ViewViewPager.StaticViewPagerAdapter.Entry(group.getTitle(), view));
        }
        itemsPager.setAdapter(new ViewViewPager.StaticViewPagerAdapter(itemGroups));

        for (Monster monster : encounter.get().getMonsters()) {
          MonsterChipView chip = new MonsterChipView(getContext(), monster);
          creatures.addView(chip);
        }
      }
    } else {
      description.text("");
      encounterView.text(campaign.isPresent() ?
          (campaign.get().getEncounterId().isEmpty() ? "<Select Encounter>" :
              Encounters.extractShortId(campaign.get().getEncounterId())) : "");
      locations.text("");
      categoryText.removeAllViews();
      creatures.removeAllViews();
    }
  }
}
