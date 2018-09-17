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

import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.values.Condition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by balsiger on 9/17/18.
 */
public class FSCharacter extends AbstractCreature<FSCharacter> implements Comparable<FSCharacter> {

  private static final String FIELD_NAME = "name";

  private final User player;

  private List<Character.Level> levels = new ArrayList<>();
  private int xp = 0;
  protected List<Condition> conditionsHistory = new ArrayList<>();

  public FSCharacter(User player) {
    super(player.getId() + "/" + FSCharacters.PATH);

    this.player = player;
  }

  protected FSCharacter(DocumentSnapshot snapshot, User player) {
    super(snapshot);

    this.player = player;
  }

  @Override
  public int compareTo(FSCharacter that) {
    int name = this.getName().compareTo(that.getName());
    if (name != 0) {
      return name;
    }

    return this.getId().compareTo(that.getId());
  }
}
