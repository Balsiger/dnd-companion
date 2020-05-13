/*
 * Copyright (c) 2017-2019 Peter Balsiger
 * All rights reserved
 *
 * This file is part of the Roleplay Companion.
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

package net.ixitxachitls.companion.data.values;

import net.ixitxachitls.companion.FakeCompanionContext;
import net.ixitxachitls.companion.data.ItemTemplates;
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.templates.ItemTemplate;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.storage.TestAssetAccessor;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ItemTest {

  @Before
  public void loadEntities() {
    Templates.init(new TestAssetAccessor(), main);
  }

  @Test
  public void lookupByCategory() {
    Assert.assertThat(
        Templates.get().getItemTemplates().lookupTemplates(Template.ItemLookupProto.newBuilder()
            .addCategoryOr("sword")
            .addCategoryOr("bow")
            .build()).stream().map(t -> t.getName()).collect(Collectors.toList()),
        CoreMatchers.is(Arrays.asList(new String[]{
            "Arrows", "Bastard Sword", "Composite Longbow", "Composite Shortbow",
            "Dagger of Venom", "Falchion", "Greatsword", "Longbow", "Longsword", "Rapier",
            "Scimitar", "Short Sword", "Shortbow", "Two Bladed Sword"
        })));
  }

  @Test
  public void lookupByMaterial() {
    Assert.assertThat(
        Templates.get().getItemTemplates().lookupTemplates(Template.ItemLookupProto.newBuilder()
            .addMaterialOr(Template.ItemTemplateProto.Substance.Material.BONE)
            .addMaterialOr(Template.ItemTemplateProto.Substance.Material.ICE)
            .build()).stream().map(t -> t.getName()).collect(Collectors.toList()),
        CoreMatchers.is(Arrays.asList(new String[]{
            "Amulet of Natural Armor +1", "Amulet of Natural Armor +2",
            "Amulet of Natural Armor +5", "Hand of Kiaransalee's Glory",
            "Horn of Goodness or Evil", "Spectral Dagger",
        })));
  }

  @Test
  public void lookupByName() {
    assertEquals("name lookup",
        Templates.get().getItemTemplates().lookup(new FakeCompanionContext(),
            Template.ItemLookupProto.newBuilder()
                .setName("longsword")
                .build(), "creator", new CampaignDate()).get().getName(), "Longsword");
  }

  @Test
  public void lookupBySize() {
    Assert.assertThat(
        Templates.get().getItemTemplates().lookupTemplates(Template.ItemLookupProto.newBuilder()
            .addSizeOr(Value.SizeProto.Size.DIMINUTIVE)
            .addSizeOr(Value.SizeProto.Size.TINY)
            .addCategoryOr("gear")
            .build()).stream().map(t -> t.getName()).collect(Collectors.toList()),
        CoreMatchers.is(Arrays.asList(new String[]{
            "Armor Lubricant", "Bullseye Lantern", "Caltrops", "Chain", "Common Lamp", "Hammer",
            "Hooded Lantern", "Map Case", "Piton", "Spyglass", "Torch", "Trail Ration", "Waterskin",
        })));
  }

  @Test
  public void lookupByValue() {
    Assert.assertThat(
        Templates.get().getItemTemplates().lookupTemplates(Template.ItemLookupProto.newBuilder()
            .setValueMin(Value.MoneyProto.newBuilder().setGold(75000).build())
            .build()).stream().map(t -> t.getName()).collect(Collectors.toList()),
        CoreMatchers.is(Arrays.asList(new String[]{
            "Claw of the Revenancer", "Eyes of the Spider", "Pearl of Power 9th",
        })));
    Assert.assertThat(
        Templates.get().getItemTemplates().lookupTemplates(Template.ItemLookupProto.newBuilder()
            .setValueMax(Value.MoneyProto.newBuilder().setCopper(1).build())
            .addCategoryOr("gear")
            .build()).stream().map(t -> t.getName()).collect(Collectors.toList()),
        CoreMatchers.is(Arrays.asList(new String[]{"Candle", "Chalk", "Firewood", })));
    Assert.assertThat(
        Templates.get().getItemTemplates().lookupTemplates(Template.ItemLookupProto.newBuilder()
            .setValueMin(Value.MoneyProto.newBuilder().setGold(5000).build())
            .setValueMax(Value.MoneyProto.newBuilder().setGold(5000).build())
            .build()).stream().map(t -> t.getName()).collect(Collectors.toList()),
        CoreMatchers.is(Arrays.asList(new String[]{
            "Bag of Holding II", "Dusty Rose Ioun Stone", "Ring of Shocking Grasp",
        })));
  }

  @Test
  public void lookupByWeight() {
    Assert.assertThat(
        Templates.get().getItemTemplates().lookupTemplates(Template.ItemLookupProto.newBuilder()
            .setWeightMin(Value.WeightProto.newBuilder()
                .setImperial(Value.WeightProto.Imperial.newBuilder()
                    .setPounds(Value.RationalProto.newBuilder().setLeader(50).build()).build())
                .build())
            .build()).stream().map(t -> t.getName()).collect(Collectors.toList()),
        CoreMatchers.is(Arrays.asList(new String[]{"Full Plate", "Half-plate",})));
    Assert.assertThat(
        Templates.get().getItemTemplates().lookupTemplates(Template.ItemLookupProto.newBuilder()
            .setWeightMax(Value.WeightProto.newBuilder()
                .setImperial(Value.WeightProto.Imperial.newBuilder()
                    .setPounds(Value.RationalProto.newBuilder().setLeader(1).build()).build())
                .build())
            .addCategoryOr("gear")
            .setValueMin(Value.MoneyProto.newBuilder().setGold(100).build())
            .build()).stream().map(t -> t.getName()).collect(Collectors.toList()),
        CoreMatchers.is(Arrays.asList(new String[]{"Folding Boat", "Spyglass",})));
    Assert.assertThat(
        Templates.get().getItemTemplates().lookupTemplates(Template.ItemLookupProto.newBuilder()
            .setWeightMin(Value.WeightProto.newBuilder()
                .setImperial(Value.WeightProto.Imperial.newBuilder()
                    .setPounds(Value.RationalProto.newBuilder().setLeader(25).build()).build())
                .build())
            .setWeightMax(Value.WeightProto.newBuilder()
                .setImperial(Value.WeightProto.Imperial.newBuilder()
                    .setPounds(Value.RationalProto.newBuilder().setLeader(30).build()).build())
                .build())
            .build()).stream().map(t -> t.getName()).collect(Collectors.toList()),
        CoreMatchers.is(Arrays.asList(new String[]{
            "Bag of Holding II", "Barrel", "Breastplate", "Chain Shirt", "Chest", "Hide Armor",
            "Riding Saddle", "Scale Mail",
        })));
  }

  @Test
  public void lookupRandom() {
    List<ItemTemplate> templates =
        templates("Longsword", "Short Sword", "Dagger of Venom", "Quilted Silk Box", "Bolas",
            "Claw of the Revenancer");
    assertEquals("0", "Longsword", randomItem(templates, 0));
    assertEquals("1", "Longsword", randomItem(templates, 1));
    assertEquals("100", "Longsword", randomItem(templates, 100));
    assertEquals("999", "Longsword", randomItem(templates, 999));
    assertEquals("1000", "Short Sword", randomItem(templates, 1000));
    assertEquals("1500", "Short Sword", randomItem(templates, 1500));
    assertEquals("1999", "Short Sword", randomItem(templates, 1999));
    assertEquals("2000", "Dagger of Venom", randomItem(templates, 2000));
    assertEquals("2050", "Dagger of Venom", randomItem(templates, 2050));
    assertEquals("2099", "Dagger of Venom", randomItem(templates, 2099));
    assertEquals("2100", "Quilted Silk Box", randomItem(templates, 2100));
    assertEquals("2150", "Quilted Silk Box", randomItem(templates, 2150));
    assertEquals("2599", "Quilted Silk Box", randomItem(templates, 2599));
    assertEquals("2600", "Bolas", randomItem(templates, 2600));
    assertEquals("2601", "Bolas", randomItem(templates, 2601));
    assertEquals("2609", "Bolas", randomItem(templates, 2609));
    assertEquals("2610", "Claw of the Revenancer", randomItem(templates, 2610));
  }

  @Test
  public void nestedContent() {
    Item item = Templates.get().getItemTemplates().lookup(new FakeCompanionContext(),
        Template.ItemLookupProto.newBuilder()
            .setName("Backpack")
            .addContent(Template.ItemLookupProto.newBuilder().setName("Candle").build())
            .addContent(Template.ItemLookupProto.newBuilder().setName("Longsword").build())
            .addContent(Template.ItemLookupProto.newBuilder().setName("Chalk").build())
            .build(), "creator", new CampaignDate()).get();
    Assert.assertThat(item.getContents().stream()
        .map(i -> i.getName())
        .collect(Collectors.toList()),
        CoreMatchers.is(Arrays.asList(new String[]{"Candle", "Longsword", "Chalk"})));
  }

  @Test
  public void presetValues() {
    Item item = Templates.get().getItemTemplates().lookup(new FakeCompanionContext(),
        Template.ItemLookupProto.newBuilder()
            .setName("Longsword")
            .setValue(Value.MoneyProto.newBuilder().setGold(100).build())
            .setHp(42)
            .build(), "creator", new CampaignDate()).get();
    assertEquals("value", 100, item.getValue().asGold(), 0.1);
    assertEquals("hp", 42, item.getHp());
  }

  @Test
  public void templates() {
    Item item = Templates.get().getItemTemplates().lookup(new FakeCompanionContext(),
        Template.ItemLookupProto.newBuilder()
            .setName("Short Sword")
            .addTemplates("Weapon +2")
            .addTemplates("Frost")
            .build(), "creator", new CampaignDate()).get();
    assertEquals("name", "Short Sword +2 Frost", item.getName());
    assertEquals("value", "8310 gp", item.getValue().toString());
  }

  @Test
  public void weightedProbability() {
    List<ItemTemplate> templates =
        templates("Longsword", "Short Sword", "Dagger of Venom", "Quilted Silk Box", "Bolas",
            "Claw of the Revenancer");
    assertEquals("weighted", 2611,
        Templates.get().getItemTemplates().totalWeightedProbability(templates));
  }

  private String randomItem(List<ItemTemplate> templates, int random) {
    return Templates.get().getItemTemplates().randomItemTemplate(templates, random).get().getName();
  }

  private List<ItemTemplate> templates(String ... names) {
    ItemTemplates templates = Templates.get().getItemTemplates();
    return Arrays.asList(names).stream()
        .map(n -> templates.get(n).get())
        .collect(Collectors.toList());
  }
}