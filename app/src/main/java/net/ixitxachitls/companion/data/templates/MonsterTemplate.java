/*
 * Copyright (c) 2017-2018 Peter Balsiger
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

import net.ixitxachitls.companion.data.documents.Feat;
import net.ixitxachitls.companion.data.documents.Quality;
import net.ixitxachitls.companion.data.enums.Alignment;
import net.ixitxachitls.companion.data.enums.AlignmentStatus;
import net.ixitxachitls.companion.data.enums.MonsterSubType;
import net.ixitxachitls.companion.data.enums.MonsterType;
import net.ixitxachitls.companion.data.enums.Size;
import net.ixitxachitls.companion.data.values.Modifier;
import net.ixitxachitls.companion.data.values.Speed;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.proto.Value;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The representation of a monster template that can be used to create actual, real monsters in
 * the game.
 */
public class MonsterTemplate extends StoredTemplate<Template.MonsterTemplateProto> {

  public static final String TYPE = "monster";
  private final Template.MonsterTemplateProto proto;

  protected MonsterTemplate(String name, Template.MonsterTemplateProto proto) {
    super(proto.getTemplate(), name);
    this.proto = proto;
  }

  public Alignment getAlignment() {
    return Alignment.fromProto(proto.getAlignment());
  }

  public AlignmentStatus getAlignmentStatus() {
    return AlignmentStatus.fromProto(proto.getAlignmentStatus());
  }

  public Collection<Feat> getAutomaticFeats() {
    return proto.getAutomaticFeatList().stream()
        .map(p -> new Feat(p, "Automatic feat as " + proto.getTemplate().getName()))
        .collect(Collectors.toList());
  }

  public int getBaseAttack() {
    return proto.getBaseAttack();
  }

  public int getCharisma() {
    return proto.getAbilities().getCharisma();
  }

  public int getConstitution() {
    return proto.getAbilities().getConstitution();
  }

  public int getDexterity() {
    return proto.getAbilities().getDexterity();
  }

  public Collection<Feat> getFeats() {
    return proto.getFeatList().stream()
        .map(f -> new Feat(f, getName()))
        .collect(Collectors.toList());
  }

  public int getFortitudeSave() {
    return proto.getSaves().getFortitude();
  }

  public int getIntelligenec() {
    return proto.getAbilities().getIntelligence();
  }

  public List<Modifier> getNaturalArmor() {
    return proto.getNaturalArmor().getModifierList().stream()
        .map(p -> Modifier.fromProto(p, getName()))
        .collect(Collectors.toList());
  }

  @Override
  public Set<String> getProductIds() {
    return extractProductIds(proto.getTemplate());
  }

  public Collection<Quality> getQualities() {
    return proto.getQualityList().stream()
        .map(f -> new Quality(f, getName()))
        .collect(Collectors.toList());
  }

  public int getReflexSave() {
    return proto.getSaves().getReflex();
  }

  public Size getSize() {
    return Size.fromProto(proto.getSize().getSize());
  }

  public int getSkillPointBonus() {
    return proto.getSkillPointBonus();
  }

  public List<Speed> getSpeeds() {
    return proto.getSpeedList().stream().map(Speed::fromProto).collect(Collectors.toList());
  }

  public int getStrength() {
    return proto.getAbilities().getStrength();
  }

  public List<MonsterSubType> getSubtypes() {
    return proto.getSubtypeList().stream()
        .map(MonsterSubType::fromProto)
        .collect(Collectors.toList());
  }

  public MonsterType getType() {
    return MonsterType.fromProto(proto.getType());
  }

  public int getWalkingSpeed() {
    for (Value.SpeedProto speed : proto.getSpeedList()) {
      if (speed.getMode() == Value.SpeedProto.Mode.RUN) {
        return speed.getSquares();
      }
    }

    return 0;
  }

  public int getWillSave() {
    return proto.getSaves().getWill();
  }

  public int getWisdom() {
    return proto.getAbilities().getWisdom();
  }

  public boolean isPrimaryRace() {
    return proto.getMainRace();
  }

  public int getHitDice() {
    return proto.getHitDice().getNumber();
  }

  public int getHitDie() {
    return proto.getHitDice().getDice();
  }

  public boolean hasBonusFeat(int level) {
    return level == 1 && proto.getBonusFeat();
  }

  public boolean hasNaturalArmor() {
    return proto.hasNaturalArmor();
  }

  @Override
  public String toString() {
    return getName();
  }

  public static Template.MonsterTemplateProto defaultProto() {
    return Template.MonsterTemplateProto.getDefaultInstance();
  }

  public static MonsterTemplate fromProto(Template.MonsterTemplateProto proto) {
    MonsterTemplate template = new MonsterTemplate(proto.getTemplate().getName(), proto);

    return template;
  }
}
