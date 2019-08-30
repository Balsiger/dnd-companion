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

package net.ixitxachitls.companion.data.documents;

import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.data.CompanionContext;
import net.ixitxachitls.companion.util.Dice;

/**
 * A representation of a monster in the game.
 */
public class Monster extends Creature<Monster> {

  protected static final String PATH = "monsters";
  private static final DocumentFactory<Monster> FACTORY = () -> new Monster();

  public String getImagePath() {
    return "images/monsters/" + getName().toLowerCase() + ".jpg";
  }

  @Override
  public boolean hasInitiative(int encounterNumber) {
    return true;
  }

  @Override
  public String toString() {
    return getName();
  }

  public static Monster create(CompanionContext context, String campaignId, String name,
                               int initiativeModifier, int encounterNumber) {
    Monster monster = create(context, campaignId, name);
    monster.setEncounterInitiative(encounterNumber, Dice.d20() + initiativeModifier);

    return monster;
  }

  public static Monster create(CompanionContext context, String campaignId, String name) {
    Monster monster = Document.create(FACTORY, context, campaignId + "/" + PATH);
    monster.setName(name);
    monster.setCampaignId(campaignId);
    monster.initFromRace(name);

    return monster;
  }

  protected static Monster fromData(CompanionContext context, DocumentSnapshot snapshot) {
    return Document.fromData(FACTORY, context, snapshot);
  }
}
