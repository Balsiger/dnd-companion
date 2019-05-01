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

import android.support.annotation.CallSuper;

import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.data.values.TimedCondition;

import java.util.Map;

/**
 * A condition on a creature.
 */
public class CreatureCondition extends Document<CreatureCondition> {

  protected static final String FIELD_CREATURE = "creature";
  protected static final String FIELD_CONDITION = "condition";

  private static final Document.DocumentFactory<CreatureCondition> FACTORY =
      () -> new CreatureCondition();

  private TimedCondition condition;
  private String creatureId;

  public TimedCondition getCondition() {
    return condition;
  }

  public String getCreatureId() {
    return creatureId;
  }

  @Override
  public String toString() {
    return creatureId + ": " + condition;
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();

    creatureId = data.get(FIELD_CREATURE, "");
    if (data.has(FIELD_CONDITION)) {
      condition = TimedCondition.read(data.getNested(FIELD_CONDITION));
    }
  }

  @Override
  @CallSuper
  protected Map<String, Object> write(Map<String, Object> data) {
    data.put(FIELD_CREATURE, creatureId);
    data.put(FIELD_CONDITION, condition.write());

    return data;
  }

  public static CreatureCondition create(CompanionContext context, String creatureId,
                                         TimedCondition timedCondition) {
    CreatureCondition condition =
        Document.create(FACTORY, context, creatureId + "/" + CreatureConditions.PATH);
    condition.creatureId = creatureId;
    condition.condition = timedCondition;

    return condition;
  }

  protected static CreatureCondition fromData(CompanionContext context, DocumentSnapshot snapshot) {
    CreatureCondition condition = Document.fromData(FACTORY, context, snapshot);

    return condition;
  }
}
