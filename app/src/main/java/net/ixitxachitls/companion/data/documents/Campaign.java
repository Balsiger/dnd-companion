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
import net.ixitxachitls.companion.data.Templates;
import net.ixitxachitls.companion.data.templates.WorldTemplate;
import net.ixitxachitls.companion.data.values.Battle;
import net.ixitxachitls.companion.data.values.Calendar;
import net.ixitxachitls.companion.data.values.CampaignDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import androidx.annotation.CallSuper;

/**
 * A campaign in the game.
 */
public class Campaign extends Document<Campaign> implements Comparable<Campaign> {

  public static final Campaign DEFAULT = createDefault();

  private static final DocumentFactory<Campaign> FACTORY = () -> new Campaign();

  private static final String FIELD_NAME = "name";
  private static final String FIELD_WORLD = "world";
  private static final String FIELD_DATE = "date";
  private static final String FIELD_ENCOUNTER = "encounter";
  private static final String FIELD_INVITES = "invites";
  private static final String FIELD_ADVENTURE = "adventure_id";
  private static final String FIELD_ENCOUNTER_ID = "encounter_id";
  private static final String FIELD_HOUSE_RULE_HPT = "house_rule_hp";

  private static final String GENERIC_NAME = "Generic";
  private static WorldTemplate GENERIC;

  // Mutable state.
  private User dm;
  private String name;
  private WorldTemplate worldTemplate;
  private CampaignDate date = new CampaignDate();
  private Battle battle = new Battle(this);
  private List<String> invites = new ArrayList<>();
  private String adventureId = "";
  private String encounterId = "";
  private boolean houseRuleHp = false;

  public String getAdventureId() {
    return adventureId;
  }

  public void setAdventureId(String adventureId) {
    this.adventureId = adventureId;
    store();

    context.encounters().loadEncounters(adventureId);
  }

  public Battle getBattle() {
    return battle;
  }

  public Calendar getCalendar() {
    return worldTemplate.getCalendar();
  }

  public CampaignDate getDate() {
    return date;
  }

  public void setDate(CampaignDate date) {
    this.date = date;

    // Check if any conditions on characters have run out.
    for (Character character : characters().getCampaignCharacters(getId())) {
      character.updateConditions(date);
    }
  }

  public User getDm() {
    return dm;
  }

  public String getEncounterId() {
    return encounterId;
  }

  public void setEncounterId(String encounterId) {
    this.encounterId = encounterId;
    store();
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

  public WorldTemplate getWorldTemplate() {
    return worldTemplate;
  }

  public void setWorldTemplate(String worldTemplate) {
    this.worldTemplate = world(worldTemplate);
  }

  public boolean isDefault() {
    return this == DEFAULT;
  }

  public void setHouseRuleHp(boolean rule) {
    this.houseRuleHp = rule;
  }

  public boolean amDM() {
    return context.me() == dm;
  }

  public Characters characters() {
    return context.characters();
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

  public Optional<Character> getCharacter(String id) {
    return context.characters().get(id);
  }

  public boolean hasHouseRuleHp() {
    return houseRuleHp;
  }

  public void invite(String email) {
    if (!invites.contains(email)) {
      invites.add(email);
      store();

      Invites.invite(email, getId());
    }
  }

  public Monsters monsters() {
    return context.monsters();
  }

  @Override
  public String toString() {
    return getName() + " (" + getId() + ")";
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

  @Override
  @CallSuper
  protected void read() {
    super.read();
    name = data.get(FIELD_NAME, name);
    worldTemplate = world(data.get(FIELD_WORLD, GENERIC_NAME));
    date = CampaignDate.read(data.getNested(FIELD_DATE));
    battle.read(data.getNested(FIELD_ENCOUNTER));
    invites = data.get(FIELD_INVITES, invites);
    adventureId = data.get(FIELD_ADVENTURE, "");
    encounterId = data.get(FIELD_ENCOUNTER_ID, "");
    houseRuleHp = data.get(FIELD_HOUSE_RULE_HPT, false);

    if (!adventureId.isEmpty()) {
      context.encounters().loadEncounters(adventureId);
    }
  }

  @Override
  public Data write() {
    return Data.empty().set(FIELD_NAME, name)
        .set(FIELD_WORLD, worldTemplate.getName())
        .set(FIELD_DATE, date.write())
        .set(FIELD_ENCOUNTER, battle.write())
        .set(FIELD_INVITES, invites)
        .set(FIELD_ADVENTURE, adventureId)
        .set(FIELD_ENCOUNTER_ID, encounterId)
        .set(FIELD_HOUSE_RULE_HPT, houseRuleHp);
  }

  private void uninviteUser(String email) {
    Invites.uninvite(email, getId());
  }

  private WorldTemplate world(String name) {
    Optional<WorldTemplate> world = Templates.get().getWorldTemplates().get(name);
    if (world.isPresent()) {
      return world.get();
    }

    return generic();
  }

  protected static Campaign create(CompanionContext context, User dm) {
    Campaign campaign = Document.create(FACTORY, context, dm.getId() + "/" + Campaigns.PATH);

    campaign.dm = context.users().fromPath(campaign.getPath());
    campaign.worldTemplate = generic();
    return campaign;
  }

  private static Campaign createDefault() {
    Campaign campaign = new Campaign();
    campaign.name = "NO CAMPAIGN";
    campaign.temporary = true;

    return campaign;
  }

  protected static Campaign fromData(CompanionContext context, DocumentSnapshot snapshot) {
    Campaign campaign = Document.fromData(FACTORY, context, snapshot);

    campaign.dm = context.users().fromPath(campaign.getPath());

    return campaign;
  }

  private static WorldTemplate generic() {
    if (GENERIC == null) {
      GENERIC = Templates.get().getWorldTemplates().get(GENERIC_NAME).get();
    }

    return GENERIC;
  }

  /**
   * Get the campaign with the given id. If it does not exist, a campaign with the given
   * id is created. If you want to handle non existing campaigns, make sure not to store this.
   */
  protected static Campaign getOrCreate(CompanionContext context, String id) {
    Campaign campaign = Document.getOrCreate(FACTORY, context, id);

    campaign.dm = context.users().fromPath(campaign.getPath());
    campaign.worldTemplate = generic();

    return campaign;
  }
}
