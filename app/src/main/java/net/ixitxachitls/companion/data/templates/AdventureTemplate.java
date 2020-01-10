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

package net.ixitxachitls.companion.data.templates;

import com.google.common.collect.ImmutableList;

import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A template for a complete Adventure
 */
public class AdventureTemplate extends StoredTemplate<Template.AdventureTemplateProto> {

  public static final String TYPE = "adventure";

  private final Template.AdventureTemplateProto proto;
  private final List<EncounterTemplate> encounters = new ArrayList<>();

  public AdventureTemplate(Template.AdventureTemplateProto proto) {
    super(proto.getTemplate().getId());

    this.proto = proto;
  }

  public List<EncounterTemplate> getEncounters() {
    ensureEncounters();
    return Collections.unmodifiableList(this.encounters);
  }

  public String getId() {
    return proto.getTemplate().getId();
  }

  public String getTitle() {
    return proto.getTemplate().getName();
  }

  public Optional<EncounterTemplate> getEncounter(String encounterId) {
    ensureEncounters();
    for (EncounterTemplate encounter : encounters) {
      if (encounter.getId().equals(encounterId)) {
        return Optional.of(encounter);
      }
    }

    return Optional.empty();
  }

  @Override
  public Set<String> getProductIds() {
    return extractProductIds(proto.getTemplate());
  }

  private void ensureEncounters() {
    if (encounters.isEmpty()) {
      encounters.addAll(proto.getEncounterList().stream()
          .map(proto -> new EncounterTemplate(proto))
          .collect(Collectors.toList()));
    }
  }

  public static Template.AdventureTemplateProto defaultProto() {
    return Template.AdventureTemplateProto.getDefaultInstance();
  }

  public static AdventureTemplate fromProto(Template.AdventureTemplateProto proto) {
    return new AdventureTemplate(proto);
  }

  public class EncounterTemplate {
    private final Template.AdventureTemplateProto.Encounter proto;

    public EncounterTemplate(Template.AdventureTemplateProto.Encounter proto) {
      this.proto = proto;
    }

    public List<Ceiling> getCeilings() {
      return proto.getEnvironment().getCeilingList().stream()
          .map(f -> new Ceiling(f.getName(), f.getDescription(), f.getHeightFeet(),
              f.getMinHeightFeet(), f.getMaxHeightFeet()))
          .collect(Collectors.toList());
    }

    public String getDescription() {
      return proto.getDescription();
    }

    public List<Door> getDoors() {
      return proto.getEnvironment().getDoorList().stream()
          .map(f -> new Door(f.getName(), f.getDescription(), f.getThicknessInches(),
              f.getHardness(), f.getHp()))
          .collect(Collectors.toList());
    }

    public int getEncounterLevel() {
      return proto.getEncounterLevel();
    }

    public List<String> getFeels() {
      return proto.getSenses().getFeelList();
    }

    public List<Spot> getFloors() {
      return proto.getEnvironment().getFloorList().stream()
          .map(f -> new Spot(f.getName(), f.getDescription(), f.getCheckList().stream()
              .map(c -> new Check(c.getName(), c.getDc(), c.getModifier(), c.getConditionList()))
              .collect(Collectors.toList())))
          .collect(Collectors.toList());
    }

    public String getId() {
      return proto.getShortName();
    }

    public List<String> getLights() {
      return proto.getSenses().getLightList();
    }

    public List<String> getLocations() {
      return proto.getLocationList();
    }

    public String getName() {
      return proto.getName();
    }

    public List<ReadAloud> getReadAlouds() {
      return proto.getReadAloudList().stream()
          .map(r -> new ReadAloud(r.getCondition(), r.getText()))
          .collect(Collectors.toList());
    }

    public String getShortDescription() {
      return proto.getShortDescription();
    }

    public List<String> getSmells() {
      return proto.getSenses().getSmellList();
    }

    public List<String> getSounds() {
      return proto.getSenses().getSoundList();
    }

    public List<Spot> getTerrains() {
      return proto.getEnvironment().getTerrainList().stream()
          .map(f -> new Spot(f.getName(), f.getDescription(), f.getCheckList().stream()
              .map(c -> new Check(c.getName(), c.getDc(), c.getModifier(), c.getConditionList()))
              .collect(Collectors.toList())))
          .collect(Collectors.toList());
    }

    public List<String> getTouchs() {
      return proto.getSenses().getTouchList();
    }

    public List<Spot> getTraps() {
      return proto.getEnvironment().getTrapList().stream()
          .map(f -> new Spot(f.getName(), f.getDescription(), f.getCheckList().stream()
              .map(c -> new Check(c.getName(), c.getDc(), c.getModifier(), c.getConditionList()))
              .collect(Collectors.toList())))
          .collect(Collectors.toList());
    }

    public List<Spot> getWalls() {
      return proto.getEnvironment().getWallsList().stream()
          .map(f -> new Spot(f.getName(), f.getDescription(), f.getCheckList().stream()
              .map(c -> new Check(c.getName(), c.getDc(), c.getModifier(), c.getConditionList()))
              .collect(Collectors.toList())))
          .collect(Collectors.toList());
    }

