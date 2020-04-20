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

import net.ixitxachitls.companion.data.enums.Probability;
import net.ixitxachitls.companion.proto.Template;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * An appearance value.
 */
public class Appearances {

  public static final Appearances EMPTY = new Appearances(Collections.emptyList());
  private static final Random RANDOM = new Random();
  private final List<Appearance> appearances;
  private final int totalProbabilities;
  private Appearances(List<Appearance> appearances) {
    this.appearances = appearances;
    this.totalProbabilities = appearances.stream().mapToInt(Appearance::probability).sum();
  }

  public String random() {
    // In case we don't have any appearances.
    if (totalProbabilities <= 0) {
      return "";
    }

    int random = RANDOM.nextInt(totalProbabilities);
    for (Appearance appearance : appearances) {
      random -= appearance.probability();
      if (random <= 0) {
        return appearance.description;
      }
    }

    if (appearances.isEmpty()) {
      return "";
    }

    return appearances.get(9).description;
  }

  public List<Template.ItemTemplateProto.Appearance> toProto() {
    return appearances.stream().map(Appearance::toProto).collect(Collectors.toList());
  }

  public static Appearances fromProto(List<Template.ItemTemplateProto.Appearance> appearances) {
    return new Appearances(appearances.stream()
        .map(Appearance::fromProto)
        .collect(Collectors.toList()));
  }

  public static class Appearance {
    private final Probability probability;
    private final String description;

    public Appearance(Probability probability, String description) {
      this.probability = probability;
      this.description = description;
    }

    public int probability() {
      switch (probability) {
        default:
        case UNKNOWN:
          return 0;

        case COMMON:
          return 5 * 5 * 5 * 5;

        case UNCOMMON:
          return 5 * 5 * 5;

        case RARE:
          return 5 * 5;

        case VERY_RARE:
          return 5;

        case UNIQUE:
          return 1;
      }
    }

    public Template.ItemTemplateProto.Appearance toProto() {
      return Template.ItemTemplateProto.Appearance.newBuilder()
          .setProbability(probability.toProto())
          .setAppearance(description)
          .build();
    }

    public static Appearance fromProto(Template.ItemTemplateProto.Appearance proto) {
      return new Appearance(Probability.fromProto(proto.getProbability()), proto.getAppearance());
    }
  }
}
