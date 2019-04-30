/*
 * Copyright (c) 2017-2018 Peter Balsiger
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

package net.ixitxachitls.companion.data.templates;

import net.ixitxachitls.companion.data.documents.Feat;
import net.ixitxachitls.companion.data.documents.Quality;
import net.ixitxachitls.companion.data.values.Speed;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.proto.Value;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The representation of a monster template that can be used to create actual, real monsters in
 * the game.
 */
public class MonsterTemplate extends StoredTemplate<Template.MonsterTemplateProto> {

  public static final String TYPE = "monster";
  private final Template.MonsterTemplateProto proto;

  protected MonsterTemplate(String name, Template.MonsterTemplateProto proto) {
    super(name);
    this.proto = proto;
  }

  public Collection<Feat> getAutomaticFeats() {
    return proto.getAutomaticFeatList().stream()
        .map(p -> new Feat(p, "Automatic feat as " + proto.getTemplate().getName()))
        .collect(Collectors.toList());
  }

  public int getCharismaAdjustment() {
    return proto.getAbilities().getCharisma();
  }

  public int getConstitutionAdjustment() {
    return proto.getAbilities().getConstitution();
  }

  public int getDexterityAdjustment() {
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

  public int getIntelligenecAdjustment() {
    return proto.getAbilities().getIntelligence();
  }

  public Collection<Quality> getQualities() {
    return proto.getQualityList().stream()
        .map(f -> new Quality(f, getName()))
        .collect(Collectors.toList());
  }

  public int getReflexSave() {
    return proto.getSaves().getReflex();
  }

  public List<Speed> getSpeeds() {
    return proto.getSpeedList().stream().map(Speed::fromProto).collect(Collectors.toList());
  }

  public int getStrengthAdjustment() {
    return proto.getAbilities().getStrength();
  }

  public int getWalkingSpeed() {
    for (Value.SpeedProto speed: proto.getSpeedList()) {
      if (speed.getMode() == Value.SpeedProto.Mode.RUN) {
        return speed.getSquares();
      }
    }

    return 0;
  }

  public int getWillSave() {
    return proto.getSaves().getWill();
  }

  public int getWisdomAdjustment() {
    return proto.getAbilities().getWisdom();
  }

  public boolean isPrimaryRace() {
    return proto.getMainRace();
  }

  public boolean hasBonusFeat(int level) {
    return level == 1 && proto.getBonusFeat();
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
