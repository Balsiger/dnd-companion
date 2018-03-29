/*
 * Copyright (c) 2017-{2018} Peter Balsiger
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
 * along with the Roleplay Companion; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ixitxachitls.companion.data.dynamics;

import android.support.annotation.NonNull;

import net.ixitxachitls.companion.proto.Data;
import net.ixitxachitls.companion.storage.DataBaseContentProvider;
import net.ixitxachitls.companion.util.Dice;

/**
 * Representation of a monster in the game.
 */
public class Creature extends BaseCreature<Data.CreatureProto> implements Comparable<Creature> {
  @Deprecated public static final String Type = "creature";
  public static final String TYPE = "creatures";
  public static final String TABLE = "creatures";
  public static final String TABLE_LOCAL = TABLE + "_local";

  public Creature(long id, String name, String campaignId, int initiativeModifier) {
    this(id, name, campaignId);

    this.initiative = Dice.d20() + initiativeModifier;
  }

  public Creature(long id, String name, String campaignId) {
    super(id, TABLE, name, true,
        DataBaseContentProvider.CREATURES_LOCAL, campaignId);
  }

  @Override
  public Data.CreatureProto toProto() {
    return toCreatureProto();
  }

  public static Creature fromProto(long id, Data.CreatureProto proto) {
    Creature creature = new Creature(id, proto.getName(), proto.getCampaignId());
    creature.fromProto(proto);

    return creature;
  }

  @Override
  public boolean store() {
    boolean changed = super.store();
    if (changed) {
      if (Creatures.has(this)) {
        Creatures.update(this);
      } else {
        Creatures.add(this);
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
    return getName() + " (" + getCreatureId() + "/local)";
  }
}
