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

package net.ixitxachitls.companion.data.documents;

/**
 * A representation of a monster in the game.
 */
public class Monster extends Creature<Monster> {

  /*
  @Deprecated public static final String Type = "creature";
  public static final String TYPE = "creatures";
  public static final String TABLE = "creatures";
  public static final String TABLE_LOCAL = TABLE + "_local";

  private final CompanionContext companionContext;

  public Creature(CompanionContext companionContext, long id, String name, String campaignId,
                  int initiativeModifier) {
    this(companionContext, id, name, campaignId);

    this.initiative = Dice.d20() + initiativeModifier;
  }

  public Creature(CompanionContext companionContext, long id, String name, String campaignId) {
    super(companionContext, id, TABLE, name, true, DataBaseContentProvider.CREATURES_LOCAL, campaignId);
    this.companionContext = companionContext;
  }

  @Override
  public Entry.CreatureProto toProto() {
    return toCreatureProto();
  }

  public static Creature fromProto(CompanionContext companionContext, long id, Entry.CreatureProto proto) {
    Creature creature = new Creature(companionContext, id, proto.getName(), proto.getCampaignId());
    creature.fromProto(proto);

    return creature;
  }

  @Override
  public boolean store() {
    boolean changed = super.store();
    if (changed) {
      if (companionContext.creatures().has(this)) {
        companionContext.creatures().update(this);
      } else {
        companionContext.creatures().add(this);
      }
    }

    return changed;
}

  @Override
  public int compareTo(@NonNull Creature that) {
    int name = this.name.compareTo(that.name);
    if (name != 0) {
      return name;
    }

    return this.getCreatureId().compareTo(that.getCreatureId());
  }

  @Override
  public String toString() {
    return getName() + " (" + Status.nameFor(getCreatureId()) + "/local)";
  }
   */
}
