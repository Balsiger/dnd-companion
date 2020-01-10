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

package net.ixitxachitls.companion.data.documents;

import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.CompanionApplication;
import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.templates.AdventureTemplate;
import net.ixitxachitls.companion.data.values.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import androidx.annotation.CallSuper;

/**
 * An encounter in a campaign
 */
public class Encounter extends Document<Encounter> implements Item.Owner {

  private static final String FIELD_CAMPAIGN_ID = "campaign_id";
  private static final String FIELD_ADVENTURE_ID = "adventure_id";
  private static final String FIELD_ENCOUNTER_ID = "encounter_id";
  private static final String FIELD_ITEMS_GROUPS= "items-groups";

  private static final Document.DocumentFactory<Encounter> FACTORY = () -> new Encounter();

  private String campaignId;
  private String adventureId;
  private String encounterId;
  private List<ItemGroup> itemGroups = new ArrayList<>();
  private List<Monster> monsters = new ArrayList<>();
  private Optional<ItemGroup> adHocGroup = Optional.empty();

  public String getEncounterId() {
    return encounterId;
  }

  @Override
  protected Data write() {
    return Data.empty()
        .set(FIELD_CAMPAIGN_ID, campaignId)
        .set(FIELD_ADVENTURE_ID, adventureId)
        .set(FIELD_ENCOUNTER_ID, encounterId)
        .setNested(FIELD_ITEMS_GROUPS, itemGroups);
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();

    campaignId = data.get(FIELD_CAMPAIGN_ID, "");
    adventureId = data.get(FIELD_ADVENTURE_ID, "");
    encounterId = data.get(FIELD_ENCOUNTER_ID, "");
    itemGroups = data.getNestedList(FIELD_ITEMS_GROUPS).stream()
        .map(ItemGroup::read)
        .collect(Collectors.toList());
  }



  @Override
  public String toString() {
    return encounterId + " (" + adventureId + ")";
  }

  protected static Encounter fromData(CompanionContext context, DocumentSnapshot snapshot) {
    return Document.fromData(FACTORY, context, snapshot);
  }

  public static Encounter create(CompanionContext context, String campaignId, String adventureId,
                                 String encounterId) {
    Encounter encounter =
        Document.createWithId(FACTORY, context, createId(campaignId, adventureId, encounterId));
    encounter.campaignId = campaignId;
    encounter.adventureId = adventureId;
    encounter.encounterId = encounterId;

    Optional<AdventureTemplate> adventure =
        Templates.get().getAdventureTemplates().get(adventureId);
    if (adventure.isPresent() && adventure.get().getEncounter(encounterId).isPresent()) {
      encounter.initialize(adventure.get().getEncounter(encounterId).get());
    }

    return encounter;
  }

  private void initialize(AdventureTemplate.EncounterTemplate encounter) {
    for (AdventureTemplate.EncounterTemplate.ItemGroupInitializer groupInitializer
        : encounter.getItemGroups()) {
      List<Item> items = new ArrayList<>();
      for (AdventureTemplate.EncounterTemplate.ItemInitializer itemInitializer :
          groupInitializer.getItems()) {
        Optional<Item> item =
            Templates.get().getItemTemplates().lookup(context, itemInitializer.getLookup());
        if (item.isPresent()) {
          items.add(item.get());
        } else {
          Status.error("Cannot find item matching " + itemInitializer.getLookup());
        }
      }

      itemGroups.add(new ItemGroup(groupInitializer.getName(), groupInitializer.getDescription(),
          items));
    }

    for (AdventureTemplate.EncounterTemplate.CreatureInitializer creature
        : encounter.getCreatures()) {
      // TODO(Merlin): For now we assume all creatures are monsters.
      monsters.add(Monster.create(context, campaignId, creature.getName()));
    }
  }

  private static String createId(String campaignId, String adventureId, String encounterId) {
    return campaignId + "/" + Encounters.PATH_ADVENTURES + "/" + adventureId + "/"
        + Encounters.PATH_ENCOUNTERS + "/" + encounterId;
  }

  public List<Monster> getMonsters() {
    return monsters;
  }

  public List<ItemGroup> getItemGroups() {
    return itemGroups;
  }

  public static class ItemGroup extends NestedDocument {
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_ITEMS = "items";

    private String title;
    private String description;
    private List<Item> items;

    public ItemGroup(String title, String description, List<Item> items) {
      this.title = title;
      this.description = description;
      this.items = items;
    }

    public String getTitle() {
      return title;
    }

    public String getDescription() {
      return description;
    }

    public List<Item> getItems() {
      return items;
    }

    public void addItem(Item item) {
      items.add(item);
    }

    public Optional<Item> getItem(String id) {
      for (Item item : items) {
        if (item.getId().equals(id)) {
          return Optional.of(item);
        }
      }

      return Optional.empty();
    }

    public boolean remove(Item item) {
      return items.remove(item);
    }

    @Override
    public Data write() {
      return Data.empty()
          .set(FIELD_TITLE, title)
          .set(FIELD_DESCRIPTION, description)
          .setNested(FIELD_ITEMS, items);
    }

    public static ItemGroup read(Data data) {
      return new ItemGroup(data.get(FIELD_TITLE, ""), data.get(FIELD_DESCRIPTION, ""),
          data.getNestedList(FIELD_ITEMS).stream()
              .map(Item::read)
              .collect(Collectors.toList()));
    }
  }

  public Optional<Item> getItem(String id) {
    for (ItemGroup group : itemGroups) {
      Optional<Item> item = group.getItem(id);
      if (item.isPresent()) {
        return item;
      }
    }

    return Optional.empty();
  }

  public boolean removeItem(Item item) {
    for (ItemGroup group : itemGroups) {
      if (group.remove(item)) {
        store();
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean amDM() {
    return CompanionApplication.get().campaigns().get(campaignId).amDM();
  }

  @Override
  public void add(Item item) {
    if (!adHocGroup.isPresent()) {
      adHocGroup = Optional.of(new ItemGroup("Ad Hoc Items",
          "Items dynamically added before or during the encounter", new ArrayList<>()));
    }

    adHocGroup.get().addItem(item);

    updated(item);
  }

  @Override
  public void updated(Item item) {
    store();
  }

  @Override
  public boolean isCharacter() {
    return false;
  }
}
