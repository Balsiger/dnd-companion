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
import net.ixitxachitls.companion.data.Entries;
import net.ixitxachitls.companion.data.statics.World;
import net.ixitxachitls.companion.data.values.Calendar;
import net.ixitxachitls.companion.data.values.CampaignDate;
import net.ixitxachitls.companion.data.values.Encounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A campaign in the game.
 */
public class Campaign extends Document<Campaign> implements Comparable<Campaign> {

  private static final DocumentFactory<Campaign> FACTORY = () -> new Campaign();

  private static final String FIELD_NAME = "name";
  private static final String FIELD_WORLD = "world";
  private static final String FIELD_DATE = "date";
  private static final String FIELD_ENCOUNTER = "encounter";
  private static final String FIELD_INVITES = "invites";
  private static final String FIELD_ADVENTURE = "adventure";

  private static final String GENERIC_NAME = "Generic";
  private static World GENERIC;

  // Mutable state.
  private User dm;
  private String name;
  private World world;
  private CampaignDate date = new CampaignDate();
  private Encounter encounter = new Encounter(this);
  private List<String> invites = new ArrayList<>();
  private String adventureId = "";

  protected static Campaign create(CompanionContext context, User dm) {
    Campaign campaign = Document.create(FACTORY, context, dm.getId() + "/" + Campaigns.PATH);

    campaign.dm = context.users().fromPath(campaign.getPath());
    campaign.world = generic();
    return campaign;
  }

  /**
   * Get the campaign with the given id. If it does not exist, a campaign with the given
   * id is created. If you want to handle non existing campaigns, make sure not to store this.
   */
  protected static Campaign getOrCreate(CompanionContext context, String id) {
    Campaign campaign = Document.getOrCreate(FACTORY, context, id);

    campaign.dm = context.users().fromPath(campaign.getPath());
    campaign.world = generic();

    return campaign;
  }

  protected static Campaign fromData(CompanionContext context, DocumentSnapshot snapshot) {
    Campaign campaign = Document.fromData(FACTORY, context, snapshot);

    campaign.dm = context.users().fromPath(campaign.getPath());

    return campaign;
  }

  public Encounter getEncounter() {
    return encounter;
  }

  public List<String> getInvites() {
    return invites;
  }

  public String getName() {
    if (name == null) {
      return "";
    }

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
    return context.me() == dm;
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

  public Monsters monsters() {
    return context.monsters();
  }

  public Characters characters() {
    return context.characters();
  }

  public Optional<Character> getCharacter(String id) {
    return context.characters().get(id);
  }

  public void invite(String email) {
    if (!invites.contains(email)) {
      invites.add(email);
      store();

      Invites.invite(email, getId());
    }
  }

  public void uninvite(String email) {
    if (invites.contains(email)) {
      invites.remove(email);
      store();

      Invites.uninvite(email, getId());
    }
  }

  public void uninviteAll() {
    for (String email : invites) {
      uninviteUser(email);
    }

    invites.clear();
    store();
  }

  public void setAdventure(Adventure adventure) {
    this.adventureId = adventure.getId();
    store();
  }

  public Optional<Adventure> getAdventure() {
    return context.adventures().get(adventureId);
  }

  private void uninviteUser(String email) {
    Invites.uninvite(email, getId());
  }

  @Override
  @CallSuper
  protected void read() {
    super.read();
    name = get(FIELD_NAME, name);
    world = world(get(FIELD_WORLD, GENERIC_NAME));
    date = CampaignDate.read(get(FIELD_DATE));
    encounter.read(get(FIELD_ENCOUNTER));
    invites = get(FIELD_INVITES, invites);
    adventureId = get(FIELD_ADVENTURE, "");
  }

  @Override
  protected Map<String, Object> write(Map<String, Object> data) {
    data.put(FIELD_NAME, name);
    data.put(FIELD_WORLD, world.getName());
    data.put(FIELD_DATE, date.write());
    data.put(FIELD_ENCOUNTER, encounter.write());
    data.put(FIELD_INVITES, invites);
    data.put(FIELD_ADVENTURE, adventureId);

    return data;
  }

  private World world(String name) {
    Optional<World> world = Entries.get().getWorlds().get(name);
    if (world.isPresent()) {
      return world.get();
    }

    return generic();
  }

  private static World generic() {
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
  public int compareTo(Campaign other) {
    if (this == other) {
      return 0;
    }

    if (getId().equals(other.getId())) {
      return 0;
    }

    if (this.amDM() && !other.amDM()) {
      return -1;
    }

    if (!this.amDM() && other.amDM()) {
      return +1;
    }

    int compare = getName().compareTo(other.getName());
    if (compare != 0) {
      return compare;
    }

    return getId().compareTo(other.getId());
  }

}
