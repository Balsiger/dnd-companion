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

import com.google.common.collect.ImmutableList;
import com.google.firebase.firestore.DocumentSnapshot;

import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.dynamics.Character;
import net.ixitxachitls.companion.data.dynamics.Characters;
import net.ixitxachitls.companion.data.dynamics.Creatures;
import net.ixitxachitls.companion.data.statics.World;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.data.values.Calendar;
import net.ixitxachitls.companion.data.values.CampaignDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A campaign in the game.
 */
public class FSCampaign extends Document<FSCampaign> implements Comparable<FSCampaign> {

  private static final String FIELD_NAME = "name";
  private static final String FIELD_WORLD = "world";
  private static final String FIELD_DATE = "date";
  private static final String FIELD_INVITES = "invites";

  private static final String GENERIC_NAME = "Generic";
  private static World GENERIC;

  private final User dm;
  private final Users users;
  private final Characters characters;
  private final Creatures creatures;

  private String name;
  private World world;
  private CampaignDate date;
  private Battle battle;
  private List<String> invites = new ArrayList<>();

  private int nextBattleNumber = 0;

  public FSCampaign(User dm, Users users, Characters characters, Creatures creatures) {
    super(dm.getId() + "/" + FSCampaigns.PATH);

    this.dm = dm;
    this.users = users;
    this.characters = characters;
    this.creatures = creatures;
    this.world = generic();
  }

  protected FSCampaign(DocumentSnapshot snapshot, User dm, Users users, Characters characters,
                       Creatures creatures) {
    super(snapshot);

    this.dm = dm;
    this.users = users;
    this.characters = characters;
    this.creatures = creatures;
  }

  public List<String> getInvites() {
    return invites;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public World getWorld() {
    return world;
  }

  public void setWorld(String world) {
    this.world = world(world);
  }

  public User getDm() {
    return dm;
  }

  public boolean amDM() {
    return users.getMe() == dm;
  }

  public Calendar getCalendar() {
    return world.getCalendar();
  }

  public CampaignDate getDate() {
    return date;
  }

  public void setDate(CampaignDate date) {
    this.date = date;
  }

  public Battle getBattle() {
    return battle;
  }

  public Creatures creatures() {
    return creatures;
  }

  public ImmutableList<String> getCreatureIds() {
    return creatures.getCampaignCreatureIds(getId()).getValue();
  }

  public ImmutableList<String> getCharacterIds() {
    return characters.getCampaignCharacterIds(getId()).getValue();
  }

  public Optional<Character> getCharacter(String id) {
    return characters.getCharacter(id).getValue();
  }

  public void invite(String email) {
    if (!invites.contains(email)) {
      invites.add(email);
      store();
    }
  }

  public void uninvite(String email) {
    if (invites.contains(email)) {
      invites.remove(email);
      store();
    }
  }

  @Override
  protected void read() {
    name = get(FIELD_NAME, name);
    world = world(get(FIELD_WORLD, GENERIC_NAME));
    date = CampaignDate.read(get(FIELD_DATE, new HashMap<String, Object>()));
    invites = get(FIELD_INVITES, invites);
  }

  @Override
  protected Map<String, Object> write(Map<String, Object> data) {
    data.put(FIELD_NAME, name);
    data.put(FIELD_WORLD, world.getName());
    data.put(FIELD_DATE, date.write());
    data.put(FIELD_INVITES, invites);
    return data;
  }

  private World world(String name) {
    Optional<World> world = Entries.get().getWorlds().get(name);
    if (world.isPresent()) {
      return world.get();
    }

    return generic();
  }

  private World generic() {
    if (GENERIC == null) {
      GENERIC = Entries.get().getWorlds().get(GENERIC_NAME).get();
    }

    return GENERIC;
  }

  @Override
  public String toString() {
    return getName() + " (" + getId() + ")";
  }

  @Override
  public int compareTo(FSCampaign other) {
    if (getId().equals(other.getId())) {
      return 0;
    }

    if (this.amDM() && !other.amDM()) {
      return -1;
    }

    if (!this.amDM() && other.amDM()) {
      return +1;
    }

    int compare = getName().compareToIgnoreCase(other.getName());
    if (compare != 0) {
      return compare;
    }

    return getId().compareTo(other.getId());
  }
}
