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

package net.ixitxachitls.companion.data.values;

import net.ixitxachitls.companion.Status;
import net.ixitxachitls.companion.proto.Entity;

/**
 * A substance in the game.
 */
public class Substance {

  public static final Substance ZERO = new Substance(Material.unknown, Distance.ZERO);

  private final Material material;
  private final Distance thickness;

  public enum Material {
    unknown, paper, cloth, rope, glass, ice, leather, hide, wood, stone, iron, steel, crystal,
    mithral, adamantine, bone,
  }

  protected Substance(Material material, Distance thickness) {
    this.material = material;
    this.thickness = thickness;
  }

  public int computeHp() {
    return 0;
  }

  public static Substance fromProto(Entity.ItemTemplateProto.Substance proto) {
    return new Substance(convert(proto.getMaterial()), Distance.fromProto(proto.getThickness()));
  }

  public Entity.ItemTemplateProto.Substance toProto() {
    return Entity.ItemTemplateProto.Substance.newBuilder()
        .setMaterial(convert(material))
        .setThickness(thickness.toProto())
        .build();
  }

  private static Material convert(Entity.ItemTemplateProto.Substance.Material material) {
    switch (material) {
      case UNRECOGNIZED:
      default:
        Status.error("Cannot convert material " + material);

      case UNKNOWN:
        return Material.unknown;

      case PAPER:
        return Material.paper;

      case CLOTH:
        return Material.cloth;

      case ROPE:
        return Material.rope;

      case GLASS:
        return Material.glass;

      case ICE:
        return Material.ice;

      case LEATHER:
        return Material.leather;

      case HIDE:
        return Material.hide;

      case WOOD:
        return Material.wood;

      case STONE:
        return Material.stone;

      case IRON:
        return Material.iron;

      case STEEL:
        return Material.steel;

      case CRYSTAL:
        return Material.crystal;

      case MITHRAL:
        return Material.mithral;

      case ADAMANTINE:
        return Material.adamantine;

      case BONE:
        return Material.bone;
    }
  }

  private Entity.ItemTemplateProto.Substance.Material convert(Material material) {
    switch (material) {
      default:
        Status.error("Cannot convert material " + material);

      case unknown:
        return Entity.ItemTemplateProto.Substance.Material.UNKNOWN;

      case paper:
        return Entity.ItemTemplateProto.Substance.Material.PAPER;

      case cloth:
        return Entity.ItemTemplateProto.Substance.Material.CLOTH;

      case rope:
        return Entity.ItemTemplateProto.Substance.Material.ROPE;

      case glass:
        return Entity.ItemTemplateProto.Substance.Material.GLASS;

      case ice:
        return Entity.ItemTemplateProto.Substance.Material.ICE;

      case leather:
        return Entity.ItemTemplateProto.Substance.Material.LEATHER;

      case hide:
        return Entity.ItemTemplateProto.Substance.Material.HIDE;

      case wood:
        return Entity.ItemTemplateProto.Substance.Material.WOOD;

      case stone:
        return Entity.ItemTemplateProto.Substance.Material.STONE;

      case iron:
        return Entity.ItemTemplateProto.Substance.Material.IRON;

      case steel:
        return Entity.ItemTemplateProto.Substance.Material.STEEL;

      case crystal:
        return Entity.ItemTemplateProto.Substance.Material.CRYSTAL;

      case mithral:
        return Entity.ItemTemplateProto.Substance.Material.MITHRAL;

      case adamantine:
        return Entity.ItemTemplateProto.Substance.Material.ADAMANTINE;

      case bone:
        return Entity.ItemTemplateProto.Substance.Material.BONE;
    }
  }
}