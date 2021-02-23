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

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.data.values.Distance;
import net.ixitxachitls.companion.data.values.Duration;
import net.ixitxachitls.companion.proto.Template;
import net.ixitxachitls.companion.proto.Value;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * A template for a spell.
 */
public class SpellTemplate extends StoredTemplate<Template.SpellTemplateProto> {
  public static final String TYPE = "spell";

  private final Template.SpellTemplateProto proto;

  public SpellTemplate(Template.SpellTemplateProto proto, String name) {
    super(proto.getTemplate(), name);
    this.proto = proto;
  }

  public String getShortDescription() {
    return proto.getTemplate().getShortDescription();
  }

  @Override
  public Set<String> getProductIds() {
    return extractProductIds(proto.getTemplate());
  }

  public String formatSchool() {
    return Strings.toWords(proto.getSchool().toString()) +
        (proto.getSubschoolCount() > 0 ?
            " (" + Strings.COMMA_JOINER.join(proto.getSubschoolList().stream()
                .map(s -> Strings.toWords(s.toString())).collect(Collectors.toList())) + ")" : "") +
        (proto.getDescriptorCount() > 0 ?
            " [" + Strings.COMMA_JOINER.join(proto.getDescriptorList().stream()
                .map(s -> Strings.toWords(s.toString())).collect(Collectors.toList())) + "]" : "");
  }

  public String formatLevel() {
    return Strings.COMMA_JOINER.join(proto.getLevelList().stream()
        .map(l -> Strings.toWords(l.getSpellClass().toString()) + " " + l.getLevel())
        .collect(Collectors.toList()));
  }

  public String formatComponents() {
    return Strings.COMMA_JOINER.join(proto.getComponentsList().stream()
        .map(c -> Strings.toWords(c.toString()))
        .collect(Collectors.toList()))
        + (proto.getMaterialCount() > 0 ?
        " (" + Strings.COMMA_JOINER.join(proto.getMaterialList().stream()
            .flatMap(m -> m.getComponentList().stream())
            .collect(Collectors.toList())) + ")" : "")
        + (proto.hasFocus() ?
        " (" + Strings.COMMA_JOINER.join(proto.getFocus().getComponentList()) + ")" : "");
  }

  public Duration getCastingTime() {
    return Duration.fromProto(proto.getCastingTime());
  }

  public String formatRange(int level) {
    switch (proto.getSpecialRange()) {
      case UNRECOGNIZED:
      case UNKNOWN_RANGE:
      default:
        return Distance.fromProto(proto.getRange()).toString();

      case PERSONAL_OR_TOUCH:
        return "Personal or Touch";

      case PERSONAL_AND_TOUCH:
        return "Personal and Touch";

      case PERSONAL_OR_CLOSE:
        return "Personal or " + computeCloseRange(level) + " ft";

      case PERSONAL:
        return "Personal";

      case TOUCH:
        return "Touch";

      case CLOSE:
        return computeCloseRange(level) + " ft";

      case MEDIUM:
        return computeMediumRange(level) + " ft";

      case LONG:
        return computeLongRange(level) + " ft";

      case UNLIMITED:
        return "Unlimited";

      case FOURTY_FEET_PER_LEVEL:
        return (level * 40) + " ft";

      case SEE_TEXT_RANGE:
        return "See Text";

      case ANYWHERE_WITHIN_AREA_WARDED:
        return "Anywhere within area warded";

      case ONE_MILE_PER_LEVEL:
        return Strings.plural(level, "mile", "miles");

      case UP_TO_TEN_FEET_PER_LEVEL:
        return (10 * level) + " ft";
    }
  }

  public String formatEffect() {
    return proto.getEffect().getDescription();
  }

  public String formatDuration(int level) {
    List<String> results = new ArrayList<>();

    if (proto.getDuration().hasDuration()) {
      results.add(Duration.fromProto(proto.getDuration().getDuration()).multiply(level)
          .toString());
    }

    if (proto.getDuration().hasAdditionalDuration()) {
      results.add(Duration.fromProto(proto.getDuration().getAdditionalDuration()).multiply(level)
          .toString());
    }

    if (!proto.getDuration().getFlags().isEmpty()) {
      results.add(proto.getDuration().getFlags());
    }

    results.add(proto.getDuration().getDurationDescription());

    return Strings.SPACE_JOINER.join(results);
  }

  public String getTarget() {
    return proto.getTarget();
  }

  public String getSavingThrow() {
    return proto.getSavingThrow();
  }

  public String getSpellResistance() {
    return proto.getSpellResistance();
  }

  public String formatSavingThrow(Value.SpellClass spellClass, int abilityBonus) {
    if (proto.getSavingThrow().equals("None")) {
      return proto.getSavingThrow();
    }

    return proto.getSavingThrow() + " [DC " +  saveDC(spellClass, abilityBonus) + "]";
}

  private int saveDC(Value.SpellClass spellClass, int abilityBonus) {
    for (Template.SpellTemplateProto.Level level : proto.getLevelList()) {
      if (level.getSpellClass() == spellClass) {
        return 10 + level.getLevel() + abilityBonus;
      }
    }

    if (proto.getLevelList().isEmpty()) {
      return 10 + abilityBonus;
    }

    return 10 + proto.getLevel(0).getLevel() + abilityBonus;
  }


  public List<Template.SpellTemplateProto.Level> getLevels() {
    return proto.getLevelList();
  }

  private static int computeCloseRange(int level) {
    return 25 + (level / 2) * 5;
  }

  private static int computeMediumRange(int level) {
    return 100 + level * 10;
  }

  private static int computeLongRange(int level) {
    return 400 + level * 40;
  }

  public static Template.SpellTemplateProto defaultProto() {
    return Template.SpellTemplateProto.getDefaultInstance();
  }

  public static SpellTemplate fromProto(Template.SpellTemplateProto proto) {
    SpellTemplate spell = new SpellTemplate(proto, proto.getTemplate().getName());
    return spell;
  }
}