    @Deprecated
    public List<ItemInitializer> getItems() {
      return proto.getTreasureList().stream()
          .flatMap(c -> c.getItemList().stream())
          .map(c -> new ItemInitializer(c))
          .collect(Collectors.toList());
    }

    public List<ItemGroupInitializer> getItemGroups() {
      return proto.getTreasureList().stream()
          .map(c -> new ItemGroupInitializer(c.getName(), c.getDescription(),
              c.getItemList().stream()
                  .map(i -> new ItemInitializer(i))
                  .collect(Collectors.toList())))
          .collect(Collectors.toList());
    }

    public List<CreatureInitializer> getCreatures() {
      return proto.getCreatureList().stream()
          .map(c -> new CreatureInitializer(c.getName(), c.getReason(), c.getTacticsList()))
          .collect(Collectors.toList());
    }

    public class ItemGroupInitializer {
      private final String name;
      private final String description;
      private final List<ItemInitializer> items;

      public ItemGroupInitializer(String name, String description, List<ItemInitializer> items) {
        this.name = name;
        this.description = description;
        this.items = items;
      }

      public String getDescription() {
        return description;
      }

      public List<ItemInitializer> getItems() {
        return items;
      }

      public String getName() {
        return name;
      }
    }

    public class ItemInitializer {
      private final Template.ItemLookupProto lookup;

      ItemInitializer(Template.ItemLookupProto lookup) {
        this.lookup = lookup;
      }

      public Template.ItemLookupProto getLookup() {
        return lookup;
      }
    }

    public class CreatureInitializer {
      private final String name;
      private final String reason;
      private final List<String> tactics;

      public CreatureInitializer(String name, String reason, List<String> tactics) {
        this.name = name;
        this.reason = reason;
        this.tactics = tactics;
      }

      public String getName() {
        return name;
      }

      public String getReason() {
        return reason;
      }

      public List<String> getTactics() {
        return tactics;
      }
    }

    public class Check {
      private final String name;
      private final int dc;
      private final int modifier;
      private final ImmutableList<String> conditions;

      public Check(String name, int dc, int modifier, List<String> conditions) {
        this.name = name;
        this.dc = dc;
        this.modifier = modifier;
        this.conditions = ImmutableList.copyOf(conditions);
      }

      public ImmutableList<String> getConditions() {
        return conditions;
      }

      public int getDc() {
        return dc;
      }

      public int getModifier() {
        return modifier;
      }

      public String getName() {
        return name;
      }

      public String format() {
        return name + " DC " + dc + ", " + modifier + " if " + Strings.AND_JOINER.join(conditions);
      }
    }

    public class Spot {
      private final String name;
      private final String description;
      private final ImmutableList<Check> checks;

      public Spot(String name, String description, List<Check> checks) {
        this.name = name;
        this.description = description;
        this.checks = ImmutableList.copyOf(checks);
      }

      public ImmutableList<Check> getChecks() {
        return checks;
      }

      public String getDescription() {
        return description;
      }

      public String getName() {
        return name;
      }

      public String format() {
        return "\\bold{" + name + "}: " + description + ";" + Strings.COMMA_JOINER.join(
            checks.stream().map(Check::format).collect(Collectors.toList()));
      }
    }

    public class Ceiling {
      private final String name;
      private final String description;
      private final int heightFeet;
      private final int heightMinFeet;
      private final int heightMaxFeet;

      public Ceiling(String name, String description, int heightFeet, int heightMinFeet,
                     int heightMaxFeet) {
        this.name = name;
        this.description = description;
        this.heightFeet = heightFeet;
        this.heightMinFeet = heightMinFeet;
        this.heightMaxFeet = heightMaxFeet;
      }

      public String format() {
        return "\\bold{" + name + "}: " + formatHeight() + "\n" + description;
      }

      private String formatHeight() {
        if (heightMinFeet == heightMaxFeet) {
          return heightFeet + " ft";
        }

        return heightFeet + " ft (" + heightMinFeet + "-" + heightMaxFeet + ")";
      }
    }

    public class Door {
      private final String name;
      private final String description;
      private final int thicknessInches;
      private final int hardness;
      private final int hp;

      public Door(String name, String description, int thicknessInches, int hardness, int hp) {
        this.name = name;
        this.description = description;
        this.thicknessInches = thicknessInches;
        this.hardness = hardness;
        this.hp = hp;
      }

      public String format() {
        return "\\bold{" + name + "}: (" +
            thicknessInches + " inches thick, hardness " + hardness + ", " + hp + " hp" + ")\n" +
            description;
      }
    }

    public class ReadAloud {
      private final String condition;
      private final String text;

      public ReadAloud(String condition, String text) {
        this.condition = condition;
        this.text = text;
      }

      public String getCondition() {
        return condition;
      }

      public String getText() {
        return text;
      }
    }
  }
}
